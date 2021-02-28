package ie.polymorphsoft.bitwig

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


enum class ControllerInputs {
    REW, FF, STOP, PLAY, REC,
    F1, F2, F3, F4, F5,
    JogWheel, UP, DOWN, LEFT, RIGHT,
    FADER1, FADER2, FADER3, FADER4, FADER5, FADER6, FADER7, FADER8, MASTER_FADER,
    PMR1, PMR2, PMR3, PMR4, PMR5, PMR6, PMR7, PMR8,
    BANK_DOWN, BANK_UP,
}

enum class ControllerInputActions {
    ON, OFF //For JogWheel, ON = Clockwise, OFF = Anticlockwise
}

sealed class InputEvent

data class ControllerInputEvent(val input:ControllerInputs, val action: ControllerInputActions, val value: Int = 0):InputEvent()

sealed class BitwigInputEvent: InputEvent()
sealed class BitwigTrackEvent(val track: Int):BitwigInputEvent()
sealed class ContinuousBitwigInputEvent(track: Int, val value: Int):BitwigTrackEvent(track)
sealed class ToggleBitwigInputEvent(track: Int, val on: Boolean):BitwigTrackEvent(track)
class MutedEvent(track: Int, on: Boolean): ToggleBitwigInputEvent(track, on)
class SoloedEvent(track: Int, on: Boolean): ToggleBitwigInputEvent(track, on)
class ArmedEvent(track: Int, on: Boolean): ToggleBitwigInputEvent(track, on)
class BankChanged(val bankStartIndex: Int): BitwigInputEvent()

// Because the Zoom R24 accepts no MIDI input (e.g. to set LEDs), all output events are effectively for Bitwig
sealed class OutputEvent
object Play: OutputEvent()
object Stop:OutputEvent()
object Record:OutputEvent()
object FastForward: OutputEvent()
object Rewind: OutputEvent()
object BankUp: OutputEvent()
object BankDown: OutputEvent()
object JogClockwise: OutputEvent()
object JogAntiClockwise: OutputEvent();
object ZoomIn: OutputEvent()
object ZoomOut: OutputEvent()
object ArrowUp: OutputEvent()
object ArrowDown: OutputEvent()
object ArrowLeft: OutputEvent()
object ArrowRight: OutputEvent()
class Fader(val track: Int, val level: Int): OutputEvent()
class Pan(val track: Int, val level: Int): OutputEvent()
class MasterFader(val level: Int): OutputEvent()
class MasterPan(val level: Int): OutputEvent()
class Mute(val track: Int, val on: Boolean = true): OutputEvent()
class Solo(val track: Int, val on: Boolean = true): OutputEvent()
class Rec(val track: Int, val on: Boolean = true): OutputEvent()

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
            BitwigMode.TRACKS -> this.copy(mode = BitwigMode.DEVICES)
            BitwigMode.DEVICES -> this.copy(mode = BitwigMode.TRACKS)
        }
    }
    fun shiftOn() = this.copy(shift = true)
    fun shiftOff() = this.copy(shift = false)
    fun ctrlOn() = this.copy(ctrl = true)
    fun ctrlOff() = this.copy(ctrl = false)
    override fun toString(): String {
        return "Model(mode=$mode, shift=$shift, ctrl=$ctrl, currentBank=$currentBank, \nmuteState=$muteState, \nsoloState=$soloState, \nrecState=$recState\n)"
    }
}

enum class BitwigMode {
    TRACKS, DEVICES
}

data class TrackState(val state: Array<Boolean> ) {
    fun isOn(index: Int) = state[index]
    private fun setCopy(index: Int, value: Boolean) = run {
        val newArray = state.copyOf()
        newArray[index] = value
        this.copy(state = newArray)
    }
    fun on(index: Int) = setCopy(index, true)
    fun off(index: Int) = setCopy(index, false)
    fun toggle(index: Int) = setCopy(index, !state[index])
    fun set(index: Int, on:Boolean) = setCopy(index, on)
    override fun toString(): String {
        val onIndices = state
            .mapIndexed { index, b -> Pair(index, b) }
            .filter { p -> p.second }
            .map {p -> p.first.toString()}
        return "TrackState(state=$onIndices)"
    }


}

fun initModel() = Model(BitwigMode.TRACKS)
fun initMute() = TrackState(Array(BANK_SIZE* NO_OF_BANKS){false})
fun initSolo() = TrackState(Array(BANK_SIZE* NO_OF_BANKS){false})
fun initRec() = TrackState(Array(BANK_SIZE* NO_OF_BANKS){false})

