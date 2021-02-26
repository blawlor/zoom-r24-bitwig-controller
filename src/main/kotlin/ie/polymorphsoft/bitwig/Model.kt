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

//TODO Move to companion object?
val BANK_SIZE = 8
val NO_OF_BANKS = 8

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
object ShiftOn: BitwigEvent()
object ShiftOff: BitwigEvent()
object CtrlOn: BitwigEvent()
object CtrlOff: BitwigEvent()
object BankUp: BitwigEvent()
object BankDown: BitwigEvent()
object JogClockwise: BitwigEvent()
object JogAntiClockwise: BitwigEvent();
object ZoomIn: BitwigEvent()
object ZoomOut: BitwigEvent()
object ArrowUp: BitwigEvent()
object ArrowDown: BitwigEvent()
object ArrowLeft: BitwigEvent()
object ArrowRight: BitwigEvent()
class Fader(val track: Int, val level: Int): BitwigEvent()
class MasterFader(val level: Int): BitwigEvent()
class Mute(val track: Int, val on: Boolean = true): BitwigEvent()
class Solo(val track: Int, val on: Boolean = true): BitwigEvent()
class Rec(val track: Int, val on: Boolean = true): BitwigEvent()

// The internal state of the extension is modeled here.
data class Model(val mode: BitwigMode,
                 val shift: Boolean = false,
                 val ctrl: Boolean = false,
                 val currentBank: Int = 0,
                 val muteState:  TrackState = initMute(),
                 val soloState: TrackState = initSolo(),
                 val recState: TrackState = initRec()) {
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
    fun bankUp() = if (currentBank< NO_OF_BANKS-1) this.copy(currentBank = currentBank+1) else this
    fun bankDown() = if (currentBank>0) this.copy(currentBank = currentBank-1) else this
}

data class TrackState(val state: Array<Boolean> ) {
    fun isOn(index: Int) = state[index]
    private fun set(index: Int, value: Boolean) = run {
        val newArray = state.copyOf()
        newArray[index] = value
        this.copy(state = newArray)
    }
    fun on(index: Int) = set(index, true)
    fun off(index: Int) = set(index, false)
    fun toggle(index: Int) = set(index, !state[index])
}

fun initModel() = Model(BitwigMode.ARRANGE)
fun initMute() = TrackState(Array(BANK_SIZE* NO_OF_BANKS){false})
fun initSolo() = TrackState(Array(BANK_SIZE* NO_OF_BANKS){false})
fun initRec() = TrackState(Array(BANK_SIZE* NO_OF_BANKS){false})

