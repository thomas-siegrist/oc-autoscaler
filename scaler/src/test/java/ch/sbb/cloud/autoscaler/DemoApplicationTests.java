package ch.sbb.cloud.autoscaler;

import ch.sbb.cloud.autoscaler.model.actionevents.ActionEvent;
import ch.sbb.cloud.autoscaler.model.actionevents.Scale;
import ch.sbb.cloud.autoscaler.service.OcScaleClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

    @Autowired
    private OcScaleClient ocScaleClient;

    @Test
    public void contextLoads() {
        ActionEvent actionEvent = new ActionEvent();
        actionEvent.project = "usecase";
        actionEvent.service = "emailservice";
        actionEvent.scale = Scale.DOWN;
        ocScaleClient.scale(actionEvent);
    }

}
