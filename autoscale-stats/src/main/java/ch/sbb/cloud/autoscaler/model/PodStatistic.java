package ch.sbb.cloud.autoscaler.model;

import java.io.Serializable;

/**
 * Created by thomas on 15.09.16.
 */
public class PodStatistic implements Serializable {

    private String project;
    private String service;
    private Integer podCount;

    public String composedUniqueId() {
        return this.project + "|" + this.service;
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

    public Integer getPodCount() {
        return podCount;
    }

    public void setPodCount(Integer podCount) {
        this.podCount = podCount;
    }
}
