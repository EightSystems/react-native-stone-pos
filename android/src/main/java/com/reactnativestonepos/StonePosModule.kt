package com.reactnativestonepos

import android.content.Context
import com.facebook.react.bridge.*
import com.reactnativestonepos.executors.*
import com.reactnativestonepos.helpers.StoneTransactionHelpers
import stone.application.StoneStart
import stone.user.UserModel
import stone.utils.Stone
import stone.utils.keys.StoneKeyType

class StonePosModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  companion object {
    private const val IS_RUNNING_IN_POS = "IS_RUNNING_IN_POS"
    private const val STONE_SDK_VERSION = "STONE_SDK_VERSION"
    private var STONE_QRCODE_PROVIDER_ID = ""
    private var STONE_QRCODE_AUTHORIZATION = ""

    var currentUserList: List<UserModel>? = null
    fun hasStoneCodeInList(stoneCode: String): Boolean {
      if (currentUserList?.findLast { it.stoneCode.equals(stoneCode) } != null) {
        return true
      }

      return false
    }

    fun updateUserList(reactContext: Context) {
      synchronized(this) {
        if (Stone.isInitialized()) {
          currentUserList = StoneStart.init(
            reactContext, hashMapOf(
              StoneKeyType.QRCODE_PROVIDERID to STONE_QRCODE_PROVIDER_ID,
              StoneKeyType.QRCODE_AUTHORIZATION to STONE_QRCODE_AUTHORIZATION
            )
          )
        }
      }
    }

    fun userListCount(): Int {
      return if (currentUserList != null) currentUserList!!.size else 0
    }

