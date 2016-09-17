package ch.sbb.cloud.autoscaler.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by micic on 14.09.16.
 */
public class PodStatisticSnapshot implements StatisticSnapshot, Serializable {

    private LocalDateTime time;
    private PodStatistic PodStatistic;

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public PodStatistic getPodStatistic() {
        return PodStatistic;
    }

    public void setPodStatistic(PodStatistic podStatistic) {
        this.PodStatistic = podStatistic;
    }
}
