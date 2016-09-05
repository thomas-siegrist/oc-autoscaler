package ch.sbb.cloud.autoscaler.api;

/**
 * Created by thomas on 01.09.16.
 */

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ch.sbb.cloud.autoscaler.api.model.ConfigurationRequestBody;
import ch.sbb.cloud.autoscaler.model.Configuration;
import ch.sbb.cloud.autoscaler.model.Metrics;
import ch.sbb.cloud.autoscaler.repository.ConfigurationRepository;

/**
 * Created by thomas on 04.08.16.
 */
@RestController()
@RequestMapping("/autoscaler/configurations")
public class ConfigurationApi {

    @Autowired
    private RabbitTemplate template;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @RequestMapping(
            path = "",
            produces = "application/json",
            method = RequestMethod.GET
            )
            public List<Configuration> getConfigurations() {
        return (List<Configuration>) configurationRepository.findAll();
    }

    @Transactional
    @RequestMapping(
            path = "{project}/{service}",
            consumes = "application/json",
            method = RequestMethod.POST
            )
            public ResponseEntity<Void> createConfiguration(
                    @PathVariable(value = "project") String project,
                    @PathVariable(value = "service") String service,
                    @RequestBody ConfigurationRequestBody requestBody
            ) {
        List<Configuration> configurations = findConfigurations(project, service, requestBody);
        if (configurations.size() == 0) {
            configurationRepository.save(newConfigurationFor(project, service, requestBody));
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            Configuration configuration = configurations.get(0);
            configuration.setScaleUp(requestBody.scaleUp);
            configuration.setScaleDown(requestBody.scaleDown);
            configurationRepository.save(configuration);
            return ResponseEntity.ok().build();
        }
    }

    @Transactional
    @RequestMapping(
            path = "{project}/{service}/{metricName}",
            method = RequestMethod.DELETE
            )
            public ResponseEntity<Void> deleteConfiguration(
                    @PathVariable(value = "project") String project,
                    @PathVariable(value = "service") String service,
                    @PathVariable(value = "metricName") String metricName
            ) {
        List<Configuration> configurations = configurationRepository.findByProjectAndServiceAndMetricName(project, service, metricName);
        if (configurations.size() == 0) {
            return ResponseEntity.status(HttpStatus.GONE).build();
        }
        configurationRepository.delete(configurations.get(0));
        return ResponseEntity.ok().build();
    }

    private List<Configuration> findConfigurations(String project, String service, ConfigurationRequestBody requestBody) {
        Metrics metrics = requestBody.metrics;
        String metricName = requestBody.metricName;
        List<Configuration> configurations = configurationRepository.findByProjectAndServiceAndMetricName(project, service, metricName);
        if (configurations.size() > 1)
            System.out.println("Uuups, Configuration exists more than once: " + project + "|" + service + "|" + requestBody);
        return configurations;
    }

    private Configuration newConfigurationFor(String project, String service, ConfigurationRequestBody requestBody) {
        Configuration configuration = new Configuration();
        configuration.setProject(project);
        configuration.setService(service);
        configuration.setMetrics(requestBody.metrics);
        configuration.setScaleUp(requestBody.scaleUp);
        configuration.setScaleDown(requestBody.scaleDown);
        return configuration;
    }

}
