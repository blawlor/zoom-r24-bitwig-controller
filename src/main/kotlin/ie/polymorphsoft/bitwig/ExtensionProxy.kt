package ie.polymorphsoft.bitwig

import com.bitwig.extension.controller.api.ControllerHost

class ExtensionProxy(val host: ControllerHost) {
    private var model: Model = Model.initModel()

    init {
        host.println("Proxy initialized.")
    }


}