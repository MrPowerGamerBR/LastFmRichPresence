package com.mrpowergamerbr.lastfmrichpresence.entities

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

// Sent by the client to be received by the server
@Serializable(ServerboundDiscordPayload.Serializer::class)
sealed class ServerboundDiscordPayload {
    object Serializer : JsonContentPolymorphicSerializer<ServerboundDiscordPayload>(ServerboundDiscordPayload::class) {
        override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out ServerboundDiscordPayload> {
            val cmd = element.jsonObject["cmd"]!!.jsonPrimitive.content

            return when (cmd) {
                "DISPATCH" -> ServerboundDiscordSetActivity.serializer()
                else -> error("I don't know how to handle cmd \"$cmd\"!")
            }
        }
    }

    abstract val cmd: String
    abstract val nonce: String?
}

@Serializable
data class DiscordHandshake(
    val v: String,
    @SerialName("client_id")
    val clientId: String
)

@Serializable
data class ServerboundDiscordSetActivity(
    override val cmd: String,
    override val nonce: String,
    val args: SetActivityArgs,
) : ServerboundDiscordPayload() {
    @Serializable
    data class SetActivityArgs(
        val pid: Long,
        val activity: JsonObject
    )
}