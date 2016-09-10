package ch.sbb.cloud.autoscaler.service;

import ch.sbb.cloud.autoscaler.model.actionevents.ActionEvent;
import ch.sbb.cloud.autoscaler.model.actionevents.Scale;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by thomas on 27.08.16.
 */
@Component
public class OcScaleClient {

    private static final Logger LOG = LoggerFactory.getLogger(OcScaleClient.class);

    @Value("${openshift.master}")
    private String master;

    @Value("${openshift.user}")
    private String user;

    @Value("${openshift.password}")
    private String password;

    private NamespacedOpenShiftClient openShiftClient;

    @PostConstruct
    public void init() {

        Config kubeConfig = new Config();
        kubeConfig.setUsername(user);
        kubeConfig.setPassword(password);
        kubeConfig.setMasterUrl(master);
        openShiftClient = new DefaultOpenShiftClient(kubeConfig);
    }

    public void scale(ActionEvent actionEvent) {

        Integer currentReplicas = getReplicasForService(actionEvent.project, actionEvent.service);

        int newNumberOfReplicas = newNumberOfReplicas(actionEvent.scale, currentReplicas);
        if (newNumberOfReplicas >= 0) {
            LOG.info("Scaling " + actionEvent.project + ":" + actionEvent.service + " to {" + newNumberOfReplicas + "}");
            setReplicasForService(actionEvent.project, actionEvent.service, newNumberOfReplicas);
        }
    }

    private int getReplicasForService(String project, String service) {
        return openShiftClient
                .inNamespace(project)
                .deploymentConfigs()
                .withName(service)
                .get()
                .getSpec().getReplicas();
    }

    private void setReplicasForService(String project, String service, int replicas) {
        openShiftClient
                .inNamespace(project)
                .deploymentConfigs()
                .withName(service)
                .edit()
                .editSpec()
                .withReplicas(replicas)
                .endSpec()
                .done();
    }

    private int newNumberOfReplicas(Scale scale, Integer currentReplicas) {
        switch (scale) {
            case UP:
                return ++currentReplicas;
            case DOWN:
                return --currentReplicas;
        }
        return currentReplicas;
    }
}
