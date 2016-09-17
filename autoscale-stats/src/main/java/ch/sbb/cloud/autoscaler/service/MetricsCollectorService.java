package ch.sbb.cloud.autoscaler.service;

import ch.sbb.cloud.autoscaler.model.*;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by micic on 14.09.16.
 */
@Component
class MetricsCollectorService {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsCollectorService.class);

    private static final String AUTOSCALE_INTERPRETER_API_URL = "/autoscaler/stats";

    private static final String AUTOSCALE_SCALER_API_URL = "/autoscaler/podstats";

    @Value("${autoscale-interpreter.url}")
    private String autoscaleInterpreterUrl;

    @Value("${autoscale-scaler.url}")
    private String autoscaleScalerUrl;

    private static Cache<String, MetricsStatisticSnapshot> metricsStatisticCache =
            CacheBuilder
                    .newBuilder()
                    .build();

    private static Cache<String, PodStatisticSnapshot> podsStatisticCache =
            CacheBuilder
                    .newBuilder()
                    .build();

    private Comparator<StatisticSnapshot> statisticSnapshotComparator = Comparator.comparing(
            StatisticSnapshot::getTime
    );

    @Scheduled(fixedRate = 10000)
    public void collectMetrics() {
        collectMetricsStats();
        collectPodStats();
    }

    private void collectPodStats() {

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<PodStatistic[]> respEntity = restTemplate.getForEntity(
                autoscaleScalerUrl + AUTOSCALE_SCALER_API_URL,
                PodStatistic[].class
        );

        PodStatistic[] podsList = respEntity.getBody();

        if (podsList != null ) {
            for (PodStatistic podStat : podsList) {

                LOG.info(
                        "Writing pod snapshot for {} with value: {}",
                        podStat.composedUniqueId(),
                        podStat.getPodCount()
                );

                PodStatisticSnapshot snap = new PodStatisticSnapshot();
                snap.setTime(LocalDateTime.now());
                snap.setPodStatistic(podStat);

                podsStatisticCache.put(podStat.composedUniqueId() + snap.getTime(), snap);
            }
        }
    }

    private void collectMetricsStats() {

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<MetricsStatistic[]> respEntity = restTemplate.getForEntity(
                autoscaleInterpreterUrl + AUTOSCALE_INTERPRETER_API_URL,
                MetricsStatistic[].class
        );

        MetricsStatistic[] metricsList = respEntity.getBody();

        if (metricsList != null ) {
            for (MetricsStatistic metricStat : metricsList) {

                LOG.info(
                        "Writing statistic snapshot for {} with value: {}",
                        metricStat.composedUniqueId(),
                        metricStat.getCurrentValue()
                );

                MetricsStatisticSnapshot snap = new MetricsStatisticSnapshot();
                snap.setTime(LocalDateTime.now());
                snap.setMetricsStatistic(metricStat);

                metricsStatisticCache.put(metricStat.composedUniqueId() + snap.getTime(), snap);
            }
        }
    }

    List<MetricsStatisticSnapshot> getMetricsStatisticSnapshots() {
        return metricsStatisticCache
                .asMap()
                .values()
                .stream()
                .sorted((a, b) -> statisticSnapshotComparator.compare(a, b))
                .collect(Collectors.toList());
    }

    List<PodStatisticSnapshot> getPodsStatisticSnapshots() {
        return podsStatisticCache
                .asMap()
                .values()
                .stream()
                .sorted((a, b) -> statisticSnapshotComparator.compare(a, b))
                .collect(Collectors.toList());
    }
}
