package com.mrpowergamerbr.lastfmrichpresence

import com.mrpowergamerbr.lastfmrichpresence.entities.ClientboundDiscordPayload
import com.mrpowergamerbr.lastfmrichpresence.entities.DiscordHandshake
import com.mrpowergamerbr.lastfmrichpresence.entities.DiscordPacketWrapper
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import java.io.RandomAccessFile

class DiscordIPCClient(
    val onPacketReceived: (ClientboundDiscordPayload) -> (Unit),
) {
    lateinit var pipe: RandomAccessFile

    fun start() {
        pipe = (0..10).firstNotNullOf {
            try {
                RandomAccessFile("\\\\?\\pipe\\discord-ipc-$it", "rw")
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        println("Writing to pipe...")
        // write to pipe
        sendPacket(
            DiscordPacketWrapper(
                0,
                Json.encodeToJsonElement(
                    DiscordHandshake(
                        "1",
                        "384465696496549888"
                    )
                ).jsonObject
            )
        )

        println("Written to pipe!")

        while (true) {
            val length = pipe.length()
            if (length == 0L) {
                // No data has been received yet, sleep and try again later
                Thread.sleep(50)
                continue
            }

            val dataAsByteArray = ByteArray(pipe.length().toInt())
            pipe.readFully(dataAsByteArray)

            val packet = DiscordPacketWrapper.fromBytes(dataAsByteArray)

            when (packet.op) {
                1 -> {
                    try {
                        val receivedPacket = Json.decodeFromJsonElement<ClientboundDiscordPayload>(packet.data)

                        onPacketReceived.invoke(receivedPacket)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            println(packet)
        }
    }

    fun sendPacket(packet: DiscordPacketWrapper) {
        pipe.write(packet.toBytes())
    }

    fun close() {
        // TODO: Close
    }
}