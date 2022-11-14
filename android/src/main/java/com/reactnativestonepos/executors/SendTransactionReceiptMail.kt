package com.reactnativestonepos.executors

import android.app.Activity
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.reactnativestonepos.StonePosModule
import com.reactnativestonepos.helpers.CodedException
import com.reactnativestonepos.helpers.ConversionHelpers
import com.reactnativestonepos.helpers.writableMapOf
import stone.application.enums.Action
import stone.application.enums.ReceiptType
import stone.application.interfaces.StoneActionCallback
import stone.database.transaction.TransactionDAO
import stone.providers.SendEmailTransactionProvider
import stone.repository.remote.email.pombo.email.Contact

class SendTransactionReceiptMail(
  reactApplicationContext: ReactApplicationContext,
  currentActivity: Activity?
) : BaseExecutor(reactApplicationContext, currentActivity) {
  fun executeAction(
    transactionAtk: String,
    receiptType: String,
    toContact: ReadableArray,
    fromContact: ReadableMap,
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
        val transactionProvider = SendEmailTransactionProvider(
          if (useDefaultUI) {
            currentActivity!!
          } else {
            reactApplicationContext
          }, transactionObject
        )

        transactionProvider.receiptType = if (receiptType == "CLIENT") {
          ReceiptType.CLIENT
        } else {
          ReceiptType.MERCHANT
        }

        if (fromContact.hasKey("email")) {
          transactionProvider.from = Contact(
            fromContact.getString("email"),
            if (fromContact.hasKey("name")) {
              fromContact.getString("name")
            } else {
              "Application Name"
            }
          )
        } else {
          throw CodedException(
            "101",
            "You need to have at least the email in the fromContact parameter"
          )
        }

        if (toContact.size() > 0) {
          toContact.toArrayList().mapIndexed { index, it ->
            {
              val contactData = it as ReadableMap
              if (contactData.hasKey("email")) {
                transactionProvider.addTo(
                  Contact(
                    contactData.getString("email"),
                    if (contactData.hasKey("name")) {
                      contactData.getString("name")
                    } else {
                      "Customer Name"
                    }
                  )
                )
              } else {
                throw CodedException("102", String.format("toContact index %d has no email", index))
              }
            }
          }
        } else {
          throw CodedException("102", "You need at least 1 email in the toContact list")
        }

        transactionProvider.useDefaultUI(useDefaultUI)
        transactionProvider.dialogMessage = if (dialogMessage.isNullOrEmpty()) {
          "Enviando e-mail..."
        } else dialogMessage
        transactionProvider.dialogTitle = if (dialogTitle.isNullOrEmpty()) {
          "Aguarde"
        } else dialogTitle

        transactionProvider.connectionCallback = object : StoneActionCallback {
          override fun onSuccess() {
            try {
              val trx =
                TransactionDAO(reactApplicationContext).findTransactionWithAtk(transactionObject.acquirerTransactionKey)
              if (trx != null) {
                promise.resolve(
                  ConversionHelpers.convertTransactionToWritableMap(
                    trx
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