@Synchronized
fun update(model: Model, inputEvent: InputEvent): Pair<Model, OutputEvent?> {
    return when (inputEvent) {
        is ControllerInputEvent -> updateForController(model, inputEvent)
        is BitwigInputEvent -> updateForBitwig(model, inputEvent)
    }
}

private fun updateForController(model: Model, inputEvent: ControllerInputEvent): Pair<Model, OutputEvent?> {
    return when (inputEvent.input){
        ControllerInputs.F1 ->
            when (inputEvent.action) {
                ControllerInputActions.ON -> Pair(model.shiftOn(), null)
                ControllerInputActions.OFF -> Pair(model.shiftOff(), null)
            }
        ControllerInputs.F2 ->
            when(inputEvent.action) {
                ControllerInputActions.ON -> Pair(model.ctrlOn(), null)
                ControllerInputActions.OFF -> Pair(model.ctrlOff(), null)
            }
        ControllerInputs.F3 -> Pair(model, null) //TODO Assign to something
        ControllerInputs.F4 -> Pair(model, null) // TODO Assign to something
        ControllerInputs.F5 -> onOrNothing(model, inputEvent, Pair(model.toggleMode(), null))
        ControllerInputs.PLAY -> onOrNothing(model, inputEvent, Play)//TODO Check to see if playing
        ControllerInputs.REC ->onOrNothing(model, inputEvent, Record)
        ControllerInputs.STOP ->onOrNothing(model, inputEvent, Stop)
        ControllerInputs.FF -> onOrNothing(model, inputEvent, FastForward)
        ControllerInputs.REW -> onOrNothing(model, inputEvent, Rewind)

        ControllerInputs.FADER1 -> updateFader(model, 0, inputEvent.action, inputEvent.value)
        ControllerInputs.FADER2 -> updateFader(model, 1, inputEvent.action, inputEvent.value)
        ControllerInputs.FADER3 -> updateFader(model, 2, inputEvent.action, inputEvent.value)
        ControllerInputs.FADER4 -> updateFader(model, 3, inputEvent.action, inputEvent.value)
        ControllerInputs.FADER5 -> updateFader(model, 4, inputEvent.action, inputEvent.value)
        ControllerInputs.FADER6 -> updateFader(model, 5, inputEvent.action, inputEvent.value)
        ControllerInputs.FADER7 -> updateFader(model, 6, inputEvent.action, inputEvent.value)
        ControllerInputs.FADER8 -> updateFader(model, 7, inputEvent.action, inputEvent.value)

        ControllerInputs.MASTER_FADER -> updateMasterFader(model, inputEvent.action, inputEvent.value)

        ControllerInputs.JogWheel -> updateJog(model, inputEvent.action)
        ControllerInputs.UP -> direction(model, inputEvent, ArrowUp, ArrowDown)
        ControllerInputs.DOWN -> direction(model, inputEvent, ArrowDown, ArrowUp)
        ControllerInputs.LEFT -> direction(model, inputEvent, ArrowLeft, ArrowRight)
        ControllerInputs.RIGHT -> direction(model, inputEvent, ArrowRight, ArrowLeft)

        ControllerInputs.BANK_DOWN -> onOrNothing(model, inputEvent, Pair(model, if (model.currentBank>0) BankDown else null)) // Don't send the event if the bank was already 0
        ControllerInputs.BANK_UP -> onOrNothing(model, inputEvent, Pair(model, BankUp))

        ControllerInputs.PMR1 -> updatePMR(model, 0, inputEvent.action)
        ControllerInputs.PMR2 -> updatePMR(model, 1, inputEvent.action)
        ControllerInputs.PMR3 -> updatePMR(model, 2, inputEvent.action)
        ControllerInputs.PMR4 -> updatePMR(model, 3, inputEvent.action)
        ControllerInputs.PMR5 -> updatePMR(model, 4, inputEvent.action)
        ControllerInputs.PMR6 -> updatePMR(model, 5, inputEvent.action)
        ControllerInputs.PMR7 -> updatePMR(model, 6, inputEvent.action)
        ControllerInputs.PMR8 -> updatePMR(model, 7, inputEvent.action)

    }
}

