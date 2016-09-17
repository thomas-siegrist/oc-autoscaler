package ch.sbb.cloud.autoscaler.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by micic on 14.09.16.
 */
public class MetricsStatisticSnapshot implements StatisticSnapshot, Serializable {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetricsStatisticSnapshot that = (MetricsStatisticSnapshot) o;

        if (time != null ? !time.equals(that.time) : that.time != null) return false;
        return metricsStatistic != null ? metricsStatistic.equals(that.metricsStatistic) : that.metricsStatistic == null;

    }

    @Override
    public int hashCode() {
        int result = time != null ? time.hashCode() : 0;
        result = 31 * result + (metricsStatistic != null ? metricsStatistic.hashCode() : 0);
        return result;
    }
}
