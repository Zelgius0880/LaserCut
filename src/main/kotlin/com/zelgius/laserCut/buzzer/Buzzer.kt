package com.zelgius.laserCut.buzzer


import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


interface Buzzer {
    /**
     * start the buzzer at a specific frequency
     *
     * @param frequency
     */
    fun buzz(note: Note, divider: Float)

    /**
     * start the buzzer at a specific frequency for a specified duration in milliseconds
     *
     * @param frequency
     * @param duration number of milliseconds
     */
    suspend fun buzz(note: Note, duration: Int, divider: Float)

    suspend fun playMelody(melody: Array<Note>, tempo: IntArray, divider: Float = 1f) {
        melody.forEachIndexed { index, note ->
            // to calculate the note duration, take one second
            // divided by the note type.
            //e.g. quarter note = 1000 / 4, eighth note = 1000/8, etc.
            // to calculate the note duration, take one second
            // divided by the note type.
            //e.g. quarter note = 1000 / 4, eighth note = 1000/8, etc.
            val noteDuration = tempo[index].let {
                if(it > 0) 1000 / tempo[index]
                else {
                    (1000 / tempo[index].absoluteValue * 1.5).toInt()
                }
            }

            buzz(note, noteDuration, divider)

            // to distinguish the notes, set a minimum time between them.
            // the note's duration + 30% seems to work well:

            // to distinguish the notes, set a minimum time between them.
            // the note's duration + 30% seems to work well:
            val pauseBetweenNotes = (noteDuration * 1).toInt()
            delay(pauseBetweenNotes.toLong())

            // stop the tone playing:

            // stop the tone playing:
            buzz(Note.NONE, (noteDuration * 0.5).roundToInt(), divider)
        }

        stop()
    }

    /**
     * stop the buzzer
     */
    fun stop() {
        buzz(Note.NONE, 1f)
    }

    companion object {
        const val STOP_FREQUENCY = 0
    }
}