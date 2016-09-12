package ch.sbb.cloud.autoscaler.service;

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

    private final MetricsEventSender eventSender;
    private final RabbitTemplate rabbitTemplate;

    public RabbitMetricsPublisher(String projectName, String serviceName, RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.eventSender = new MetricsEventSender(projectName, serviceName, rabbitTemplate);
    }

    public void publishMQDepth(String queueName) {

        RabbitAdmin rabbitAdmin = new RabbitAdmin(rabbitTemplate.getConnectionFactory());
        Long queueDepth = Long.parseLong(
                rabbitAdmin.getQueueProperties(queueName).get(MQ_MSG_COUNT_PROP).toString()
        );

        LOG.info("Queue depth of \"{}\" is {}", queueName, queueDepth);

        this.eventSender.sendMetricsEvent(Metrics.QUEUE_DEPTH, queueName, queueDepth);
    }
}
