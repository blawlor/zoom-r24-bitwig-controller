package ie.polymorphsoft.bitwig

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ModelTest {
    @Test
    internal fun switchMode() {
        val model = Model(Mode.TRACKS)
        val inputEvent = ControllerInputEvent(ControllerInputs.F5, ControllerInputActions.ON)
        val (newModel, _) = update(model, inputEvent)
        assertEquals(Mode.DEVICES, newModel.mode)
        val (newModel2, _) = update(newModel, inputEvent)
        assertEquals(Mode.TRACKS, newModel2.mode)
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
        assertEquals(10, (bitwigEvent as Volume).level)
        assertEquals(0, (bitwigEvent as Volume).track)
        val (_, bitwigEvent2) = update(model, ControllerInputEvent(ControllerInputs.FADER2, ControllerInputActions.ON, 100))
        assertNotNull(bitwigEvent2)
        assertEquals(100, (bitwigEvent2 as Volume).level)
        assertEquals(1, (bitwigEvent2 as Volume).track)
        val (_, bitwigEvent3) = update(model, ControllerInputEvent(ControllerInputs.FADER3, ControllerInputActions.ON, 100))
        assertNotNull(bitwigEvent3)
        assertEquals(100, (bitwigEvent3 as Volume).level)
        assertEquals(2, (bitwigEvent3 as Volume).track)
        val (_, bitwigEvent4) = update(model, ControllerInputEvent(ControllerInputs.FADER4, ControllerInputActions.ON, 127))
        assertNotNull(bitwigEvent4)
        assertEquals(127, (bitwigEvent4 as Volume).level)
        assertEquals(3, (bitwigEvent4 as Volume).track)
        val (_, bitwigEvent5) = update(model, ControllerInputEvent(ControllerInputs.FADER5, ControllerInputActions.ON, 127))
        assertNotNull(bitwigEvent5)
        assertEquals(127, (bitwigEvent5 as Volume).level)
        assertEquals(4, (bitwigEvent5 as Volume).track)
        val (_, bitwigEvent6) = update(model, ControllerInputEvent(ControllerInputs.FADER6, ControllerInputActions.ON, 127))
        assertNotNull(bitwigEvent6)
        assertEquals(127, (bitwigEvent6 as Volume).level)
        assertEquals(5, (bitwigEvent6 as Volume).track)
        val (_, bitwigEvent7) = update(model, ControllerInputEvent(ControllerInputs.FADER7, ControllerInputActions.ON, 127))
        assertNotNull(bitwigEvent7)
        assertEquals(127, (bitwigEvent7 as Volume).level)
        assertEquals(6, (bitwigEvent7 as Volume).track)
        val (_, bitwigEvent8) = update(model, ControllerInputEvent(ControllerInputs.FADER8, ControllerInputActions.ON, 127))
        assertNotNull(bitwigEvent8)
        assertEquals(127, (bitwigEvent8 as Volume).level)
        assertEquals(7, (bitwigEvent8 as Volume).track)
        val (_, bitwigEventM) = update(model, ControllerInputEvent(ControllerInputs.MASTER_FADER, ControllerInputActions.ON, 0))
        assertNotNull(bitwigEventM)
        assertEquals(0, (bitwigEventM as MasterVolume).level)
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
        assertEquals(0, (bitwigEventM as MasterPan).level)
    }


    @Test
    internal fun bank(){
        val model = initModel()
        assertEquals(0, model.currentTrackBank)
        val (newModel0, bitwigEvent0) = update(model, ControllerInputEvent(ControllerInputs.BANK_DOWN, ControllerInputActions.ON))
        assertNotNull(bitwigEvent0)
        assertEquals(TrackBankDown, bitwigEvent0)
        assertEquals(0, newModel0.currentTrackBank)
        val (newModel1, bitwigEvent1) = update(model, ControllerInputEvent(ControllerInputs.BANK_UP, ControllerInputActions.ON))
        assertNotNull(bitwigEvent1)
        assertEquals(TrackBankUp, bitwigEvent1)
        assertEquals(0, newModel1.currentTrackBank)//Model not to be updated. This is done by BSW events
        val (newModel2, bitwigEvent2) = update(newModel1, ControllerInputEvent(ControllerInputs.BANK_UP, ControllerInputActions.ON))
        assertNotNull(bitwigEvent2)
        assertEquals(TrackBankUp, bitwigEvent2)
        val (newModel3, bitwigEvent3) = update(newModel2, ControllerInputEvent(ControllerInputs.BANK_UP, ControllerInputActions.ON))
        assertNotNull(bitwigEvent3)
        assertEquals(TrackBankUp, bitwigEvent3)
        val (newModel4, bitwigEvent4) = update(newModel3.copy(currentTrackBank = 1), ControllerInputEvent(ControllerInputs.BANK_DOWN, ControllerInputActions.ON))
        assertNotNull(bitwigEvent4)
        assertEquals(TrackBankDown, bitwigEvent4)
    }

    @Test
    internal fun muteSoloRec(){
        val model = initModel()
        val (newModel1a, event1) = update(model, ControllerInputEvent(ControllerInputs.PMR1, ControllerInputActions.ON))
        val (model1, _) = update(newModel1a, ControllerInputEvent(ControllerInputs.PMR1, ControllerInputActions.OFF)) //Off button shouldn't make any difference - testing this
        assertNotNull(event1)
        assertTrue(event1 is Mute)
        assertEquals(0, (event1 as Mute).track)
        assertTrue((event1 as Mute).on)
        assertFalse(model1.muteState.isOn(0)) //This event should not change the state - that can only come from Bitwig

        //Set mute directly
        val (model2, event2) = update(model1.copy(muteState = model.muteState.set(0, true)), ControllerInputEvent(ControllerInputs.PMR1, ControllerInputActions.ON))
        assertNotNull(event2)
        assertTrue(event2 is Mute)
        assertEquals(0, (event2 as Mute).track)
        assertFalse((event2 as Mute).on)
        assertTrue(model2.muteState.isOn(0)) // Model unchanged

        //Shift PMR = Solo
        val (model3, event3) = update(model2.copy(shift = true), ControllerInputEvent(ControllerInputs.PMR1, ControllerInputActions.ON))
        assertNotNull(event3)
        assertTrue(event3 is Solo)
        assertEquals(0, (event3 as Solo).track)
        assertTrue((event3 as Solo).on)
        assertFalse(model3.soloState.isOn(0))

        //Ctrl on
        val (model4, event4) = update(model3.copy(ctrl = true, shift = false), ControllerInputEvent(ControllerInputs.PMR1, ControllerInputActions.ON))
        assertNotNull(event4)
        assertTrue(event4 is Rec)
        assertEquals(0, (event4 as Rec).track)
        assertTrue((event4 as Rec).on)
        assertFalse(model4.recState.isOn(0)) // Model unchanged

        //Ctrl off again
        val (model5, event5) = update(model4.copy(ctrl = false), ControllerInputEvent(ControllerInputs.PMR2, ControllerInputActions.ON))
        assertNotNull(event5)
        assertTrue(event5 is Mute)
        assertEquals(1, (event5 as Mute).track)
        assertTrue((event5 as Mute).on)
        assertTrue(model5.muteState.isOn(0)) // Mute still on from earlier
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