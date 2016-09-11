package ch.sbb.cloud.autoscaler.model;


import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by micic on 11.09.16.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "servicelimit_domainkey",
        columnNames = {"project", "service"}
))
public class ServiceLimit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "project")
    private String project;

    @Column(name = "service")
    private String service;

    @Column(name = "min_pods")
    private Long minPods;

    @Column(name = "max_pods")
    private Long maxPods;

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

    public Long getMinPods() {
        return minPods;
    }

    public void setMinPods(Long minPods) {
        this.minPods = minPods;
    }

    public Long getMaxPods() {
        return maxPods;
    }

    public void setMaxPods(Long maxPods) {
        this.maxPods = maxPods;
    }
}
