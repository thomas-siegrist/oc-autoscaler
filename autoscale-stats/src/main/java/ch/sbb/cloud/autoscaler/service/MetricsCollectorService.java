package ch.sbb.cloud.autoscaler.service;

import ch.sbb.cloud.autoscaler.model.MetricsStatistic;
import ch.sbb.cloud.autoscaler.model.MetricsStatisticSnapshot;
import com.hazelcast.config.MultiMapConfig;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final String HZ_STATS_MAP = "STATS_SNAPSHOT_MAP";

    private static final Logger LOG = LoggerFactory.getLogger(MetricsCollectorService.class);

    @Autowired
    private HazelcastInstance hz;

    Comparator<MetricsStatisticSnapshot> metricsStatsSnapshotComperator = Comparator.comparing(
            MetricsStatisticSnapshot::getTime
    );

    @PostConstruct
    public void init() {

        MultiMapConfig multiMapConfig = new MultiMapConfig();
        multiMapConfig
                .setName(HZ_STATS_MAP)
                .setBackupCount(0)
                .setAsyncBackupCount(1)
                .setValueCollectionType(MultiMapConfig.ValueCollectionType.LIST);
        hz.getConfig().addMultiMapConfig(multiMapConfig);
        hz.getMultiMap(HZ_STATS_MAP).clear();
    }

    @Scheduled(fixedRate = 10000)
    public void collectMetrics() {

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<MetricsStatistic[]> respEntity = restTemplate.getForEntity(
                "http://autoscale-interpreter.openshift.local:80/autoscaler/stats",
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
                        .getMultiMap(HZ_STATS_MAP)
                        .put(metricStat.composedUniqueId(), snap);
            }
        }
    }

    List<MetricsStatisticSnapshot> getMetricsStatisticSnapshots() {
        return hz
                .getMultiMap(HZ_STATS_MAP)
                .values()
                .stream()
                .map(s -> (MetricsStatisticSnapshot)s)
                .sorted((a, b) -> metricsStatsSnapshotComperator.compare(a, b))
                .collect(Collectors.toList());
    }
}
