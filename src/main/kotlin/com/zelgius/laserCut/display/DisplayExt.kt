package com.zelgius.laserCut.display

import java.awt.Color
import java.awt.Font
import kotlin.math.roundToInt


fun Display.error(text: String) {
    graphics.color = Color.BLACK
    graphics.fillRect(0, 0, width - 1, 31)
    displayImage()

    graphics.color = Color.WHITE
    graphics.font = Font("Monospaced", Font.PLAIN, 24)
    graphics.drawString(text, 0, 16)
    displayImage()
}


fun Display.text(text: String) {
    graphics.color = Color.BLACK
    graphics.fillRect(0, 0, width - 1, 23)
    displayImage()

    graphics.color = Color.WHITE
    graphics.font = Font("Monospaced", Font.PLAIN, 14)
    graphics.drawString(text, 0, 16)
    displayImage()
}

val Display.progressSize
    get() = width - 2

fun Display.progress(progress: Int) {
    if (progress > 100 || progress < 0) {
        clearProgress()
    } else {
        graphics.color = Color.WHITE
        graphics.drawRect(0, 24, width - 1, 7)
        graphics.fillRect(2, 26, (progress / 100f * progressSize).roundToInt(), 4)
        displayImage()
    }
}

private fun Display.clearProgress() {
    graphics.color = Color.WHITE
    graphics.drawRect(0, 24, width - 1, 7)
    graphics.color = Color.BLACK
    graphics.fillRect(2, 26, progressSize -1, 4)
    displayImage()
}

fun Display.indeterminate(progress: Int) {
    if (progress > 100 || progress < 0) {
        clearProgress()
    } else {
        graphics.color = Color.WHITE
        graphics.drawRect(0, 24, width - 1, 7)
        graphics.fillRect(2, 26, (progress / 100f * progressSize).roundToInt(), 2)
        graphics.fillRect(((100 - progress) / 100f * progressSize).roundToInt(), 28, (progress / 100f * progressSize).roundToInt(), 2)

        graphics.color = Color.BLACK
        graphics.fillRect(((100 - progress) / 100f * progressSize).roundToInt(), 26, (progress / 100f * progressSize).roundToInt(), 2)
        graphics.fillRect(2, 28, (progress / 100f * progressSize).roundToInt(), 2)
        displayImage()
    }
}

fun Display.work(count: Int, clearText: Boolean) {
    if(clearText) {
        graphics.color = Color.BLACK
        graphics.fillRect(0, 0, width - 1, 23)
        displayImage()
    }

    graphics.color = Color.WHITE
    graphics.font = Font("Monospaced", Font.PLAIN, 14)
    graphics.drawString("Working: ", 0, 16)
    displayImage()

    graphics.color = Color.BLACK
    graphics.fillRect(64, 0, 63, 23)
    displayImage()


    graphics.color = Color.WHITE
    graphics.font = Font("Monospaced", Font.PLAIN, 14)
    graphics.drawString("$count", 70, 16)
    displayImage()

}