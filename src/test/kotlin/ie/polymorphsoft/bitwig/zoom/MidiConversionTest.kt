package ie.polymorphsoft.bitwig.zoom

import com.bitwig.extension.api.util.midi.ShortMidiMessage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class MidiConversionTest {

    @Test
    internal fun pmr1Button() {
        var event = ShortMidiMessage(144, 0, 127).inputEvent()
        assertNotNull(event)
        event?.let {
            assertEquals(Inputs.PMR1, it.input)
            assertEquals(InputActions.ON, it.action)
        }
        event = ShortMidiMessage(144, 0, 0).inputEvent()
        assertNotNull(event)
        event?.let {
            assertEquals(Inputs.PMR1, it.input)
            assertEquals(InputActions.OFF, it.action)
        }
        event = ShortMidiMessage(144, 0, 12).inputEvent()
        assertNull(event)
    }

    @Test
    internal fun pmr2Button() {
        var event = ShortMidiMessage(144, 1, 127).inputEvent()
        assertNotNull(event)
        event?.let {
            assertEquals(Inputs.PMR2, it.input)
            assertEquals(InputActions.ON, it.action)
        }
        event = ShortMidiMessage(144, 1, 0).inputEvent()
        assertNotNull(event)
        event?.let {
            assertEquals(Inputs.PMR2, it.input)
            assertEquals(InputActions.OFF, it.action)
        }
        event = ShortMidiMessage(144, 1, 12).inputEvent()
        assertNull(event)
    }

    @Test
    internal fun jogWheel() {
        var event = ShortMidiMessage(176, 60, 1).inputEvent()
        assertNotNull(event)
        event?.let {
            assertEquals(Inputs.JogWheel, it.input)
            assertEquals(InputActions.ON, it.action)
        }
        event = ShortMidiMessage(176, 60, 65).inputEvent()
        assertNotNull(event)
        event?.let {
            assertEquals(Inputs.JogWheel, it.input)
            assertEquals(InputActions.OFF, it.action)
        }
        event = ShortMidiMessage(176, 60, 0).inputEvent()
        assertNull(event)
    }

    @Test
    internal fun shift() {
        var event = ShortMidiMessage(144, 54, 127).inputEvent()
        assertNotNull(event)
        event?.let {
            assertEquals(Inputs.F1, it.input)
            assertEquals(InputActions.ON, it.action)
        }
        event = ShortMidiMessage(144, 54, 0).inputEvent()
        assertNotNull(event)
        event?.let {
            assertEquals(Inputs.F1, it.input)
            assertEquals(InputActions.OFF, it.action)
        }
    }

    @Test
    internal fun ctrl() {
        var event = ShortMidiMessage(144, 55, 127).inputEvent()
        assertNotNull(event)
        event?.let {
            assertEquals(Inputs.F2, it.input)
            assertEquals(InputActions.ON, it.action)
        }
        event = ShortMidiMessage(144, 55, 0).inputEvent()
        assertNotNull(event)
        event?.let {
            assertEquals(Inputs.F2, it.input)
            assertEquals(InputActions.OFF, it.action)
        }
    }
}