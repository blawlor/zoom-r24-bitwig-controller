package ie.polymorphsoft.bitwig.midi

import com.bitwig.extension.api.util.midi.ShortMidiMessage
import ie.polymorphsoft.bitwig.ControllerInputActions
import ie.polymorphsoft.bitwig.ControllerInputEvent
import ie.polymorphsoft.bitwig.ControllerInputs

/*
These structures and functions serve to convert Bitwig Midi events into InputEvents that are
specific to the Zoom R12/24 family. All possible events are converted even if not (yet) used
subsequently to control Bitwig.
 */

private data class BV(val data1: Int, val data2: Int)

fun ShortMidiMessage.inputEvent(): ControllerInputEvent? {
    return when (statusByte){
        144 ->
            when (BV(data1, data2)){

                // Play/Mute/Record
                BV(0, 127), BV(8,127), BV(16,127) -> ControllerInputEvent(ControllerInputs.PMR1, ControllerInputActions.ON)
                BV(0, 0), BV(8,0), BV(16,0) -> ControllerInputEvent(ControllerInputs.PMR1, ControllerInputActions.OFF)
                BV(1, 127), BV(9,127), BV(17,127) -> ControllerInputEvent(ControllerInputs.PMR2, ControllerInputActions.ON)
                BV(1, 0), BV(9,0), BV(17,0) -> ControllerInputEvent(ControllerInputs.PMR2, ControllerInputActions.OFF)
                BV(2, 127), BV(10,127), BV(18,127) -> ControllerInputEvent(ControllerInputs.PMR3, ControllerInputActions.ON)
                BV(2, 0), BV(10,0), BV(18,0) -> ControllerInputEvent(ControllerInputs.PMR3, ControllerInputActions.OFF)
                BV(3, 127), BV(11,127), BV(19,127) -> ControllerInputEvent(ControllerInputs.PMR4, ControllerInputActions.ON)
                BV(3, 0), BV(11,0), BV(19,0) -> ControllerInputEvent(ControllerInputs.PMR4, ControllerInputActions.OFF)
                BV(4, 127), BV(12,127), BV(20,127) -> ControllerInputEvent(ControllerInputs.PMR5, ControllerInputActions.ON)
                BV(4, 0), BV(12,0), BV(20,0) -> ControllerInputEvent(ControllerInputs.PMR5, ControllerInputActions.OFF)
                BV(5, 127), BV(13,127), BV(21,127) -> ControllerInputEvent(ControllerInputs.PMR6, ControllerInputActions.ON)
                BV(5, 0), BV(13,0), BV(21,0) -> ControllerInputEvent(ControllerInputs.PMR6, ControllerInputActions.OFF)
                BV(6, 127), BV(14,127), BV(22,127) -> ControllerInputEvent(ControllerInputs.PMR7, ControllerInputActions.ON)
                BV(6, 0), BV(14,0), BV(22,0) -> ControllerInputEvent(ControllerInputs.PMR7, ControllerInputActions.OFF)
                BV(7, 127), BV(15,127), BV(23,127) -> ControllerInputEvent(ControllerInputs.PMR8, ControllerInputActions.ON)
                BV(7, 0), BV(15,0), BV(23,0) -> ControllerInputEvent(ControllerInputs.PMR8, ControllerInputActions.OFF)

                //Bank
                BV(46, 127) -> ControllerInputEvent(ControllerInputs.BANK_DOWN, ControllerInputActions.ON)
                BV(46, 0) -> ControllerInputEvent(ControllerInputs.BANK_DOWN, ControllerInputActions.OFF)
                BV(47, 127) -> ControllerInputEvent(ControllerInputs.BANK_UP, ControllerInputActions.ON)
                BV(47, 0) -> ControllerInputEvent(ControllerInputs.BANK_UP, ControllerInputActions.OFF)

                //F1 - F5
                BV(54,127) -> ControllerInputEvent(ControllerInputs.F1, ControllerInputActions.ON)
                BV(54,0) -> ControllerInputEvent(ControllerInputs.F1, ControllerInputActions.OFF)
                BV(55, 127) -> ControllerInputEvent(ControllerInputs.F2, ControllerInputActions.ON)
                BV(55, 0) -> ControllerInputEvent(ControllerInputs.F2, ControllerInputActions.OFF)
                BV(56, 127) -> ControllerInputEvent(ControllerInputs.F3, ControllerInputActions.ON)
                BV(56, 0) -> ControllerInputEvent(ControllerInputs.F3, ControllerInputActions.OFF)
                BV(57, 127) -> ControllerInputEvent(ControllerInputs.F4, ControllerInputActions.ON)
                BV(57, 0) -> ControllerInputEvent(ControllerInputs.F4, ControllerInputActions.OFF)
                BV(58, 127) -> ControllerInputEvent(ControllerInputs.F5, ControllerInputActions.ON)
                BV(58, 0) -> ControllerInputEvent(ControllerInputs.F5, ControllerInputActions.OFF)

                // Transport
                BV(91, 127) -> ControllerInputEvent(ControllerInputs.REW, ControllerInputActions.ON)
                BV(91, 0) -> ControllerInputEvent(ControllerInputs.REW, ControllerInputActions.OFF)
                BV(92, 127) -> ControllerInputEvent(ControllerInputs.FF, ControllerInputActions.ON)
                BV(92, 0) -> ControllerInputEvent(ControllerInputs.FF, ControllerInputActions.OFF)
                BV(93, 127) -> ControllerInputEvent(ControllerInputs.STOP, ControllerInputActions.ON)
                BV(93, 0) -> ControllerInputEvent(ControllerInputs.STOP, ControllerInputActions.OFF)
                BV(94, 127) -> ControllerInputEvent(ControllerInputs.PLAY, ControllerInputActions.ON)
                BV(94, 0) -> ControllerInputEvent(ControllerInputs.PLAY, ControllerInputActions.OFF)
                BV(95, 127) -> ControllerInputEvent(ControllerInputs.REC, ControllerInputActions.ON)
                BV(95, 0) -> ControllerInputEvent(ControllerInputs.REC, ControllerInputActions.OFF)

                // Navigation
                BV(96, 127) -> ControllerInputEvent(ControllerInputs.UP, ControllerInputActions.ON)
                BV(96, 0) -> ControllerInputEvent(ControllerInputs.UP, ControllerInputActions.OFF)
                BV(97, 127) -> ControllerInputEvent(ControllerInputs.DOWN, ControllerInputActions.ON)
                BV(97, 0) -> ControllerInputEvent(ControllerInputs.DOWN, ControllerInputActions.OFF)
                BV(98, 127) -> ControllerInputEvent(ControllerInputs.LEFT, ControllerInputActions.ON)
                BV(98, 0) -> ControllerInputEvent(ControllerInputs.LEFT, ControllerInputActions.OFF)
                BV(99, 127) -> ControllerInputEvent(ControllerInputs.RIGHT, ControllerInputActions.ON)
                BV(99, 0) -> ControllerInputEvent(ControllerInputs.RIGHT, ControllerInputActions.OFF)

                else -> null
            }
        176 -> when (BV(data1, data2)) {
            //Jogwheel
            BV(60,1) -> ControllerInputEvent(ControllerInputs.JogWheel, ControllerInputActions.ON) //Clockwise
            BV(60,65) -> ControllerInputEvent(ControllerInputs.JogWheel, ControllerInputActions.OFF) //Anticlockwise
            else -> null
        }
        //Faders
        224 -> ControllerInputEvent(ControllerInputs.FADER1, ControllerInputActions.ON, data2)
        225 -> ControllerInputEvent(ControllerInputs.FADER2, ControllerInputActions.ON, data2)
        226 -> ControllerInputEvent(ControllerInputs.FADER3, ControllerInputActions.ON, data2)
        227 -> ControllerInputEvent(ControllerInputs.FADER4, ControllerInputActions.ON, data2)
        228 -> ControllerInputEvent(ControllerInputs.FADER5, ControllerInputActions.ON, data2)
        229 -> ControllerInputEvent(ControllerInputs.FADER6, ControllerInputActions.ON, data2)
        230 -> ControllerInputEvent(ControllerInputs.FADER7, ControllerInputActions.ON, data2)
        231 -> ControllerInputEvent(ControllerInputs.FADER8, ControllerInputActions.ON, data2)
        232 -> ControllerInputEvent(ControllerInputs.MASTER_FADER, ControllerInputActions.ON, data2)
        else ->
            null
    }
}