package ch.sbb.cloud.autoscaler.service;

import ch.sbb.cloud.autoscaler.model.Configuration;
import ch.sbb.cloud.autoscaler.model.MetricsEvent;
import ch.sbb.cloud.autoscaler.model.actionevents.ActionEvent;
import ch.sbb.cloud.autoscaler.model.actionevents.Scale;
import ch.sbb.cloud.autoscaler.repository.ConfigurationRepository;
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
import java.util.stream.Collectors;

/**
 * Created by thomas on 01.09.16.
 */
@Component
public class MetricsInterpreterService {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsInterpreterService.class);

    private static final long SCALE_UP_DELAY_IN_SECONDS = 20;
    private static final long SCALE_DOWN_DELAY_IN_SECONDS = 60;

    public static final String HZ_METRICS_MAP = "Metrics";
    public static final String HZ_LAST_SCALED_UP_MAP = "Last-Scaled-up";
    public static final String HZ_LAST_SCALED_DOWN_MAP = "Last-Scaled-down";

    @Autowired
    private HazelcastInstance hz;

    @Autowired
    private RabbitTemplate amqp;

    @Autowired
    private RabbitTemplate template;

    @Autowired
    private ConfigurationRepository configurationRepository;

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
            LOG.info("Scaling up service {}", metricsEvent.getService());
            scaleUp(metricsEvent, configuration);
        } else if (value <= configuration.getScaleDown()) {
            LOG.info("Scaling down service {}", metricsEvent.getService());
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
            send(actionEventFor(configuration, Scale.UP));
            hz
                    .getMap(HZ_LAST_SCALED_UP_MAP)
                    .put(
                            metricsEvent.composedUniqueId(),
                            LocalDateTime.now()
                    );
        }
    }

    private boolean isUpscalingAllowed(MetricsEvent metricsEvent) {
        LocalDateTime lastUpScaling = (LocalDateTime) hz
                .getMap(HZ_LAST_SCALED_UP_MAP)
                .get(metricsEvent.composedUniqueId());
        return lastUpScaling == null || lastUpScaling.isAfter(LocalDateTime.now().minusSeconds(SCALE_UP_DELAY_IN_SECONDS));
    }

    private void scaleDown(MetricsEvent metricsEvent, Configuration configuration) {
        if (isDownscalingAllowed(metricsEvent)) {
            send(actionEventFor(configuration, Scale.DOWN));
            hz
                    .getMap(HZ_LAST_SCALED_DOWN_MAP)
                    .put(
                            metricsEvent.composedUniqueId(),
                            LocalDateTime.now()
                    );
        }
    }

    private boolean isDownscalingAllowed(MetricsEvent metricsEvent) {
        LocalDateTime lastDownScaling = (LocalDateTime) hz
                .getMap(HZ_LAST_SCALED_DOWN_MAP)
                .get(metricsEvent.composedUniqueId());
        return lastDownScaling == null || lastDownScaling.isAfter(LocalDateTime.now().minusSeconds(SCALE_DOWN_DELAY_IN_SECONDS));
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
        ActionEvent actionEvent = new ActionEvent();
        actionEvent.setProject(configuration.getProject());
        actionEvent.setService(configuration.getService());
        actionEvent.setScale(scale);
        return actionEvent;
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
