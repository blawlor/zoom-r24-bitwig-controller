package ie.polymorphsoft;

import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.ControllerHost;
import ie.polymorphsoft.bitwig.ExtensionProxy;

public class ZoomR24ControllerExtension extends ControllerExtension
{
   protected ZoomR24ControllerExtension(final ZoomR24ControllerExtensionDefinition definition, final ControllerHost host)
   {
      super(definition, host);
   }

   private ExtensionProxy extensionProxy;

   @Override
   public void init()
   {
      final ControllerHost host = getHost();
      extensionProxy = new ExtensionProxy(host);
      host.println("Entering init method");
//      host.addDeviceNameBasedDiscoveryPair(new String[]{"R24 Midi 1"}, new String[]{"R24 Midi 1"});

      // TODO: Perform your driver initialization here.
      // For now just show a popup notification for verification that it is running.
      host.showPopupNotification("Zoom R24 Controller Initialized");
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
