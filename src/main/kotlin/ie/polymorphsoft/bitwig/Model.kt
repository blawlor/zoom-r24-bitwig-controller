package ie.polymorphsoft.bitwig

/*
This extension uses the Elm Architecture (https://guide.elm-lang.org/architecture/) widely adopted
by other frameworks like React.

The Model represents an immutable internal state of the extension which interacts with incoming InputEvents
to produce new versions of the Model and optionally outgoing BitwigEvents (typically sent to the Bitbig API).

The update function interprets the incoming events into model changes and events. Note that there is no
knowledge of midi or the Bitwig API encoded in the model or the update function.
 */

val BANK_SIZE = 8
val NO_OF_BANKS = 8



/*
Input events (from controller and from BWS).
 */
sealed class InputEvent

data class ControllerInputEvent(val input:ControllerInputs, val action: ControllerInputActions, val value: Int = 0):InputEvent()

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


sealed class BWSInputEvent: InputEvent()
sealed class BWSTrackEvent(val track: Int):BWSInputEvent()
sealed class ContinuousBWSInputEvent(track: Int, val value: Int):BWSTrackEvent(track)
sealed class ToggleBWSInputEvent(track: Int, val on: Boolean):BWSTrackEvent(track)
class MutedEvent(track: Int, on: Boolean): ToggleBWSInputEvent(track, on)
class SoloedEvent(track: Int, on: Boolean): ToggleBWSInputEvent(track, on)
class ArmedEvent(track: Int, on: Boolean): ToggleBWSInputEvent(track, on)
class BankChanged(val bankStartIndex: Int): BWSInputEvent()

/*
Output events (to controller and to BWS)
In reality the Zoom R24 family does not accept midi commands,
so all output events are for BWS
 */
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

/*
 The Model, which holds any relevant state of the extension, in an immutable data class.
 */
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
        return "TrackState(on=$onIndices)"
    }
}

fun initModel() = Model(BitwigMode.TRACKS)
fun initMute() = TrackState(Array(BANK_SIZE* NO_OF_BANKS){false})
fun initSolo() = TrackState(Array(BANK_SIZE* NO_OF_BANKS){false})
fun initRec() = TrackState(Array(BANK_SIZE* NO_OF_BANKS){false})


/*
 The Update function, which interprets input events in the context of the current
 model, and returns a Pair: the updated model and an optional output event.
 */
@Synchronized
fun update(model: Model, inputEvent: InputEvent): Pair<Model, OutputEvent?> {
    return when (inputEvent) {
        is ControllerInputEvent -> updateForControllerEvents(model, inputEvent)
        is BWSInputEvent -> updateForBWSEvents(model, inputEvent)
    }
}

private fun updateForControllerEvents(model: Model, inputEvent: ControllerInputEvent): Pair<Model, OutputEvent?> {
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
        ControllerInputs.F5 -> inputEvent.isOnThen(model, Pair(model.toggleMode(), null))
        ControllerInputs.PLAY -> inputEvent.isOnThen(model, Play)//TODO Check to see if playing
        ControllerInputs.REC -> inputEvent.isOnThen(model, Record)
        ControllerInputs.STOP -> inputEvent.isOnThen(model, Stop)
        ControllerInputs.FF -> inputEvent.isOnThen(model, FastForward)
        ControllerInputs.REW -> inputEvent.isOnThen(model, Rewind)

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
        ControllerInputs.UP -> updateForDirection(model, inputEvent, ArrowUp, ArrowDown)
        ControllerInputs.DOWN -> updateForDirection(model, inputEvent, ArrowDown, ArrowUp)
        ControllerInputs.LEFT -> updateForDirection(model, inputEvent, ArrowLeft, ArrowRight)
        ControllerInputs.RIGHT -> updateForDirection(model, inputEvent, ArrowRight, ArrowLeft)

        ControllerInputs.BANK_DOWN -> inputEvent.isOnThen(model, Pair(model, if (model.currentBank>0) BankDown else null)) // Don't send the event if the bank was already 0
        ControllerInputs.BANK_UP -> inputEvent.isOnThen(model, Pair(model, BankUp))

        ControllerInputs.PMR1 -> updateForPMR(model, 0, inputEvent.action)
        ControllerInputs.PMR2 -> updateForPMR(model, 1, inputEvent.action)
        ControllerInputs.PMR3 -> updateForPMR(model, 2, inputEvent.action)
        ControllerInputs.PMR4 -> updateForPMR(model, 3, inputEvent.action)
        ControllerInputs.PMR5 -> updateForPMR(model, 4, inputEvent.action)
        ControllerInputs.PMR6 -> updateForPMR(model, 5, inputEvent.action)
        ControllerInputs.PMR7 -> updateForPMR(model, 6, inputEvent.action)
        ControllerInputs.PMR8 -> updateForPMR(model, 7, inputEvent.action)

    }
}



private fun updateForDirection(model: Model, inputEvent: ControllerInputEvent, normal: OutputEvent, shift: OutputEvent) =
    if (inputEvent.action == ControllerInputActions.ON) {
        if (!model.shift) Pair(model, normal)
        else Pair(model, shift) // Shift to reverse arrows (my up key is broken!!)
    } else {
        Pair(model, null)
    }

private fun updateForPMR(model: Model, track: Int, action: ControllerInputActions): Pair<Model, OutputEvent?> =
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
    return Pair(model, Mute(track, !model.muteState.isOn(track.toAbsoluteTrackNumber(model.currentBank))))
}

private fun toggleSoloState(model: Model, track: Int):Pair<Model, OutputEvent?> {
    return Pair(model, Solo(track, !model.soloState.isOn(track.toAbsoluteTrackNumber(model.currentBank))))
}

private fun toggleRecState(model: Model, track: Int):Pair<Model, OutputEvent?> {
    return Pair(model, Rec(track, !model.recState.isOn(track.toAbsoluteTrackNumber(model.currentBank))))
}


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


private fun updateForBWSEvents(model: Model, inputEvent: BWSInputEvent): Pair<Model, OutputEvent?> {
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

/*
 Utility functions
 */
private fun Int.toAbsoluteTrackNumber(bankNumber: Int) = (bankNumber* BANK_SIZE) + this

private fun ControllerInputEvent.isOnThen(model: Model, outputEvent: OutputEvent): Pair<Model, OutputEvent?> =
    when(action) {
        ControllerInputActions.ON -> Pair(model, outputEvent)
        ControllerInputActions.OFF -> Pair(model, null)
    }

private fun ControllerInputEvent.isOnThen(model: Model, result: Pair<Model, OutputEvent?>): Pair<Model, OutputEvent?> =
    when(action) {
        ControllerInputActions.ON -> result
        ControllerInputActions.OFF -> Pair(model, null)
    }