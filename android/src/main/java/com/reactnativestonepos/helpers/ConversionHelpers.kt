package com.reactnativestonepos.helpers

import br.com.stone.payment.domain.datamodel.TransAppSelectedInfo
import com.facebook.react.bridge.*
import stone.application.enums.EntryMode
import stone.application.enums.InstalmentTransactionEnum
import stone.application.enums.TypeOfTransactionEnum
import stone.database.transaction.TransactionObject
import stone.user.Address
import stone.user.UserModel


class ConversionHelpers {
  companion object {
    fun convertByteArrayToWritableArray(byteArray: ByteArray): WritableArray {
      val value = Arguments.createArray()
      for (i in 0..byteArray.size) {
        value.pushInt(byteArray[i].toInt())
      }

      return value
    }

    fun convertReadableArrayToByteArray(readableArray: ReadableArray): ByteArray {
      var byteArray = ByteArray(readableArray.size())

      for (i in 0..readableArray.size()) {
        byteArray[i] = readableArray.getInt(i).toByte()
      }

      return byteArray
    }

    fun convertTransSelectedInfoToWritableMap(transAppSelectedInfo: TransAppSelectedInfo): WritableMap {
      return writableMapOf(
        "brandName" to transAppSelectedInfo.brandName,
        "aid" to transAppSelectedInfo.aid,
        "transactionTypeInfo" to writableMapOf(
          "appLabel" to transAppSelectedInfo.transactionTypeInfo.appLabel,
          "id" to transAppSelectedInfo.transactionTypeInfo.id,
          "transTypeEnum" to transAppSelectedInfo.transactionTypeInfo.transTypeEnum.toString(),
        ),
        "cardAppLabel" to transAppSelectedInfo.cardAppLabel,
        "paymentBusinessModel" to transAppSelectedInfo.paymentBusinessModel.toString(),
        "brandId" to transAppSelectedInfo.brandId,
      )
    }

    fun convertAddressToWritableMap(address: Address): WritableMap {
      return writableMapOf(
        "distric" to address.distric,
        "city" to address.city,
        "street" to address.street,
        "doorNumber" to address.doorNumber,
        "neighborhood" to address.neighborhood,
      )
    }

    fun convertUserToWritableMap(userModel: UserModel): WritableMap {
      return writableMapOf(
        "stoneCode" to userModel.stoneCode,
        "merchantName" to userModel.merchantName,
        "merchantAddress" to convertAddressToWritableMap(userModel.merchantAddress),
        "merchantDocumentNumber" to userModel.merchantDocumentNumber,
        "saleAffiliationKey" to userModel.saleAffiliationKey,
      )
    }

    fun convertTransactionToWritableMap(
      transactionObject: TransactionObject,
      messageFromAuthorizer: String? = null
    ): WritableMap {
      return writableMapOf(
        "amount" to transactionObject.amount,
        "emailSent" to transactionObject.emailSent,
        "timeToPassTransaction" to transactionObject.timeToPassTransaction,
        "initiatorTransactionKey" to transactionObject.initiatorTransactionKey,
        "acquirerTransactionKey" to transactionObject.acquirerTransactionKey,
        "cardHolderNumber" to transactionObject.cardHolderNumber,
        "cardHolderName" to transactionObject.cardHolderName,
        "date" to transactionObject.date,
        "time" to transactionObject.time,
        "aid" to transactionObject.aid,
        "arcq" to transactionObject.arcq,
        "authorizationCode" to transactionObject.authorizationCode,
        "iccRelatedData" to transactionObject.iccRelatedData,
        "transactionReference" to transactionObject.transactionReference,
        "actionCode" to transactionObject.actionCode,
        "commandActionCode" to transactionObject.commandActionCode,
        "pinpadUsed" to transactionObject.pinpadUsed,
        "saleAffiliationKey" to transactionObject.saleAffiliationKey,
        "cne" to transactionObject.cne,
        "cvm" to transactionObject.cvm,
        "balance" to transactionObject.balance,
        "serviceCode" to transactionObject.serviceCode,
        "subMerchantCategoryCode" to transactionObject.subMerchantCategoryCode,
        "entryMode" to transactionObject.entryMode.toString(),
        "cardBrandName" to transactionObject.cardBrandName,
        "instalmentTransaction" to writableMapOf(
          "count" to transactionObject.instalmentTransaction.count,
          "interest" to transactionObject.instalmentTransaction.interest,
          "name" to transactionObject.instalmentTransaction.toString(),
        ),
        "transactionStatus" to transactionObject.transactionStatus.toString(),
        "instalmentType" to transactionObject.instalmentType.toString(),
        "typeOfTransactionEnum" to transactionObject.typeOfTransactionEnum.toString(),
        //"cancellationDate" to transactionObject.cancellationDate?.toString(),
        "shortName" to transactionObject.shortName,
        "subMerchantAddress" to transactionObject.subMerchantAddress,
        "userModel" to convertUserToWritableMap(transactionObject.userModel),
        "cvv" to transactionObject.cvv,
        "isFallbackTransaction" to transactionObject.isFallbackTransaction,
        "subMerchantCity" to transactionObject.subMerchantCity,
        "subMerchantTaxIdentificationNumber" to transactionObject.subMerchantTaxIdentificationNumber,
        "subMerchantRegisteredIdentifier" to transactionObject.subMerchantRegisteredIdentifier,
        "subMerchantPostalAddress" to transactionObject.subMerchantPostalAddress,
        "appLabel" to transactionObject.appLabel,
        //"transAppSelectedInfo" to convertTransSelectedInfoToWritableMap(transactionObject.transAppSelectedInfo),
        "cardExpireDate" to transactionObject.cardExpireDate,
        "cardSequenceNumber" to transactionObject.cardSequenceNumber,
        "externalId" to transactionObject.externalId,
        "messageFromAuthorizer" to messageFromAuthorizer
      )
    }

    fun convertReadableMapToTransaction(transactionSetup: ReadableMap): TransactionObject {
      val requiredValues =
        listOf(
          "amountInCents",
          "typeOfTransaction",
          "capture",
          "installmentCount",
          "installmentHasInterest"
        )

      requiredValues.forEach {
        if (!transactionSetup.hasKey(it)) {
          throw Exception(
            String.format("%s is required", it)
          )
        }
      }

      val stoneTransaction = TransactionObject()

      /**
       * Required properties
       */
      stoneTransaction.amount = transactionSetup.getString("amountInCents")!!
      stoneTransaction.isCapture = transactionSetup.getBoolean("capture")
      stoneTransaction.typeOfTransaction =
        if (transactionSetup.getString("typeOfTransaction") == "CREDIT") {
          TypeOfTransactionEnum.CREDIT
        } else {
          TypeOfTransactionEnum.values().findLast {
            it.toString() == transactionSetup.getString("typeOfTransaction")!!
          } ?: throw Exception("typeOfTransaction not valid")
        }

      if (transactionSetup.getString("entryMode")?.isNotEmpty() == true) {
        stoneTransaction.entryMode = EntryMode.values().findLast {
          it.toString() == transactionSetup.getString("entryMode")!!
        } ?: throw Exception("entryMode not valid")
      }

      /**
       * We iterate through the enum and find the perfect match between count and has Interest
       */
      stoneTransaction.instalmentTransaction =
        InstalmentTransactionEnum.values().findLast {
          it.count == transactionSetup.getInt("installmentCount") && it.interest == transactionSetup.getBoolean(
            "installmentHasInterest"
          )
        } ?: throw Exception("Mix of installmentCount and installmentHasInterest not valid")

      /**
       * The thing about type-hinted languages is that you can't do dynamic assign out of the box
       */
      if (transactionSetup.getString("initiatorTransactionKey")?.isNotEmpty() == true) {
        stoneTransaction.initiatorTransactionKey =
          transactionSetup.getString("initiatorTransactionKey")
      }

      if (transactionSetup.getString("shortName")?.isNotEmpty() == true) {
        stoneTransaction.shortName =
          if (transactionSetup.getString("shortName")!!.length > 14) {
            transactionSetup.getString("shortName")!!.substring(0, 14)
          } else {
            transactionSetup.getString("shortName")!!
          }
      }

      if (transactionSetup.getString("subMerchantCategoryCode")?.isNotEmpty() == true) {
        stoneTransaction.subMerchantCategoryCode =
          transactionSetup.getString("subMerchantCategoryCode")!!
      }

      if (transactionSetup.getString("subMerchantAddress")?.isNotEmpty() == true) {
        stoneTransaction.subMerchantAddress =
          transactionSetup.getString("subMerchantAddress")!!
      }

      if (transactionSetup.getString("subMerchantCity")?.isNotEmpty() == true) {
        stoneTransaction.subMerchantCity =
          transactionSetup.getString("subMerchantCity")!!
      }

      if (transactionSetup.getString("subMerchantPostalAddress")?.isNotEmpty() == true) {
        stoneTransaction.subMerchantPostalAddress =
          transactionSetup.getString("subMerchantPostalAddress")!!
      }

      if (transactionSetup.getString("subMerchantRegisteredIdentifier")?.isNotEmpty() == true) {
        stoneTransaction.subMerchantRegisteredIdentifier =
          transactionSetup.getString("subMerchantRegisteredIdentifier")!!
      }

      if (transactionSetup.getString("subMerchantTaxIdentificationNumber")?.isNotEmpty() == true) {
        stoneTransaction.subMerchantRegisteredIdentifier =
          transactionSetup.getString("subMerchantTaxIdentificationNumber")!!
      }

      return stoneTransaction
    }
  }
}
