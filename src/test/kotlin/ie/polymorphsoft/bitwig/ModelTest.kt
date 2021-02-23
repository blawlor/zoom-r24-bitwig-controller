package ie.polymorphsoft.bitwig

import ie.polymorphsoft.*
import ie.polymorphsoft.bitwig.Model.Companion.update
import ie.polymorphsoft.bitwig.zoom.InputActions
import ie.polymorphsoft.bitwig.zoom.InputEvent
import ie.polymorphsoft.bitwig.zoom.Inputs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
}