import kotlinx.html.*
import kotlinx.html.js.onEndedFunction
import me.theghostin.nimble.app
import me.theghostin.nimble.html
import org.w3c.dom.HTMLAudioElement
import kotlin.browser.window

data class Model(
        val station_url: String = "/pirate-radio/station/",
        val station: Station = Station(),
        val songs: List<String> = listOf()
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
data class Songs(val songs: List<String>?) : Msg()

fun main(args: Array<String>) {
    app<Msg, Model>(Model()) {
        window.fetch("${model.station_url}${model.station.songs_url}/audio/").then { it.text() }
                .then {
                    // the endpoint returns html linking to the files, we're gonna scrape the file names from that.
                    // find anything that starts with > and ends with .wav. (that's the link text)
                    // map that to a list and remove the >
                    send(Songs(Regex(">(.+).wav").findAll(it)
                            .map { it.value.substring(1) }.toList()))
                }

        inbox { when (it) {
            is Songs -> model.copy(songs = it.songs ?: listOf())
        } }

        html {
            img(src = "${model.station_url}${model.station.artwork}") {}
            radio(model.songs.map {
                "${model.station_url}${model.station.songs_url}/audio/$it"
            })
            h1 { +model.station.title }
            p { unsafe { +model.station.summary } }
        }
    }
}

fun DIV.radio(songs: List<String>) {
    if (songs.isEmpty()) return
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
