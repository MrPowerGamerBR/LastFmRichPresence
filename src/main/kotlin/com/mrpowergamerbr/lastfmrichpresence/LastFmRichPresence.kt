package com.mrpowergamerbr.lastfmrichpresence

import com.jagrosh.discordipc.IPCClient
import com.jagrosh.discordipc.entities.RichPresence
import com.jagrosh.discordipc.entities.pipe.PipeStatus
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import mu.KotlinLogging
import org.jsoup.Jsoup
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.concurrent.thread

object LastFmRichPresence {
    private val logger = KotlinLogging.logger {}
    private var client: IPCClient? = null

    @JvmStatic
    fun main(args: Array<String>) {
        val parser = ArgParser("LastFmRichPresence")
        val lastFmUsername by parser.option(ArgType.String, description = "last.fm username").required()
        val clientId by parser.option(ArgType.String, description = "Discord's rich presence ID")
            .default("384465696496549888")

        parser.parse(args)

        thread {
            while (true) {
                try {
                    logger.info { "Fetching $lastFmUsername's last.fm..." }
                    val document = Jsoup.connect("https://www.last.fm/user/$lastFmUsername/partial/recenttracks?ajax=1")
                        .get()

                    val nowScrobbling = document.getElementsByClass("chartlist-row--now-scrobbling")
                        .firstOrNull()

                    if (nowScrobbling != null) {
                        // Set activity
                        val name = nowScrobbling.getElementsByClass("chartlist-name").text()
                        val artist = nowScrobbling.getElementsByClass("chartlist-artist").text()
                        val coverArt = nowScrobbling.getElementsByClass("cover-art")
                            .first()!!
                            .getElementsByTag("img")
                            .attr("src")
                            .replace("64s", "128s")
                        val dataTimestamp = nowScrobbling.attr("data-timestamp")
                        val lovedTheTrack = nowScrobbling.getElementsByClass("chartlist-loved")
                            .first()!!
                            .getElementsByTag("div")
                            .attr("data-toggle-button-current-state") == "loved"

                        val newSong = TrackedSong(
                            name,
                            artist
                        )

                        logger.info { "Currently we are scrobbling $newSong!" }

                        if (client?.status != PipeStatus.CONNECTED) {
                            logger.info { "We aren't connected to the Discord IPC pipe! Connecting... IPC status: ${client?.status}" }
                            val ipcClient = IPCClient(clientId.toLong())
                            ipcClient.connect()

                            // Wait until connected
                            while (ipcClient.status != PipeStatus.CONNECTED)
                                Thread.sleep(100)

                            this.client = ipcClient
                        }

                        logger.info { "Updating rich presence..." }
                        val builder = RichPresence.Builder()
                        builder.setState(artist)
                            .setDetails("\uD83C\uDFA7 $name")
                            .setState(artist)
                            .setLargeImage(coverArt, "last.fm: $lastFmUsername")
                            .apply {
                                if (lovedTheTrack)
                                    this.setSmallImage("https://media.discordapp.net/attachments/393332226881880074/956571320240459887/sparkling-heart_1f496_crop.png", "$lastFmUsername loves this track!")
                            }
                            .setStartTimestamp(
                                OffsetDateTime.ofInstant(
                                    Instant.ofEpochSecond(
                                        dataTimestamp.toLong()
                                    ),
                                    ZoneId.of("UTC")
                                )
                            )

                        (client ?: error("Client is null but it shouldn't be!"))
                            .sendRichPresence(builder.build())
                    } else {
                        logger.info { "We aren't scrobbling, so we will disconnect the IPC client..." }
                        client?.close()
                        client = null
                    }
                } catch (e: Exception) {
                    logger.warn(e) { "Something went wrong while trying to fetch and update the rich presence!" }
                }

                Thread.sleep(5_000)
            }
        }
    }

    data class TrackedSong(
        val name: String,
        val artist: String
    )
}