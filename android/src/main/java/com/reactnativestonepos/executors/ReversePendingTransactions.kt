package com.reactnativestonepos.executors

import android.app.Activity
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.reactnativestonepos.StonePosModule
import com.reactnativestonepos.helpers.CodedException
import com.reactnativestonepos.helpers.writableMapOf
import stone.application.enums.Action
import stone.application.interfaces.StoneActionCallback
import stone.providers.ReversalProvider

class ReversePendingTransactions(
  reactApplicationContext: ReactApplicationContext,
  currentActivity: Activity?
) : BaseExecutor(reactApplicationContext, currentActivity) {
  fun executeAction(
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

      val transactionProvider = ReversalProvider(
        if (useDefaultUI) {
          currentActivity!!
        } else {
          reactApplicationContext
        }
      )

      transactionProvider.useDefaultUI(useDefaultUI)
      transactionProvider.dialogMessage = if (dialogMessage.isNullOrEmpty()) {
        "Cancelando transações com erro"
      } else {
        dialogMessage
      }

      transactionProvider.dialogTitle = if (dialogTitle.isNullOrEmpty()) {
        "Cancelando..."
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
