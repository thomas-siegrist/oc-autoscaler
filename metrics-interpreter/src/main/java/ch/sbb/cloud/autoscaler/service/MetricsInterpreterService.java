package ch.sbb.cloud.autoscaler.service;

import ch.sbb.cloud.autoscaler.model.Configuration;
import ch.sbb.cloud.autoscaler.model.MetricsEvent;
import ch.sbb.cloud.autoscaler.model.actionevents.ActionEvent;
import ch.sbb.cloud.autoscaler.model.actionevents.Scale;
import ch.sbb.cloud.autoscaler.repository.ConfigurationRepository;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by thomas on 01.09.16.
 */
@Component
public class MetricsInterpreterService {

    @Autowired
    // TODO: aggregate with Hazelcast
    private HazelcastInstance hz;

    @Autowired
    private RabbitTemplate amqp;

    @Autowired
    private ConfigurationRepository configurationRepository;

    public void postNewEvent(MetricsEvent metricsEvent) {
        persistInHazelcast(metricsEvent);
        interpreteEvent(metricsEvent);
    }

    private void persistInHazelcast(MetricsEvent metricsEvent) {
        hz
                .getMultiMap("Metrics")
                .put(
                        metricsEvent.composedUniqueId(),
                        metricsEvent.getValue()
                );
    }

    private void interpreteEvent(MetricsEvent metricsEvent) {
        Configuration configuration = searchForMatchingConfiguration(metricsEvent);
        if (configuration == null)
            return;

        Long value = metricsEvent.getValue();
        if (value >= configuration.getScaleUp())
            sendActionEvent(configuration, Scale.UP);
        if (value <= configuration.getScaleDown())
            sendActionEvent(configuration, Scale.DOWN);
        // TODO: remember last scaled up/down in order to infere minimal delays between actions
        // Else do nothing.
    }

    private Configuration searchForMatchingConfiguration(MetricsEvent metricsEvent) {
        List<Configuration> configurations = configurationRepository.findByProjectAndTargetServiceAndMetricsAndMetricsProviderService(
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

    private void sendActionEvent(Configuration configuration, Scale scale) {
        amqp.convertAndSend(
                "amq.direct",
                "action-event-queue",
                actionEventFor(configuration, scale));
    }

    private ActionEvent actionEventFor(Configuration configuration, Scale scale) {
        ActionEvent actionEvent = new ActionEvent();
        actionEvent.project = configuration.getProject();
        actionEvent.service = configuration.getTargetService();
        actionEvent.scale = scale;
        return actionEvent;
    }

}
