package ie.polymorphsoft.bitwig

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ModelTest {
    @Test
    internal fun switchMode() {
        val model = Model(BitwigMode.TRACKS)
        val inputEvent = ControllerInputEvent(ControllerInputs.F5, ControllerInputActions.ON)
        val (newModel, _) = update(model, inputEvent)
        assertEquals(BitwigMode.DEVICES, newModel.mode)
        val (newModel2, _) = update(newModel, inputEvent)
        assertEquals(BitwigMode.TRACKS, newModel2.mode)
    }

    @Test
    internal fun shiftAndCtrl(){
        val model = initModel()
        val (newModel, event1) = update(model, ControllerInputEvent(ControllerInputs.F1, ControllerInputActions.ON))
        assertTrue(newModel.shift)
        assertNull(event1)
        val (newModel2, event2) = update(model, ControllerInputEvent(ControllerInputs.F1, ControllerInputActions.OFF))
        assertFalse(newModel2.shift)
        assertNull(event2)
        val (newModel3, event3) = update(model, ControllerInputEvent(ControllerInputs.F2, ControllerInputActions.ON))
        assertTrue(newModel3.ctrl)
        assertNull(event3)
        val (newModel4, event4) = update(model, ControllerInputEvent(ControllerInputs.F2, ControllerInputActions.OFF))
        assertFalse(newModel4.ctrl)
        assertNull(event4)

    }

    @Test
    internal fun transport() {
        val model = initModel()
        val (_, bitwigEvent) = update(model, ControllerInputEvent(ControllerInputs.PLAY, ControllerInputActions.ON))
        assertEquals(Play, bitwigEvent)
        val (_, bitwigEvent2) = update(model, ControllerInputEvent(ControllerInputs.STOP, ControllerInputActions.ON))
        assertEquals(Stop, bitwigEvent2)
        val (_, bitwigEvent3) = update(model, ControllerInputEvent(ControllerInputs.REC, ControllerInputActions.ON))
        assertEquals(Record, bitwigEvent3)
        val (_, bitwigEvent4) = update(model, ControllerInputEvent(ControllerInputs.FF, ControllerInputActions.ON))
        assertEquals(FastForward, bitwigEvent4)
        val (_, bitwigEvent5) = update(model, ControllerInputEvent(ControllerInputs.REW, ControllerInputActions.ON))
        assertEquals(Rewind, bitwigEvent5)
    }

    @Test
    internal fun faders() {
        val model = initModel()
        val (_, bitwigEventWrong) = update(model, ControllerInputEvent(ControllerInputs.FADER1, ControllerInputActions.OFF, 10))
        assertNull(bitwigEventWrong)
        val (_, bitwigEvent) = update(model, ControllerInputEvent(ControllerInputs.FADER1, ControllerInputActions.ON, 10))
        assertNotNull(bitwigEvent)
        assertEquals(10, (bitwigEvent as Fader).level)
        assertEquals(0, (bitwigEvent as Fader).track)
        val (_, bitwigEvent2) = update(model, ControllerInputEvent(ControllerInputs.FADER2, ControllerInputActions.ON, 100))
        assertNotNull(bitwigEvent2)
        assertEquals(100, (bitwigEvent2 as Fader).level)
        assertEquals(1, (bitwigEvent2 as Fader).track)
        val (_, bitwigEvent3) = update(model, ControllerInputEvent(ControllerInputs.FADER3, ControllerInputActions.ON, 100))
        assertNotNull(bitwigEvent3)
        assertEquals(100, (bitwigEvent3 as Fader).level)
        assertEquals(2, (bitwigEvent3 as Fader).track)
        val (_, bitwigEvent4) = update(model, ControllerInputEvent(ControllerInputs.FADER4, ControllerInputActions.ON, 127))
        assertNotNull(bitwigEvent4)
        assertEquals(127, (bitwigEvent4 as Fader).level)
        assertEquals(3, (bitwigEvent4 as Fader).track)
        val (_, bitwigEvent5) = update(model, ControllerInputEvent(ControllerInputs.FADER5, ControllerInputActions.ON, 127))
        assertNotNull(bitwigEvent5)
        assertEquals(127, (bitwigEvent5 as Fader).level)
        assertEquals(4, (bitwigEvent5 as Fader).track)
        val (_, bitwigEvent6) = update(model, ControllerInputEvent(ControllerInputs.FADER6, ControllerInputActions.ON, 127))
        assertNotNull(bitwigEvent6)
        assertEquals(127, (bitwigEvent6 as Fader).level)
        assertEquals(5, (bitwigEvent6 as Fader).track)
        val (_, bitwigEvent7) = update(model, ControllerInputEvent(ControllerInputs.FADER7, ControllerInputActions.ON, 127))
        assertNotNull(bitwigEvent7)
        assertEquals(127, (bitwigEvent7 as Fader).level)
        assertEquals(6, (bitwigEvent7 as Fader).track)
        val (_, bitwigEvent8) = update(model, ControllerInputEvent(ControllerInputs.FADER8, ControllerInputActions.ON, 127))
        assertNotNull(bitwigEvent8)
        assertEquals(127, (bitwigEvent8 as Fader).level)
        assertEquals(7, (bitwigEvent8 as Fader).track)
        val (_, bitwigEventM) = update(model, ControllerInputEvent(ControllerInputs.MASTER_FADER, ControllerInputActions.ON, 0))
        assertNotNull(bitwigEventM)
        assertEquals(0, (bitwigEventM as MasterFader).level)
    }

    @Test
    internal fun pans() {
        val (model, _) = update(initModel(), ControllerInputEvent(ControllerInputs.F1, ControllerInputActions.ON)) //Shift
        val (_, bitwigEventWrong) = update(model, ControllerInputEvent(ControllerInputs.FADER1, ControllerInputActions.OFF, 10))
        assertNull(bitwigEventWrong)
        val (_, bitwigEvent) = update(model, ControllerInputEvent(ControllerInputs.FADER1, ControllerInputActions.ON, 10))
        assertNotNull(bitwigEvent)
        assertTrue(bitwigEvent is Pan)
        assertEquals(10, (bitwigEvent as Pan).level)
        assertEquals(0, (bitwigEvent as Pan).track)
        val (_, bitwigEvent2) = update(model, ControllerInputEvent(ControllerInputs.FADER2, ControllerInputActions.ON, 100))
        assertNotNull(bitwigEvent2)
        assertEquals(100, (bitwigEvent2 as Pan).level)
        assertEquals(1, (bitwigEvent2 as Pan).track)
        val (_, bitwigEvent3) = update(model, ControllerInputEvent(ControllerInputs.FADER3, ControllerInputActions.ON, 100))
        assertNotNull(bitwigEvent3)
        assertEquals(100, (bitwigEvent3 as Pan).level)
        assertEquals(2, (bitwigEvent3 as Pan).track)
        val (_, bitwigEvent4) = update(model, ControllerInputEvent(ControllerInputs.FADER4, ControllerInputActions.ON, 127))
        assertNotNull(bitwigEvent4)
        assertEquals(127, (bitwigEvent4 as Pan).level)
        assertEquals(3, (bitwigEvent4 as Pan).track)
        val (_, bitwigEvent5) = update(model, ControllerInputEvent(ControllerInputs.FADER5, ControllerInputActions.ON, 127))
        assertNotNull(bitwigEvent5)
        assertEquals(127, (bitwigEvent5 as Pan).level)
        assertEquals(4, (bitwigEvent5 as Pan).track)
        val (_, bitwigEvent6) = update(model, ControllerInputEvent(ControllerInputs.FADER6, ControllerInputActions.ON, 127))
        assertNotNull(bitwigEvent6)
        assertEquals(127, (bitwigEvent6 as Pan).level)
        assertEquals(5, (bitwigEvent6 as Pan).track)
        val (_, bitwigEvent7) = update(model, ControllerInputEvent(ControllerInputs.FADER7, ControllerInputActions.ON, 127))
        assertNotNull(bitwigEvent7)
        assertEquals(127, (bitwigEvent7 as Pan).level)
        assertEquals(6, (bitwigEvent7 as Pan).track)
        val (_, bitwigEvent8) = update(model, ControllerInputEvent(ControllerInputs.FADER8, ControllerInputActions.ON, 127))
        assertNotNull(bitwigEvent8)
        assertEquals(127, (bitwigEvent8 as Pan).level)
        assertEquals(7, (bitwigEvent8 as Pan).track)
        val (_, bitwigEventM) = update(model, ControllerInputEvent(ControllerInputs.MASTER_FADER, ControllerInputActions.ON, 0))
        assertNotNull(bitwigEventM)
        assertEquals(0, (bitwigEventM as MasterFader).level)
    }


    @Test
    internal fun bank(){
        val model = initModel()
        assertEquals(0, model.currentBank)
        val (newModel0, bitwigEvent0) = update(model, ControllerInputEvent(ControllerInputs.BANK_DOWN, ControllerInputActions.ON))
        assertNull(bitwigEvent0)
        assertEquals(0, newModel0.currentBank)
        val (newModel1, bitwigEvent1) = update(model, ControllerInputEvent(ControllerInputs.BANK_UP, ControllerInputActions.ON))
        assertNotNull(bitwigEvent1)
        assertEquals(BankUp, bitwigEvent1)
        assertEquals(1, newModel1.currentBank)
        val (newModel2, bitwigEvent2) = update(newModel1, ControllerInputEvent(ControllerInputs.BANK_UP, ControllerInputActions.ON))
        assertNotNull(bitwigEvent2)
        assertEquals(BankUp, bitwigEvent2)
        assertEquals(2, newModel2.currentBank)
        val (newModel3, bitwigEvent3) = update(newModel2, ControllerInputEvent(ControllerInputs.BANK_UP, ControllerInputActions.ON))
        assertNotNull(bitwigEvent3)
        assertEquals(BankUp, bitwigEvent3)
        assertEquals(3, newModel3.currentBank)
        val (newModel4, bitwigEvent4) = update(newModel3, ControllerInputEvent(ControllerInputs.BANK_DOWN, ControllerInputActions.ON))
        assertNotNull(bitwigEvent4)
        assertEquals(BankDown, bitwigEvent4)
        assertEquals(2, newModel4.currentBank)
    }

    @Test
    internal fun muteSoloRec(){
        val model = initModel()
        val (newModel1a, bitwigEvent1a) = update(model, ControllerInputEvent(ControllerInputs.PMR1, ControllerInputActions.ON))
        val (newModel1, bitwigEvent1) = update(newModel1a, ControllerInputEvent(ControllerInputs.PMR1, ControllerInputActions.OFF))
        assertNotNull(bitwigEvent1a)
        assertTrue(bitwigEvent1a is Mute)
        assertEquals(0, (bitwigEvent1a as Mute).track)
        assertTrue((bitwigEvent1a as Mute).on)
        assertTrue(newModel1a.muteState.isOn(0))
        val (newModel2, bitwigEvent2) = update(newModel1, ControllerInputEvent(ControllerInputs.PMR1, ControllerInputActions.ON))
        assertNotNull(bitwigEvent2)
        assertTrue(bitwigEvent2 is Mute)
        assertEquals(0, (bitwigEvent2 as Mute).track)
        assertFalse((bitwigEvent2 as Mute).on)
        assertFalse(newModel2.muteState.isOn(0))
        val (newModel3, bitwigEvent3) = update(newModel2, ControllerInputEvent(ControllerInputs.F1, ControllerInputActions.ON)) //Shift held on
        val (newModel4, bitwigEvent4) = update(newModel3, ControllerInputEvent(ControllerInputs.PMR1, ControllerInputActions.ON))
        assertNotNull(bitwigEvent4)
        assertTrue(bitwigEvent4 is Solo)
        assertEquals(0, (bitwigEvent4 as Solo).track)
        assertTrue((bitwigEvent4 as Solo).on)
        assertTrue(newModel4.soloState.isOn(0))
        val (newModel5, bitwigEvent5) = update(newModel4, ControllerInputEvent(ControllerInputs.F1, ControllerInputActions.OFF)) //Shift off
        val (newModel6, bitwigEvent6) = update(newModel5, ControllerInputEvent(ControllerInputs.PMR1, ControllerInputActions.ON))
        assertNotNull(bitwigEvent6)
        assertTrue(bitwigEvent6 is Mute)
        assertEquals(0, (bitwigEvent6 as Mute).track)
        assertTrue((bitwigEvent6 as Mute).on)
        assertTrue(newModel6.soloState.isOn(0))
        val (newModel7, bitwigEvent7) = update(newModel6, ControllerInputEvent(ControllerInputs.F2, ControllerInputActions.ON)) //Ctrl on
        val (newModel8, bitwigEvent8) = update(newModel7, ControllerInputEvent(ControllerInputs.PMR1, ControllerInputActions.ON))
        assertNotNull(bitwigEvent8)
        assertTrue(bitwigEvent8 is Rec)
        assertEquals(0, (bitwigEvent8 as Rec).track)
        assertTrue((bitwigEvent8 as Rec).on)
        assertTrue(newModel8.recState.isOn(0))
        val (newModel9, bitwigEvent9) = update(newModel8, ControllerInputEvent(ControllerInputs.F2, ControllerInputActions.OFF)) //Ctrl off
        val (newModel10, bitwigEvent10) = update(newModel9, ControllerInputEvent(ControllerInputs.PMR2, ControllerInputActions.ON))
        assertNotNull(bitwigEvent10)
        assertTrue(bitwigEvent10 is Mute)
        assertEquals(1, (bitwigEvent10 as Mute).track)
        assertTrue((bitwigEvent10 as Mute).on)
        assertTrue(newModel10.muteState.isOn(1))
    }

    @Test
    internal fun jog(){
        val model = initModel()
        val (newModel1, bitwigEvent1) = update(model, ControllerInputEvent(ControllerInputs.JogWheel, ControllerInputActions.ON))
        assertNotNull(bitwigEvent1)
        assertTrue(bitwigEvent1 is JogClockwise)
        val (newModel2, bitwigEvent2) = update(newModel1, ControllerInputEvent(ControllerInputs.JogWheel, ControllerInputActions.OFF))
        assertNotNull(bitwigEvent2)
        assertTrue(bitwigEvent2 is JogAntiClockwise)
        val (newModel3, bitwigEvent3) = update(newModel1, ControllerInputEvent(ControllerInputs.F1, ControllerInputActions.ON)) // Shift on
        val (newModel4, bitwigEvent4) = update(newModel3, ControllerInputEvent(ControllerInputs.JogWheel, ControllerInputActions.ON))
        assertNotNull(bitwigEvent4)
        assertTrue(bitwigEvent4 is ZoomIn)
        val (newModel5, bitwigEvent5) = update(newModel4, ControllerInputEvent(ControllerInputs.JogWheel, ControllerInputActions.OFF))
        assertNotNull(bitwigEvent5)
        assertTrue(bitwigEvent5 is ZoomOut)
    }

    @Test
    internal fun bwsMuteSoloRec(){
        val model = initModel()
        val (newModel1, bitwigEvent1) = update(model, MutedEvent(1, true))
        assertNull(bitwigEvent1)
        assertTrue(newModel1.muteState.isOn(1))
        val (newModel2, _) = update(newModel1, MutedEvent(1, false))
        assertFalse(newModel2.muteState.isOn(1))
        val (newModel3, _) = update(newModel2, SoloedEvent(3, true))
        assertTrue(newModel3.soloState.isOn(3))
        val (newModel4, _) = update(newModel3, SoloedEvent(3, false))
        assertFalse(newModel4.soloState.isOn(3))
        val (newModel5, _) = update(newModel4, ArmedEvent(3, true))
        assertTrue(newModel5.recState.isOn(3))
        val (newModel6, _) = update(newModel5, ArmedEvent(3, false))
        assertFalse(newModel6.recState.isOn(3))
    }


}