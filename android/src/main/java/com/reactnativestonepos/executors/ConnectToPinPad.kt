package com.reactnativestonepos.executors

import android.app.Activity
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.reactnativestonepos.StonePosModule
import com.reactnativestonepos.helpers.CodedException
import com.reactnativestonepos.helpers.StoneTransactionHelpers
import com.reactnativestonepos.helpers.writableMapOf
import stone.application.enums.Action
import stone.application.interfaces.StoneActionCallback
import stone.providers.BluetoothConnectionProvider
import stone.utils.PinpadObject

class ConnectToPinPad(
  reactApplicationContext: ReactApplicationContext,
  currentActivity: Activity?
) : BaseExecutor(reactApplicationContext, currentActivity) {
  fun executeAction(
    pinpadName: String,
    pinpadMacAddress: String,
    dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean,
    progressCallbackEventName: String,
    promise: Promise
  ) {
    checkSDKInitializedAndHandleExceptions(promise) {
      if (StonePosModule.currentUserList.isNullOrEmpty()) {
        throw CodedException("401", "You need to activate the terminal first")
      }

      if (StoneTransactionHelpers.isRunningInPOS(reactApplicationContext)) {
        throw CodedException("402", "You cannot connect to a pinpad in POS")
      }

      val transactionProvider = BluetoothConnectionProvider(
        if (useDefaultUI) {
          currentActivity!!
        } else {
          reactApplicationContext
        }, PinpadObject(pinpadName, pinpadMacAddress)
      )

      transactionProvider.useDefaultUI(useDefaultUI)
      transactionProvider.dialogMessage = if (dialogMessage.isNullOrEmpty()) {
        "Criando conexao com o pinpad selecionado"
      } else {
        dialogMessage
      }

      transactionProvider.dialogTitle = if (dialogTitle.isNullOrEmpty()) {
        "Conectando..."
      } else {
        dialogTitle
      }

      transactionProvider.connectionCallback = object : StoneActionCallback {
        override fun onSuccess() {
          promise.resolve(true)
        }

        override fun onError() {
          promise.reject("405", "Generic Error - Transaction Failed [onError from Provider] - Check adb log output")
        }

        override fun onStatusChanged(action: Action?) {
          reactApplicationContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(
              progressCallbackEventName, writableMapOf(
                "initiatorTransactionKey" to null,
                "status" to action?.name
              )
            )
        }
      }

      transactionProvider.execute()
    }
  }
}
