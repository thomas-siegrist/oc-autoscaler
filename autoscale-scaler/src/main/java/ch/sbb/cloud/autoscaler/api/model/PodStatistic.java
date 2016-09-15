package ch.sbb.cloud.autoscaler.api.model;

import java.io.Serializable;

/**
 * Created by thomas on 15.09.16.
 */
public class PodStatistic implements Serializable {

    public String project;
    public String service;
    public Integer podCount;

    private PodStatistic(String project, String service, Integer podCount) {
        this.project = project;
        this.service = service;
        this.podCount = podCount;
    }

    public static PodStatisticBuilder builder() {
        return new PodStatisticBuilder();
    }

    public static class PodStatisticBuilder {
        private String project;
        private String service;
        private Integer podCount;

        public PodStatisticBuilder project(String project) {
            this.project = project;
            return this;
        }

        public PodStatisticBuilder service(String service) {
            this.service = service;
            return this;
        }

        public PodStatisticBuilder podCount(Integer podCount) {
            this.podCount = podCount;
            return this;
        }

        public PodStatistic build() {
            return new PodStatistic(
                    project,
                    service,
                    podCount
            );
        }

    }

}
