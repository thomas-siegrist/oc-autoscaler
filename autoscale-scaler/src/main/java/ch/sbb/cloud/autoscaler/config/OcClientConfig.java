package ch.sbb.cloud.autoscaler.config;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by thomas on 15.09.16.
 */
@Configuration
public class OcClientConfig {

    @Value("${openshift.master}")
    private String master;

    @Value("${openshift.user}")
    private String user;

    @Value("${openshift.password}")
    private String password;

    @Bean
    public NamespacedOpenShiftClient createNamespacedOpenShiftClient() {
        Config kubeConfig = new Config();
        kubeConfig.setUsername(user);
        kubeConfig.setPassword(password);
        kubeConfig.setMasterUrl(master);
        kubeConfig.setTrustCerts(true);
        return new DefaultOpenShiftClient(kubeConfig);
    }

}
