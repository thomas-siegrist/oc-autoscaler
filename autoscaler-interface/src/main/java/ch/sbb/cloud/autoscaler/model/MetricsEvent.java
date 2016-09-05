package ch.sbb.cloud.autoscaler.model;

import java.io.Serializable;

/**
 * Created by thomas on 01.09.16.
 */
public class MetricsEvent implements Serializable {

    private final String project;
    private final String service;
    private final Metrics metrics;
    private final String metricName;
    private final Long value;

    public MetricsEvent(String project, String service, Metrics metrics, String metricName, Long value) {
        this.project = project;
        this.service = service;
        this.metricName = metricName;
        this.metrics = metrics;
        this.value = value;
    }

    public String getProject() {
        return project;
    }

    /**
     * @return The uniqe identifier of the Metrics-Event as String. It is meant to be the unique identifier for grouping and scaling.
     */
    public String composedUniqueId() {
        return project + "|" + service + "|" + metricName;
    }

    public String getService() {
        return service;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public Long getValue() {
        return value;
    }

    public String getMetricName() {
        return metricName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetricsEvent that = (MetricsEvent) o;

        if (!project.equals(that.project)) return false;
        if (!service.equals(that.service)) return false;
        return metrics.equals(that.metrics);
    }

    @Override
    public int hashCode() {
        int result = project.hashCode();
        result = 31 * result + service.hashCode();
        result = 31 * result + metrics.hashCode();
        return result;
    }
}