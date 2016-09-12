package ch.sbb.cloud.autoscaler.model.openshiftorigin;

import java.io.Serializable;

/**
 * Created by thomas on 27.08.16.
 */
public class OcScale implements Serializable {

    public String apiVersion = "extensions/v1beta1";
    public String kind = "Scale";
    public OcMetadata metadata;
    public OcSpec spec;


}
