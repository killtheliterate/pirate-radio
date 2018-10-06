package me.theghostin.peerjs

// Web API
external interface MediaStream
external interface RTCDataChannel
external interface RTCPeerConnection

external class Peer(id: String? = definedExternally, options: PeerOptions? = definedExternally) {
    val id: String = definedExternally
    //  We recommend keeping track of connections yourself rather than relying on this hash.
    val connections: Map<String, DataConnection?>
    val disconnected: Boolean
    val destroyed: Boolean

    fun connect(id: String, options: ConnectionOptions? = definedExternally): DataConnection
    fun reconnect()
    fun destroy()
    fun call(id: String, stream: MediaStream, options: CallOptions? = definedExternally)
    fun on(event: PeerEvent, callback: (Any?) -> Unit)
}
fun Peer.open(callback: (String) -> Unit) = on(PeerEvent.open) { callback(it.unsafeCast<String>()) }
fun Peer.close(callback: () -> Unit) = on(PeerEvent.close) { callback() }
fun Peer.disconnected(callback: () -> Unit) = on(PeerEvent.disconnected) { callback() }
fun Peer.error(callback: (PeerError) -> Unit) = on(PeerEvent.error) {
    (it.asDynamic().type as String).replace("-", "_").let {
        callback(PeerError.valueOf(it))
    }
}
fun Peer.connection(callback: DataConnection.() -> Unit) = on(PeerEvent.connection) { callback(it.unsafeCast<DataConnection>()) }
fun Peer.call(callback: MediaConnection.() -> Unit) = on(PeerEvent.call) { callback(it.unsafeCast<MediaConnection>()) }

data class PeerOptions(
        val key: String? = null,
        val host: String? = null,
        val port: Number? = null,
        val path: String = "/",
        val secure: Boolean? = null,
        // val config:
        val debug: Number? = null
)

external interface MediaConnection {
    val metadata: Any
    val peer: String
    val type: String // always "media"

    fun answer(stream: MediaStream)
    fun close()
    fun on(event: MediaConnectionEvent, callback: () -> Unit)
}

external interface DataConnection {
    val dataChannel: RTCDataChannel
    val label: String
    val metadata: Any
    val open: Boolean
    val peerConnection: RTCPeerConnection
    val peer: String
    val reliable: Boolean
    val serialization: SerializationType
    val type: String // always "data"
    val bufferSize: Number

    fun send(data: Any)
    fun close()
    fun on(event: DataConnectionEvent, callback: (Any?) -> Unit)
}
fun DataConnection.data(callback: (Any) -> Unit) = on(DataConnectionEvent.data) { it?.let { callback(it) } }
fun DataConnection.open(callback: () -> Unit) = on(DataConnectionEvent.open) { callback() }
fun DataConnection.close(callback: () -> Unit) = on(DataConnectionEvent.close) { callback() }
fun DataConnection.error(callback: (Any) -> Unit) = on(DataConnectionEvent.error) { it?.let { callback(it) } }

interface ConnectionOptions {
    val label: String?
    val metadata: Any?
    val serialization: SerializationType?
    val reliable: Boolean?
}

interface CallOptions {
    val metadata: Any?
}

// TODO: as string convert _ -> -
enum class SerializationType {
    binary,
    binary_utf8,
    json,
    none
}
enum class PeerEvent {
    open,
    connection,
    call,
    close,
    disconnected,
    error
}
enum class PeerError {
    browser_incompatible,
    disconnected,
    invalid_id,
    invalid_key,
    network,
    peer_unavailable,
    ssl_unavailable,
    server_error,
    socket_error,
    socket_closed,
    unavailable_id,
    webrtc
}
enum class DataConnectionEvent {
    data,
    open,
    close,
    error
}
enum class MediaConnectionEvent {
    stream,
    close,
    error
}
