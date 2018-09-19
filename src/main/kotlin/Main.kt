import kotlinx.html.*
import kotlinx.html.js.onEndedFunction
import me.theghostin.nimble.app
import me.theghostin.nimble.html
import org.w3c.dom.HTMLAudioElement

data class Model(
        val station_url: String = "/pirate-radio/station",
        val station: Station = Station()
)

data class Station(
        val songs_url: String = "/songs",
        val title: String = "Pirate Radio",
        val summary: String = "radio for <b>pirates</b>",
        val artwork: String? = "artwork.gif"
)

// data class Song(
//         val file: String,
//         val title: String = "untitled",
//         val artist: String = "anonymous",
//         val album: String? = null,
//         val year: Int? = null,
//         val comment: String? = null,
//         val artwork: String? = null
// )

sealed class Msg

fun main(args: Array<String>) {
    app<Msg, Model>(Model()) {
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
            img(src = "${model.station_url}${model.station.artwork}") {}
            radio(listOf(
                    "mansion.wav",
                    "somewhere.wav"
            ).map {
                "${model.station_url}${model.station.songs_url}/audio/$it"
            })
            h1 { +model.station.title }
            p { unsafe { +model.station.summary } }
        }
    }
}

fun DIV.radio(songs: List<String>) {
    var i = 0
    audio {
        src = songs[i]
        autoPlay = true
        onEndedFunction = {
            i = (i + 1).let { when (it <= songs.size - 1) {
                true -> it
                false -> 0
            } }
            (it.target as HTMLAudioElement).src = songs[i]
        }
    }
}
