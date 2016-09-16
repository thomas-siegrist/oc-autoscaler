package ch.sbb.cloud.autoscaler.model;

/**
 * Created by micic on 16.09.16.
 */
public class ChartRowContentNumber implements ChartRowContent {

    Long v;

    public ChartRowContentNumber(Long value) {
        v = value;
    }

    public Long getV() {
        return v;
    }
}
