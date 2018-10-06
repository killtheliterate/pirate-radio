import me.theghostin.peerjs.DataConnection
import kotlin.browser.window
import kotlin.js.Promise

data class Pirate(
        val station_url: String = "/pirate-radio/station",
        val id: String? = null,
        val crew: Map<String, DataConnection?> = mapOf(),

        // this is kind of a hack on top of the PeerJS servers, trying not to build our own signaling server.
        // basically if we _can't_ connect as this id then we should connect normally and then `call` this
        // peer to bootstrap orchestration.
        var captain: String = "${station_url.replace("/", "")}-host1",
        // if the captain leaves a new captain is chosen and mates reorganize accordingly.
        // the new captain will need the song URL and current time in ms to reboot the stream.
        var cache: Pair<String, Int>? = null
) {
    val connection: ConnectionState get() = when {
        id.isNullOrEmpty() -> ConnectionState.offline
        captain == id -> ConnectionState.streaming
        captain != id -> ConnectionState.listening

        // I don't think this should be possible given the above cases
        else -> ConnectionState.error
    }
}

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

fun Pirate.radio(): Promise<Msg> =
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
                        .catch { MeUpdate(copy(id = null)) }
            }
            ConnectionState.offline -> Promise.resolve(MeUpdate(copy(id = null)))
            else -> Promise.resolve(NoOp)
        }

enum class ConnectionState {
    // renders audio tag and streams it via RTC, effectively hosting
    streaming,
    // listening to a stream via RTC from one of the hosts
    listening,
    offline,
    error
}

// the keybase pages endpoint returns html linking to the files, we're gonna scrape the file names from that.
// find anything that starts with `>` and ends with the extension. (that's the link text)
fun String.files(extension: String) = Regex(">(.+).$extension").findAll(this)
        // remove the `>`
        .map { it.value.substring(1) }
