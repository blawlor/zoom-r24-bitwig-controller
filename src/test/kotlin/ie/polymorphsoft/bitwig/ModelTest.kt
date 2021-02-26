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
    internal fun shiftAndCtrl(){
        val model = initModel()
        val (newModel, event1) = update(model, InputEvent(Inputs.F1, InputActions.ON))
        assertTrue(newModel.shift)
        assertEquals(ShiftOn, event1)
        val (newModel2, event2) = update(model, InputEvent(Inputs.F1, InputActions.OFF))
        assertFalse(newModel2.shift)
        assertEquals(ShiftOff, event2)
        val (newModel3, event3) = update(model, InputEvent(Inputs.F2, InputActions.ON))
        assertTrue(newModel3.ctrl)
        assertEquals(CtrlOn, event3)
        val (newModel4, event4) = update(model, InputEvent(Inputs.F2, InputActions.OFF))
        assertFalse(newModel4.ctrl)
        assertEquals(CtrlOff, event4)

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

    @Test
    internal fun bank(){
        val model = initModel()
        assertEquals(0, model.currentBank)
        val (newModel0, bitwigEvent0) = update(model, InputEvent(Inputs.BANK_DOWN, InputActions.ON))
        assertNull(bitwigEvent0)
        assertEquals(0, newModel0.currentBank)
        val (newModel1, bitwigEvent1) = update(model, InputEvent(Inputs.BANK_UP, InputActions.ON))
        assertNotNull(bitwigEvent1)
        assertEquals(BankUp, bitwigEvent1)
        assertEquals(1, newModel1.currentBank)
        val (newModel2, bitwigEvent2) = update(newModel1, InputEvent(Inputs.BANK_UP, InputActions.ON))
        assertNotNull(bitwigEvent2)
        assertEquals(BankUp, bitwigEvent2)
        assertEquals(2, newModel2.currentBank)
        val (newModel3, bitwigEvent3) = update(newModel2, InputEvent(Inputs.BANK_UP, InputActions.ON))
        assertNotNull(bitwigEvent3)
        assertEquals(BankUp, bitwigEvent3)
        assertEquals(3, newModel3.currentBank)
        val (newModel4, bitwigEvent4) = update(newModel3, InputEvent(Inputs.BANK_DOWN, InputActions.ON))
        assertNotNull(bitwigEvent4)
        assertEquals(BankDown, bitwigEvent4)
        assertEquals(2, newModel4.currentBank)
    }

    @Test
    internal fun muteSoloRec(){
        val model = initModel()
        val (newModel1, bitwigEvent1) = update(model, InputEvent(Inputs.PMR1, InputActions.ON))
        assertNotNull(bitwigEvent1)
        assertTrue(bitwigEvent1 is Mute)
        assertEquals(0, (bitwigEvent1 as Mute).track)
        assertTrue((bitwigEvent1 as Mute).on)
        assertTrue(newModel1.muteState.isOn(0))
        val (newModel2, bitwigEvent2) = update(newModel1, InputEvent(Inputs.PMR1, InputActions.ON))
        assertNotNull(bitwigEvent1)
        assertTrue(bitwigEvent2 is Mute)
        assertEquals(0, (bitwigEvent2 as Mute).track)
        assertFalse((bitwigEvent2 as Mute).on)
        assertFalse(newModel2.muteState.isOn(0))
        val (newModel3, bitwigEvent3) = update(newModel2, InputEvent(Inputs.F1, InputActions.ON)) //Shift held on
        val (newModel4, bitwigEvent4) = update(newModel3, InputEvent(Inputs.PMR1, InputActions.ON))
        assertNotNull(bitwigEvent4)
        assertTrue(bitwigEvent4 is Solo)
        assertEquals(0, (bitwigEvent4 as Solo).track)
        assertTrue((bitwigEvent4 as Solo).on)
        assertTrue(newModel4.soloState.isOn(0))
        val (newModel5, bitwigEvent5) = update(newModel4, InputEvent(Inputs.F1, InputActions.OFF)) //Shift off
        val (newModel6, bitwigEvent6) = update(newModel5, InputEvent(Inputs.PMR1, InputActions.ON))
        assertNotNull(bitwigEvent6)
        assertTrue(bitwigEvent6 is Mute)
        assertEquals(0, (bitwigEvent6 as Mute).track)
        assertTrue((bitwigEvent6 as Mute).on)
        assertTrue(newModel6.soloState.isOn(0))
        val (newModel7, bitwigEvent7) = update(newModel6, InputEvent(Inputs.F2, InputActions.ON)) //Ctrl on
        val (newModel8, bitwigEvent8) = update(newModel7, InputEvent(Inputs.PMR1, InputActions.ON))
        assertNotNull(bitwigEvent8)
        assertTrue(bitwigEvent8 is Rec)
        assertEquals(0, (bitwigEvent8 as Rec).track)
        assertTrue((bitwigEvent8 as Rec).on)
        assertTrue(newModel8.recState.isOn(0))
        val (newModel9, bitwigEvent9) = update(newModel8, InputEvent(Inputs.F2, InputActions.OFF)) //Ctrl off
        val (newModel10, bitwigEvent10) = update(newModel9, InputEvent(Inputs.PMR2, InputActions.ON))
        assertNotNull(bitwigEvent10)
        assertTrue(bitwigEvent10 is Mute)
        assertEquals(1, (bitwigEvent10 as Mute).track)
        assertTrue((bitwigEvent10 as Mute).on)
        assertTrue(newModel10.muteState.isOn(1))
    }

    @Test
    internal fun jog(){
        val model = initModel()
        val (newModel1, bitwigEvent1) = update(model, InputEvent(Inputs.JogWheel, InputActions.ON))
        assertNotNull(bitwigEvent1)
        assertTrue(bitwigEvent1 is JogClockwise)
        val (newModel2, bitwigEvent2) = update(newModel1, InputEvent(Inputs.JogWheel, InputActions.OFF))
        assertNotNull(bitwigEvent2)
        assertTrue(bitwigEvent2 is JogAntiClockwise)
        val (newModel3, bitwigEvent3) = update(newModel1, InputEvent(Inputs.F1, InputActions.ON)) // Shift on
        val (newModel4, bitwigEvent4) = update(newModel3, InputEvent(Inputs.JogWheel, InputActions.ON))
        assertNotNull(bitwigEvent4)
        assertTrue(bitwigEvent4 is ZoomIn)
        val (newModel5, bitwigEvent5) = update(newModel4, InputEvent(Inputs.JogWheel, InputActions.OFF))
        assertNotNull(bitwigEvent5)
        assertTrue(bitwigEvent5 is ZoomOut)
    }
}