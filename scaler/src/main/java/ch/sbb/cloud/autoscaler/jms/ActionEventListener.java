package ch.sbb.cloud.autoscaler.jms;

import ch.sbb.cloud.autoscaler.model.actionevents.ActionEvent;
import ch.sbb.cloud.autoscaler.service.OcScaleClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by thomas on 04.08.16.
 */
@Component
public class ActionEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(ActionEventListener.class);

    @Autowired
    private OcScaleClient ocScaleClient;

    @RabbitListener(queues = "action-event-queue")
    public void receive(String json) {
        try {
            ActionEvent actionEvent = parse(json);
            ocScaleClient.scale(actionEvent);
        } catch (IOException e) {
            LOG.error("Error during parsing of the input-message: " + json, e);
        }
    }

    private ActionEvent parse(String json) throws IOException {
        return new ObjectMapper().readValue(json, ActionEvent.class);
    }

}
