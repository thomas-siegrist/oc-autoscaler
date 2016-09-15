package ch.sbb.cloud.autoscaler.api;

import ch.sbb.cloud.autoscaler.model.stats.MetricsStatistic;
import ch.sbb.cloud.autoscaler.service.MetricsPersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by micic on 13.09.16.
 */
@RestController()
@RequestMapping("/autoscaler/stats")
public class StatsApi {

    private static final Logger LOG = LoggerFactory.getLogger(StatsApi.class);

    @Autowired
    private MetricsPersistenceService persistenceService;

    @RequestMapping(
            path = "",
            produces = "application/json",
            method = RequestMethod.GET
            )
            public List<MetricsStatistic> getMetricsStats() {

        return persistenceService.getAllStatistics();
    }
}
