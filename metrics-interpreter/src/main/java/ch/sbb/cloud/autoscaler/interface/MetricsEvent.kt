package ch.sbb.cloud.autoscaler.`interface`

/**
 * Created by thomas on 20.08.16.
 */
data class MetricsEvent(
        val metricName: String,
        val service: String,
        val value: Double
)