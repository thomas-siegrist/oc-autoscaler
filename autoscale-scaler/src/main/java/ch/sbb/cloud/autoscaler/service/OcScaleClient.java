package ch.sbb.cloud.autoscaler.service;

import ch.sbb.cloud.autoscaler.model.actionevents.ActionEvent;
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
        long newNumberOfReplicas = newNumberOfReplicas(actionEvent, currentReplicas);

        if (newNumberOfReplicas != currentReplicas) {
            LOG.info("Scaling {}:{} to {}", actionEvent.project, actionEvent.service, newNumberOfReplicas);
            setReplicasForService(actionEvent.project, actionEvent.service, newNumberOfReplicas);
        } else {
            LOG.info(
                    "No scaling required for {}:{}. Replicas:{}, min:{}, max:{}",
                    actionEvent.project,
                    actionEvent.service,
                    currentReplicas,
                    actionEvent.minReplicas,
                    actionEvent.maxReplicas
            );
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

    private void setReplicasForService(String project, String service, Long replicas) {
        openShiftClient
                .inNamespace(project)
                .deploymentConfigs()
                .withName(service)
                .edit()
                .editSpec()
                .withReplicas(replicas.intValue())
                .endSpec()
                .done();
    }

    private long newNumberOfReplicas(ActionEvent actionEvent, Integer currentReplicas) {
        long newNumberOfReplicas;
        switch (actionEvent.scale) {
            case UP:
                newNumberOfReplicas = ++currentReplicas;
                break;
            case DOWN:
                newNumberOfReplicas = --currentReplicas;
                break;
            default:
                newNumberOfReplicas = currentReplicas;
        }

        if (newNumberOfReplicas > actionEvent.maxReplicas)
            newNumberOfReplicas = actionEvent.maxReplicas;

        if (newNumberOfReplicas < actionEvent.minReplicas)
            newNumberOfReplicas = actionEvent.minReplicas;

        return newNumberOfReplicas;
    }
}
