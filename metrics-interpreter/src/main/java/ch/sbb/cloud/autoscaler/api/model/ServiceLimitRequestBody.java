package ch.sbb.cloud.autoscaler.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;

/**
 * Created by micic on 11.09.16.
 */
@JsonSerialize
public class ServiceLimitRequestBody implements Serializable {

    @JsonProperty(required = true)
    public Long minPods;

    @JsonProperty(required = true)
    public Long maxPods;

}
