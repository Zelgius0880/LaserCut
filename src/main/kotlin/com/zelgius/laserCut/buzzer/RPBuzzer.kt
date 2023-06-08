package com.zelgius.laserCut.buzzer

import com.pi4j.context.Context
import com.pi4j.io.pwm.Pwm
import com.pi4j.io.pwm.PwmType
import kotlinx.coroutines.delay

class RPBuzzer(pi4j: Context, address: Int): Buzzer {
    val pwm =  pi4j.create(
        Pwm.newConfigBuilder(pi4j)
        .id("BCM$address")
        .name("Buzzer")
        .address(address)
        .pwmType(PwmType.HARDWARE)
        .provider("pigpio-pwm")
        .initial(0)
        .shutdown(0)
        .build())

    override fun buzz(note: Note, divider: Float) {
        if(note == Note.NONE) {
            pwm.off()
            pwm.frequency = 0
            pwm.setDutyCycle(0)
        }
        else pwm.on(30, note.frequency)
    }

    override suspend fun buzz(note: Note, duration: Int, divider: Float) {
        buzz(note, divider)
        delay(duration.toLong())
    }
}