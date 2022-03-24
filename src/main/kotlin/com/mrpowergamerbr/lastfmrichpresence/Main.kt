
import com.mrpowergamerbr.lastfmrichpresence.DiscordIPCClient
import com.mrpowergamerbr.lastfmrichpresence.entities.ClientboundDiscordPayload
import com.mrpowergamerbr.lastfmrichpresence.entities.DiscordDispatch
import com.mrpowergamerbr.lastfmrichpresence.entities.DiscordHandshake
import com.mrpowergamerbr.lastfmrichpresence.entities.DiscordPacketWrapper
import com.mrpowergamerbr.lastfmrichpresence.entities.ServerboundDiscordSetActivity
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import org.jsoup.Jsoup
import java.io.RandomAccessFile
import java.util.*
import kotlin.concurrent.thread

fun main() {
    println("Hello World!")

    var ready = false
    var activitySet = false

    val client = DiscordIPCClient {
        if (it is DiscordDispatch) {
            ready = true
        }
    }

    thread {
        while (true) {
            if (ready) {
                val document = Jsoup.connect("https://www.last.fm/pt/user/MrPowerGamerBR")
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

                    client.sendPacket(
                        DiscordPacketWrapper.ofPayload(
                            1,
                            ServerboundDiscordSetActivity(
                                "SET_ACTIVITY",
                                UUID.randomUUID().toString(),
                                ServerboundDiscordSetActivity.SetActivityArgs(
                                    ProcessHandle.current().pid(),
                                    buildJsonObject {
                                        put("state", artist)
                                        put("details", "\uD83C\uDFA7 $name")
                                        putJsonObject("assets") {
                                            put("large_image", coverArt)
                                            put("large_text", "last.fm: MrPowerGamerBR")
                                        }
                                    }
                                )
                            )
                        )
                    )
                    activitySet = true
                } else if (activitySet) {
                    client.sendPacket(
                        DiscordPacketWrapper.ofPayload(
                            1,
                            ServerboundDiscordSetActivity(
                                "SET_ACTIVITY",
                                UUID.randomUUID().toString(),
                                ServerboundDiscordSetActivity.SetActivityArgs(
                                    ProcessHandle.current().pid(),
                                    buildJsonObject {}
                                )
                            )
                        )
                    )
                    activitySet = false
                    // Not scrobbling, remove activity if present
                }
            }
            Thread.sleep(5_000)
        }
    }

    client.start()
}