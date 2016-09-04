package ch.sbb.cloud.autoscaler.service;

import ch.sbb.cloud.autoscaler.model.metricsevents.MetricsEvent;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by thomas on 01.09.16.
 */
@Component
public class MetricsEventService {

    @Autowired
    private HazelcastInstance hz;

    public void includeNewEvent(MetricsEvent metricsEvent) {
        hz
                .getMap("Metrics")
                .put(metricsEvent.composedUniqueId(), metricsEvent.getValue());
    }

}
