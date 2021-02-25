package ie.polymorphsoft.bitwig

import ie.polymorphsoft.bitwig.zoom.InputActions
import ie.polymorphsoft.bitwig.zoom.InputEvent
import ie.polymorphsoft.bitwig.zoom.Inputs
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ModelTest {
    @Test
    internal fun switchMode() {
        val model = Model(BitwigMode.ARRANGE)
        val inputEvent = InputEvent(Inputs.F5, InputActions.ON)
        val (newModel, bitwigEvent) = update(model, inputEvent)
        assertEquals(BitwigMode.CLIP, newModel.mode)
        assertTrue(bitwigEvent is SwitchToClipLauncherView)
        val (newModel2, bitwigEvent2) = update(newModel, inputEvent)
        assertEquals(BitwigMode.MIX, newModel2.mode)
        assertTrue(bitwigEvent2 is SwitchToMixerView)
        val (newModel3, bitwigEvent3) = update(newModel2, inputEvent)
        assertEquals(BitwigMode.ARRANGE, newModel3.mode)
        assertTrue(bitwigEvent3 is SwitchToArrangeView)
    }

    @Test
    internal fun transport() {
        val model = initModel()
        val (_, bitwigEvent) = update(model, InputEvent(Inputs.PLAY, InputActions.ON))
        assertEquals(Play, bitwigEvent)
        val (_, bitwigEvent2) = update(model, InputEvent(Inputs.STOP, InputActions.ON))
        assertEquals(Stop, bitwigEvent2)
        val (_, bitwigEvent3) = update(model, InputEvent(Inputs.REC, InputActions.ON))
        assertEquals(Record, bitwigEvent3)
        val (_, bitwigEvent4) = update(model, InputEvent(Inputs.FF, InputActions.ON))
        assertEquals(FastForward, bitwigEvent4)
        val (_, bitwigEvent5) = update(model, InputEvent(Inputs.REW, InputActions.ON))
        assertEquals(Rewind, bitwigEvent5)
    }

    @Test
    internal fun faders() {
        val model = initModel()
        val (_, bitwigEventWrong) = update(model, InputEvent(Inputs.FADER1, InputActions.OFF, 10))
        assertNull(bitwigEventWrong)
        val (_, bitwigEvent) = update(model, InputEvent(Inputs.FADER1, InputActions.ON, 10))
        assertNotNull(bitwigEvent)
        assertEquals(10, (bitwigEvent as Fader).level)
        assertEquals(0, (bitwigEvent as Fader).track)
        val (_, bitwigEvent2) = update(model, InputEvent(Inputs.FADER2, InputActions.ON, 100))
        assertNotNull(bitwigEvent2)
        assertEquals(100, (bitwigEvent2 as Fader).level)
        assertEquals(1, (bitwigEvent2 as Fader).track)
        val (_, bitwigEvent3) = update(model, InputEvent(Inputs.FADER3, InputActions.ON, 100))
        assertNotNull(bitwigEvent3)
        assertEquals(100, (bitwigEvent3 as Fader).level)
        assertEquals(2, (bitwigEvent3 as Fader).track)
        val (_, bitwigEvent4) = update(model, InputEvent(Inputs.FADER4, InputActions.ON, 127))
        assertNotNull(bitwigEvent4)
        assertEquals(127, (bitwigEvent4 as Fader).level)
        assertEquals(3, (bitwigEvent4 as Fader).track)
        val (_, bitwigEvent5) = update(model, InputEvent(Inputs.FADER5, InputActions.ON, 127))
        assertNotNull(bitwigEvent5)
        assertEquals(127, (bitwigEvent5 as Fader).level)
        assertEquals(4, (bitwigEvent5 as Fader).track)
        val (_, bitwigEvent6) = update(model, InputEvent(Inputs.FADER6, InputActions.ON, 127))
        assertNotNull(bitwigEvent6)
        assertEquals(127, (bitwigEvent6 as Fader).level)
        assertEquals(5, (bitwigEvent6 as Fader).track)
        val (_, bitwigEvent7) = update(model, InputEvent(Inputs.FADER7, InputActions.ON, 127))
        assertNotNull(bitwigEvent7)
        assertEquals(127, (bitwigEvent7 as Fader).level)
        assertEquals(6, (bitwigEvent7 as Fader).track)
        val (_, bitwigEvent8) = update(model, InputEvent(Inputs.FADER8, InputActions.ON, 127))
        assertNotNull(bitwigEvent8)
        assertEquals(127, (bitwigEvent8 as Fader).level)
        assertEquals(7, (bitwigEvent8 as Fader).track)
        val (_, bitwigEventM) = update(model, InputEvent(Inputs.MASTER_FADER, InputActions.ON, 0))
        assertNotNull(bitwigEventM)
        assertEquals(0, (bitwigEventM as MasterFader).level)
    }
}