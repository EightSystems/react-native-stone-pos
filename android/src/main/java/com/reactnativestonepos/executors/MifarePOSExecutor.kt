package com.reactnativestonepos.executors

import android.app.Activity
import br.com.gertec.gedi.s
import br.com.stone.posandroid.hal.api.mifare.MifareKeyType
import br.com.stone.posandroid.providers.PosMifareProvider
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.reactnativestonepos.helpers.ConversionHelpers
import com.reactnativestonepos.helpers.writableArrayFrom
import com.reactnativestonepos.helpers.writableMapOf
import stone.application.enums.Action
import stone.application.interfaces.StoneActionCallback


class MifarePOSExecutor(
  reactApplicationContext: ReactApplicationContext,
  currentActivity: Activity?
) : BaseExecutor(reactApplicationContext, currentActivity) {
  var mifareProvider: PosMifareProvider? = null

  private fun hexStringToByteArray(hexString: String): ByteArray {
    val hexStringLength = hexString.length
    if (hexStringLength % 2 == 0) {
      return hexString.chunked(2).map {
        it.toInt(16).toByte()
      }.toByteArray()
    } else {
      throw Exception("Hex String needs to be divisible by 2")
    }
  }

  private fun activateCardExecuteBlockAndPowerOff(
    promise: Promise,
    dialogMessage: String? = null,
    dialogTitle: String? = null,
    useDefaultUI: Boolean = false,
    onStatusChangedCallback: ((action: Action?) -> Unit)? = null,
    progressCallbackEventName: String? = null,
    block: (mifareProvider: PosMifareProvider) -> Unit
  ) {
    if (mifareProvider != null) {
      mifareProvider!!.cancelDetection()
      mifareProvider = null
    }

    mifareProvider = PosMifareProvider(
      if (useDefaultUI) {
        currentActivity!!
      } else {
        reactApplicationContext
      }
    )

    if (mifareProvider != null) {
      mifareProvider!!.useDefaultUI(useDefaultUI)
      mifareProvider!!.dialogMessage = if (dialogMessage.isNullOrEmpty()) {
        "Aguardando cartão..."
      } else {
        dialogMessage
      }

      mifareProvider!!.dialogTitle = if (dialogTitle.isNullOrEmpty()) {
        "Aguarde..."
      } else {
        dialogTitle
      }

      mifareProvider!!.connectionCallback = object : StoneActionCallback {
        override fun onSuccess() {
          block(mifareProvider!!)

          //Cancel detections
          mifareProvider!!.cancelDetection()
        }

        override fun onError() {
          promise.reject("101", "Error detecting card [Generic error - onError from provider]");

          mifareProvider!!.cancelDetection()
        }

        override fun onStatusChanged(action: Action?) {
          if (onStatusChangedCallback != null) {
            onStatusChangedCallback(action)
          } else {
            reactApplicationContext
              .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
              .emit(
                progressCallbackEventName ?: "MIFARE_PROGRESS",
                writableMapOf(
                  "initiatorTransactionKey" to null,
                  "status" to action?.name
                )
              )
          }
        }
      }

      mifareProvider!!.execute()
    } else {
      promise.reject("102", "Mifare Provider is null")
    }
  }

  fun executeDetectCard(
    dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean,
    progressCallbackEventName: String,
    promise: Promise
  ) {
    activateCardExecuteBlockAndPowerOff(
      promise,
      dialogMessage,
      dialogTitle,
      useDefaultUI,
      progressCallbackEventName = progressCallbackEventName
    ) { mifareProvider ->
      promise.resolve(
        writableArrayFrom(
          mifareProvider.cardUUID.map {
            it.toString()
          }
        )
      )
    }
  }

  fun executeAuthenticateSector(
    keyType: Int, key: String, sector: Int,
    dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean,
    progressCallbackEventName: String,
    promise: Promise
  ) {
    activateCardExecuteBlockAndPowerOff(
      promise,
      dialogMessage,
      dialogTitle,
      useDefaultUI,
      progressCallbackEventName = progressCallbackEventName
    ) { mifareProvider ->
      val keyTypeFound = MifareKeyType.values().findLast {
        it.ordinal == keyType
      }

      if (keyTypeFound != null) {
        try {
          mifareProvider.authenticateSector(
            keyTypeFound,
            hexStringToByteArray(key),
            sector.toByte()
          )

          promise.resolve(true)
        } catch (e: PosMifareProvider.MifareException) {
          promise.reject("103", "Authentication error: ${
            mifareProvider.listOfErrors.joinToString {
              it.name
            }
          }")
        }
      } else {
        promise.reject("102", "Key Type is not Valid")
      }
    }
  }

  fun executeReadBlock(
    keyType: Int, key: String,
    sector: Int, block: Int,
    dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean,
    progressCallbackEventName: String,
    promise: Promise
  ) {
    activateCardExecuteBlockAndPowerOff(
      promise,
      dialogMessage,
      dialogTitle,
      useDefaultUI,
      progressCallbackEventName = progressCallbackEventName
    ) { mifareProvider ->
      try {
        val keyTypeFound = MifareKeyType.values().findLast {
          it.ordinal == keyType
        }

        if (keyTypeFound != null) {
          mifareProvider.authenticateSector(
            keyTypeFound,
            hexStringToByteArray(key),
            sector.toByte()
          )

          var byteArrayRead = ByteArray(16)

          mifareProvider.readBlock(
            sector = sector.toByte(),
            block = block.toByte(),
            data = byteArrayRead
          )

          promise.resolve(
            writableArrayFrom(
              byteArrayRead.map {
                it.toString()
              }
            )
          )
        } else {
          promise.reject("102", "Key Type is not Valid")
        }
      } catch (e: PosMifareProvider.MifareException) {
        promise.reject("103", "Read error: ${
          mifareProvider.listOfErrors.joinToString {
            it.name
          }
        }")
      }
    }
  }

  fun executeWriteBlock(
    keyType: Int, key: String,
    sector: Int, block: Int,
    data: String,
    dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean,
    progressCallbackEventName: String,
    promise: Promise
  ) {
    activateCardExecuteBlockAndPowerOff(
      promise,
      dialogMessage,
      dialogTitle,
      useDefaultUI,
      progressCallbackEventName = progressCallbackEventName
    ) { mifareProvider ->
      try {
        if (data.length == 16) {
          val keyTypeFound = MifareKeyType.values().findLast {
            it.ordinal == keyType
          }

          if (keyTypeFound != null) {
            mifareProvider.authenticateSector(
              keyTypeFound,
              hexStringToByteArray(key),
              sector.toByte()
            )

            mifareProvider.writeBlock(
              sector = sector.toByte(),
              block = block.toByte(),
              data = String.format("%-16s", data).toByteArray()
            )

            promise.resolve(
              true
            )
          } else {
            promise.reject("102", "Key Type is not Valid")
          }
        } else {
          promise.reject("104", "Data doesn't have 16 bytes")
        }
      } catch (e: PosMifareProvider.MifareException) {
        promise.reject("103", "Write error: ${
          mifareProvider.listOfErrors.joinToString {
            it.name
          }
        }")
      }
    }
  }

  fun executeBackupBlock(
    sector: Byte, srcBlock: Byte, dstBlock: Byte, dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean, promise: Promise
  ) {
    activateCardExecuteBlockAndPowerOff(
      promise,
      dialogMessage,
      dialogTitle,
      useDefaultUI,
    ) { mifareProvider ->
      mifareProvider.backupBlock(
        sector,
        srcBlock,
        dstBlock
      )
    }
  }

  fun executeRestoreBlock(
    sector: Byte, srcBlock: Byte, dstBlock: Byte, dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean, promise: Promise
  ) {
    activateCardExecuteBlockAndPowerOff(
      promise,
      dialogMessage,
      dialogTitle,
      useDefaultUI,
    ) { mifareProvider ->
      mifareProvider.restoreBlock(
        sector,
        srcBlock,
        dstBlock
      )
    }
  }

  fun executeIncrementValue(
    sector: Byte, block: Byte, value: Byte, dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean, promise: Promise
  ) {
    activateCardExecuteBlockAndPowerOff(
      promise,
      dialogMessage,
      dialogTitle,
      useDefaultUI,
    ) { mifareProvider ->
      mifareProvider.incrementValue(
        sector,
        block,
        value
      )
    }
  }

  fun executeDecrementValue(
    sector: Byte, block: Byte, value: Byte, dialogMessage: String?,
    dialogTitle: String?,
    useDefaultUI: Boolean, promise: Promise
  ) {
    activateCardExecuteBlockAndPowerOff(
      promise,
      dialogMessage,
      dialogTitle,
      useDefaultUI,
    ) { mifareProvider ->
      mifareProvider.decrementValue(
        sector,
        block,
        value
      )
    }
  }
}
