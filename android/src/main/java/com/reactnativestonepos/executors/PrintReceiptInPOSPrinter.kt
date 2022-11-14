package com.reactnativestonepos.executors

import android.app.Activity
import br.com.stone.posandroid.providers.PosPrintReceiptProvider
import br.com.stone.posandroid.providers.PosReprintReceiptProvider
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.reactnativestonepos.StonePosModule
import com.reactnativestonepos.helpers.CodedException
import com.reactnativestonepos.helpers.StoneTransactionHelpers
import com.reactnativestonepos.helpers.writableMapOf
import stone.application.enums.Action
import stone.application.enums.ReceiptType
import stone.application.interfaces.StoneActionCallback
import stone.database.transaction.TransactionDAO

class PrintReceiptInPOSPrinter(
  reactApplicationContext: ReactApplicationContext,
  currentActivity: Activity?
) : BaseExecutor(reactApplicationContext, currentActivity) {
  fun executeAction(
    receiptType: String,
    transactionAtk: String,
    isReprint: Boolean,
    dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean,
    progressCallbackEventName: String,
    promise: Promise
  ) {
    checkSDKInitializedAndHandleExceptions(promise) {
      if (!StoneTransactionHelpers.isRunningInPOS(reactApplicationContext)) {
        throw CodedException("101", "You can only run this in a POS")
      }

      if (StonePosModule.currentUserList.isNullOrEmpty()) {
        throw CodedException("401", "You need to activate the terminal first")
      }

      val transactionDAO = TransactionDAO(reactApplicationContext)
      val transactionObject = transactionDAO.findTransactionWithAtk(transactionAtk)

      if (transactionObject != null) {
        val transactionProvider = if (isReprint) {
          PosReprintReceiptProvider(
            if (useDefaultUI) {
              currentActivity!!
            } else {
              reactApplicationContext
            }, transactionAtk, if (receiptType == "CLIENT") {
              ReceiptType.CLIENT
            } else {
              ReceiptType.MERCHANT
            }
          )
        } else {
          PosPrintReceiptProvider(
            if (useDefaultUI) {
              currentActivity!!
            } else {
              reactApplicationContext
            }, transactionObject, if (receiptType == "CLIENT") {
              ReceiptType.CLIENT
            } else {
              ReceiptType.MERCHANT
            }
          )
        }

        transactionProvider.useDefaultUI(useDefaultUI)
        transactionProvider.dialogMessage = if (dialogMessage.isNullOrEmpty()) {
          "Imprimindo comprovante..."
        } else dialogMessage
        transactionProvider.dialogTitle = if (dialogTitle.isNullOrEmpty()) {
          "Aguarde"
        } else dialogTitle

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
