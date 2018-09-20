import kotlinx.html.*
import kotlinx.html.js.onEndedFunction
import me.theghostin.nimble.Nimble
import me.theghostin.nimble.app
import me.theghostin.nimble.html
import org.w3c.dom.HTMLAudioElement
import kotlin.browser.window
import kotlin.js.Math

data class Model(
        val station_url: String = "/pirate-radio/station",
        val station: Station = Station()
)

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
data class SongsUpdate(val songs: Map<String, Song>) : Msg()
data class StationUpdate(val station: Station) : Msg()

fun main(args: Array<String>) {
    app<Msg, Model>(Model()) {
        inbox { when (it) {
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
        } }

        station(model.station_url)

        html {
            console.log(model)
            img(src = model.station.artwork) {}
            radio(model.station.songs)
            h1 { +model.station.title }
            p { unsafe { +model.station.summary } }

        }
    }
}

fun Nimble<Msg, Model>.station(url: String) {
    /*
     * audio
     */
    window.fetch("$url/songs").then { it.text() }
            .then { html ->
                html.files("(mp3|wav)").map { filename ->
                    "$url/songs/$filename" to Song()
                }.toMap()
            }
            .then { send(SongsUpdate(it)) }
            .catch { e -> console.log(e) }

    /*
     * meta data
     */
    // window.fetch("$url/pirate-radio.json").then { it.text() }
    //         // TODO: I think switching to kotlinx will make this work but idk
    //         .then { JSON.parse<Station>(it) }
    //         .then { send(StationUpdate(it)) }
    //         .catch { e -> console.log(e) }
}

fun DIV.radio(songs: Map<String, Song>) {
    if (songs.isEmpty()) return
    var i = (0 until songs.size).random
    audio {
        id = "pirate-radio"
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


