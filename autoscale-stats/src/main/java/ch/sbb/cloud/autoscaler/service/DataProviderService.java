package ch.sbb.cloud.autoscaler.service;

import ch.sbb.cloud.autoscaler.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.time.format.DateTimeFormatter;

/**
 * Created by micic on 14.09.16.
 */
@RestController
public class DataProviderService {

    @Autowired
    private MetricsCollectorService metricsCollector;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @RequestMapping(
            value = "/api/metricsdata/{project}/{service}/{metric}",
            method = RequestMethod.GET)
    public ChartData metricsData(
            @PathVariable("project") String project,
            @PathVariable("service") String service,
            @PathVariable("metric") Metrics metric) {

        ChartColDef dateTimeCol = new ChartColDef(ChartColDefType.string, "DateTime");
        ChartColDef metricCol = new ChartColDef(ChartColDefType.number, service);

        final ChartData data = new ChartData();
        data.cols.add(dateTimeCol);
        data.cols.add(metricCol);

        metricsCollector.getMetricsStatisticSnapshots().forEach(s -> {
            MetricsStatistic stat = s.getMetricsStatistic();
            if (stat.getService().equals(service)
                    && stat.getProject().equals(project)
                    && stat.getMetrics().equals(metric)) {

                data.rows.add(
                        new ChartRow()
                                .addContentEntry(new ChartRowContentString(formatter.format(s.getTime())))
                                .addContentEntry(new ChartRowContentNumber(stat.getCurrentValue()))
                );
            }
        });

        return data;
    }

    @RequestMapping(
            value = "/api/podsdata/{project}/{service}",
            method = RequestMethod.GET)
    public ChartData data(
            @PathVariable("project") String project,
            @PathVariable("service") String service) {

        ChartColDef dateTimeCol = new ChartColDef(ChartColDefType.string, "DateTime");
        ChartColDef podCol = new ChartColDef(ChartColDefType.number, service);

        final ChartData data = new ChartData();
        data.cols.add(dateTimeCol);
        data.cols.add(podCol);

        metricsCollector.getPodsStatisticSnapshots().forEach(s -> {
            PodStatistic stat = s.getPodStatistic();
            if (stat.getService().equals(service)
                    && stat.getProject().equals(project)) {

                data.rows.add(
                        new ChartRow()
                                .addContentEntry(new ChartRowContentString(formatter.format(s.getTime())))
                                .addContentEntry(new ChartRowContentNumber(Long.valueOf(stat.getPodCount())))
                );
            }
        });

        return data;
    }
}
