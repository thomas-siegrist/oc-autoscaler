package ch.sbb.cloud.autoscaler.api;

/**
 * Created by thomas on 01.09.16.
 */

import ch.sbb.cloud.autoscaler.api.model.ConfigurationRequestBody;
import ch.sbb.cloud.autoscaler.model.AutoscaleConfiguration;
import ch.sbb.cloud.autoscaler.model.Metrics;
import ch.sbb.cloud.autoscaler.repository.AutoscaleConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by thomas on 04.08.16.
 */
@RestController()
@RequestMapping("/autoscaler/configurations")
public class AutoscaleConfigurationApi {

    private static final Logger LOG = LoggerFactory.getLogger(AutoscaleConfigurationApi.class);

    @Autowired
    private RabbitTemplate template;

    @Autowired
    private AutoscaleConfigurationRepository autoscaleConfigurationRepository;

    @RequestMapping(
            path = "",
            produces = "application/json",
            method = RequestMethod.GET
    )
    public List<AutoscaleConfiguration> getConfigurations() {
        return (List<AutoscaleConfiguration>) autoscaleConfigurationRepository.findAll();
    }

    @Transactional
    @RequestMapping(
            path = "{project}/{service}",
            consumes = "application/json",
            method = RequestMethod.POST
    )
    public ResponseEntity<Long> createConfiguration(
            @PathVariable(value = "project") String project,
            @PathVariable(value = "service") String service,
            @RequestBody ConfigurationRequestBody requestBody
    ) {
        List<AutoscaleConfiguration> autoscaleConfigurations = findConfigurations(project, service, requestBody);
        if (autoscaleConfigurations.size() == 0) {
            AutoscaleConfiguration entity = autoscaleConfigurationRepository.save(newConfigurationFor(project, service, requestBody));
            return ResponseEntity.status(HttpStatus.CREATED).body(entity.getId());
        } else {
            AutoscaleConfiguration autoscaleConfiguration = autoscaleConfigurations.get(0);
            autoscaleConfiguration.setScaleUp(requestBody.scaleUp);
            autoscaleConfiguration.setScaleDown(requestBody.scaleDown);
            autoscaleConfiguration.setMetricName(requestBody.metricName);
            autoscaleConfigurationRepository.save(autoscaleConfiguration);
            return ResponseEntity.ok().body(autoscaleConfiguration.getId());
        }
    }

    @Transactional
    @RequestMapping(
            path = "{id}",
            method = RequestMethod.DELETE
    )
    public ResponseEntity<String> deleteConfiguration(@PathVariable(value = "id") Long id) {
        AutoscaleConfiguration autoscaleConfiguration = autoscaleConfigurationRepository.findOne(id);
        if (autoscaleConfiguration == null) {
            return ResponseEntity.status(HttpStatus.GONE).body("Object does not exist!");
        }
        autoscaleConfigurationRepository.delete(autoscaleConfiguration);
        return ResponseEntity.ok().body("Successfully deleted!");
    }

    private String nullToEmptyString(String metricName) {
        return metricName == null ? "" : metricName;
    }

    private List<AutoscaleConfiguration> findConfigurations(String project, String service, ConfigurationRequestBody requestBody) {
        Metrics metrics = requestBody.metrics;
        String metricName = requestBody.metricName;
        List<AutoscaleConfiguration> autoscaleConfigurations = autoscaleConfigurationRepository.findByProjectAndServiceAndMetricsAndMetricName(project, service, metrics, metricName);
        if (autoscaleConfigurations.size() > 1)
            LOG.info("Uuups, AutoscaleConfiguration exists more than once: {} | {} | {}", project, service, requestBody);
        return autoscaleConfigurations;
    }

    private AutoscaleConfiguration newConfigurationFor(String project, String service, ConfigurationRequestBody requestBody) {
        AutoscaleConfiguration autoscaleConfiguration = new AutoscaleConfiguration();
        autoscaleConfiguration.setProject(project);
        autoscaleConfiguration.setService(service);
        autoscaleConfiguration.setMetrics(requestBody.metrics);
        autoscaleConfiguration.setMetricName(requestBody.metricName);
        autoscaleConfiguration.setScaleUp(requestBody.scaleUp);
        autoscaleConfiguration.setScaleDown(requestBody.scaleDown);
        return autoscaleConfiguration;
    }

}
