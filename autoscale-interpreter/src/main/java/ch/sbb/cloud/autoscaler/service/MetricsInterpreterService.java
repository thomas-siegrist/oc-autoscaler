package ch.sbb.cloud.autoscaler.service;

import ch.sbb.cloud.autoscaler.model.AutoscaleConfiguration;
import ch.sbb.cloud.autoscaler.model.MetricsEvent;
import ch.sbb.cloud.autoscaler.model.ServiceLimit;
import ch.sbb.cloud.autoscaler.model.actionevents.ActionEvent;
import ch.sbb.cloud.autoscaler.model.actionevents.Scale;
import ch.sbb.cloud.autoscaler.model.stats.MetricsStatistic;
import ch.sbb.cloud.autoscaler.repository.AutoscaleConfigurationRepository;
import ch.sbb.cloud.autoscaler.repository.ServiceLimitRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by thomas on 01.09.16.
 */
@Component
public class MetricsInterpreterService {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsInterpreterService.class);

    private static final long SCALE_UP_DELAY_IN_SECONDS = 30;

    private static final long SCALE_DOWN_DELAY_IN_SECONDS = 30;

    @Autowired
    private RabbitTemplate template;

    @Autowired
    private AutoscaleConfigurationRepository autoscaleConfigurationRepository;

    @Autowired
    private ServiceLimitRepository serviceLimitRepository;

    @Autowired
    private MetricsPersistenceService persistenceService;

    public void postNewEvent(MetricsEvent metricsEvent) {

        // Calculate the overall value only once...
        Long value = calculateValue(metricsEvent);

        persistenceService.persistMetricsEvent(metricsEvent);
        persistenceService.persistMetricsStatistic(fromEventToStat(metricsEvent, value));

        interpretEvent(metricsEvent, value);
    }

    private MetricsStatistic fromEventToStat(MetricsEvent metricsEvent, Long currentMetricValue) {
        MetricsStatistic stat = new MetricsStatistic();
        stat.setProject(metricsEvent.getProject());
        stat.setService(metricsEvent.getService());
        stat.setMetricName(metricsEvent.getMetricName());
        stat.setMetrics(metricsEvent.getMetrics());
        stat.setCurrentValue(currentMetricValue);

        return stat;
    }

    private void interpretEvent(MetricsEvent metricsEvent, Long value) {
        AutoscaleConfiguration configuration = searchForMatchingConfiguration(metricsEvent);
        if (configuration == null) {
            LOG.warn("No metrics configuration found for {}", metricsEvent.composedUniqueId());
            return; // No Config => No Action
        }

        if (value >= configuration.getScaleUp()) {
            scaleUp(metricsEvent, configuration);
        } else if (value <= configuration.getScaleDown()) {
            scaleDown(metricsEvent, configuration);
        } else {
            LOG.info("No scaling required for service {}", metricsEvent.getService());
        }
    }

    private Long calculateValue(MetricsEvent metricsEvent) {
        Long value = metricsEvent.getValue();
        if (metricsEvent.getMetrics().isAggregate()) {
            value = persistenceService.calculateAggregateFor(metricsEvent);
        }
        return value;
    }

    private void scaleUp(MetricsEvent metricsEvent, AutoscaleConfiguration configuration) {
        if (isUpscalingAllowed(metricsEvent)) {
            LOG.info("Scaling up service {}", metricsEvent.getService());
            send(actionEventFor(configuration, Scale.UP));
            persistenceService.setLastScaleTimeToNow(metricsEvent);
        }
    }

    private void scaleDown(MetricsEvent metricsEvent, AutoscaleConfiguration configuration) {
        if (isDownscalingAllowed(metricsEvent)) {
            LOG.info("Scaling down service {}", metricsEvent.getService());
            send(actionEventFor(configuration, Scale.DOWN));
            persistenceService.setLastScaleTimeToNow(metricsEvent);
        }
    }

    private boolean isUpscalingAllowed(MetricsEvent metricsEvent) {
        return persistenceService.getLastScaleDateTime(metricsEvent)
                .orElse(LocalDateTime.MIN)
                .isBefore(LocalDateTime.now().minusSeconds(SCALE_UP_DELAY_IN_SECONDS));
    }

    private boolean isDownscalingAllowed(MetricsEvent metricsEvent) {
        return persistenceService.getLastScaleDateTime(metricsEvent)
                .orElse(LocalDateTime.MIN)
                .isBefore(LocalDateTime.now().minusSeconds(SCALE_DOWN_DELAY_IN_SECONDS));
    }

    private AutoscaleConfiguration searchForMatchingConfiguration(MetricsEvent metricsEvent) {
        List<AutoscaleConfiguration> autoscaleConfigurations = autoscaleConfigurationRepository.findByProjectAndServiceAndMetricsAndMetricName(
                metricsEvent.getProject(),
                metricsEvent.getService(),
                metricsEvent.getMetrics(),
                metricsEvent.getMetricName()
        );

        if (autoscaleConfigurations.size() > 0) {
            return autoscaleConfigurations.get(0);
        }
        return null;
    }

    private ActionEvent actionEventFor(AutoscaleConfiguration autoscaleConfiguration, Scale scale) {

        String project = autoscaleConfiguration.getProject();
        String service = autoscaleConfiguration.getService();

        ActionEvent actionEvent = new ActionEvent();
        actionEvent.setProject(project);
        actionEvent.setService(service);
        actionEvent.setScale(scale);

        ServiceLimit serviceLimit = getServiceLimitFor(project, service);
        if (serviceLimit != null) {
            actionEvent.setMinReplicas(serviceLimit.getMinPods());
            actionEvent.setMaxReplicas(serviceLimit.getMaxPods());
        }

        return actionEvent;
    }

    private ServiceLimit getServiceLimitFor(String project, String service) {
        List<ServiceLimit> limits = serviceLimitRepository.findByProjectAndService(project, service);
        if (limits.size() == 1) {
            return limits.get(0);
        } else {
            LOG.error("No or invalid service limits defined for service {}:{}", project, service);
            return null;
        }
    }

    private void send(ActionEvent actionEvent) {
        template.convertAndSend("action-event-queue", toJson(actionEvent));
    }

    private String toJson(ActionEvent event) {
        try {
            return new ObjectMapper().writeValueAsString(event);
        } catch (JsonProcessingException e) {
            LOG.error("Error converting ActionEvent to JSON!", e);
            return null;
        }
    }

}
