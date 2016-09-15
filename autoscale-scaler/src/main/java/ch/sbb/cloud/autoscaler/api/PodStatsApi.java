package ch.sbb.cloud.autoscaler.api;

import ch.sbb.cloud.autoscaler.api.model.PodStatistic;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by micic on 13.09.16.
 */
@RestController()
@RequestMapping("/autoscaler/podstats")
public class PodStatsApi {

    private static final Logger LOG = LoggerFactory.getLogger(PodStatsApi.class);

    @Autowired
    private NamespacedOpenShiftClient openShiftClient;

    @RequestMapping(
            path = "",
            produces = "application/json",
            method = RequestMethod.GET
    )
    public List<PodStatistic> getMetricsStats() {
        List<PodStatistic> podStatistics = new ArrayList<>();

        List<String> projects = openShiftClient
                .inAnyNamespace()
                .namespaces()
                .list()
                .getItems()
                .stream()
                .map(namespace -> namespace.getMetadata().getName())
                .collect(Collectors.toList());

        for (String project : projects) {
            List<String> services = openShiftClient
                    .inNamespace(project)
                    .services()
                    .list()
                    .getItems()
                    .stream()
                    .map(namespace -> namespace.getMetadata().getName())
                    .collect(Collectors.toList());

            for (String service : services) {
                int podCount = getPodsForService(service, project);
                podStatistics.add(createPodStatistic(project, service, podCount));
            }

        }

        return podStatistics;
    }

    private int getPodsForService(String project, String service) {
        return openShiftClient
                .inNamespace(project)
                .deploymentConfigs()
                .withName(service)
                .get()
                .getSpec().getReplicas();
    }

    private PodStatistic createPodStatistic(String projectName, String serviceName, int podCount) {
        return PodStatistic.builder()
                .project(projectName)
                .service(serviceName)
                .podCount(podCount)
                .build();
    }

}
