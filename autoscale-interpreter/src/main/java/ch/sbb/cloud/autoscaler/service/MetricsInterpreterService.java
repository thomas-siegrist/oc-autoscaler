package ch.sbb.cloud.autoscaler.service;

import ch.sbb.cloud.autoscaler.model.Configuration;
import ch.sbb.cloud.autoscaler.model.MetricsEvent;
import ch.sbb.cloud.autoscaler.model.ServiceLimit;
import ch.sbb.cloud.autoscaler.model.actionevents.ActionEvent;
import ch.sbb.cloud.autoscaler.model.actionevents.Scale;
import ch.sbb.cloud.autoscaler.repository.ConfigurationRepository;
import ch.sbb.cloud.autoscaler.repository.ServiceLimitRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.config.MultiMapConfig;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by thomas on 01.09.16.
 */
@Component
public class MetricsInterpreterService {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsInterpreterService.class);

    private static final long SCALE_UP_DELAY_IN_SECONDS = 10;

    private static final long SCALE_DOWN_DELAY_IN_SECONDS = 60;

    private static final String HZ_METRICS_MAP = "Metrics";
    private static final String HZ_LAST_SCALE_MAP = "Last-Scale";

    @Autowired
    private HazelcastInstance hz;

    @Autowired
    private RabbitTemplate amqp;

    @Autowired
    private RabbitTemplate template;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private ServiceLimitRepository serviceLimitRepository;

    @PostConstruct
    public void init() {
        MultiMapConfig multiMapConfig = new MultiMapConfig();
        multiMapConfig
                .setName(HZ_METRICS_MAP)
                .setBackupCount(0)
                .setAsyncBackupCount(1)
                .setValueCollectionType(MultiMapConfig.ValueCollectionType.LIST);
        hz.getConfig().addMultiMapConfig(multiMapConfig);
    }

    public void postNewEvent(MetricsEvent metricsEvent) {
        persistInHazelcast(metricsEvent);
        interpretEvent(metricsEvent);
    }

    private void persistInHazelcast(MetricsEvent metricsEvent) {
        hz
                .getMultiMap(HZ_METRICS_MAP)
                .put(
                        metricsEvent.composedUniqueId(),
                        metricsEvent.getValue()
                );
    }

    private void interpretEvent(MetricsEvent metricsEvent) {
        Configuration configuration = searchForMatchingConfiguration(metricsEvent);
        if (configuration == null) {
            LOG.warn("No metrics configuration found for {}", metricsEvent.composedUniqueId());
            return; // No Config => No Action
        }

        Long value = calculateValue(metricsEvent);
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
            value = calculateAggregateFor(metricsEvent);
        }
        return value;
    }

    private Long calculateAggregateFor(MetricsEvent metricsEvent) {
        List<Long> values = hz
                .getMultiMap(HZ_METRICS_MAP)
                .get(metricsEvent.composedUniqueId())
                .stream()
                .map(v -> (Long) v)
                .collect(Collectors.toList());

        return values.stream()
                .skip(values.size() > 10 ? values.size() - 10 : 0)
                .collect(Collectors.averagingLong(v -> v))
                .longValue();
    }

    private void scaleUp(MetricsEvent metricsEvent, Configuration configuration) {
        if (isUpscalingAllowed(metricsEvent)) {
            LOG.info("Scaling up service {}", metricsEvent.getService());
            send(actionEventFor(configuration, Scale.UP));
            setLastScaleTimeToNow(metricsEvent);
        }
    }

    private void scaleDown(MetricsEvent metricsEvent, Configuration configuration) {
        if (isDownscalingAllowed(metricsEvent)) {
            LOG.info("Scaling down service {}", metricsEvent.getService());
            send(actionEventFor(configuration, Scale.DOWN));
            setLastScaleTimeToNow(metricsEvent);
        }
    }

    private boolean isUpscalingAllowed(MetricsEvent metricsEvent) {
        return getLastScaleDateTime(metricsEvent)
                .orElse(LocalDateTime.MIN)
                .isBefore(LocalDateTime.now().minusSeconds(SCALE_UP_DELAY_IN_SECONDS));
    }

    private boolean isDownscalingAllowed(MetricsEvent metricsEvent) {
        return getLastScaleDateTime(metricsEvent)
                .orElse(LocalDateTime.MIN)
                .isBefore(LocalDateTime.now().minusSeconds(SCALE_DOWN_DELAY_IN_SECONDS));
    }

    private Optional<LocalDateTime> getLastScaleDateTime(MetricsEvent metricsEvent) {
        return Optional.ofNullable((LocalDateTime) hz
                .getMap(HZ_LAST_SCALE_MAP)
                .get(metricsEvent.composedUniqueId()));
    }

    private void setLastScaleTimeToNow(MetricsEvent metricsEvent) {
        hz
                .getMap(HZ_LAST_SCALE_MAP)
                .put(
                        metricsEvent.composedUniqueId(),
                        LocalDateTime.now()
                );
    }

    private Configuration searchForMatchingConfiguration(MetricsEvent metricsEvent) {
        List<Configuration> configurations = configurationRepository.findByProjectAndServiceAndMetricsAndMetricName(
                metricsEvent.getProject(),
                metricsEvent.getService(),
                metricsEvent.getMetrics(),
                metricsEvent.getMetricName()
        );

        if (configurations.size() > 0) {
            return configurations.get(0);
        }
        return null;
    }

    private ActionEvent actionEventFor(Configuration configuration, Scale scale) {

        String project = configuration.getProject();
        String service = configuration.getService();

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
