package ch.sbb.cloud.autoscaler.api;

/**
 * Created by thomas on 01.09.16.
 */

import ch.sbb.cloud.autoscaler.api.model.ConfigurationRequestBody;
import ch.sbb.cloud.autoscaler.model.Configuration;
import ch.sbb.cloud.autoscaler.model.metricsevents.Metrics;
import ch.sbb.cloud.autoscaler.repository.ConfigurationRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            return ResponseEntity.ok().build();
        }
    }

    @RequestMapping(
            path = "{project}/{service}/{metrics}/{metricsProviderService}",
            method = RequestMethod.DELETE
    )
    public ResponseEntity<Void> deleteConfiguration(
            @PathVariable(value = "project") String project,
            @PathVariable(value = "service") String service,
            @PathVariable(value = "metrics") Metrics metrics,
            @PathVariable(value = "metricsProviderService") String metricsProviderService
    ) {
        List<Configuration> configurations = configurationRepository.findByProjectAndTargetServiceAndMetricsAndMetricsProviderService(project, service, metrics, metricsProviderService);
        if (configurations.size() == 0) {
            return ResponseEntity.status(HttpStatus.GONE).build();
        }
        configurationRepository.delete(configurations.get(0));
        return ResponseEntity.ok().build();
    }

    private List<Configuration> findConfigurations(String project, String service, ConfigurationRequestBody
            requestBody) {
        Metrics metrics = requestBody.metrics;
        String metricsProviderService = requestBody.metricsProviderService;
        List<Configuration> configurations = configurationRepository.findByProjectAndTargetServiceAndMetricsAndMetricsProviderService(project, service, metrics, metricsProviderService);
        if (configurations.size() > 1)
            System.out.println("Uuups, Configuration exists more than once: " + project + "|" + service + "|" + requestBody);
        return configurations;
    }

    private Configuration newConfigurationFor(String project, String service, ConfigurationRequestBody requestBody) {
        Configuration configuration = new Configuration();
        configuration.setProject(project);
        configuration.setTargetService(service);
        configuration.setMetrics(requestBody.metrics);
        configuration.setMetricsProviderService(requestBody.metricsProviderService);
        configuration.setScaleUp(requestBody.scaleUp);
        configuration.setScaleDown(requestBody.scaleDown);
        return configuration;
    }

}
