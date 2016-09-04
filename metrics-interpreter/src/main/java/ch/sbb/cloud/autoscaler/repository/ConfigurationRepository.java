package ch.sbb.cloud.autoscaler.repository;

import ch.sbb.cloud.autoscaler.model.Configuration;
import ch.sbb.cloud.autoscaler.model.Metrics;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by thomas on 01.09.16.
 */
public interface ConfigurationRepository extends CrudRepository<Configuration, Long> {

    List<Configuration> findByProjectAndTargetServiceAndMetricsAndMetricsProviderService(String project, String service, Metrics metrics, String metricsProviderService);
}
