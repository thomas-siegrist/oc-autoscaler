package ch.sbb.cloud.autoscaler.model;

import java.time.LocalDateTime;

/**
 * Created by micic on 17.09.16.
 */
public interface StatisticSnapshot {
    LocalDateTime getTime();
}
