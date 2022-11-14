package com.reactnativestonepos.executors

import android.app.Activity
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.reactnativestonepos.StonePosModule
import com.reactnativestonepos.helpers.CodedException
import com.reactnativestonepos.helpers.ConversionHelpers
import com.reactnativestonepos.helpers.writableMapOf
import stone.application.enums.Action
import stone.application.interfaces.StoneActionCallback
import stone.database.transaction.TransactionDAO
import stone.providers.CancellationProvider

class VoidTransaction(
  reactApplicationContext: ReactApplicationContext,
  currentActivity: Activity?
) : BaseExecutor(reactApplicationContext, currentActivity) {
  fun executeAction(
    transactionAtk: String,
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

      val transactionObject = TransactionDAO(reactApplicationContext).findTransactionWithAtk(transactionAtk)

      if (transactionObject != null) {
        val transactionProvider = CancellationProvider(
          if (useDefaultUI) {
            currentActivity!!
          } else {
            reactApplicationContext
          }, transactionObject
        )

        transactionProvider.useDefaultUI(useDefaultUI)

        transactionProvider.dialogMessage = if (!dialogMessage.isNullOrEmpty()) {
          dialogMessage
        } else {
          "Executando transação..."
        }

        transactionProvider.dialogTitle = if (!dialogTitle.isNullOrEmpty()) {
          dialogTitle
        } else {
          "Executando..."
        }

        transactionProvider.connectionCallback = object : StoneActionCallback {
          override fun onSuccess() {
            try {
              val trx =
                TransactionDAO(reactApplicationContext).findTransactionWithAtk(transactionObject.acquirerTransactionKey)
              if (trx != null) {
                promise.resolve(
                  ConversionHelpers.convertTransactionToWritableMap(
                    trx,
                    transactionProvider.messageFromAuthorize
                  )
                )
              } else {
                promise.resolve(null)
              }
            } catch (e: Exception) {
              promise.reject(e)
            }
          }

          override fun onError() {
            promise.reject("405", "Generic Error - Transaction Failed [onError from Provider] - Check adb log output")
          }

          override fun onStatusChanged(action: Action?) {
            reactApplicationContext
              .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
              .emit(
                progressCallbackEventName, writableMapOf(
                  "initiatorTransactionKey" to transactionObject.initiatorTransactionKey,
                  "status" to action?.name
                )
              )
          }
        }

        transactionProvider.execute()
      } else {
        throw CodedException("402", "Transaction not found")
      }
    }
  }
}
