package ie.polymorphsoft.bitwig.zoom

import com.bitwig.extension.api.util.midi.ShortMidiMessage

/*
These structures and functions serve to convert Bitwig Midi events into InputEvents that are
specific to the Zoom R12/24 family. All possible events are converted even if not (yet) used
subsequently to control Bitwig.
 */
enum class Inputs {
    REW, FF, STOP, PLAY, REC,
    F1, F2, F3, F4, F5,
    JogWheel, UP, DOWN, LEFT, RIGHT,
    FADER1, FADER2, FADER3, FADER4, FADER5, FADER6, FADER7, FADER8, MASTER_FADER,
    PMR1, PMR2, PMR3, PMR4, PMR5, PMR6, PMR7, PMR8,
    BANK_DOWN, BANK_UP
}

enum class InputActions {
    ON, OFF //For JogWheel, ON = Clockwise, OFF = Anticlockwise
}

data class InputEvent(val input:Inputs, val action: InputActions, val value: Int = 0)

private data class BV(val data1: Int, val data2: Int)

fun ShortMidiMessage.inputEvent(): InputEvent? {
    return when (statusByte){
        144 ->
            when (BV(data1, data2)){

                // Play/Mute/Record
                BV(0, 127), BV(8,127), BV(16,127) -> InputEvent(Inputs.PMR1, InputActions.ON)
                BV(0, 0), BV(8,0), BV(16,0) -> InputEvent(Inputs.PMR1, InputActions.OFF)
                BV(1, 127), BV(9,127), BV(17,127) -> InputEvent(Inputs.PMR2, InputActions.ON)
                BV(1, 0), BV(9,0), BV(17,0) -> InputEvent(Inputs.PMR2, InputActions.OFF)
                BV(2, 127), BV(10,127), BV(18,127) -> InputEvent(Inputs.PMR3, InputActions.ON)
                BV(2, 0), BV(10,0), BV(18,0) -> InputEvent(Inputs.PMR3, InputActions.OFF)
                BV(3, 127), BV(11,127), BV(19,127) -> InputEvent(Inputs.PMR4, InputActions.ON)
                BV(3, 0), BV(11,0), BV(19,0) -> InputEvent(Inputs.PMR4, InputActions.OFF)
                BV(4, 127), BV(12,127), BV(20,127) -> InputEvent(Inputs.PMR5, InputActions.ON)
                BV(4, 0), BV(12,0), BV(20,0) -> InputEvent(Inputs.PMR5, InputActions.OFF)
                BV(5, 127), BV(13,127), BV(21,127) -> InputEvent(Inputs.PMR6, InputActions.ON)
                BV(5, 0), BV(13,0), BV(21,0) -> InputEvent(Inputs.PMR6, InputActions.OFF)
                BV(6, 127), BV(14,127), BV(22,127) -> InputEvent(Inputs.PMR7, InputActions.ON)
                BV(6, 0), BV(14,0), BV(22,0) -> InputEvent(Inputs.PMR7, InputActions.OFF)
                BV(7, 127), BV(15,127), BV(23,127) -> InputEvent(Inputs.PMR8, InputActions.ON)
                BV(7, 0), BV(15,0), BV(23,0) -> InputEvent(Inputs.PMR8, InputActions.OFF)

                //Bank
                BV(46, 127) -> InputEvent(Inputs.BANK_DOWN, InputActions.ON)
                BV(46, 0) -> InputEvent(Inputs.BANK_DOWN, InputActions.OFF)
                BV(47, 127) -> InputEvent(Inputs.BANK_UP, InputActions.ON)
                BV(47, 0) -> InputEvent(Inputs.BANK_UP, InputActions.OFF)

                //F1 - F5
                BV(54,0) -> InputEvent(Inputs.F1, InputActions.OFF)
                BV(55, 127) -> InputEvent(Inputs.F2, InputActions.ON)
                BV(55, 0) -> InputEvent(Inputs.F2, InputActions.OFF)
                BV(56, 127) -> InputEvent(Inputs.F3, InputActions.ON)
                BV(56, 0) -> InputEvent(Inputs.F3, InputActions.OFF)
                BV(57, 127) -> InputEvent(Inputs.F4, InputActions.ON)
                BV(57, 0) -> InputEvent(Inputs.F4, InputActions.OFF)
                BV(58, 127) -> InputEvent(Inputs.F5, InputActions.ON)
                BV(58, 0) -> InputEvent(Inputs.F5, InputActions.OFF)

                // Transport
                BV(91, 127) -> InputEvent(Inputs.REW, InputActions.ON)
                BV(91, 0) -> InputEvent(Inputs.REW, InputActions.OFF)
                BV(92, 127) -> InputEvent(Inputs.FF, InputActions.ON)
                BV(92, 0) -> InputEvent(Inputs.FF, InputActions.OFF)
                BV(93, 127) -> InputEvent(Inputs.STOP, InputActions.ON)
                BV(93, 0) -> InputEvent(Inputs.STOP, InputActions.OFF)
                BV(94, 127) -> InputEvent(Inputs.PLAY, InputActions.ON)
                BV(94, 0) -> InputEvent(Inputs.PLAY, InputActions.OFF)
                BV(95, 127) -> InputEvent(Inputs.REC, InputActions.ON)
                BV(95, 0) -> InputEvent(Inputs.REC, InputActions.OFF)

                // Navigation
                BV(96, 127) -> InputEvent(Inputs.UP, InputActions.ON)
                BV(96, 0) -> InputEvent(Inputs.UP, InputActions.OFF)
                BV(97, 127) -> InputEvent(Inputs.DOWN, InputActions.ON)
                BV(97, 0) -> InputEvent(Inputs.DOWN, InputActions.OFF)
                BV(98, 127) -> InputEvent(Inputs.LEFT, InputActions.ON)
                BV(98, 0) -> InputEvent(Inputs.LEFT, InputActions.OFF)
                BV(99, 127) -> InputEvent(Inputs.RIGHT, InputActions.ON)
                BV(99, 0) -> InputEvent(Inputs.RIGHT, InputActions.OFF)

                else -> null
            }
        176 -> when (BV(data1, data2)) {
            //Jogwheel
            BV(60,1) -> InputEvent(Inputs.JogWheel, InputActions.ON) //Clockwise
            BV(60,65) -> InputEvent(Inputs.JogWheel, InputActions.OFF) //Anticlockwise
            else -> null
        }
        //Faders
        224 -> InputEvent(Inputs.FADER1, InputActions.ON, data2)
        225 -> InputEvent(Inputs.FADER2, InputActions.ON, data2)
        226 -> InputEvent(Inputs.FADER3, InputActions.ON, data2)
        227 -> InputEvent(Inputs.FADER4, InputActions.ON, data2)
        228 -> InputEvent(Inputs.FADER5, InputActions.ON, data2)
        229 -> InputEvent(Inputs.FADER6, InputActions.ON, data2)
        230 -> InputEvent(Inputs.FADER7, InputActions.ON, data2)
        231 -> InputEvent(Inputs.FADER8, InputActions.ON, data2)
        232 -> InputEvent(Inputs.MASTER_FADER, InputActions.ON, data2)
        else ->
            null
    }
}