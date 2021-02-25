package ie.polymorphsoft.bitwig

import ie.polymorphsoft.bitwig.zoom.InputActions
import ie.polymorphsoft.bitwig.zoom.InputEvent
import ie.polymorphsoft.bitwig.zoom.Inputs

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
object SwitchToArrangeView : BitwigEvent()
object SwitchToClipLauncherView: BitwigEvent()
object SwitchToMixerView: BitwigEvent()
object Play: BitwigEvent()
object Stop:BitwigEvent()
object Record:BitwigEvent()
object FastForward: BitwigEvent()
object Rewind: BitwigEvent()
class Fader(val track: Int, val level: Int): BitwigEvent()
class MasterFader(val level: Int): BitwigEvent()


// The internal state of the extension is modeled here.
data class Model(val mode: BitwigMode,
                 val shift: Boolean = false,
                 val ctrl: Boolean = false,
                 val currentBank: Int = 1) {
    fun toggleMode(): Model {
        return when (mode) {
            BitwigMode.ARRANGE -> this.copy(mode = BitwigMode.CLIP)
            BitwigMode.CLIP -> this.copy(mode = BitwigMode.MIX)
            BitwigMode.MIX -> this.copy(mode = BitwigMode.ARRANGE)
        }
    }
    fun shiftOn() = this.copy(shift = true)
    fun shiftOff() = this.copy(shift = false)
    fun ctrlOn() = this.copy(ctrl = true)
    fun ctrlOff() = this.copy(ctrl = false)
    fun bankUp() = if (currentBank<3) this.copy(currentBank = currentBank+1) else this
    fun bankDown() = if (currentBank>1) this.copy(currentBank = currentBank-1) else this
}

fun initModel() = Model(BitwigMode.ARRANGE)

fun update(model: Model, inputEvent: InputEvent): Pair<Model, BitwigEvent?> {
    return when (inputEvent.input){
        Inputs.F1 ->
            when (inputEvent.action) {
                InputActions.ON -> return Pair(model.shiftOn(), null)
                InputActions.OFF -> return Pair(model.shiftOff(), null)
            }
        Inputs.F2 ->
            when(inputEvent.action) {
                InputActions.ON -> return Pair(model.ctrlOn(), null)
                InputActions.OFF -> return Pair(model.ctrlOff(), null)
            }
        Inputs.F3 -> return Pair(model, null)
        Inputs.F4 -> return Pair(model, null)
        Inputs.F5 ->
            when(inputEvent.action) {
                InputActions.ON -> {
                    val newModel = model.toggleMode()
                    return Pair(newModel, toggleViewEvent(newModel.mode))
                }
                InputActions.OFF -> return Pair(model, null)
            }
        Inputs.PLAY -> //TODO Check to see if playing
            when(inputEvent.action) {
                InputActions.ON -> return Pair(model, Play)
                InputActions.OFF -> return Pair(model, null)
            }
        Inputs.REC ->
            when(inputEvent.action) {
                InputActions.ON -> return Pair(model, Record)
                InputActions.OFF -> return Pair(model, null)
            }
        Inputs.STOP ->
            when(inputEvent.action) {
                InputActions.ON -> return Pair(model, Stop)
                InputActions.OFF -> return Pair(model, null)
            }
        Inputs.FF ->
            when(inputEvent.action) {
                InputActions.ON -> return Pair(model, FastForward)
                InputActions.OFF -> return Pair(model, null)
            }
        Inputs.REW ->
            when(inputEvent.action) {
                InputActions.ON -> return Pair(model, Rewind)
                InputActions.OFF -> return Pair(model, null)
            }
        Inputs.FADER1 -> fader(model, 0, inputEvent.action, inputEvent.value)
        Inputs.FADER2 -> fader(model, 1, inputEvent.action, inputEvent.value)
        Inputs.FADER3 -> fader(model, 2, inputEvent.action, inputEvent.value)
        Inputs.FADER4 -> fader(model, 3, inputEvent.action, inputEvent.value)
        Inputs.FADER5 -> fader(model, 4, inputEvent.action, inputEvent.value)
        Inputs.FADER6 -> fader(model, 5, inputEvent.action, inputEvent.value)
        Inputs.FADER7 -> fader(model, 6, inputEvent.action, inputEvent.value)
        Inputs.FADER8 -> fader(model, 7, inputEvent.action, inputEvent.value)
        Inputs.MASTER_FADER ->
            when(inputEvent.action) {
                InputActions.ON -> Pair(model, MasterFader(inputEvent.value))
                InputActions.OFF -> Pair(model, null)
            }
        Inputs.JogWheel -> Pair(model, null)
        Inputs.UP -> Pair(model, null)
        Inputs.DOWN -> Pair(model, null)
        Inputs.LEFT -> Pair(model, null)
        Inputs.RIGHT -> Pair(model, null)
        Inputs.BANK_DOWN -> Pair(model, null)
        Inputs.BANK_UP -> Pair(model, null)
        Inputs.PMR1 -> Pair(model, null)
        Inputs.PMR2 -> Pair(model, null)
        Inputs.PMR3 -> Pair(model, null)
        Inputs.PMR4 -> Pair(model, null)
        Inputs.PMR5 -> Pair(model, null)
        Inputs.PMR6 -> Pair(model, null)
        Inputs.PMR7 -> Pair(model, null)
        Inputs.PMR8 -> Pair(model, null)
    }
}

private fun fader(model: Model, track: Int, action: InputActions, value: Int): Pair<Model, BitwigEvent?> =
        when(action) {
            InputActions.ON -> Pair(model, Fader(track, value))
            InputActions.OFF -> Pair(model, null)
        }

private fun toggleViewEvent(mode: BitwigMode): BitwigEvent {
    return when (mode) {
        BitwigMode.ARRANGE -> SwitchToArrangeView
        BitwigMode.CLIP -> SwitchToClipLauncherView
        BitwigMode.MIX -> SwitchToMixerView
    }
}
