package com.reactnativestonepos.executors

import android.app.Activity
import android.os.AsyncTask
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.reactnativestonepos.helpers.CodedException
import com.reactnativestonepos.helpers.StoneTransactionHelpers
import stone.application.enums.Action
import stone.application.interfaces.StoneActionCallback
import stone.application.interfaces.StoneCallbackInterface
import stone.providers.BaseProvider

open class BaseExecutor(
  reactApplicationContext: ReactApplicationContext,
  currentActivity: Activity?
) {
  val reactApplicationContext = reactApplicationContext
  val currentActivity = currentActivity

  companion object {
    val providerMap: HashMap<String, BaseProvider> = HashMap()
  }

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

  fun executeTaskWithReference(referenceName: String, baseProvider: BaseProvider) {
    synchronized(providerMap) {
      if (!providerMap.containsKey(referenceName)) {
        providerMap[referenceName] = baseProvider

        try {
          val currentCallback: StoneCallbackInterface = baseProvider.connectionCallback

          baseProvider.connectionCallback = object : StoneActionCallback {
            override fun onSuccess() {
              currentCallback.onSuccess()

              providerMap.remove(referenceName)
            }

            override fun onError() {
              currentCallback.onError()

              providerMap.remove(referenceName)
            }

            override fun onStatusChanged(p0: Action?) {
              if (currentCallback is StoneActionCallback) {
                currentCallback.onStatusChanged(p0)
              }
            }
          }

          baseProvider.execute()
        } catch (e: Exception) {
          providerMap.remove(referenceName)

          throw e
        }
      } else {
        throw CodedException(
          "300",
          "Task with reference $referenceName is already executing, please cancel it first"
        )
      }
    }
  }

  fun cancelTaskWithReference(
    referenceName: String,
    block: ((provider: BaseProvider) -> Unit)? = null
  ) {
    synchronized(providerMap) {
      if (providerMap.containsKey(referenceName)) {

        if (block != null) {
          providerMap[referenceName]?.let { block.invoke(it) }
        } else {
          providerMap[referenceName]?.cancel(false);
        }

        providerMap.remove(referenceName)
      } else {
        throw CodedException(
          "301",
          "Task with reference $referenceName is not executing"
        )
      }
    }
  }
}