private fun updateForBitwig(model: Model, inputEvent: BitwigInputEvent): Pair<Model, OutputEvent?> {
    return when (inputEvent) {
        is MutedEvent -> {
            val newMuteState = model.muteState.set(inputEvent.track, inputEvent.on)//if (inputEvent.on) model.muteState.on(track) else model.muteState.off(track)
            Pair(model.copy(muteState = newMuteState), null)
        }
        is SoloedEvent -> {
            val track = inputEvent.track
            val newSoloState = if (inputEvent.on) model.soloState.on(track) else model.soloState.off(track)
            Pair(model.copy(soloState = newSoloState), null)
        }
        is ArmedEvent -> {
            val track = inputEvent.track
            val newRecState = if (inputEvent.on) model.recState.on(track) else model.recState.off(track)
            Pair(model.copy(recState = newRecState), null)
        }
        is BankChanged -> Pair(model.copy(currentBank = inputEvent.bankStartIndex/8), null)
    }
}


private fun direction(model: Model, inputEvent: ControllerInputEvent, normal: OutputEvent, shift: OutputEvent) =
    if (inputEvent.action == ControllerInputActions.ON) {
        if (!model.shift) Pair(model, normal)
        else Pair(model, shift) // Shift to reverse arrows (my up key is broken!!)
    } else {
        Pair(model, null)
    }

private fun updatePMR(model: Model, track: Int, action: ControllerInputActions): Pair<Model, OutputEvent?> =
    if (action == ControllerInputActions.ON){
        if (!model.shift && !model.ctrl){
            toggleMuteState(model, track)
        } else if(model.shift && !model.ctrl){
            toggleSoloState(model, track)
        } else if (!model.shift && model.ctrl) {
            toggleRecState(model, track)
        } else {
            Pair(model, null)
        }
    } else {
        Pair(model,null)
    }

private fun toggleMuteState(model: Model, track: Int):Pair<Model, OutputEvent?> {
    return Pair(model, Mute(track, !model.muteState.isOn(absoluteTrackNumber(model.currentBank, track))))
}

private fun toggleSoloState(model: Model, track: Int):Pair<Model, OutputEvent?> {
    return Pair(model, Solo(track, !model.soloState.isOn(absoluteTrackNumber(model.currentBank, track))))
}

private fun toggleRecState(model: Model, track: Int):Pair<Model, OutputEvent?> {
    return Pair(model, Rec(track, !model.recState.isOn(absoluteTrackNumber(model.currentBank, track))))
}

private fun absoluteTrackNumber(bankNumber: Int, relativeTrackNumber:Int) = (bankNumber* BANK_SIZE) + relativeTrackNumber

private fun updateJog(model: Model, action: ControllerInputActions): Pair<Model, OutputEvent?> {
    return if (model.shift){
        when(action){
            ControllerInputActions.ON -> Pair(model, ZoomIn)
            ControllerInputActions.OFF -> Pair(model, ZoomOut)
        }
    } else {
        when (action) {
            ControllerInputActions.ON -> Pair(model, JogClockwise)
            ControllerInputActions.OFF -> Pair(model, JogAntiClockwise)
        }
    }
}

private fun updateFader(model: Model, track: Int, action: ControllerInputActions, value: Int): Pair<Model, OutputEvent?> =
        when(action) {
            ControllerInputActions.ON -> if (model.shift) Pair(model, Pan(track, value)) else Pair(model, Fader(track, value))
            ControllerInputActions.OFF -> Pair(model, null)
        }

private fun updateMasterFader(model: Model, action: ControllerInputActions, value: Int): Pair<Model, OutputEvent?> =
    when(action) {
        ControllerInputActions.ON -> if (model.shift) Pair(model, MasterPan(value)) else Pair(model, MasterFader(value))
        ControllerInputActions.OFF -> Pair(model, null)
    }

private fun onOrNothing(model: Model, inputEvent: ControllerInputEvent, outputEvent: OutputEvent):Pair<Model, OutputEvent?> =
    when(inputEvent.action) {
        ControllerInputActions.ON -> Pair(model, outputEvent)
        ControllerInputActions.OFF -> Pair(model, null)
    }

private fun onOrNothing(model: Model, inputEvent: ControllerInputEvent, result: Pair<Model, OutputEvent?>):Pair<Model, OutputEvent?> =
    when(inputEvent.action) {
        ControllerInputActions.ON -> result
        ControllerInputActions.OFF -> Pair(model, null)
    }