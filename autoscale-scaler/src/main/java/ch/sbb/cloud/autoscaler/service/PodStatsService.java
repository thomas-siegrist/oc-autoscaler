package ch.sbb.cloud.autoscaler.service;

import ch.sbb.cloud.autoscaler.api.PodStatsApi;
import ch.sbb.cloud.autoscaler.api.model.PodStatistic;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by siegrist on 17.09.16.
 */
@Component
public class PodStatsService {

    private static final Logger LOG = LoggerFactory.getLogger(PodStatsApi.class);

    @Autowired
    private NamespacedOpenShiftClient openShiftClient;

    @Autowired
    @Value("${influxdbhost}")
    private String influxDBHost;

    @Autowired
    @Value("${influxdbport}")
    private String influxDBPort;

    @Scheduled(fixedDelay = 30_000, initialDelay = 10_000)
    public void postPodStatsToInfluxDB() {
        getPodStatistics().stream().forEach(podStats -> writeToInfluxDB(podStats));
    }

    private void writeToInfluxDB(PodStatistic metricsEvent) {
        Point point = Point.measurement(metricsEvent.compositeUniqueId() + "|POD_COUNT")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("value", metricsEvent.podCount)
                .build();

        influxDB().write("autoscaler", "default", point);
    }

    private InfluxDB influxDB() {
        InfluxDB influxDB = InfluxDBFactory.connect("http://" + influxDBHost + ":" + influxDBPort, "root", "root");
        if (influxDB != null && !influxDB.describeDatabases().contains("autoscaler")) {
            influxDB.createDatabase("autoscaler");
        }
        return influxDB;
    }

    public List<PodStatistic> getPodStatistics() {
        List<PodStatistic> podStatistics = new ArrayList<>();

        /*List<String> projects = openShiftClient
                .inAnyNamespace()
                .namespaces()
                .list()
                .getItems()
                .stream()
                .map(namespace -> namespace.getMetadata().getName())
                .collect(Collectors.toList());

        for (String project : projects) {*/
        String project = "usecase";
        List<String> services = openShiftClient
                .inNamespace(project)
                .services()
                .list()
                .getItems()
                .stream()
                .map(namespace -> namespace.getMetadata().getName())
                .collect(Collectors.toList());

        for (String service : services) {
            int podCount = getPodsForService(project, service);
            podStatistics.add(createPodStatistic(project, service, podCount));
        }

        //}

        return podStatistics;
    }

    private int getPodsForService(String project, String service) {
        try {
            return openShiftClient
                    .inNamespace(project)
                    .deploymentConfigs()
                    .withName(service)
                    .get()
                    .getSpec().getReplicas();
        } catch (Throwable t) {
            LOG.error(t.getMessage());
            return -1;
        }
    }

    private PodStatistic createPodStatistic(String projectName, String serviceName, int podCount) {
        return PodStatistic.builder()
                .project(projectName)
                .service(serviceName)
                .podCount(podCount)
                .build();
    }

}
