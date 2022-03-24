package com.mrpowergamerbr.lastfmrichpresence.entities

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

// Sent by the server to be received by the client
@Serializable(ClientboundDiscordPayload.Serializer::class)
sealed class ClientboundDiscordPayload {
    object Serializer : JsonContentPolymorphicSerializer<ClientboundDiscordPayload>(ClientboundDiscordPayload::class) {
        override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out ClientboundDiscordPayload> {
            val cmd = element.jsonObject["cmd"]!!.jsonPrimitive.content

            return when (cmd) {
                "DISPATCH" -> DiscordDispatch.serializer()
                "SET_ACTIVITY" -> ClientboundDiscordSetActivity.serializer()
                else -> error("I don't know how to handle cmd \"$cmd\"!")
            }
        }
    }

    abstract val cmd: String
    abstract val nonce: String?
    abstract val evt: String?
}

@Serializable
data class DiscordDispatch(
    override val cmd: String,
    override val nonce: String?,
    override val evt: String?,
    val data: JsonObject,
) : ClientboundDiscordPayload()

@Serializable
data class ClientboundDiscordSetActivity(
    override val cmd: String,
    override val nonce: String,
    override val evt: String?,
    val data: JsonObject,
) : ClientboundDiscordPayload()