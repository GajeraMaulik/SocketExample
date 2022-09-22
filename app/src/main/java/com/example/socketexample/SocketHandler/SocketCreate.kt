package com.example.socketexample.SocketHandler

import android.app.Application
import android.util.Log.d
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.WebSocket
import org.jetbrains.annotations.Nullable
import java.net.URISyntaxException
import java.util.*

object SocketCreate{

     var mSocket: Socket

init {
    mSocket = IO.socket("http://localhost:3000/")
}

    @Throws(URISyntaxException::class)
    @Synchronized
    fun setSocket():Socket{
        val options = IO.Options()
        options.transports = arrayOf(WebSocket.NAME)
    //   options.path = "/wc"
        //    options.extraHeaders = Collections.singletonMap("Authorization", listOf(token))
        options.reconnectionAttempts = 10
        try {

            mSocket = IO.socket("https://nms.blaucomm.co.uk:8761/")

          //mSocket = IO.socket("http://192.168.1.106:3000/",options)
            d("error"," cre${mSocket}")

        }catch (e:URISyntaxException){
            d("error","err$e")
        }
        return mSocket;
    }


    @Synchronized
    fun getSocket(): Socket {
        return mSocket
    }

    @Synchronized
    fun establishConnection() {
        mSocket.connect()
    }

    @Synchronized
    fun closeConnection() {
        mSocket.disconnect()
    }
    fun removeEvent(event: String) {
        mSocket.off(event)
    }


    fun connect(listener: Emitter.Listener) {
        on(Socket.EVENT_CONNECT, listener)
        mSocket.connect()
    }


    fun on(event: String, listener: Emitter.Listener) {
        mSocket.on(event, listener)
    }

    fun emit(event: String, @Nullable obj: Any, @Nullable ack: Ack?) {
        mSocket.emit(event, obj, ack)
    }



}