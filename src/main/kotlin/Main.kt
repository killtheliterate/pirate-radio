import kotlinx.html.*
import kotlinx.html.js.onEndedFunction
import me.theghostin.nimble.app
import me.theghostin.nimble.html
import org.w3c.dom.HTMLAudioElement
import kotlin.js.Math

data class Model(
        val station: Station = Station(),
        val songs: Map<String, Song> = mapOf(),
        val me: Pirate = Pirate()
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


