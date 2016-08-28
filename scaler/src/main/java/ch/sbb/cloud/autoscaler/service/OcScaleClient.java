package ch.sbb.cloud.autoscaler.service;

import ch.sbb.cloud.autoscaler.model.actionevents.ActionEvent;
import ch.sbb.cloud.autoscaler.model.actionevents.Scale;
import ch.sbb.cloud.autoscaler.model.openshiftorigin.OcMetadata;
import ch.sbb.cloud.autoscaler.model.openshiftorigin.OcScale;
import ch.sbb.cloud.autoscaler.model.openshiftorigin.OcSpec;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * Created by thomas on 27.08.16.
 */
@Component
public class OcScaleClient {

    private static final Logger LOG = LoggerFactory.getLogger(OcScaleClient.class);

    private RestTemplate rest = restWithoutSSLTrusts();

    public void scale(ActionEvent actionEvent) {
        ResponseEntity<String> currentDeploymentConfig = rest.exchange(
                "https://192.168.99.1:8443/oapi/v1/namespaces/{project}/deploymentconfigs/{service}"
                        .replace("{project}", actionEvent.project)
                        .replace("{service}", actionEvent.service),
                HttpMethod.GET,
                new HttpEntity<String>(httpHeaders()),
                String.class
        );
        DocumentContext jsonContext = JsonPath.parse(currentDeploymentConfig.getBody());
        Integer currentReplicas = (Integer) jsonContext.read("$.spec.replicas");

        int newNumberOfRelpicas = newNumberOfRelpicas(actionEvent.scale, currentReplicas);
        if (newNumberOfRelpicas >= 0) {
            LOG.info("Scaling " + actionEvent.project + ":" + actionEvent.service + " to {" + newNumberOfRelpicas + "}");
            rest.put(
                    "https://192.168.99.1:8443/oapi/v1/namespaces/{project}/deploymentconfigs/{service}/scale"
                            .replace("{project}", actionEvent.project)
                            .replace("{service}", actionEvent.service),
                    httpEntity(actionEvent, newNumberOfRelpicas)
            );
        }
    }

    private HttpEntity<OcScale> httpEntity(ActionEvent actionEvent, Integer newNumberOfRelpicas) {
        OcScale ocScale = new OcScale();

        ocScale.metadata = metadataFor(actionEvent);
        ocScale.spec = specFor(actionEvent, newNumberOfRelpicas);

        return new HttpEntity<>(ocScale, httpHeaders());
    }

    private OcMetadata metadataFor(ActionEvent actionEvent) {
        OcMetadata ocMetadata = new OcMetadata();
        ocMetadata.name = actionEvent.service;
        ocMetadata.namespace = actionEvent.project;
        return ocMetadata;
    }

    private OcSpec specFor(ActionEvent actionEvent, Integer newNumberOfRelpicas) {
        OcSpec ocSpec = new OcSpec();
        ocSpec.replicas = newNumberOfRelpicas;
        return ocSpec;
    }

    private int newNumberOfRelpicas(Scale scale, Integer currentReplicas) {
        switch (scale) {
            case UP:
                return ++currentReplicas;
            case DOWN:
                return --currentReplicas;
        }
        return currentReplicas;
    }

    private HttpHeaders httpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer J6W54uw9VVmiITKhI-zsvmVYgeO7UncfHfXdF1K6lSY");
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json");
        return headers;
    }

    private RestTemplate restWithoutSSLTrusts() {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        SSLContext sslContext = null;

        try {
            sslContext = org.apache.http.ssl.SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        }

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);

        return new RestTemplate(requestFactory);
    }

}
