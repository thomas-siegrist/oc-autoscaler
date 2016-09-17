package ch.sbb.cloud.autoscaler.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by micic on 14.09.16.
 */
public class MetricsStatisticSnapshot implements Serializable {

    private LocalDateTime time;
    private MetricsStatistic metricsStatistic;

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public MetricsStatistic getMetricsStatistic() {
        return metricsStatistic;
    }

    public void setMetricsStatistic(MetricsStatistic metricsStatistic) {
        this.metricsStatistic = metricsStatistic;
    }
}
