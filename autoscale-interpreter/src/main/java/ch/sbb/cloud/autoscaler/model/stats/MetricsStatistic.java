package ch.sbb.cloud.autoscaler.model.stats;

import ch.sbb.cloud.autoscaler.model.Metrics;

import java.io.Serializable;

/**
 * Created by micic on 13.09.16.
 */
public class MetricsStatistic implements Serializable {

    private String project;
    private String service;
    private Metrics metrics;
    private String metricName;
    private Long currentValue;

    public String composedUniqueId() {
        return this.project + "|" + this.service + "|" + this.metrics + "|" + this.metricName;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public Long getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Long currentValue) {
        this.currentValue = currentValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetricsStatistic that = (MetricsStatistic) o;

        if (!project.equals(that.project)) return false;
        if (!service.equals(that.service)) return false;
        if (metrics != that.metrics) return false;
        return metricName != null ? metricName.equals(that.metricName) : that.metricName == null;
    }

    @Override
    public int hashCode() {
        int result = project.hashCode();
        result = 31 * result + service.hashCode();
        result = 31 * result + metrics.hashCode();
        result = 31 * result + (metricName != null ? metricName.hashCode() : 0);
        return result;
    }
}
