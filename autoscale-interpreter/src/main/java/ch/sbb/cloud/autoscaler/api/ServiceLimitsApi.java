package ch.sbb.cloud.autoscaler.api;

import ch.sbb.cloud.autoscaler.api.model.ServiceLimitRequestBody;
import ch.sbb.cloud.autoscaler.model.ServiceLimit;
import ch.sbb.cloud.autoscaler.repository.ServiceLimitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by micic on 11.09.16.
 */
@RestController()
@RequestMapping("/autoscaler/servicelimits")
public class ServiceLimitsApi {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceLimitsApi.class);

    @Autowired
    private ServiceLimitRepository serviceLimitsRepository;

    @RequestMapping(
            path = "",
            produces = "application/json",
            method = RequestMethod.GET
    )
    public List<ServiceLimit> getServiceLimits() {

        return (List<ServiceLimit>) serviceLimitsRepository.findAll();
    }

    @Transactional
    @RequestMapping(
            path = "{project}/{service}",
            consumes = "application/json",
            method = RequestMethod.POST
    )
    public ResponseEntity<Void> createServiceLimit(
            @PathVariable(value = "project") String project,
            @PathVariable(value = "service") String service,
            @RequestBody ServiceLimitRequestBody requestBody
    ) {

        List<ServiceLimit> limits = findServiceLimits(project, service);
        if (limits.size() == 0) {
            serviceLimitsRepository.save(newServiceLimitFor(project, service, requestBody));
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            ServiceLimit limit = limits.get(0);
            limit.setMinPods(requestBody.minPods);
            limit.setMaxPods(requestBody.maxPods);
            serviceLimitsRepository.save(limit);
            return ResponseEntity.ok().build();
        }
    }

    @Transactional
    @RequestMapping(
            path = "{project}/{service}",
            method = RequestMethod.DELETE
    )
    public ResponseEntity<String> deleteServiceLimit(
            @PathVariable(value = "project") String project,
            @PathVariable(value = "service") String service
    ) {
        List<ServiceLimit> limits = serviceLimitsRepository.findByProjectAndService(project, service);
        if (limits.size() == 0) {
            return ResponseEntity.status(HttpStatus.GONE).body("Object not found!");
        }
        serviceLimitsRepository.delete(limits.get(0));
        return ResponseEntity.ok().body("Successfully deleted!");
    }

    private List<ServiceLimit> findServiceLimits(String project, String service) {
        List<ServiceLimit> configurations = serviceLimitsRepository.findByProjectAndService(project, service);
        if (configurations.size() > 1)
            LOG.info("Uuups, ServiceLimit exists more than once: {} | {} | {}", project, service);
        return configurations;
    }

    private ServiceLimit newServiceLimitFor(String project, String service, ServiceLimitRequestBody requestBody) {
        ServiceLimit limit = new ServiceLimit();
        limit.setProject(project);
        limit.setService(service);
        limit.setMinPods(requestBody.minPods);
        limit.setMaxPods(requestBody.maxPods);
        return limit;
    }
}