fun update(model: Model, inputEvent: InputEvent): Pair<Model, BitwigEvent?> {
    return when (inputEvent.input){
        Inputs.F1 ->
            when (inputEvent.action) {
                InputActions.ON -> Pair(model.shiftOn(), ShiftOn)
                InputActions.OFF -> Pair(model.shiftOff(), ShiftOff)
            }
        Inputs.F2 ->
            when(inputEvent.action) {
                InputActions.ON -> Pair(model.ctrlOn(), CtrlOn)
                InputActions.OFF -> Pair(model.ctrlOff(), CtrlOff)
            }
        Inputs.F3 -> Pair(model, null)
        Inputs.F4 -> Pair(model, null)
        Inputs.F5 ->
            when(inputEvent.action) {
                InputActions.ON -> {
                    val newModel = model.toggleMode()
                    Pair(newModel, toggleViewEvent(newModel.mode))
                }
                InputActions.OFF -> Pair(model, null)
            }
        Inputs.PLAY -> //TODO Check to see if playing
            when(inputEvent.action) {
                InputActions.ON -> Pair(model, Play)
                InputActions.OFF -> Pair(model, null)
            }
        Inputs.REC ->
            when(inputEvent.action) {
                InputActions.ON -> Pair(model, Record)
                InputActions.OFF -> Pair(model, null)
            }
        Inputs.STOP ->
            when(inputEvent.action) {
                InputActions.ON -> Pair(model, Stop)
                InputActions.OFF -> Pair(model, null)
            }
        Inputs.FF ->
            when(inputEvent.action) {
                InputActions.ON -> Pair(model, FastForward)
                InputActions.OFF -> Pair(model, null)
            }
        Inputs.REW ->
            when(inputEvent.action) {
                InputActions.ON -> Pair(model, Rewind)
                InputActions.OFF -> Pair(model, null)
            }
        Inputs.FADER1 -> updateFader(model, 0, inputEvent.action, inputEvent.value)
        Inputs.FADER2 -> updateFader(model, 1, inputEvent.action, inputEvent.value)
        Inputs.FADER3 -> updateFader(model, 2, inputEvent.action, inputEvent.value)
        Inputs.FADER4 -> updateFader(model, 3, inputEvent.action, inputEvent.value)
        Inputs.FADER5 -> updateFader(model, 4, inputEvent.action, inputEvent.value)
        Inputs.FADER6 -> updateFader(model, 5, inputEvent.action, inputEvent.value)
        Inputs.FADER7 -> updateFader(model, 6, inputEvent.action, inputEvent.value)
        Inputs.FADER8 -> updateFader(model, 7, inputEvent.action, inputEvent.value)
        Inputs.MASTER_FADER ->
            when(inputEvent.action) {
                InputActions.ON -> Pair(model, MasterFader(inputEvent.value))
                InputActions.OFF -> Pair(model, null)
            }
        Inputs.JogWheel -> updateJog(model, inputEvent.action)
        Inputs.UP -> if (!model.shift) Pair(model, ArrowUp) else Pair(model, ArrowDown) // Shift to reverse arrows (my up key is broken!!)
        Inputs.DOWN -> if (!model.shift) Pair(model, ArrowDown) else Pair(model, ArrowUp)
        Inputs.LEFT -> if (!model.shift) Pair(model, ArrowLeft) else Pair(model, ArrowRight)
        Inputs.RIGHT -> if (!model.shift) Pair(model, ArrowRight) else Pair(model, ArrowLeft)
        Inputs.BANK_DOWN -> Pair(model.bankDown(), if (model.currentBank>0) BankDown else null) // Don't send the event if the bank was already 0
        Inputs.BANK_UP -> Pair(model.bankUp(), BankUp)
        Inputs.PMR1 -> updatePMR(model, 0)
        Inputs.PMR2 -> updatePMR(model, 1)
        Inputs.PMR3 -> updatePMR(model, 2)
        Inputs.PMR4 -> updatePMR(model, 3)
        Inputs.PMR5 -> updatePMR(model, 4)
        Inputs.PMR6 -> updatePMR(model, 5)
        Inputs.PMR7 -> updatePMR(model, 6)
        Inputs.PMR8 -> updatePMR(model, 7)
    }
}

private fun updatePMR(model: Model, track: Int): Pair<Model, BitwigEvent?> {
    return if (!model.shift && !model.ctrl){
        toggleMuteState(model, track)
    } else if(model.shift && !model.ctrl){
        toggleSoloState(model, track)
    } else if (!model.shift && model.ctrl) {
        toggleRecState(model, track)
    } else {
        Pair(model, null)
    }
}

private fun toggleMuteState(model: Model, track: Int):Pair<Model, BitwigEvent?> {
    val newMuteState = model.muteState.toggle(track)
    return Pair(model.copy(muteState = newMuteState), Mute(track, newMuteState.isOn(track)))
}

private fun toggleSoloState(model: Model, track: Int):Pair<Model, BitwigEvent?> {
    val newSoloState = model.soloState.toggle(track)
    return Pair(model.copy(soloState = newSoloState), Solo(track, newSoloState.isOn(track)))
}

private fun toggleRecState(model: Model, track: Int):Pair<Model, BitwigEvent?> {
    val newRecState = model.recState.toggle(track)
    return Pair(model.copy(recState = newRecState), Rec(track, newRecState.isOn(track)))
}

private fun updateJog(model: Model, action: InputActions): Pair<Model, BitwigEvent?> {
    return if (model.shift){
        when(action){
            InputActions.ON -> Pair(model, ZoomIn)
            InputActions.OFF -> Pair(model, ZoomOut)
        }
    } else {
        when (action) {
            InputActions.ON -> Pair(model, JogClockwise)
            InputActions.OFF -> Pair(model, JogAntiClockwise)
        }
    }
}

private fun updateFader(model: Model, track: Int, action: InputActions, value: Int): Pair<Model, BitwigEvent?> =
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
