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
    private val cursorDevice: Device
    private val remoteControlsPage: CursorRemoteControlsPage
    init {
        host.println("Proxy initialized.")
        application = host.createApplication()
        transport = host.createTransport()
        arranger = host.createArranger(0)
        currentTrackBank = host.createTrackBank(BANK_SIZE, 0, 0)
        masterTrack = host.createMasterTrack(0)
        cursorTrack = host.createCursorTrack("R24_CURSOR_TRACK", "Cursor Track", 0,0, true)
        cursorDevice = cursorTrack.createCursorDevice("R24_CURSOR_DEVICE", "Cursor Device",0,CursorDeviceFollowMode.FOLLOW_SELECTION)
        remoteControlsPage = cursorDevice.createCursorRemoteControlsPage(8)

        currentTrackBank.scrollPosition().markInterested()
        currentTrackBank.scrollPosition().addValueObserver{ index -> doUpdate(TrackBankChanged(index))}

        for (trackNumber in 0..currentTrackBank.sizeOfBank - 1) {
            val track = currentTrackBank.getItemAt(trackNumber)
            track.exists().markInterested()
            val volume = track.volume()
            volume.setIndication(true)
            val pan = track.pan()
            pan.setIndication(true)
         }

        // This trackbank is used to iterate over all possible tracks and attach observers of mute,solo,arm
        val allTracks = host.createTrackBank(BANK_SIZE* NO_OF_BANKS,0,0)
        for (trackNumber in 0..allTracks.sizeOfBank-1) {
            val track = allTracks.getItemAt(trackNumber)
            track.exists().markInterested()
            track.position().markInterested()
            track.mute().addValueObserver { value -> trackObserver(track,  MutedEvent(track.position().get(), value)) }
            track.solo().addValueObserver { value -> trackObserver(track, SoloedEvent(track.position().get(), value)) }
            track.arm().addValueObserver { value -> trackObserver(track, ArmedEvent(track.position().get(), value)) }
            //As tracks are created in their positions, this event is triggered
            track.position().addValueObserver{pos -> if (pos > -1) initializeNewTrack(track)}
        }

        for (parameterIndex in 0..NO_OF_PARAMS-1) {
            remoteControlsPage.getParameter(parameterIndex).markInterested()
            remoteControlsPage.getParameter(parameterIndex).exists().markInterested()
        }

        cursorDevice.isEnabled().markInterested()
        cursorDevice.isWindowOpen().markInterested()

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

    private fun onMidi0(msg: ShortMidiMessage) {
        msg.inputEvent()?.let {
            doUpdate(it)
        }
    }

    private fun trackObserver(track: Track, event: BWSTrackEvent) {
        if (track.exists().get()) doUpdate(event)
    }

    @Synchronized
    private fun doUpdate(inputEvent: InputEvent) {
        host.println("Incoming event: $inputEvent")
        val (updatedModel, outputEvent) = update(model, inputEvent)
        model = updatedModel
        host.println("Outgoing event: $outputEvent")
        fireBitwigEvent(outputEvent) //Careful about loops here
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
                is Volume -> {
                    val track = currentTrackBank.getItemAt(it.track)
                    if (track.exists().get()) {
                        track.volume().set(it.level, 128)
                    } else {
                        host.showPopupNotification("Track does not exist")
                        host.println("Track " + track + " does not exist")
                    }
                }
                is MasterVolume -> masterTrack.volume().set(it.level, 128)
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
                TrackBankDown -> currentTrackBank.scrollPageBackwards()
                TrackBankUp -> currentTrackBank.scrollPageForwards()
                DeviceBankDown -> remoteControlsPage.selectPreviousPage(false)
                DeviceBankUp -> remoteControlsPage.selectNextPage(false)
                JogClockwise -> transport.incPosition(1.0, true)
                JogAntiClockwise -> transport.incPosition(-1.0, true)
                ZoomIn -> application.zoomIn()
                ZoomOut -> application.zoomOut()
                ArrowUp -> application.arrowKeyUp()
                ArrowDown -> application.arrowKeyDown()
                ArrowLeft -> application.arrowKeyLeft()
                ArrowRight -> application.arrowKeyRight()
                is Mute ->  currentTrackBank.getItemAt(it.track).mute().set(it.on)
                is Solo ->  currentTrackBank.getItemAt(it.track).solo().set(it.on)
                is Rec ->   currentTrackBank.getItemAt(it.track).arm().set(it.on)
                is ToggleMode -> switchModeIndication(it.mode)
                is Parameter ->{
                    val parameter = remoteControlsPage.getParameter(it.param)
                    if (parameter.exists().get()) {
                        parameter.set(it.value, 128)
                    } else {
                        host.showPopupNotification("Parameter does not exist")
                        host.println("Parameter " + it.param + " does not exist")
                    }
                }
            }
        }
    }

    private fun switchModeIndication(mode: Mode){
        when (mode) {
            Mode.TRACKS -> {
                setDeviceIndication(false)
                setTrackIndication(true)
            }
            Mode.DEVICES -> {
                setDeviceIndication(true)
                setTrackIndication(false)
            }
        }
    }

    private fun setTrackIndication(enable: Boolean){
        for (trackNumber in 0..currentTrackBank.sizeOfBank - 1) {
            val track = currentTrackBank.getItemAt(trackNumber)
            track.volume().setIndication(enable)
            track.pan().setIndication(enable)
        }
    }

    private fun setDeviceIndication(enable: Boolean){
        for(parameterIndex in 0..NO_OF_PARAMS-1) {
            remoteControlsPage.getParameter(parameterIndex).setIndication(enable)
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