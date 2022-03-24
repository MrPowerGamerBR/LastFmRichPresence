package com.mrpowergamerbr.lastfmrichpresence.entities

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.nio.ByteBuffer
import java.util.*

data class DiscordPacketWrapper(
    val op: Int,
    val data: JsonObject
) {
    companion object {
        fun fromBytes(bytes: ByteArray): DiscordPacketWrapper {
            val byteBuf = ByteBuffer.wrap(bytes)

            val op = Integer.reverseBytes(byteBuf.int)
            val size = Integer.reverseBytes(byteBuf.int)
            val payloadAsByteArray = ByteArray(size)
            byteBuf.get(payloadAsByteArray)
            val payloadAsString = payloadAsByteArray.toString(Charsets.UTF_8)

            println(payloadAsString)

            return DiscordPacketWrapper(
                op,
                Json.parseToJsonElement(payloadAsString).jsonObject
            )
        }

        inline fun <reified T> ofPayload(op: Int, data: T) = DiscordPacketWrapper(
            op,
            Json.encodeToJsonElement(
                data
            ).jsonObject
        )
    }

    // https://github.com/jagrosh/DiscordIPC/blob/master/src/main/java/com/jagrosh/discordipc/entities/Packet.java
    fun toBytes(): ByteArray {
        val d: ByteArray = Json.encodeToString(data).toByteArray(Charsets.UTF_8)
        val packet: ByteBuffer = ByteBuffer.allocate(d.size + 2 * Integer.BYTES)
        packet.putInt(Integer.reverseBytes(op))
        packet.putInt(Integer.reverseBytes(d.size))
        packet.put(d)
        return packet.array()
    }
}