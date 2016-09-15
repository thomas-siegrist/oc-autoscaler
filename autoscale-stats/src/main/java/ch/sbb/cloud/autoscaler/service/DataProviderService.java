package ch.sbb.cloud.autoscaler.service;

import ch.sbb.cloud.autoscaler.model.Metrics;
import ch.sbb.cloud.autoscaler.model.MetricsStatistic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;

/**
 * Created by micic on 14.09.16.
 */
@RestController
public class DataProviderService {

    @Autowired
    private MetricsCollectorService metricsCollector;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @RequestMapping("/api/data")
    public String data() {

        final StringBuffer ret = new StringBuffer("time,pods\n");

        metricsCollector.getMetricsStatisticSnapshots().forEach(s -> {
            MetricsStatistic stat = s.getMetricsStatistic();
            if (stat.getService().equals("frontendservice")
                    && stat.getMetrics().equals(Metrics.NUMBER_OF_HTTP_CONNECTIONS)) {
                ret
                        .append(formatter.format(s.getTime()))
                        .append(",")
                        .append(stat.getCurrentValue())
                        .append("\n");
            }
        });

        return ret.toString();
    }
}
