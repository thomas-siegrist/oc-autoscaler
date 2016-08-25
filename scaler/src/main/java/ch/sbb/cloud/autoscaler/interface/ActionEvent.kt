package ch.sbb.cloud.autoscaler.`interface`

/**
 * Created by thomas on 20.08.16.
 */
data class ActionEvent(
        val service: String,
        val action: Scale
)

