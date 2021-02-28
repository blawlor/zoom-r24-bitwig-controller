package ie.polymorphsoft.bitwig

import com.bitwig.extension.api.util.midi.ShortMidiMessage
import com.bitwig.extension.callback.ShortMidiMessageReceivedCallback
import com.bitwig.extension.controller.api.*
import ie.polymorphsoft.bitwig.midi.inputEvent

/*
A class in Kotlin that allows us to continue using a Java class as the extension, but most of the real work is done here.
An instance of the Model is created.
Midi events are converted to InputControllerEvents and sent through the model.
Observers convert callbacks from BWS into BitwigInputEvents and sends them through the model.
(the update method is synchonized to ensure that one event at a time is processed)
Output events generated from the model are interpreted and converted into calls to the Bitwig API.
 */
class ExtensionProxy(val host: ControllerHost) {
    private var model: Model = initModel()
    private val application: Application
    private val transport: Transport
    private var arranger: Arranger
    private var currentTrackBank: TrackBank
    private var masterTrack: MasterTrack
    private var cursorTrack: CursorTrack
    private val userControls: UserControlBank
    init {
        host.println("Proxy initialized.")
        application = host.createApplication()
        transport = host.createTransport()
        arranger = host.createArranger(0)
        currentTrackBank = host.createTrackBank(BANK_SIZE, 0, 0)
        masterTrack = host.createMasterTrack(0)
        cursorTrack = host.createCursorTrack("R24_CURSOR_TRACK", "Cursor Track", 0,0, true)
        currentTrackBank.scrollPosition().markInterested()
        currentTrackBank.scrollPosition().addValueObserver{ index -> doUpdate(BankChanged(index))}

        for (trackNumber in 0..currentTrackBank.sizeOfBank - 1) {
            val track = currentTrackBank.getItemAt(trackNumber)
            track.exists().markInterested()
            val volume = track.volume()
            volume.setIndication(true)
            val pan = track.pan()
            pan.setIndication(true)
         }
        val allTracks = host.createTrackBank(BANK_SIZE* NO_OF_BANKS,0,0)
        for (trackNumber in 0..allTracks.sizeOfBank-1) {
            val track = allTracks.getItemAt(trackNumber)
            track.exists().markInterested()
            track.position().markInterested()
            track.mute().addValueObserver { value -> trackObserver(track,  MutedEvent(track.position().get(), value)) }
            track.solo().addValueObserver { value -> trackObserver(track, SoloedEvent(track.position().get(), value)) }
            track.arm().addValueObserver { value -> trackObserver(track, ArmedEvent(track.position().get(), value)) }
            track.position().addValueObserver{pos -> if (pos > -1) initializeNewTrack(track)}
        }
        masterTrack.volume().markInterested()
        masterTrack.pan().markInterested()
        currentTrackBank.followCursorTrack(cursorTrack)
        host.getMidiInPort(0).setMidiCallback(ShortMidiMessageReceivedCallback { msg: ShortMidiMessage -> onMidi0(msg) })
        host.getMidiInPort(0).setSysexCallback { data: String -> onSysex0(data) }
        userControls = host.createUserControls((HIGHEST_CC - LOWEST_CC + 1) * 16)
    }

    private fun initializeNewTrack(track: Track){
        doUpdate(MutedEvent(track.position().get(), track.mute().get()))
        doUpdate(SoloedEvent(track.position().get(), track.solo().get()))
        doUpdate(ArmedEvent(track.position().get(), track.arm().get()))
    }

    /** Called when we receive short MIDI message on port 0.  */
    private fun onMidi0(msg: ShortMidiMessage) {
        msg.inputEvent()?.let {
            doUpdate(it)
        }
    }

    private fun trackObserver(track: Track, event: BitwigTrackEvent){
        if (track.exists().get()){
            doUpdate(event)
        }
    }

    private fun doUpdate(inputEvent: InputEvent) {
        host.println("Incoming event: $inputEvent")
        val (updatedModel, bitwigEvent) = update(model, inputEvent)
        model = updatedModel
        fireBitwigEvent(bitwigEvent) //Careful about loops here
        host.println(model.toString())
    }

    /*
    Converts the Bitwig events coming from the Model updates into Bitwig actions.
    This is the equivalent of the Elm runtime.
     */
    private fun fireBitwigEvent(outputEvent: OutputEvent?) {
        outputEvent?.let{
            when (it) {
                is Play -> transport.play()
                is Record -> transport.record()
                is Stop -> transport.stop()
                is FastForward -> transport.fastForward()
                is Rewind -> transport.rewind()
                is Fader -> {
                    val track = currentTrackBank.getItemAt(it.track)
                    if (track.exists().get()) {
                        track.volume().set(it.level, 128)
                    } else {
                        host.showPopupNotification("Track does not exist")
                        host.println("Track " + track + " does not exist")
                    }
                }
                is MasterFader -> masterTrack.volume().set(it.level, 128)
                is Pan -> {
                    val track = currentTrackBank.getItemAt(it.track)
                    if (track.exists().get()) {
                        track.pan().set(it.level, 128)
                    } else {
                        host.showPopupNotification("Track does not exist")
                        host.println("Track " + track + " does not exist")
                    }
                }
                is MasterPan -> masterTrack.pan().set(it.level, 128)
                BankDown -> currentTrackBank.scrollPageBackwards()
                BankUp -> currentTrackBank.scrollPageForwards()
                JogClockwise -> transport.incPosition(1.0, true)
                JogAntiClockwise -> transport.incPosition(-1.0, true)
                ZoomIn -> application.zoomIn()
                ZoomOut -> application.zoomOut()
                ArrowUp -> application.arrowKeyUp()
                ArrowDown -> application.arrowKeyDown()
                ArrowLeft -> application.arrowKeyLeft()
                ArrowRight -> application.arrowKeyRight()
                is Mute -> {
                    host.println("Setting mute (${it.on}) on track ${it.track}")
                    var track = it.track
                    currentTrackBank.getItemAt(track).mute().set(it.on)
                }
                is Solo -> {
                    var track = it.track
                    currentTrackBank.getItemAt(track).solo().set(it.on)
                }
                is Rec -> {
                    var track = it.track
                    currentTrackBank.getItemAt(track).arm().set(it.on)
                }
            }
        }
    }

    /** Called when we receive sysex MIDI message on port 0.  */
    private fun onSysex0(data: String) {
        // MMC Transport Controls:
        if (data == "f07f7f0605f7") transport.rewind()
        else if (data == "f07f7f0604f7") transport.fastForward()
        else if (data == "f07f7f0601f7") transport.stop()
        else if (data == "f07f7f0602f7") transport.play()
        else if (data == "f07f7f0606f7") transport.record()
    }

    companion object {
        private const val LOWEST_CC = 0 // Lowest possible CC Controller
        private const val HIGHEST_CC = 512 // Highest possible CC Controller
    }
}