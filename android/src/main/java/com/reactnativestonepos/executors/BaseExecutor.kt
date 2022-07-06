package com.reactnativestonepos.executors

import android.app.Activity
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.reactnativestonepos.helpers.CodedException
import com.reactnativestonepos.helpers.StoneTransactionHelpers

open class BaseExecutor(
  reactApplicationContext: ReactApplicationContext,
  currentActivity: Activity?
) {
  val reactApplicationContext = reactApplicationContext
  val currentActivity = currentActivity

  fun checkSDKInitializedAndHandleExceptions(promise: Promise, block: () -> Unit) {
    try {
      if (StoneTransactionHelpers.isSDKInitialized()) {
        block()
      } else {
        throw CodedException("101", "No stone code activated")
      }
    } catch (e: CodedException) {
      promise.reject(e.code, e.internalMessage)
    } catch (e: Exception) {
      promise.reject(e)
    }
  }
}
