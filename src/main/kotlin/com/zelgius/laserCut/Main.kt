package com.zelgius.laserCut

import com.pi4j.Pi4J
import com.pi4j.context.Context
import com.pi4j.io.gpio.digital.DigitalInput
import com.pi4j.io.gpio.digital.DigitalInputConfigBuilder
import com.pi4j.io.gpio.digital.DigitalState
import com.pi4j.io.gpio.digital.PullResistance
import com.pi4j.io.i2c.I2C
import com.pi4j.io.i2c.I2CConfig
import com.pi4j.io.i2c.I2CProvider
import com.zelgius.laserCut.buzzer.Note
import com.zelgius.laserCut.buzzer.RPBuzzer
import com.zelgius.laserCut.communication.Requester
import com.zelgius.laserCut.display.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import java.net.URI
import kotlin.math.roundToInt

val coin = arrayOf(Note.B5, Note.E6) to arrayOf(100, 200).map { (5000f / it).roundToInt() }.toIntArray()
val coroutineScope = CoroutineScope(Dispatchers.Default)
val displayFlow = MutableStateFlow<(() -> Unit)?>(null)

val pi4j: Context = Pi4J.newAutoContext()
val buttonConfig1: DigitalInputConfigBuilder = DigitalInput.newConfigBuilder(pi4j)
    .id("button 1")
    .name("Press button")
    .address(27)
    .pull(PullResistance.PULL_UP)
    .debounce(3000L)
    .provider("pigpio-digital-input")
val button1: DigitalInput = pi4j.create(buttonConfig1)

val buttonConfig2: DigitalInputConfigBuilder = DigitalInput.newConfigBuilder(pi4j)
    .id("button 2")
    .name("Press button")
    .address(22)
    .pull(PullResistance.PULL_UP)
    .debounce(3000L)
    .provider("pigpio-digital-input")
val button2: DigitalInput = pi4j.create(buttonConfig2)

val i2CProvider: I2CProvider = pi4j.provider("linuxfs-i2c")
val i2cConfig: I2CConfig = I2C.newConfigBuilder(pi4j).id("SSD1306").bus(1).device(0x3C).build()
val display = Display(128, 32, i2CProvider.create(i2cConfig))

fun main(args: Array<String>) {
    var requester: Requester? = null

    coroutineScope.launch {
        displayFlow
            .filterNotNull()
            .collectLatest {
                it()
            }
    }

    try {

        println("Starting")
        display.begin()
        display.rotate(Display.Rotation.SOUTH)

        display {
            display.text("Starting ...")
        }

        val buzzer = RPBuzzer(pi4j, 12)

        button1.addListener({
            if (it.state() == DigitalState.LOW) {
                coroutineScope.launch {
                    buzzer.playMelody(coin.first, coin.second)
                    if (requester?.run() == true) {
                        buzzer.playMelody(coin.first, coin.second)
                    }
                }
            }
        })

        button2.addListener({
            if (it.state() == DigitalState.LOW) {
                coroutineScope.launch {
                    if (requester?.laserTest() == true) {
                        buzzer.playMelody(coin.first, coin.second)
                    }
                }
            }
        })

        var connected = false
        coroutineScope.launch {
            delay(500L) // as it the first text to be display, we wait a bit to let the text displayed

            var i = 0
            while (!connected) {
                if (i % 2 == 0) {
                    display.indeterminateInQueue(i)
                }
                delay(10)
                ++i
                i %= 100
            }
        }


        requester = Requester(URI("ws://127.0.0.1:8000"),
            onProgress = { progress, count, clearText ->
                display {
                    if (count != null)
                        display.work(count, clearText)
                    else {
                        if(clearText)
                            display.text("Ready")
                    }

                    display.progress(progress)
                }
            }
        )
            .apply {
                start {
                    if (!connected) {
                        coroutineScope.launch {
                            buzzer.playMelody(coin.first, coin.second)
                            delay(100)
                            buzzer.playMelody(coin.first, coin.second)
                            delay(100)
                            buzzer.playMelody(coin.first, coin.second)

                            connected = true

                            println("Display ready")

                            displayInQueue {
                                display.text("Ready")
                                display.progress(-1)
                            }
                        }
                    }
                }
            }

        while (true) {
        }
    } catch (e: Exception) {
        e.printStackTrace()
        display {
            display.error("! ERROR !")
        }
        requester?.close()

    } finally {
        pi4j.shutdown()
    }
}

suspend fun Display.textInQueue(text: String) = displayFlow.emit {
    text(text)
}

suspend fun Display.progressInQueue(progress: Int) = displayFlow.emit {
    progress(progress)
}

suspend fun Display.indeterminateInQueue(progress: Int) = displayFlow.emit {
    indeterminate(progress)
}

suspend fun displayInQueue(thingsToDisplay: () -> Unit) =
    displayFlow.emit {
        thingsToDisplay()
    }


fun display(thingsToDisplay: () -> Unit) = coroutineScope.launch {
    displayFlow.emit {
        thingsToDisplay()
    }
}