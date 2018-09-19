import kotlinx.html.*
import me.theghostin.nimble.app
import me.theghostin.nimble.html
import kotlin.browser.window

data class Model(
        val station_url: String = "/pirate-radio",
        val station: Station = Station()
)

data class Station(
        val songs_url: String = "/songs",
        val title: String = "Pirate Radio",
        val summary: String = "radio for <b>pirates</b>",
        val artwork: String? = null
)


data class Song(
        val file: String,
        val title: String = "untitled",
        val artist: String = "anonymous",
        val album: String? = null,
        val year: Int? = null,
        val comment: String? = null,
        val artwork: String? = null
)

sealed class Msg

fun main(args: Array<String>) {
    app<Msg, Model>(Model("/pirate-radio/station/")) {
        // fetch and display station data
        // window.fetch("${model.station}/meta.json").then { it.text() }
        //         .then { json -> JSON.parse<Station>(json).apply {
        //             // TODO: send(station)

        //             // fetch and play song + display song data
        //             window.fetch("$songs/playlist.json").then {it.text() }
        //                     .then { json -> JSON.parse<Station>(json).apply {
        //                         // TODO: send(playlist)
        //                     } }
        //        } }
        html {
            img(src = "${model.station_url}/artwork.gif") {}
            audio {
                src = "${model.station_url}/songs/audio/mansion.wav"
                autoPlay = true
            }
            h1 { +model.station.title }
            p { unsafe { +model.station.summary } }
        }
    }
}