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
import stone.providers.DisplayMessageProvider
import stone.utils.PinpadObject
import stone.utils.Stone

class DisplayMessageInPinPad(
  reactApplicationContext: ReactApplicationContext,
  currentActivity: Activity?
) : BaseExecutor(reactApplicationContext, currentActivity) {
  fun executeAction(
    pinpadMessage: String,
    pinpadMacAddress: String?,
    dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean,
    progressCallbackEventName: String,
    promise: Promise
  ) {
    try {
      if (StoneTransactionHelpers.isSDKInitialized()) {
        if (StonePosModule.currentUserList.isNullOrEmpty()) {
          throw CodedException("401", "You need to activate the terminal first")
        }

        if (!StoneTransactionHelpers.isRunningInPOS(reactApplicationContext)) {
          if (!Stone.isConnectedToPinpad()) {
            throw CodedException("402", "You need to connect to a pinpad first")
          }
        } else {
          throw CodedException("402", "You are running in POS mode.")
        }

        val selectedPinPad: PinpadObject? = if (!pinpadMacAddress.isNullOrEmpty()) {
          Stone.getPinpadObjectList().findLast {
            it.macAddress == pinpadMacAddress
          } ?: throw CodedException("402", "Pinpad not found")
        } else {
          if (Stone.getPinpadListSize() > 0) {
            Stone.getPinpadFromListAt(0)
          } else {
            throw CodedException("402", "No pinpad connected")
          }
        }

        val transactionProvider = DisplayMessageProvider(
          if (useDefaultUI) {
            currentActivity!!
          } else {
            reactApplicationContext
          }, pinpadMessage, selectedPinPad
        )

        transactionProvider.useDefaultUI(useDefaultUI)
        transactionProvider.dialogMessage = dialogMessage
        transactionProvider.dialogTitle = dialogTitle
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
    } catch (e: CodedException) {
      promise.reject(e.code, e.internalMessage)
    } catch (e: Exception) {
      promise.reject(e)
    }
  }
}