    fun hasPixKeysProvided(): Boolean {
      return !(STONE_QRCODE_PROVIDER_ID.isNullOrEmpty() || STONE_QRCODE_AUTHORIZATION.isNullOrEmpty())
    }
  }

  override fun getName(): String {
    return "StonePos"
  }

  @ReactMethod
  fun addListener(@Suppress("UNUSED_PARAMETER") type: String?) {
    // Keep: Required for RN built in Event Emitter Calls.
  }

  @ReactMethod
  fun removeListeners(@Suppress("UNUSED_PARAMETER") type: Int?) {
    // Keep: Required for RN built in Event Emitter Calls.
  }

  override fun getConstants(): Map<String, Any> {
    val constants: MutableMap<String, Any> = HashMap()

    constants[IS_RUNNING_IN_POS] = StoneTransactionHelpers.isRunningInPOS(reactApplicationContext)
    constants[STONE_SDK_VERSION] = Stone.getSdkVersion()

    return constants
  }

  @ReactMethod
  fun initSDK(
    appName: String,
    qrCodeProviderKey: String,
    qrCodeProviderAuthorization: String,
    promise: Promise
  ) {
    try {
      synchronized(this) {
        STONE_QRCODE_PROVIDER_ID = qrCodeProviderKey
        STONE_QRCODE_AUTHORIZATION = qrCodeProviderAuthorization

        currentUserList = StoneStart.init(
          reactApplicationContext, hashMapOf(
            StoneKeyType.QRCODE_PROVIDERID to qrCodeProviderKey,
            StoneKeyType.QRCODE_AUTHORIZATION to qrCodeProviderAuthorization
          )
        )

        Stone.setAppName(appName)

        promise.resolve(true)
      }
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  /**
   * Activation and Deactivation Methods
   */
  @ReactMethod
  fun activateCode(
    stoneCode: String,
    dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean,
    promise: Promise
  ) {
    try {
      ActivateDeactivateCode(reactApplicationContext, currentActivity).executeAction(
        isActivationAction = true,
        stoneCode,
        dialogMessage,
        dialogTitle,
        useDefaultUI,
        ignoreLastStoneCodeCheck = false,
        promise
      )
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun deactivateCode(
    stoneCode: String,
    dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean,
    ignoreLastStoneCodeCheck: Boolean,
    promise: Promise
  ) {
    try {
      ActivateDeactivateCode(reactApplicationContext, currentActivity).executeAction(
        isActivationAction = false,
        stoneCode,
        dialogMessage,
        dialogTitle,
        useDefaultUI,
        ignoreLastStoneCodeCheck,
        promise
      )
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun getActivatedCodes(promise: Promise) {
    try {
      ActivateDeactivateCode(reactApplicationContext, currentActivity).executeGetActivatedCodes(
        promise
      )
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  /**
   * Transactions fetching methods
   */

  @ReactMethod
  fun getAllTransactionsOrderByIdDesc(promise: Promise) {
    try {
      GetTransactions(reactApplicationContext, currentActivity).executeActionOrderByIdDesc(promise)
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun getLastTransaction(promise: Promise) {
    try {
      GetTransactions(reactApplicationContext, currentActivity).executeActionGetLastTransaction(
        promise
      )
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun findTransactionWithAuthorizationCode(authorizationCode: String, promise: Promise) {
    try {
      GetTransactions(
        reactApplicationContext,
        currentActivity
      ).executeFindTransactionWithAuthorizationCode(authorizationCode, promise)
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun findTransactionWithInitiatorTransactionKey(
    initiatorTransactionKey: String,
    promise: Promise
  ) {
    try {
      GetTransactions(
        reactApplicationContext,
        currentActivity
      ).executeFindTransactionWithInitiatorTransactionKey(initiatorTransactionKey, promise)
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun findTransactionWithId(transactionId: Int, promise: Promise) {
    try {
      GetTransactions(reactApplicationContext, currentActivity).executeFindTransactionWithId(
        transactionId,
        promise
      )
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  /**
   * Transaction Executors
   */

  @ReactMethod
  fun reversePendingTransactions(
    dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean,
    progressCallbackEventName: String,
    promise: Promise
  ) {
    try {
      ReversePendingTransactions(reactApplicationContext, currentActivity).executeAction(
        dialogMessage,
        dialogTitle,
        useDefaultUI,
        progressCallbackEventName,
        promise
      )
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun voidTransaction(
    transactionAtk: String,
    dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean,
    progressCallbackEventName: String,
    promise: Promise
  ) {
    try {
      VoidTransaction(reactApplicationContext, currentActivity).executeAction(
        transactionAtk,
        dialogMessage,
        dialogTitle,
        useDefaultUI,
        progressCallbackEventName,
        promise
      )
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun captureTransaction(
    transactionAtk: String,
    dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean,
    progressCallbackEventName: String,
    promise: Promise
  ) {
    try {
      CaptureTransaction(reactApplicationContext, currentActivity).executeAction(
        transactionAtk,
        dialogMessage,
        dialogTitle,
        useDefaultUI,
        progressCallbackEventName,
        promise
      )
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun makeTransaction(
    transactionSetup: ReadableMap,
    progressCallbackEventName: String,
    promise: Promise
  ) {
    try {
      MakeTransaction(reactApplicationContext, currentActivity).executeAction(
        transactionSetup,
        progressCallbackEventName,
        promise
      )
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun cancelRunningTaskMakeTransaction(
    promise: Promise
  ) {
    try {
      MakeTransaction(reactApplicationContext, currentActivity).cancelAction(
        promise
      )
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun sendTransactionReceiptMail(
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
    try {
      SendTransactionReceiptMail(reactApplicationContext, currentActivity).executeAction(
        transactionAtk,
        receiptType,
        toContact,
        fromContact,
        dialogMessage,
        dialogTitle,
        useDefaultUI,
        progressCallbackEventName,
        promise
      )
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun fetchTransactionsForCard(
    pinpadMacAddress: String?,
    dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean,
    progressCallbackEventName: String,
    promise: Promise
  ) {
    try {
      FetchTransactionsForCard(reactApplicationContext, currentActivity).executeAction(
        pinpadMacAddress,
        dialogMessage,
        dialogTitle,
        useDefaultUI,
        progressCallbackEventName,
        promise
      )
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  /**
   * Pinpad Methods
   */

  @ReactMethod
  fun displayMessageInPinPad(
    pinpadMessage: String,
    pinpadMacAddress: String?,
    dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean,
    progressCallbackEventName: String,
    promise: Promise
  ) {
    try {
      DisplayMessageInPinPad(reactApplicationContext, currentActivity).executeAction(
        pinpadMessage,
        pinpadMacAddress,
        dialogMessage,
        dialogTitle,
        useDefaultUI,
        progressCallbackEventName,
        promise
      )
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun connectToPinPad(
    pinpadName: String,
    pinpadMacAddress: String,
    dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean,
    progressCallbackEventName: String,
    promise: Promise
  ) {
    try {
      ConnectToPinPad(reactApplicationContext, currentActivity).executeAction(
        pinpadName,
        pinpadMacAddress,
        dialogMessage,
        dialogTitle,
        useDefaultUI,
        progressCallbackEventName,
        promise
      )
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  /**
   * Print methods
   */
  @ReactMethod
  fun printReceiptInPOSPrinter(
    receiptType: String,
    transactionAtk: String,
    isReprint: Boolean,
    dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean,
    progressCallbackEventName: String,
    promise: Promise
  ) {
    try {
      PrintReceiptInPOSPrinter(reactApplicationContext, currentActivity).executeAction(
        receiptType,
        transactionAtk,
        isReprint,
        dialogMessage,
        dialogTitle,
        useDefaultUI,
        progressCallbackEventName,
        promise
      )
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun printHTMLInPOSPrinter(
    htmlContent: String,
    dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean,
    progressCallbackEventName: String,
    promise: Promise
  ) {
    try {
      PrintHtmlInPOSPrinter(reactApplicationContext, currentActivity).executeAction(
        htmlContent,
        dialogMessage,
        dialogTitle,
        useDefaultUI,
        progressCallbackEventName,
        promise
      )
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun mifareDetectCard(
    dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean,
    progressCallbackEventName: String,
    promise: Promise
  ) {
    try {
      MifarePOSExecutor(reactApplicationContext, currentActivity).executeDetectCard(
        dialogMessage, dialogTitle, useDefaultUI, progressCallbackEventName, promise
      )
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun mifareAuthenticateSector(
    keyType: Int, sector: Int, key: String, dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean,
    progressCallbackEventName: String,
    promise: Promise
  ) {
    try {
      MifarePOSExecutor(reactApplicationContext, currentActivity).executeAuthenticateSector(
        keyType,
        key,
        sector,
        dialogMessage,
        dialogTitle,
        useDefaultUI,
        progressCallbackEventName,
        promise
      )
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun mifareReadBlock(
    keyType: Int, sector: Int, block: Int,
    key: String, dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean,
    progressCallbackEventName: String,
    promise: Promise
  ) {
    try {
      MifarePOSExecutor(reactApplicationContext, currentActivity).executeReadBlock(
        keyType,
        key,
        sector,
        block,
        dialogMessage,
        dialogTitle,
        useDefaultUI,
        progressCallbackEventName,
        promise
      )
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun mifareWriteBlock(
    keyType: Int, sector: Int, block: Int,
    data: String,
    key: String, dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean,
    progressCallbackEventName: String,
    promise: Promise
  ) {
    try {
      MifarePOSExecutor(reactApplicationContext, currentActivity).executeWriteBlock(
        keyType,
        key,
        sector,
        block,
        data,
        dialogMessage,
        dialogTitle,
        useDefaultUI,
        progressCallbackEventName,
        promise
      )
    } catch (e: Exception) {
      promise.reject(e)
    }
  }
}
