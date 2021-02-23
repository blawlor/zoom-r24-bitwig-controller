package ie.polymorphsoft.bitwig

import com.bitwig.extension.api.util.midi.ShortMidiMessage
import ie.polymorphsoft.BitwigMode
import ie.polymorphsoft.Model
import ie.polymorphsoft.controller
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ModelTest {
    @Test
    internal fun switchMode() {
        val model = Model(BitwigMode.ARRANGE)
        val (newModel,event) = controller(model, ShortMidiMessage(144,58,127))
        assertEquals(BitwigMode.CLIP, newModel.mode)
    }
}