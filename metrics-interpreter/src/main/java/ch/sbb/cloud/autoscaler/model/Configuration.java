package ch.sbb.cloud.autoscaler.model;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Created by thomas on 01.09.16.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "Domain-Key",
        columnNames = { "project", "service", "metric_name" }
        ))
public class Configuration implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "project")
    private String project;

    @Column(name = "service")
    private String service;

    @Column(name = "metric_name")
    private String metricName;

    @Column(name = "metrics")
    private Metrics metrics;

    @Column(name = "scale_up")
    private Long scaleUp;

    @Column(name = "scale_down")
    private Long scaleDown;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }

    public Long getScaleUp() {
        return scaleUp;
    }

    public void setScaleUp(Long scaleUp) {
        this.scaleUp = scaleUp;
    }

    public Long getScaleDown() {
        return scaleDown;
    }

    public void setScaleDown(Long scaleDown) {
        this.scaleDown = scaleDown;
    }

}
