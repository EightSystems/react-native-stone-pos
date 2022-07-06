package com.reactnativestonepos.helpers

import android.content.Context
import com.facebook.react.bridge.Promise
import com.reactnativestonepos.StonePosModule
import stone.application.interfaces.StoneCallbackInterface
import stone.providers.ActiveApplicationProvider

class StoneActivationHelpers {
  companion object {
    fun activationProvider(
      reactContext: Context,
      dialogMessage: String,
      dialogTitle: String,
      useDefaultUI: Boolean,
      promise: Promise
    ): ActiveApplicationProvider {
      val activeApplicationProvider = ActiveApplicationProvider(reactContext)

      activeApplicationProvider.dialogMessage = dialogMessage
      activeApplicationProvider.dialogTitle = dialogTitle
      activeApplicationProvider.useDefaultUI(useDefaultUI)
      activeApplicationProvider.connectionCallback = object : StoneCallbackInterface {
        override fun onSuccess() {
          StonePosModule.updateUserList(reactContext)

          promise.resolve(true)
        }

        override fun onError() {
          promise.reject("201", activeApplicationProvider.listOfErrors.toString())
        }
      }

      return activeApplicationProvider
    }
  }
}
