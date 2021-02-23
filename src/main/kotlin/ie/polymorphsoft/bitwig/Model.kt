package ie.polymorphsoft

import com.bitwig.extension.api.util.midi.ShortMidiMessage
import ie.polymorphsoft.bitwig.zoom.InputActions
import ie.polymorphsoft.bitwig.zoom.InputEvent
import ie.polymorphsoft.bitwig.zoom.Inputs
import ie.polymorphsoft.bitwig.zoom.inputEvent

enum class BitwigMode {
    ARRANGE, CLIP, MIX
}



sealed class BitwigEvent
class SwitchToArrangeView() : BitwigEvent()
class SwitchToClipLauncherView(): BitwigEvent()
class SwitchToMixerView(): BitwigEvent()

data class Model(val mode: BitwigMode) {
    fun toggleMode(): Model {
        return when (mode) {
            BitwigMode.ARRANGE -> this.copy(mode = BitwigMode.CLIP)
            BitwigMode.CLIP -> this.copy(mode = BitwigMode.MIX)
            BitwigMode.MIX -> this.copy(mode = BitwigMode.ARRANGE)
        }
    }
}

fun controller(model: Model, midiMessage: ShortMidiMessage): Pair<Model, BitwigEvent?> {
    return when(midiMessage.inputEvent()){
        InputEvent(Inputs.F5, InputActions.ON) ->  Pair(model.toggleMode(),null)
        else -> Pair(model, null)
    }
}