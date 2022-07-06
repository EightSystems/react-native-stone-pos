package com.reactnativestonepos.helpers

import android.content.Context
import android.content.pm.PackageManager
import stone.utils.Stone

class StoneTransactionHelpers {
  companion object {
    private const val PACKAGE_NAME = "br.com.stone.posandroid.acquirerapp"
    fun isRunningInPOS(reactContext: Context): Boolean {
      return try {
        val packageManager: PackageManager = reactContext.packageManager

        packageManager.getPackageInfo(
          PACKAGE_NAME, 0
        )

        true
      } catch (e: PackageManager.NameNotFoundException) {
        false
      }
    }

    fun isSDKInitialized(): Boolean {
      if (!Stone.isInitialized()) {
        throw CodedException("301", "StoneSDK is not initialized")
      }

      return true
    }
  }
}
