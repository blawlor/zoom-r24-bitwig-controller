package ie.polymorphsoft;

import com.bitwig.extension.api.util.midi.ShortMidiMessage;
import com.bitwig.extension.callback.ShortMidiMessageReceivedCallback;
import com.bitwig.extension.controller.api.*;
import com.bitwig.extension.controller.ControllerExtension;
import ie.polymorphsoft.bitwig.ExtensionProxy;

public class ZoomR24ControllerExtension extends ControllerExtension
{
   protected ZoomR24ControllerExtension(final ZoomR24ControllerExtensionDefinition definition, final ControllerHost host)
   {
      super(definition, host);
   }

   private static final int LOWEST_CC  = 0;                // Lowest possible CC Controller
   private static final int HIGHEST_CC = 512;              // Highest possible CC Controller


   private Application application;
   private Transport transport;
   private Arranger arranger;
   private TrackBank trackBank;
   private MasterTrack masterTrack;
   private UserControlBank userControls;
   private ExtensionProxy extensionProxy;

   @Override
   public void init()
   {
      final ControllerHost host = getHost();
      extensionProxy = new ExtensionProxy(host);
      host.println("Entering init method");
      application = host.createApplication();
      transport = host.createTransport();
      arranger = host.createArranger(0);
      trackBank = host.createTrackBank(512,512,512);
      masterTrack = host.createMasterTrack(512);

//      host.getMidiInPort(0).setMidiCallback((ShortMidiMessageReceivedCallback) msg -> onMidi0(msg));
//      host.getMidiInPort(0).setSysexCallback((String data) -> onSysex0(data));
      userControls = host.createUserControls((HIGHEST_CC - LOWEST_CC + 1)*16);

      // TODO: Perform your driver initialization here.
      // For now just show a popup notification for verification that it is running.
      host.showPopupNotification("Zoom R24 Controller Initialized**");
   }

   /** Called when we receive short MIDI message on port 0. */
   private void onMidi0(ShortMidiMessage msg)
   {
      // TODO: Implement your MIDI input handling code here.
   }


   /** Called when we receive sysex MIDI message on port 0. */
   private void onSysex0(final String data)
   {
      // MMC Transport Controls:
      if (data.equals("f07f7f0605f7"))
         transport.rewind();
      else if (data.equals("f07f7f0604f7"))
         transport.fastForward();
      else if (data.equals("f07f7f0601f7"))
         transport.stop();
      else if (data.equals("f07f7f0602f7"))
         transport.play();
      else if (data.equals("f07f7f0606f7"))
         transport.record();
   }

   @Override
   public void exit()
   {
      // TODO: Perform any cleanup once the driver exits
      // For now just show a popup notification for verification that it is no longer running.
      getHost().showPopupNotification("Zoom R24 Controller Exited");
   }

   @Override
   public void flush()
   {
      // TODO Send any updates you need here.
   }


}
