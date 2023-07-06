package com.reactnativestonepos.executors

import android.app.Activity
import br.com.stone.posandroid.datacontainer.api.util.toEncodedString
import br.com.stone.posandroid.providers.PosTransactionProvider
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.reactnativestonepos.StonePosModule
import com.reactnativestonepos.helpers.CodedException
import com.reactnativestonepos.helpers.ConversionHelpers
import com.reactnativestonepos.helpers.StoneTransactionHelpers
import com.reactnativestonepos.helpers.writableMapOf
import stone.application.enums.Action
import stone.application.enums.TransactionStatusEnum
import stone.application.enums.TypeOfTransactionEnum
import stone.application.interfaces.StoneActionCallback
import stone.database.transaction.TransactionDAO
import stone.providers.BaseTransactionProvider
import stone.providers.TransactionProvider
import stone.utils.PinpadObject
import stone.utils.Stone

class MakeTransaction(
  reactApplicationContext: ReactApplicationContext,
  currentActivity: Activity?
) : BaseExecutor(reactApplicationContext, currentActivity) {
  fun executeAction(
    transactionSetup: ReadableMap,
    progressCallbackEventName: String,
    promise: Promise
  ) {
    checkSDKInitializedAndHandleExceptions(promise) {
      if (StonePosModule.currentUserList.isNullOrEmpty()) {
        throw CodedException("401", "You need to activate the terminal first")
      }

      if (!StoneTransactionHelpers.isRunningInPOS(reactApplicationContext)) {
        if (!Stone.isConnectedToPinpad()) {
          throw CodedException("402", "You need to connect to a pinpad first")
        }
      }

      val selectedUser = if (transactionSetup.hasKey("stoneCode")) {
        StonePosModule.currentUserList?.findLast {
          it.stoneCode == transactionSetup.getString("stoneCode")
        } ?: throw Exception("StoneCode not found in user list")
      } else {
        StonePosModule.currentUserList?.last()
      }

      val selectedPinPad: PinpadObject? =
        if (StoneTransactionHelpers.isRunningInPOS(reactApplicationContext)) {
          null
        } else {
          if (transactionSetup.hasKey("pinpadMacAddress")) {
            Stone.getPinpadObjectList().findLast {
              it.macAddress == transactionSetup.getString("pinpadMacAddress")!!
            } ?: throw Exception("Pinpad not found")
          } else {
            if (Stone.getPinpadListSize() > 0) {
              Stone.getPinpadFromListAt(0)
            } else {
              throw Exception("No pinpad connected")
            }
          }
        }

      val useDefaultUI = if (transactionSetup.hasKey("useDefaultUI")) {
        transactionSetup.getBoolean("useDefaultUI")
      } else {
        false
      }

      val transactionObject = ConversionHelpers.convertReadableMapToTransaction(transactionSetup)

      if (transactionObject.typeOfTransaction == TypeOfTransactionEnum.PIX) {
        if (!StonePosModule.hasPixKeysProvided()) {
          throw Exception("You are trying to make a PIX transaction but didn't provide the needed keys, check the initSDK method");
        }
      }

      val transactionProvider =
        if (StoneTransactionHelpers.isRunningInPOS(reactApplicationContext)) {
          PosTransactionProvider(
            if (useDefaultUI) {
              currentActivity!!
            } else {
              reactApplicationContext
            }, transactionObject, selectedUser
          )
        } else {
          TransactionProvider(
            if (useDefaultUI) {
              currentActivity!!
            } else {
              reactApplicationContext
            }, transactionObject, selectedUser, selectedPinPad!!
          )
        }

      transactionProvider.useDefaultUI(useDefaultUI)

      transactionProvider.dialogMessage = if (transactionSetup.hasKey("dialogMessage")) {
        transactionSetup.getString("dialogMessage")
      } else {
        "Executando transação..."
      }

      transactionProvider.dialogTitle = if (transactionSetup.hasKey("dialogTitle")) {
        transactionSetup.getString("dialogTitle")
      } else {
        "Executando..."
      }

      transactionProvider.connectionCallback = object : StoneActionCallback {
        override fun onSuccess() {
          val transactionDAO = TransactionDAO(reactApplicationContext)
          try {
            val trx = transactionDAO.findTransactionWithId(transactionDAO.getLastTransactionId())
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
          if (transactionProvider.transactionStatus !== TransactionStatusEnum.UNKNOWN) {
            promise.reject(
              transactionProvider.transactionStatus.name,
              transactionProvider.messageFromAuthorize
            )
          } else {
            promise.reject(
              "405",
              "Generic Error - Transaction Failed [onError from Provider] - Check adb log output"
            )
          }
        }

        override fun onStatusChanged(action: Action?) {
          reactApplicationContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(
              progressCallbackEventName, writableMapOf(
                "initiatorTransactionKey" to transactionObject?.initiatorTransactionKey,
                "status" to action?.name,
                "transactionStatus" to transactionProvider.transactionStatus.name,
                "messageFromAuthorize" to transactionProvider.messageFromAuthorize,
                "qrCode" to if (action == Action.TRANSACTION_WAITING_QRCODE_SCAN) transactionObject?.qrCode?.toEncodedString() else null
              )
            )
        }
      }

      executeTaskWithReference("makeTransaction", transactionProvider)
    }
  }

  fun cancelAction(promise: Promise) {
    checkSDKInitializedAndHandleExceptions(promise) {
      cancelTaskWithReference("makeTransaction") {
        (it as BaseTransactionProvider).abortPayment()
      }

      promise.resolve(true)
    }
  }
}
