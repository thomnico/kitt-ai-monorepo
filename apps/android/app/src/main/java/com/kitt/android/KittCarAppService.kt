package com.kitt.android

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

class KittCarAppService : CarAppService() {
    override fun onCreateSession(): Session {
        return KittSession()
    }

    override fun createHostValidator(): HostValidator {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }
}

class KittSession : Session() {
    override fun onCreateScreen(intent: android.content.Intent): androidx.car.app.Screen {
        return KittMainScreen(carContext)
    }
}

class KittMainScreen(carContext: androidx.car.app.CarContext) : androidx.car.app.Screen(carContext) {
    override fun onGetTemplate(): androidx.car.app.model.Template {
        val builder = androidx.car.app.model.Row.Builder()
        builder.setTitle("Welcome to KITT AI")
        builder.addText("Use voice commands to interact with KITT.")

        val paneBuilder = androidx.car.app.model.Pane.Builder()
        paneBuilder.addRow(builder.build())

        return androidx.car.app.model.PaneTemplate.Builder(paneBuilder.build())
            .setHeaderAction(androidx.car.app.model.Action.APP_ICON)
            .build()
    }
}
