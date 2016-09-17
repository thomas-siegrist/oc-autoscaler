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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChartRowContentString that = (ChartRowContentString) o;

        return v != null ? v.equals(that.v) : that.v == null;
    }

    @Override
    public int hashCode() {
        return v != null ? v.hashCode() : 0;
    }
}
