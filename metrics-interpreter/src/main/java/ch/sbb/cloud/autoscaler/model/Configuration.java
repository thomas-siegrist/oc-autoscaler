package ch.sbb.cloud.autoscaler.model;


import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by thomas on 01.09.16.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "Domain-Key",
        columnNames = {"project", "target_service", "metrics_provider_service", "metrics"}
))
public class Configuration implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "project")
    private String project;

    @Column(name = "target_service")
    private String targetService;

    @Column(name = "metrics_provider_service")
    private String metricsProviderService;

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

    public String getTargetService() {
        return targetService;
    }

    public void setTargetService(String targetService) {
        this.targetService = targetService;
    }

    public String getMetricsProviderService() {
        return metricsProviderService;
    }

    public void setMetricsProviderService(String metricsProviderService) {
        this.metricsProviderService = metricsProviderService;
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
