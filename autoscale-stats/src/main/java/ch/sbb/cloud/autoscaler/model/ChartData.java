package ch.sbb.cloud.autoscaler.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by micic on 16.09.16.
 */
public class ChartData implements Serializable {

    public List<ChartColDef> cols = new ArrayList<>();
    public List<ChartRow> rows = new ArrayList<>();
}
