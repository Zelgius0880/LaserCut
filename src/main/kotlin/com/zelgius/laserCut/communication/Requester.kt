package com.zelgius.laserCut.communication

import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.File
import java.net.URI
import kotlin.math.roundToInt


open class Requester(
    private val serverUri: URI,
    private val onProgress: (progress: Int, count: Int?, clearText: Boolean) -> Unit
) {

    companion object {
        //const val PORT = "/dev/ttyACM0" // -> With Arduino UNO test chip
        const val PORT = "/dev/ttyUSB0" // -> With real machine
    }

    private var socket = IO.socket(serverUri)
    private var workProgress: Int? = null
    private var current: Int = 0

    private var connected: Boolean = false
    private var portConnected: Boolean = false

    open fun start(onConnected: () -> Unit) {
        socket.on(Socket.EVENT_CONNECT) {
            onConnected()
            connected = true
        }
        socket.on(Socket.EVENT_DISCONNECT) {
            println("disconnected")
            socket.close()
            connected = false

            Thread.sleep(2000)
            socket = IO.socket(serverUri)
            start(onConnected)
        }
        socket.on(Socket.EVENT_CONNECT_ERROR) {
            println("ERROR: ${it.joinToString()}")
            socket.close()
            connected = false
            portConnected = false

            Thread.sleep(2000)
            socket = IO.socket(serverUri)
            start(onConnected)
        }

        socket.on("connectStatus") {
            val s = it.joinToString()
            println(s)

            if (s.contains("closed")) portConnected = false
            if (s.contains("opened")) portConnected = true

        }

        socket.on("qCount") {
            val current = it.first() as Int

            val total = workProgress

            if (current == 0) {
                this.current = 0
                onProgress(-1, null, workProgress != null)
                workProgress = null
            } else if (this.current != current) {
                this.current = current
                if (total == null) {
                    workProgress = current
                    onProgress(0, current, true)
                } else {
                    onProgress((((total - current) / total.toFloat()) * 100).roundToInt(), current, false)
                }
            }
        }

        socket.connect()
    }

    fun close() {
        socket.close()
    }

    suspend fun laserTest(): Boolean {
        return emit("stop", "")
                && emit("clearAlarm", "2")
                && emit("clearAlarm", "2")
                && emit("laserTest", "0.5, 15000, 1000")
    }

    suspend fun run(): Boolean {
        val bufferedReader: BufferedReader = File("/home/pi/gcode.gcode").bufferedReader()
        val inputString = bufferedReader.use { it.readText() }
        val clearAlert = emit("clearAlarm", "2")
                && emit("clearAlarm", "2")
        val setZero = emit("setZero", "all")
        val stop = emit("stop", "")
        return setZero
                && stop
                && clearAlert
                && emit("runJob", inputString)
    }

    private suspend fun emit(event: String, vararg args: Any): Boolean =
        if (connected) {
            if (!portConnected) {
                socket.emit("connectTo", "USB,$PORT,115200")
                while (!portConnected) {
                    yield()
                }
            }

            socket.emit(event, *args)
            delay(100L)
            true
        } else false


}