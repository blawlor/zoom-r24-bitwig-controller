package ie.polymorphsoft.bitwig.midi

import com.bitwig.extension.api.util.midi.ShortMidiMessage
import ie.polymorphsoft.bitwig.ControllerInputActions
import ie.polymorphsoft.bitwig.ControllerInputs
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MidiConversionTest {

    @Test
    internal fun pmr1Button() {
        var event = ShortMidiMessage(144, 0, 127).inputEvent()
        assertNotNull(event)
        event?.let {
            assertEquals(ControllerInputs.PMR1, it.input)
            assertEquals(ControllerInputActions.ON, it.action)
        }
        event = ShortMidiMessage(144, 0, 0).inputEvent()
        assertNotNull(event)
        event?.let {
            assertEquals(ControllerInputs.PMR1, it.input)
            assertEquals(ControllerInputActions.OFF, it.action)
        }
        event = ShortMidiMessage(144, 0, 12).inputEvent()
        assertNull(event)
    }

    @Test
    internal fun pmr2Button() {
        var event = ShortMidiMessage(144, 1, 127).inputEvent()
        assertNotNull(event)
        event?.let {
            assertEquals(ControllerInputs.PMR2, it.input)
            assertEquals(ControllerInputActions.ON, it.action)
        }
        event = ShortMidiMessage(144, 1, 0).inputEvent()
        assertNotNull(event)
        event?.let {
            assertEquals(ControllerInputs.PMR2, it.input)
            assertEquals(ControllerInputActions.OFF, it.action)
        }
        event = ShortMidiMessage(144, 1, 12).inputEvent()
        assertNull(event)
    }

    @Test
    internal fun jogWheel() {
        var event = ShortMidiMessage(176, 60, 1).inputEvent()
        assertNotNull(event)
        event?.let {
            assertEquals(ControllerInputs.JogWheel, it.input)
            assertEquals(ControllerInputActions.ON, it.action)
        }
        event = ShortMidiMessage(176, 60, 65).inputEvent()
        assertNotNull(event)
        event?.let {
            assertEquals(ControllerInputs.JogWheel, it.input)
            assertEquals(ControllerInputActions.OFF, it.action)
        }
        event = ShortMidiMessage(176, 60, 0).inputEvent()
        assertNull(event)
    }

    @Test
    internal fun shift() {
        var event = ShortMidiMessage(144, 54, 127).inputEvent()
        assertNotNull(event)
        event?.let {
            assertEquals(ControllerInputs.F1, it.input)
            assertEquals(ControllerInputActions.ON, it.action)
        }
        event = ShortMidiMessage(144, 54, 0).inputEvent()
        assertNotNull(event)
        event?.let {
            assertEquals(ControllerInputs.F1, it.input)
            assertEquals(ControllerInputActions.OFF, it.action)
        }
    }

    @Test
    internal fun ctrl() {
        var event = ShortMidiMessage(144, 55, 127).inputEvent()
        assertNotNull(event)
        event?.let {
            assertEquals(ControllerInputs.F2, it.input)
            assertEquals(ControllerInputActions.ON, it.action)
        }
        event = ShortMidiMessage(144, 55, 0).inputEvent()
        assertNotNull(event)
        event?.let {
            assertEquals(ControllerInputs.F2, it.input)
            assertEquals(ControllerInputActions.OFF, it.action)
        }
    }

    @Test
    internal fun bank(){
        var event = ShortMidiMessage(144, 46, 127).inputEvent()
        assertNotNull(event)
        event?.let {
            assertEquals(ControllerInputs.BANK_DOWN, it.input)
            assertEquals(ControllerInputActions.ON, it.action)
        }
        event = ShortMidiMessage(144, 46, 0).inputEvent()
        assertNotNull(event)
        event?.let {
            assertEquals(ControllerInputs.BANK_DOWN, it.input)
            assertEquals(ControllerInputActions.OFF, it.action)
        }
        event = ShortMidiMessage(144, 47, 127).inputEvent()
        assertNotNull(event)
        event?.let {
            assertEquals(ControllerInputs.BANK_UP, it.input)
            assertEquals(ControllerInputActions.ON, it.action)
        }
        event = ShortMidiMessage(144, 47, 0).inputEvent()
        assertNotNull(event)
        event?.let {
            assertEquals(ControllerInputs.BANK_UP, it.input)
            assertEquals(ControllerInputActions.OFF, it.action)
        }

    }
}