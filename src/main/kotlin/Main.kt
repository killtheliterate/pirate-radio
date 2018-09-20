import kotlinx.html.*
import kotlinx.html.js.onEndedFunction
import me.theghostin.nimble.Nimble
import me.theghostin.nimble.app
import me.theghostin.nimble.html
import org.w3c.dom.HTMLAudioElement
import kotlin.browser.window
import kotlin.js.Math
import kotlin.js.Promise

data class Model(
        val station_url: String = "/pirate-radio/station",
        val station: Station = Station(),
        val connection: ConnectionState = ConnectionState.offline
)

enum class ConnectionState {
    // renders audio tag and streams it via RTC, effectively hosting
    streaming,
    // listening to a stream via RTC from one of the hosts
    listening,
    offline,
    error
}

data class Station(
        val title: String = "Pirate Radio",
        val summary: String = "radio for <i>pirates<i>",
        val artwork: String? =  "https://media.giphy.com/media/LUIvcbR6yytz2/giphy.gif",
        val songs: Map<String, Song> = mapOf()
)

data class Song(
        val title: String? = "untitled",
        val artist: String? = "anonymous",
        val album: String? = null,
        val year: Int? = null,
        val comment: String? = null,
        val artwork: String? = null
)

sealed class Msg
object NoOp : Msg()
data class SongsUpdate(val songs: Map<String, Song>) : Msg()
data class StationUpdate(val station: Station) : Msg()
data class ConnectionUpdate(val connection: ConnectionState) : Msg()

fun main(args: Array<String>) {
    app<Msg, Model>(Model()) {
        inbox { when (it) {
            NoOp -> model
            is SongsUpdate -> model.copy(
                station = model.station.copy(songs = it.songs)
            )
            is StationUpdate -> model.copy(
                station = model.station.copy(
                        title = it.station.title,
                        summary = it.station.summary,
                        artwork = it.station.artwork,
                        // TODO: make sure this merges and doesn't throw errors on same keys.
                        songs = model.station.songs.plus(it.station.songs)
                )
            )
            is ConnectionUpdate -> model.copy(connection = it.connection).also {
                send(pirate_radio(it))
            }
        } }

        send(pirate_radio())

        html {
            console.log("update", model)
            img(src = model.station.artwork) {}

            when (model.connection) {
                ConnectionState.streaming -> {
                    stream(model.station.songs)
                    h1 { +model.station.title }
                    p { unsafe { +model.station.summary } }
                }
                ConnectionState.offline -> b { "offline" }
                else -> b { +"unhandled connection state" }
            }
        }
    }
}

fun Nimble<Msg, Model>.pirate_radio(override: Model? = null): Promise<Msg> = (override ?: model).run {
    when (connection) {
        ConnectionState.streaming -> {
            /*
             * audio
             */
            window.fetch("$station_url/songs").then { it.text() }
                    .then { html ->
                        html.files("(mp3|wav)").map { filename ->
                            "$station_url/songs/$filename" to Song()
                        }.toMap()
                    }
                    .then { SongsUpdate(it) }
                    .catch { ConnectionUpdate(ConnectionState.error) }
        }
        ConnectionState.offline -> Promise.resolve(ConnectionUpdate(ConnectionState.streaming))
        else -> Promise.resolve(NoOp)
    }
}

fun DIV.stream(songs: Map<String, Song>) {
    if (songs.isEmpty()) return
    var i = (0 until songs.size).random
    audio {
        id = "pirate-stream"
        src = songs.keys.toList()[i]
        autoPlay = true
        onEndedFunction = {
            i = (i + 1).let { when (it <= songs.size - 1) {
                true -> it
                false -> (0 until songs.size).random
            } }
            (it.target as HTMLAudioElement).src = songs.keys.toList()[i]
        }
    }
}

private val IntRange.random: Int get() =
    (Math.random() * (endInclusive - first) + first).toInt()

// the endpoint returns html linking to the files, we're gonna scrape the file names from that.
// find anything that starts with `>` and ends with the extension. (that's the link text)
fun String.files(extension: String) = Regex(">(.+).$extension").findAll(this)
        // remove the `>`
        .map { it.value.substring(1) }


