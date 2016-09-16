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
}
