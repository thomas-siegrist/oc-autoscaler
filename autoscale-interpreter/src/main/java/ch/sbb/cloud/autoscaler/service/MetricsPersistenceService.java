package ch.sbb.cloud.autoscaler.service;

import ch.sbb.cloud.autoscaler.model.MetricsEvent;
import ch.sbb.cloud.autoscaler.model.stats.MetricsStatistic;
import com.hazelcast.config.MultiMapConfig;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by micic on 13.09.16.
 */

@Component
public class MetricsPersistenceService {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsPersistenceService.class);

    private static final String HZ_METRICS_MAP = "Metrics";
    private static final String HZ_METRICS_STATS_MAP = "MetricsStats";
    private static final String HZ_LAST_SCALE_MAP = "Last-Scale";

    @Autowired
    private HazelcastInstance hz;

    @PostConstruct
    public void init() {
        MultiMapConfig multiMapConfig = new MultiMapConfig();
        multiMapConfig
                .setName(HZ_METRICS_MAP)
                .setBackupCount(0)
                .setAsyncBackupCount(1)
                .setValueCollectionType(MultiMapConfig.ValueCollectionType.LIST);
        hz.getConfig().addMultiMapConfig(multiMapConfig);
    }

    void persistMetricsStatistic(MetricsStatistic metricsStat) {
        hz
                .getMap(HZ_METRICS_STATS_MAP)
                .put(
                        metricsStat.composedUniqueId(),
                        metricsStat
                );
    }

    public List<MetricsStatistic> getAllStatistics() {
        return hz.getMap(HZ_METRICS_STATS_MAP)
                .values()
                .stream()
                .map(v -> (MetricsStatistic)v)
                .collect(Collectors.toList());
    }

    void persistMetricsEvent(MetricsEvent metricsEvent) {
        hz
                .getMultiMap(HZ_METRICS_MAP)
                .put(
                        metricsEvent.composedUniqueId(),
                        metricsEvent.getValue()
                );
    }

    Long calculateAggregateFor(MetricsEvent metricsEvent) {
        List<Long> values = hz
                .getMultiMap(HZ_METRICS_MAP)
                .get(metricsEvent.composedUniqueId())
                .stream()
                .map(v -> (Long) v)
                .collect(Collectors.toList());

        Double avg = values.stream()
                .skip(values.size() > 10 ? values.size() - 10 : 0)
                .mapToLong(v -> v)
                .average()
                .orElseGet(() -> 0);

        return avg.longValue();
    }

    Optional<LocalDateTime> getLastScaleDateTime(MetricsEvent metricsEvent) {
        return Optional.ofNullable((LocalDateTime) hz
                .getMap(HZ_LAST_SCALE_MAP)
                .get(metricsEvent.composedUniqueId()));
    }

    void setLastScaleTimeToNow(MetricsEvent metricsEvent) {
        hz
                .getMap(HZ_LAST_SCALE_MAP)
                .put(
                        metricsEvent.composedUniqueId(),
                        LocalDateTime.now()
                );
    }
}
