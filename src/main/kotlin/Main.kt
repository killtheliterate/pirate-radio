import kotlinx.html.*
import kotlinx.html.js.onEndedFunction
import me.theghostin.nimble.app
import me.theghostin.nimble.html
import me.theghostin.peerjs.*
import org.w3c.dom.HTMLAudioElement
import kotlin.js.Math
import kotlin.js.Promise

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
            is StationUpdate -> model.copy(station = it.station)
            is MeUpdate -> model.copy(me = it.me)
        } }

        peer(model.me.captain) { model.me.copy(id = id, crew = connections).apply {
            when (connection) {
                ConnectionState.streaming -> connection {
                    data { console.log(it) }
                    error { console.log(it) }
                }
                ConnectionState.listening -> connect(captain).apply {
                    data { console.log(it) }
                    error { console.log(it) }
                    open { send("hello") }
                }
            }
        }.let{ send(MeUpdate(it)) } }

        html {
            img(src = model.station.artwork) {}

            when (model.me.connection) {
                ConnectionState.streaming -> {
                    stream(model.songs)
                    h1 { +model.station.title }
                    p { unsafe { +model.station.summary } }
                    b { +"streaming" }
                }
                ConnectionState.offline -> b { +"offline" }
                ConnectionState.listening -> b { +"listening" }
           }
       }
    }
}


// TODO: we might wanna round robin a few different captain IDs probably just by enumerating a number at the end
fun peer(host: String, block: Peer.() -> Unit) = Promise<Peer> { resolve, reject ->
    Peer(id = host).apply {
        open { resolve(this) }
        error {
            when (it) {
                PeerError.unavailable_id -> Peer().apply {
                    open { resolve(this) }
                    error {
                        reject(Error(it.name))
                    }
                }
                else -> {
                    reject(Error(it.name))
                }
            }
        }
    }
}.then { it.apply(block) }.catch { throw it }

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


