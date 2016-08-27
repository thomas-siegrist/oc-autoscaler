package ch.sbb.cloud.autoscaler.service

import ch.sbb.cloud.autoscaler.`interface`.Scale
import ch.sbb.cloud.autoscaler.`interface`.oc.api.OcScale
import org.jetbrains.hub.oauth2.client.jersey.oauth2Client
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.util.*

/**
 * Created by thomas on 25.08.16.
 */
@Component
class ScalerService {

    val CLIENT_ID = "openshift-challenging-client"

    @Autowired
    lateinit var rest: RestTemplate

    fun scale(project: String, service: String, scale: Scale) {

        val authResponseEntity = rest.getForEntity(
                URI("https://localhost:8443/oauth/authorize?client_id=openshift-challenging-client&response_type=token"),
                String::class.java
        )
        val authToken = authResponseEntity.headers["access_token"]

        var uri = URI(
                "https://localhost:8443/api/v1/namespaces/{project}/replicationcontrollers/{service}"
                        .replace("{project}", project, true)
                        .replace("{service}", service, true)
        )

        var ocScale = calculateDesiredInstances(project, scale)
        val requestEntity = HttpEntity<OcScale>(ocScale)

        var result = rest.exchange(uri, HttpMethod.PATCH, requestEntity, String::class.java)
    }

    private fun calculateDesiredInstances(project: String, scale: Scale): OcScale {
        var desiredNumberOfInstances = currentNumerOfInstances(project)
        if (Scale.UP == scale)
            desiredNumberOfInstances++
        else
            desiredNumberOfInstances--
        var ocScale = OcScale(OcScale.OcSpec(desiredNumberOfInstances))
        return ocScale
    }

    private fun currentNumerOfInstances(project: String): Int {
        var uri = URI("https://localhost:8443/api/v1/namespaces/{project}/pods".replace("{project}", project, true))
        rest.getForObject(uri, String::class.java)
        return 0 // TODO
    }

}