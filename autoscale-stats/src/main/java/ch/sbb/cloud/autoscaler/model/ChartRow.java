package ch.sbb.cloud.autoscaler.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by micic on 16.09.16.
 */
public class ChartRow {

    List<ChartRowContent> c = new ArrayList<>();

    public List<ChartRowContent> getC() {
        return c;
    }

    public ChartRow addContentEntry(ChartRowContent content) {
        c.add(content);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChartRow chartRow = (ChartRow) o;

        return c != null ? c.equals(chartRow.c) : chartRow.c == null;

    }

    @Override
    public int hashCode() {
        return c != null ? c.hashCode() : 0;
    }
}
