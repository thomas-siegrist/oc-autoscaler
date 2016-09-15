package ch.sbb.cloud.autoscaler.repository;

import java.util.List;

import ch.sbb.cloud.autoscaler.model.AutoscaleConfiguration;
import ch.sbb.cloud.autoscaler.model.Metrics;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by thomas on 01.09.16.
 */
public interface AutoscaleConfigurationRepository extends CrudRepository<AutoscaleConfiguration, Long> {

    List<AutoscaleConfiguration> findByProjectAndServiceAndMetricsAndMetricName(String project, String service, Metrics metrics, String metricName);

    List<AutoscaleConfiguration> findByProjectAndServiceAndMetrics(String project, String service, Metrics metrics);
}
