package service;

import ch.sbb.cloud.autoscaler.model.Metrics;
import ch.sbb.cloud.autoscaler.model.MetricsEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Created by micic on 03.09.16.
 */
public class RabbitMetricsPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitMetricsPublisher.class);

    private static final String MQ_MSG_COUNT_PROP = "QUEUE_MESSAGE_COUNT";

    private final RabbitTemplate rabbitTemplate;
    private final String projectName;
    private final String serviceName;

    public RabbitMetricsPublisher(String projectName, String serviceName, RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.projectName = projectName;
        this.serviceName = serviceName;
    }

    public void publishMQDepth(String queueName) {

        RabbitAdmin rabbitAdmin = new RabbitAdmin(rabbitTemplate.getConnectionFactory());
        Long queueDepth = Long.parseLong(
                rabbitAdmin.getQueueProperties(queueName).get(MQ_MSG_COUNT_PROP).toString()
        );

        LOG.info("Queue depth of \"{}\" is {}", queueName, queueDepth);

        sendMetricsEvent(queueName, queueDepth);
    }

    private void sendMetricsEvent(String queueName, Long depth) {

        MetricsEvent event = new MetricsEvent(
                projectName,
                serviceName,
                Metrics.QUEUE_DEPTH,
                queueName,
                depth
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
