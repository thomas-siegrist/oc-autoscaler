package ch.sbb.cloud.autoscaler.model;

import java.io.Serializable;

/**
 * Created by micic on 16.09.16.
 */
public class ChartColDef implements Serializable {

    public ChartColDefType type;
    public String label;

    public ChartColDef(ChartColDefType type, String label) {
        this.type = type;
        this.label = label;
    }

    public ChartColDefType getType() {
        return type;
    }

    public void setType(ChartColDefType type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
