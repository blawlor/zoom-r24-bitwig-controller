package ie.polymorphsoft.bitwig

import com.bitwig.extension.api.util.midi.ShortMidiMessage
import com.bitwig.extension.callback.BooleanValueChangedCallback
import com.bitwig.extension.callback.DoubleValueChangedCallback
import com.bitwig.extension.callback.ShortMidiMessageReceivedCallback
import com.bitwig.extension.controller.api.*
import ie.polymorphsoft.bitwig.zoom.inputEvent

/*
A class in Kotlin that allows us to continue using a Java class as the extension.
 */
class ExtensionProxy(val host: ControllerHost) {
    private var model: Model = initModel()
    private val application: Application
    private val transport: Transport
    private var arranger: Arranger
    private var trackBank: TrackBank
    private var masterTrack: MasterTrack
    private val userControls: UserControlBank
    init {
        host.println("Proxy initialized.")
        application = host.createApplication()
        transport = host.createTransport()
        arranger = host.createArranger(0)
        trackBank = host.createMainTrackBank(BANK_SIZE, 0, 0)
        masterTrack = host.createMasterTrack(512)

        for (bankNumber in 0..NO_OF_BANKS-1) {
            for (trackNumber in 0..trackBank.sizeOfBank - 1) {
                val track = trackBank.getItemAt(trackNumber)
                track.exists().markInterested()
                val volume = track.volume()
                volume.markInterested()
                volume.setIndication(true)
                val pan = track.pan()
                pan.markInterested()
                pan.setIndication(true)
                track.mute().markInterested()
                track.solo().markInterested()
                track.arm().markInterested()
            }
            trackBank.scrollPageForwards()
        }
        trackBank.scrollPosition().set(0)
        host.getMidiInPort(0).setMidiCallback(ShortMidiMessageReceivedCallback { msg: ShortMidiMessage -> onMidi0(msg) })
        host.getMidiInPort(0).setSysexCallback { data: String -> onSysex0(data) }
        userControls = host.createUserControls((HIGHEST_CC - LOWEST_CC + 1) * 16)
    }

    /** Called when we receive short MIDI message on port 0.  */
    private fun onMidi0(msg: ShortMidiMessage) {
        msg.inputEvent()?.let {
            val (updatedModel, bitwigEvent) = update(model, it)
            model = updatedModel
            fireBitwigEvent(bitwigEvent)
        }
    }

    /*
    Converts the Bitwig events coming from the Model updates into Bitwig actions.
    This is the equivalent of the Elm runtime.
     */
    private fun fireBitwigEvent(bitwigEvent: BitwigEvent?) {
        bitwigEvent?.let{
            when (it) {
                is SwitchToArrangeView -> {
                    host.showPopupNotification("Arrange")
//                    application.
                }
                is SwitchToClipLauncherView -> host.showPopupNotification("Clip")
                is SwitchToMixerView -> host.showPopupNotification("Mixer")
                is Play -> transport.play()
                is Record -> transport.record()
                is Stop -> transport.stop()
                is FastForward -> transport.fastForward()
                is Rewind -> transport.rewind()
                is Fader -> {
                    val track = trackBank.getItemAt(it.track)
                    if (track.exists().get()) {
                        if (model.shift){
                            val pan = track.pan();
                            pan.set(it.level, 128);
                        } else {
                            val volume = track.volume();
                            volume.set(it.level, 128);
                        }
                    } else {
                        host.showPopupNotification("Track " + track + " does not exist")
                        host.println("Track " + track + " does not exist")
                    }
                }
                ShiftOn -> host.println("Shift On")
                ShiftOff -> host.println("Shift Off")
                CtrlOn -> host.println("Ctrl On")
                CtrlOff -> host.println("Ctrl Off")
                BankDown -> trackBank.scrollPageBackwards()
                BankUp -> trackBank.scrollPageForwards()

            }
        }
    }

    private fun trackNumber(model: Model, track: Int) = (8 * model.currentBank - 7) + track - 1

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