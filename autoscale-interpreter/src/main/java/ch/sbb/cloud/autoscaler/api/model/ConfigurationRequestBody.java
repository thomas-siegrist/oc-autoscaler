package ch.sbb.cloud.autoscaler.api.model;

import ch.sbb.cloud.autoscaler.model.Metrics;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;

/**
 * Created by thomas on 01.09.16.
 */
@JsonSerialize
public class ConfigurationRequestBody implements Serializable {

    @JsonProperty(required = true)
    public String metricName;

    @JsonProperty(required = true)
    public Metrics metrics;

    @JsonProperty(required = true)
    public Long scaleUp;

    @JsonProperty(required = true)
    public Long scaleDown;

}
