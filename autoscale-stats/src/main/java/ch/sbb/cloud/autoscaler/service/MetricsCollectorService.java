package ch.sbb.cloud.autoscaler.service;

import ch.sbb.cloud.autoscaler.model.*;
import com.hazelcast.config.MultiMapConfig;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by micic on 14.09.16.
 */
@Component
class MetricsCollectorService {

    private static final String HZ_METRICS_STATS_MAP = "METRICS_STATS_SNAPSHOT_MAP";

    private static final String HZ_PODS_STATS_MAP = "PODS_STATS_SNAPSHOT_MAP";

    private static final Logger LOG = LoggerFactory.getLogger(MetricsCollectorService.class);

    private static final String AUTOSCALE_INTERPRETER_API_URL = "/autoscaler/stats";

    private static final String AUTOSCALE_SCALER_API_URL = "/autoscaler/podstats";

    @Value("${autoscale-interpreter.url}")
    private String autoscaleInterpreterUrl;

    @Value("${autoscale-scaler.url}")
    private String autoscaleScalerUrl;

    @Autowired
    private HazelcastInstance hz;

    private Comparator<StatisticSnapshot> statisticSnapshotComparator = Comparator.comparing(
            StatisticSnapshot::getTime
    );

    @PostConstruct
    public void init() {

        MultiMapConfig multiMapConfigMetrics = new MultiMapConfig();
        multiMapConfigMetrics
                .setName(HZ_METRICS_STATS_MAP)
                .setBackupCount(0)
                .setAsyncBackupCount(1)
                .setValueCollectionType(MultiMapConfig.ValueCollectionType.LIST);
        hz.getConfig().addMultiMapConfig(multiMapConfigMetrics);
        hz.getMultiMap(HZ_METRICS_STATS_MAP).clear();

        MultiMapConfig multiMapConfigPods = new MultiMapConfig();
        multiMapConfigPods
                .setName(HZ_PODS_STATS_MAP)
                .setBackupCount(0)
                .setAsyncBackupCount(1)
                .setValueCollectionType(MultiMapConfig.ValueCollectionType.LIST);
        hz.getConfig().addMultiMapConfig(multiMapConfigPods);
        hz.getMultiMap(HZ_PODS_STATS_MAP).clear();
    }

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
                hz
                        .getMultiMap(HZ_PODS_STATS_MAP)
                        .put(podStat.composedUniqueId(), snap);
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
                hz
                        .getMultiMap(HZ_METRICS_STATS_MAP)
                        .put(metricStat.composedUniqueId(), snap);
            }
        }
    }

    List<MetricsStatisticSnapshot> getMetricsStatisticSnapshots() {
        return hz
                .getMultiMap(HZ_METRICS_STATS_MAP)
                .values()
                .stream()
                .map(s -> (MetricsStatisticSnapshot)s)
                .sorted((a, b) -> statisticSnapshotComparator.compare(a, b))
                .collect(Collectors.toList());
    }

    List<PodStatisticSnapshot> getPodsStatisticSnapshots() {
        return hz
                .getMultiMap(HZ_PODS_STATS_MAP)
                .values()
                .stream()
                .map(s -> (PodStatisticSnapshot)s)
                .sorted((a, b) -> statisticSnapshotComparator.compare(a, b))
                .collect(Collectors.toList());
    }
}
