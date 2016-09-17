package ch.sbb.cloud.autoscaler.api;

import ch.sbb.cloud.autoscaler.api.model.PodStatistic;
import ch.sbb.cloud.autoscaler.service.PodStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by micic on 13.09.16.
 */
@RestController()
@RequestMapping("/autoscaler/podstats")
public class PodStatsApi {

    @Autowired
    private PodStatsService podStatsService;

    @RequestMapping(
            path = "",
            produces = "application/json",
            method = RequestMethod.GET
    )
    public List<PodStatistic> getMetricsStats() {
        return podStatsService.getPodStatistics();
    }

}
