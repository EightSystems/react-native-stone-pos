package com.reactnativestonepos

import android.content.Context
import com.facebook.react.bridge.*
import com.reactnativestonepos.executors.*
import com.reactnativestonepos.helpers.StoneTransactionHelpers
import stone.application.StoneStart
import stone.user.UserModel
import stone.utils.Stone

class StonePosModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  companion object {
    private const val TAG = "RNStonePos"
    private const val IS_RUNNING_IN_POS = "IS_RUNNING_IN_POS"

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
          currentUserList = StoneStart.init(reactContext)
        }
      }
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

  override fun getConstants(): Map<String, Any>? {
    val constants: MutableMap<String, Any> = HashMap()

    constants[IS_RUNNING_IN_POS] = StoneTransactionHelpers.isRunningInPOS(reactApplicationContext)

    return constants
  }

  @ReactMethod
  fun initSDK(appName: String, promise: Promise) {
    try {
      synchronized(this) {
        currentUserList = StoneStart.init(reactApplicationContext)

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
    ActivateDeactivateCode(reactApplicationContext, currentActivity).executeAction(
      isActivationAction = true,
      stoneCode,
      dialogMessage,
      dialogTitle,
      useDefaultUI,
      promise
    )
  }

  @ReactMethod
  fun deactivateCode(
    stoneCode: String,
    dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean,
    promise: Promise
  ) {
    ActivateDeactivateCode(reactApplicationContext, currentActivity).executeAction(
      isActivationAction = false,
      stoneCode,
      dialogMessage,
      dialogTitle,
      useDefaultUI,
      promise
    )
  }

  @ReactMethod
  fun getActivatedCodes(promise: Promise) {
    ActivateDeactivateCode(reactApplicationContext, currentActivity).executeGetActivatedCodes(
      promise
    )
  }

  /**
   * Transactions fetching methods
   */

  @ReactMethod
  fun getAllTransactionsOrderByIdDesc(promise: Promise) {
    GetTransactions(reactApplicationContext, currentActivity).executeActionOrderByIdDesc(promise)
  }

  @ReactMethod
  fun getLastTransaction(promise: Promise) {
    GetTransactions(reactApplicationContext, currentActivity).executeActionGetLastTransaction(
      promise
    )
  }

  @ReactMethod
  fun findTransactionWithAuthorizationCode(authorizationCode: String, promise: Promise) {
    GetTransactions(
      reactApplicationContext,
      currentActivity
    ).executeFindTransactionWithAuthorizationCode(authorizationCode, promise)
  }

  @ReactMethod
  fun findTransactionWithInitiatorTransactionKey(
    initiatorTransactionKey: String,
    promise: Promise
  ) {
    GetTransactions(
      reactApplicationContext,
      currentActivity
    ).executeFindTransactionWithInitiatorTransactionKey(initiatorTransactionKey, promise)
  }

  @ReactMethod
  fun findTransactionWithId(transactionId: Int, promise: Promise) {
    GetTransactions(reactApplicationContext, currentActivity).executeFindTransactionWithId(
      transactionId,
      promise
    )
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
    ReversePendingTransactions(reactApplicationContext, currentActivity).executeAction(
      dialogMessage,
      dialogTitle,
      useDefaultUI,
      progressCallbackEventName,
      promise
    )
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
    VoidTransaction(reactApplicationContext, currentActivity).executeAction(
      transactionAtk,
      dialogMessage,
      dialogTitle,
      useDefaultUI,
      progressCallbackEventName,
      promise
    )
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
    CaptureTransaction(reactApplicationContext, currentActivity).executeAction(
      transactionAtk,
      dialogMessage,
      dialogTitle,
      useDefaultUI,
      progressCallbackEventName,
      promise
    )
  }

  @ReactMethod
  fun makeTransaction(
    transactionSetup: ReadableMap,
    progressCallbackEventName: String,
    promise: Promise
  ) {
    MakeTransaction(reactApplicationContext, currentActivity).executeAction(
      transactionSetup,
      progressCallbackEventName,
      promise
    )
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
    FetchTransactionsForCard(reactApplicationContext, currentActivity).executeAction(
      pinpadMacAddress,
      dialogMessage,
      dialogTitle,
      useDefaultUI,
      progressCallbackEventName,
      promise
    )
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
    DisplayMessageInPinPad(reactApplicationContext, currentActivity).executeAction(
      pinpadMessage,
      pinpadMacAddress,
      dialogMessage,
      dialogTitle,
      useDefaultUI,
      progressCallbackEventName,
      promise
    )
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
    ConnectToPinPad(reactApplicationContext, currentActivity).executeAction(
      pinpadName,
      pinpadMacAddress,
      dialogMessage,
      dialogTitle,
      useDefaultUI,
      progressCallbackEventName,
      promise
    )
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
    PrintHtmlInPOSPrinter(reactApplicationContext, currentActivity).executeAction(
      htmlContent,
      dialogMessage,
      dialogTitle,
      useDefaultUI,
      progressCallbackEventName,
      promise
    )
  }
}
