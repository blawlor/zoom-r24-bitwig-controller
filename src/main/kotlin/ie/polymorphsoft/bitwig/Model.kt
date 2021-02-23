package ie.polymorphsoft.bitwig

import com.bitwig.extension.api.util.midi.ShortMidiMessage
import ie.polymorphsoft.bitwig.zoom.InputActions
import ie.polymorphsoft.bitwig.zoom.InputEvent
import ie.polymorphsoft.bitwig.zoom.Inputs
import ie.polymorphsoft.bitwig.zoom.inputEvent

/*
This extension uses the Elm Architecture (https://guide.elm-lang.org/architecture/) widely adopted
by other frameworks like React.

The Model represents an immutable internal state of the extension which interacts with incoming InputEvents
to produce new versions of the Model and optionally outgoing BitwigEvents (typically sent to the Bitbig API).

The update function interprets the incoming events into model changes and events. Note that there is no
knowledge of midi or the Bitwig API encoded in the model or the update function.
 */

enum class BitwigMode {
    ARRANGE, CLIP, MIX
}

sealed class BitwigEvent
class SwitchToArrangeView() : BitwigEvent()
class SwitchToClipLauncherView(): BitwigEvent()
class SwitchToMixerView(): BitwigEvent()


// The internal state of the extension is modeled here.
data class Model(val mode: BitwigMode) {
    fun toggleMode(): Model {
        return when (mode) {
            BitwigMode.ARRANGE -> this.copy(mode = BitwigMode.CLIP)
            BitwigMode.CLIP -> this.copy(mode = BitwigMode.MIX)
            BitwigMode.MIX -> this.copy(mode = BitwigMode.ARRANGE)
        }
    }

    companion object {
        fun initModel() = Model(BitwigMode.ARRANGE)
        fun update(model: Model, inputEvent: InputEvent): Pair<Model, BitwigEvent?> {
            return when(inputEvent){
                InputEvent(Inputs.F5, InputActions.ON) -> {
                    val newModel = model.toggleMode()
                    return Pair(newModel, toggleViewEvent(newModel.mode))
                }
                else -> Pair(model, null)
            }
        }
    }
}



private fun toggleViewEvent(mode: BitwigMode): BitwigEvent {
    return when (mode) {
        BitwigMode.ARRANGE -> SwitchToArrangeView()
        BitwigMode.CLIP -> SwitchToClipLauncherView()
        BitwigMode.MIX -> SwitchToMixerView()
    }
}
