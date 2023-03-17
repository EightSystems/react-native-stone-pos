package com.reactnativestonepos.executors

import android.app.Activity
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.reactnativestonepos.StonePosModule
import com.reactnativestonepos.helpers.CodedException
import com.reactnativestonepos.helpers.ConversionHelpers
import com.reactnativestonepos.helpers.StoneActivationHelpers
import com.reactnativestonepos.helpers.writableArrayFrom

class ActivateDeactivateCode(
  reactApplicationContext: ReactApplicationContext,
  currentActivity: Activity?
) : BaseExecutor(reactApplicationContext, currentActivity) {
  fun executeGetActivatedCodes(promise: Promise) {
    checkSDKInitializedAndHandleExceptions(promise) {
      if (StonePosModule.currentUserList != null) {
        promise.resolve(
          writableArrayFrom(
            StonePosModule.currentUserList!!.map {
              ConversionHelpers.convertUserToWritableMap(it)
            }
          )
        )
      } else {
        throw CodedException("101", "No stone code activated")
      }
    }
  }

  fun executeAction(
    isActivationAction: Boolean, stoneCode: String,
    dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean,
    ignoreLastStoneCodeCheck: Boolean,
    promise: Promise
  ) {
    checkSDKInitializedAndHandleExceptions(promise) {
      if (isActivationAction) {
        if (StonePosModule.hasStoneCodeInList(stoneCode)) {
          promise.resolve(true)
          return@checkSDKInitializedAndHandleExceptions
        }
      } else {
        if (!StonePosModule.hasStoneCodeInList(stoneCode)) {
          throw CodedException("201", "Stone Code not currently activated")
        }
      }

      if (dialogMessage != null) {
        val transactionProvider = StoneActivationHelpers.activationProvider(
          if (useDefaultUI) {
            currentActivity!!
          } else {
            reactApplicationContext
          },
          if (dialogMessage.isNullOrEmpty()) {
            if (isActivationAction) {
              "Ativando seu código"
            } else {
              "Desativando seu código"
            }
          } else {
            dialogMessage
          },
          if (dialogTitle.isNullOrEmpty()) {
            if (isActivationAction) {
              "Ativação"
            } else {
              "Desativação"
            }
          } else {
            dialogTitle
          },
          useDefaultUI,
          promise
        )

        if (isActivationAction) {
          transactionProvider.activate(stoneCode)
        } else {
          if (StonePosModule.userListCount() > 1 || ignoreLastStoneCodeCheck) {
            transactionProvider.deactivate(stoneCode)
          } else {
            throw CodedException("401", "You can't deactivate the only Stone Code in this POS")
          }
        }
      }
    }
  }
}
