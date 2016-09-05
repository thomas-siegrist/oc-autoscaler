package ch.sbb.cloud.autoscaler.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import ch.sbb.cloud.autoscaler.model.Configuration;

/**
 * Created by thomas on 01.09.16.
 */
public interface ConfigurationRepository extends CrudRepository<Configuration, Long> {

    List<Configuration> findByProjectAndServiceAndMetricName(String project, String service, String metricName);
}
