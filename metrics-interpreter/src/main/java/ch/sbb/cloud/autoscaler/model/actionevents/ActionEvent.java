package ch.sbb.cloud.autoscaler.model.actionevents;

import java.io.Serializable;

/**
 * Created by thomas on 27.08.16.
 */
public class ActionEvent implements Serializable {

    private String project;
    private String service;
    private Scale scale;
    private Long maxReplicas;
    private Long minReplicas;

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

    public Scale getScale() {
        return scale;
    }

    public void setScale(Scale scale) {
        this.scale = scale;
    }

    public Long getMinReplicas() {
        return minReplicas;
    }

    public void setMinReplicas(Long minReplicas) {
        this.minReplicas = minReplicas;
    }

    public Long getMaxReplicas() {
        return maxReplicas;
    }

    public void setMaxReplicas(Long maxReplicas) {
        this.maxReplicas = maxReplicas;
    }
}
