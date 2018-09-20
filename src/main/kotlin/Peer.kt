package me.theghostin.peerjs

// Web API
external interface MediaStream
external interface RTCDataChannel
external interface RTCPeerConnection

// Pirate.js
interface PeerOptions {
    val key: String?
    val host: String?
    val port: Number?
    val path: String?
    val secure: Boolean?
    // val config:
    val debug: Number?
}

interface ConnectionOptions {
    val label: String?
    val metadata: Any?
    val serialization: SerializationType?
    val reliable: Boolean?
}
enum class SerializationType {
    binary,
    binary_utf8, // TODO: as string convert _ -> -
    json,
    none
}
interface CallOptions {
    val metadata: Any?
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

external class Peer(id: String? = definedExternally, options: PeerOptions? = definedExternally) {
    //  We recommend keeping track of connections yourself rather than relying on this hash.
    val connections: Map<String, DataConnection>
    val disconnected: Boolean
    val destroyed: Boolean

    fun connect(id: String, options: ConnectionOptions? = definedExternally): DataConnection
    fun reconnect()
    fun destroy()
    fun call(id: String, stream: MediaStream, options: CallOptions? = definedExternally)
    fun on(event: PeerEvent, callback: () -> Unit)
}

enum class DataConnectionEvent {
    data,
    open,
    close,
    error
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

    fun send(data: JSON)
    fun close()
    fun on(event: DataConnectionEvent, callback: () -> Unit)
}

enum class MediaConnectionEvent {
    stream,
    close,
    error
}
external interface MediaConnection {
    val metadata: Any
    val peer: String
    val type: String // always "media"

    fun answer(stream: MediaStream)
    fun close()
    fun on(event: String, callback: () -> Unit)
}
