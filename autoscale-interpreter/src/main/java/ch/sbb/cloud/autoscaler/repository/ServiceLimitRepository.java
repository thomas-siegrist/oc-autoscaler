package ch.sbb.cloud.autoscaler.repository;

import ch.sbb.cloud.autoscaler.model.ServiceLimit;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by micic on 11.09.16.
 */
public interface ServiceLimitRepository extends CrudRepository<ServiceLimit, Long> {

    List<ServiceLimit> findByProjectAndService(String project, String service);
}
