package ch.sbb.cloud.autoscaler.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.sbb.cloud.autoscaler.model.Configuration;
import ch.sbb.cloud.autoscaler.model.MetricsEvent;
import ch.sbb.cloud.autoscaler.model.actionevents.ActionEvent;
import ch.sbb.cloud.autoscaler.model.actionevents.Scale;
import ch.sbb.cloud.autoscaler.repository.ConfigurationRepository;

import com.hazelcast.config.MultiMapConfig;
import com.hazelcast.core.HazelcastInstance;

import javax.annotation.PostConstruct;

/**
 * Created by thomas on 01.09.16.
 */
@Component
public class MetricsInterpreterService {

    private static final long SCALE_UP_DELAY_IN_MINUTES = 1;
    private static final String HZ_METRICS_MAP = "Metrics";
    public static final String HZ_LAST_SCALED_UP_MAP = "Last-Scaled-up";

    @Autowired
    private HazelcastInstance hz;

    @Autowired
    private RabbitTemplate amqp;

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
        interpreteEvent(metricsEvent);
    }

    private void persistInHazelcast(MetricsEvent metricsEvent) {
        hz
                .getMultiMap(HZ_METRICS_MAP)
                .put(
                        metricsEvent.composedUniqueId(),
                        metricsEvent.getValue()
                );
    }

    private void interpreteEvent(MetricsEvent metricsEvent) {
        Configuration configuration = searchForMatchingConfiguration(metricsEvent);
        if (configuration == null)
            return; // No Config => No Action

        Long value = calculateValue(metricsEvent);
        if (value >= configuration.getScaleUp()) {
            scaleUp(metricsEvent, configuration);
        }
        if (value <= configuration.getScaleDown()) {
            scaleDown(configuration);
        }

        // Else: do nothing.
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
        return lastUpScaling.isAfter(LocalDateTime.now().minusMinutes(SCALE_UP_DELAY_IN_MINUTES));
    }

    private void scaleDown(Configuration configuration) {
        send(actionEventFor(configuration, Scale.DOWN));
    }

    private Configuration searchForMatchingConfiguration(MetricsEvent metricsEvent) {
        List<Configuration> configurations = configurationRepository.findByProjectAndServiceAndMetricName(
                metricsEvent.getProject(),
                metricsEvent.getService(),
                metricsEvent.getMetricName()
                );

        if (configurations.size() > 0) {
            return configurations.get(0);
        }
        return null;
    }

    private ActionEvent actionEventFor(Configuration configuration, Scale scale) {
        ActionEvent actionEvent = new ActionEvent();
        actionEvent.project = configuration.getProject();
        actionEvent.service = configuration.getService();
        actionEvent.scale = scale;
        return actionEvent;
    }

    private void send(ActionEvent actionEvent) {
        amqp.convertAndSend(
                "amq.direct",
                "action-event-queue",
                actionEvent);
    }

}
