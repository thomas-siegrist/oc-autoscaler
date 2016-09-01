package ch.sbb.cloud.autoscaler;

import ch.sbb.cloud.autoscaler.model.actionevents.ActionEvent;
import ch.sbb.cloud.autoscaler.model.actionevents.Scale;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by thomas on 28.08.16.
 */
public class ActionEventJsonTests {

    @Test
    public void testActionEventToJsonString() throws IOException {
        ActionEvent actionEvent = new ActionEvent();
        actionEvent.project = "usecase";
        actionEvent.service = "emailservice";
        actionEvent.scale = Scale.DOWN;

        new ObjectMapper().writeValue(System.out, actionEvent);
        assertTrue(true);
    }

}
