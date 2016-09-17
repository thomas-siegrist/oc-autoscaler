package ch.sbb.cloud.autoscaler.model;

/**
 * Created by micic on 16.09.16.
 */
public class ChartRowContentString implements ChartRowContent{

    String v;

    public ChartRowContentString(String value) {
        v = value;
    }

    public String getV() {
        return v;
    }
}
