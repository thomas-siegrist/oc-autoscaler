package ch.sbb.cloud.autoscaler.jms;

import ch.sbb.cloud.autoscaler.model.MetricsEvent;
import ch.sbb.cloud.autoscaler.service.MetricsEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by thomas on 01.09.16.
 */
@Component
public class MetricsEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsEventListener.class);

    @Autowired
    private MetricsEventService metricsEventService;

    @RabbitListener(queues = "metrics-event-queue")
    public void receive(String json) {
        try {
            MetricsEvent metricsEvent = parse(json);
            metricsEventService.includeNewEvent(metricsEvent);
        } catch (IOException e) {
            LOG.error("Error during parsing of the input-message: " + json, e);
        }
    }

    private MetricsEvent parse(String json) throws IOException {
        return new ObjectMapper().readValue(json, MetricsEvent.class);
    }

}
