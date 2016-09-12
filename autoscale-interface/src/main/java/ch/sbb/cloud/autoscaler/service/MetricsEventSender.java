package ch.sbb.cloud.autoscaler.service;

import ch.sbb.cloud.autoscaler.model.Metrics;
import ch.sbb.cloud.autoscaler.model.MetricsEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Created by micic on 04.09.16.
 */
public class MetricsEventSender {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsEventSender.class);

    private final RabbitTemplate rabbitTemplate;
    private final String projectName;
    private final String serviceName;


    MetricsEventSender(String projectName, String serviceName, RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.projectName = projectName;
        this.serviceName = serviceName;
    }

    void sendMetricsEvent(Metrics type, Long value) {
        sendMetricsEvent(type, "", value);
    }

    void sendMetricsEvent(Metrics type, String metricsName, Long value) {

        MetricsEvent event = new MetricsEvent(
                projectName,
                serviceName,
                type,
                metricsName,
                value
        );

        String jsonMsg = toJson(event);
        LOG.info(jsonMsg);

        rabbitTemplate.convertAndSend("metrics-event-queue", jsonMsg);
    }

    private String toJson(MetricsEvent event) {
        try {
            return new ObjectMapper().writeValueAsString(event);
        } catch (JsonProcessingException e) {
            LOG.error("Error converting MetricsEvent to JSON!", e);
            return null;
        }
    }
}
