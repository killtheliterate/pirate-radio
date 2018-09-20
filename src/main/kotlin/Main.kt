import kotlinx.html.*
import kotlinx.html.js.onEndedFunction
import me.theghostin.nimble.app
import me.theghostin.nimble.html
import org.w3c.dom.HTMLAudioElement
import kotlin.browser.window
import kotlin.js.Math
import kotlin.js.Promise

data class Model(
        val station: Station = Station(),
        val songs: Map<String, Song> = mapOf(),
        val me: Pirate = Pirate()
)

enum class ConnectionState {
    // renders audio tag and streams it via RTC, effectively hosting
    streaming,
    // listening to a stream via RTC from one of the hosts
    listening,
    offline,
    error
}

data class Pirate(
        val station_url: String = "/pirate-radio/station",
        // this is kind of a hack on top of the PeerJS servers, trying not to build our own signaling server.
        // basically if we _can't_ connect as this id then we should connect normally and then `call` this
        // peer to bootstrap orchestration.
        val id: String = "$station_url#host-1",
        val captain: Pirate? = null,
        val crew: List<Pirate> = listOf(),
        val connection: ConnectionState = ConnectionState.offline,

        // if the captain leaves
        // the new captain is chosen and mates reorganize accordingly.
        // The new captain will need this to start the stream
        // back to where it died, approximately
        val cache: Pair<String, Int>? = null
)

data class Station(
        val title: String = "Pirate Radio",
        val summary: String = "radio for <i>pirates<i>",
        val artwork: String? =  "https://media.giphy.com/media/LUIvcbR6yytz2/giphy.gif"
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
data class MeUpdate(val me: Pirate) : Msg()

fun main(args: Array<String>) {
    app<Msg, Model>(Model()) {
        inbox { when (it) {
            NoOp -> model
            is SongsUpdate -> model.copy(songs = it.songs)
            is StationUpdate -> model.copy(
                station = model.station.copy(
                        title = it.station.title,
                        summary = it.station.summary,
                        artwork = it.station.artwork
                )
            )
            is MeUpdate -> model.copy(me = it.me) // lol, it me
                    .also { send(it.me.radio()) }
        } }

        send(model.me.radio())

        html {
            console.log("update", model)
            img(src = model.station.artwork) {}

            when (model.me.connection) {
                ConnectionState.streaming -> {
                    stream(model.songs)
                    h1 { +model.station.title }
                    p { unsafe { +model.station.summary } }
                }
                ConnectionState.offline -> b { "offline" }
                else -> b { +"unhandled connection state" }
            }
        }
    }
}

fun Pirate.radio(override: Pirate? = null): Promise<Msg> = (override ?: this).run {
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
                    .then { SongsUpdate(it.toMap()) }
                    .catch { MeUpdate(copy(connection = ConnectionState.error)) }
        }
        ConnectionState.offline -> Promise.resolve(MeUpdate(copy(connection = ConnectionState.streaming)))
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


