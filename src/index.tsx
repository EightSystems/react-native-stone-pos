/**
 * @since 0.1.7
 */
import { useEffect, useRef } from 'react';
import { NativeEventEmitter, NativeModules, Platform } from 'react-native';
import type {
  MailContact,
  ReceiptType,
  TransactionSetupType,
  TransactionType,
  UserModelType,
  ProgressEventName,
} from './types';

export type {
  MailContact,
  ReceiptType,
  TransactionSetupType,
  TransactionType,
  UserModelType,
  ProgressEventName,
  AddressModelType,
  InstalmentTransactionType,
  TransSelectedInfoType,
  TransactionTypeInfoType,
} from './types';

const LINKING_ERROR =
  `The package 'react-native-stone-pos' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const StonePos = NativeModules.StonePos
  ? NativeModules.StonePos
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

/**
 * If true we are running in a POS, like Sunmi, Ingenico, or PAX
 * @since 0.1.7
 * @category Constants
 */
export const IS_RUNNING_IN_POS = new Proxy(
  {},
  {
    get() {
      return StonePos.getConstants().IS_RUNNING_IN_POS;
    },
  }
);

const eventEmitter = new NativeEventEmitter(NativeModules.StonePos);

/**
 * React hook so you can listen to the native events.
 * This hook updates the callback even when your state changes.
 *
 * @category Hooks
 * @since 0.1.7
 * @param eventName - Native progress event name
 * @param handler - Callback that receives the event data
 */
export function useNativeEventListener(
  eventName: ProgressEventName,
  handler: (data?: any) => void
) {
  const savedHandler = useRef<(data?: any) => void>();

  useEffect(() => {
    savedHandler.current = handler;
  }, [handler]);

  useEffect(() => {
    const eventListener = (event: any) => {
      if (savedHandler.current) {
        savedHandler.current(event);
      }
    };

    const eventSubscription = eventEmitter.addListener(
      eventName as string,
      eventListener
    );

    return () => {
      eventSubscription.remove();
    };
  }, [eventName]);
}

/**
 * Initialize Stone SDK.
 * @category SDK
 * @since 0.1.7
 * @param appName - Your application name
 */
export function initSDK(appName: String): Promise<boolean> {
  return StonePos.initSDK(appName);
}

/**
 * Activate the StoneCode. It accepts any StoneCode and can use the default StoneUI for progress.
 * @category Activation
 * @since 0.1.7
 * @param stoneCode - Merchant's Stone Code.
 * @param dialogMessage - Default UI dialog main message.
 * @param dialogTitle - Default UI dialog title.
 * @param useDefaultUI - Wheter to use Stone's default progress dialog or not.
 */
export function activateCode(
  stoneCode: String,
  dialogMessage: String | null = null,
  dialogTitle: String | null = null,
  useDefaultUI: Boolean = true
): Promise<boolean> {
  return StonePos.activateCode(
    stoneCode,
    dialogMessage,
    dialogTitle,
    useDefaultUI
  );
}

/**
 * Deactivate StoneCodes from the device. If the Stone Code is not found in the device, it will throw an exception.
 * @category Activation
 * @since 0.1.7
 * @param stoneCode - Merchant's Stone Code.
 * @param dialogMessage - Default UI dialog main message.
 * @param dialogTitle - Default UI dialog title.
 * @param useDefaultUI - Wheter to use Stone's default progress dialog or not.
 */
export function deactivateCode(
  stoneCode: String,
  dialogMessage: String | null = null,
  dialogTitle: String | null = null,
  useDefaultUI: Boolean = true
): Promise<boolean> {
  return StonePos.deactivateCode(
    stoneCode,
    dialogMessage,
    dialogTitle,
    useDefaultUI
  );
}

/**
 * Get a list of all activated codes in the device. Or an empty list if no code is activated (Highly unlike in a POS).
 * @category Activation
 * @since 0.1.7
 */
export function getActivatedCodes(): Promise<UserModelType> {
  return StonePos.getActivatedCodes();
}

/**
 * Gets all the transactions ordered by their ID in Desc order.
 * @category Transaction Info
 * @since 0.1.7
 */
export function getAllTransactionsOrderByIdDesc(): Promise<TransactionType[]> {
  return StonePos.getAllTransactionsOrderByIdDesc();
}

/**
 * Gets the last transaction information.
 * @category Transaction Info
 * @since 0.1.7
 */
export function getLastTransaction(): Promise<TransactionType | null> {
  return StonePos.getLastTransaction();
}

/**
 * Find a transaction using the Authorization Code.
 * @category Transaction Info
 * @since 0.1.7
 * @param authorizationCode - Transaction Authorization Code.
 */
export function findTransactionWithAuthorizationCode(
  authorizationCode: String
): Promise<TransactionType | null> {
  return StonePos.findTransactionWithAuthorizationCode(authorizationCode);
}

/**
 * Finds a transaction with the Initiator Transaction Key (ATK).
 * @category Transaction Info
 * @since 0.1.7
 * @param initiatorTransactionKey - Transaction ATK.
 */
export function findTransactionWithInitiatorTransactionKey(
  initiatorTransactionKey: String
): Promise<TransactionType | null> {
  return StonePos.findTransactionWithInitiatorTransactionKey(
    initiatorTransactionKey
  );
}

/**
 * Finds a transaction with the transaction integer ID.
 * @category Transaction Info
 * @since 0.1.7
 * @param transactionId - Stone Internal DB transaction ID (Integer)
 */
export function findTransactionWithId(
  transactionId: Number
): Promise<TransactionType | null> {
  return StonePos.findTransactionWithId(transactionId);
}

/**
 * Reverse any pending transaction.
 * @category Transaction Executor
 * @since 0.1.7
 * @param dialogMessage - Default UI dialog main message.
 * @param dialogTitle - Default UI dialog title.
 * @param useDefaultUI - Wheter to use Stone's default progress dialog or not.
 * @param progressCallbackEventName - The native event name to map with the `useNativeEventListener` hook.
 */
export function reversePendingTransactions(
  dialogMessage: String | null = null,
  dialogTitle: String | null = null,
  useDefaultUI: boolean = true,
  progressCallbackEventName: ProgressEventName = 'REVERSE_PENDING_TRANSACTIONS_PROGRESS'
): Promise<boolean> {
  return StonePos.reversePendingTransactions(
    useDefaultUI,
    dialogMessage,
    dialogTitle,
    progressCallbackEventName
  );
}

/**
 * Voids a transaction using it's ATK.
 * @category Transaction Executor
 * @since 0.1.7
 * @param transactionAtk - Transaction Initiator Key - ATK
 * @param dialogMessage - Default UI dialog main message.
 * @param dialogTitle - Default UI dialog title.
 * @param useDefaultUI - Wheter to use Stone's default progress dialog or not.
 * @param progressCallbackEventName - The native event name to map with the `useNativeEventListener` hook.
 */
export function voidTransaction(
  transactionAtk: String,
  dialogMessage: String | null = null,
  dialogTitle: String | null = null,
  useDefaultUI: boolean = true,
  progressCallbackEventName: ProgressEventName = 'VOID_TRANSACTION_PROGRESS'
): Promise<TransactionType> {
  return StonePos.voidTransaction(
    transactionAtk,
    dialogMessage,
    dialogTitle,
    useDefaultUI,
    progressCallbackEventName
  );
}

/**
 * When you use `capture` = false at the `makeTransaction` method you need to call this method in order to "capture" the transaction hence confirming the money.
 * @category Transaction Executor
 * @since 0.1.7
 * @param transactionAtk - Transaction Initiator Key - ATK
 * @param dialogMessage - Default UI dialog main message.
 * @param dialogTitle - Default UI dialog title.
 * @param useDefaultUI - Wheter to use Stone's default progress dialog or not.
 * @param progressCallbackEventName - The native event name to map with the `useNativeEventListener` hook.
 */
export function captureTransaction(
  transactionAtk: String,
  dialogMessage: String | null = null,
  dialogTitle: String | null = null,
  useDefaultUI: boolean = true,
  progressCallbackEventName: ProgressEventName = 'CAPTURE_TRANSACTION_PROGRESS'
): Promise<TransactionType> {
  return StonePos.captureTransaction(
    transactionAtk,
    dialogMessage,
    dialogTitle,
    useDefaultUI,
    progressCallbackEventName
  );
}

/**
 * Creates a transaction.
 *
 * Stone uses an `InstalmentTransactionEnum` to decide on which installment plan is chosen,
 * we decided to make it simplier, just pass installmentCount, and installmentHasInterest,
 * that way we try to figure out the enum we need to use.
 *
 * @category Transaction Executor
 * @since 0.1.7
 * @param transactionSetup - The transaction information.
 * @param progressCallbackEventName - The native event name to map with the `useNativeEventListener` hook.
 */
export function makeTransaction(
  {
    installmentCount = 1,
    installmentHasInterest = false,
    ...restOfTransactionSetup
  }: TransactionSetupType,
  progressCallbackEventName: ProgressEventName = 'MAKE_TRANSACTION_PROGRESS'
): Promise<TransactionType> {
  return StonePos.makeTransaction(
    {
      installmentCount,
      installmentHasInterest,
      ...restOfTransactionSetup,
    },
    progressCallbackEventName
  );
}

/**
 * Sends a transaction receipt via email.
 * Please note: Looks like sometimes Stone has issues sending the receipts via e-mail. So, please test it.
 *
 * @category Transaction Receipts
 * @since 0.1.7
 * @param transactionAtk - Transaction Initiator Key - ATK
 * @param receiptType - If it's the customer (client), or merchant receipt.
 * @param toContact - List of emails to send.
 * @param fromContact - Which email to use in the `From` field.
 * @param dialogMessage - Default UI dialog main message.
 * @param dialogTitle - Default UI dialog title.
 * @param useDefaultUI - Wheter to use Stone's default progress dialog or not.
 * @param progressCallbackEventName - The native event name to map with the `useNativeEventListener` hook.
 */
export function sendTransactionReceiptMail(
  transactionAtk: String,
  receiptType: ReceiptType = 'CLIENT',
  toContact: MailContact[] = [],
  fromContact: MailContact,
  dialogMessage: String | null = null,
  dialogTitle: String | null = null,
  useDefaultUI: Boolean = true,
  progressCallbackEventName: ProgressEventName = 'SEND_TRANSACTION_RECEIPT_MAIL_PROGRESS'
): Promise<TransactionType> {
  return StonePos.sendTransactionReceiptMail(
    transactionAtk,
    receiptType,
    toContact,
    fromContact,
    dialogMessage,
    dialogTitle,
    useDefaultUI,
    progressCallbackEventName
  );
}

/**
 * Returns all transactions that are registered in this device for a scanned Card
 *
 * @category Transaction Info
 * @since 0.1.7
 * @param pinpadMacAddress - When using a Bluetooth PinPad you need to send it's mac address here, if you send null here, we will use the first in the list if you are not running in a POS.
 * @param dialogMessage - Default UI dialog main message.
 * @param dialogTitle - Default UI dialog title.
 * @param useDefaultUI - Wheter to use Stone's default progress dialog or not.
 * @param progressCallbackEventName - The native event name to map with the `useNativeEventListener` hook.
 */
export function fetchTransactionsForCard(
  pinpadMacAddress: String | null = null,
  dialogMessage: String | null = null,
  dialogTitle: String | null = null,
  useDefaultUI: Boolean = true,
  progressCallbackEventName: ProgressEventName = 'FETCH_TRANSACTION_FOR_CARD_PROGRESS'
): Promise<TransactionType[]> {
  return StonePos.fetchTransactionsForCard(
    pinpadMacAddress,
    dialogMessage,
    dialogTitle,
    useDefaultUI,
    progressCallbackEventName
  );
}

/**
 * Pinpad Related Methods
 */

/**
 * Use this if you are running in a mobile and want to display a message in PinPad, if you don't send the macAddress we try to use the first pinpad at the list.
 *
 * @category Bluetooth Pinpad Methods
 * @since 0.1.7
 * @param pinpadMessage - The message to display in the PinPad.
 * @param pinpadMacAddress - When using a Bluetooth PinPad you need to send it's mac address here, if you send null here, we will use the first in the list if you are not running in a POS.
 * @param dialogMessage - Default UI dialog main message.
 * @param dialogTitle - Default UI dialog title.
 * @param useDefaultUI - Wheter to use Stone's default progress dialog or not.
 * @param progressCallbackEventName - The native event name to map with the `useNativeEventListener` hook.
 */
export function displayMessageInPinPad(
  pinpadMessage: String,
  pinpadMacAddress: String | null = null,
  dialogMessage: String | null = null,
  dialogTitle: String | null = null,
  useDefaultUI: Boolean = true,
  progressCallbackEventName: ProgressEventName = 'DISPLAY_MESSAGE_IN_PINPAD_PROGRESS'
): Promise<boolean> {
  return StonePos.displayMessageInPinPad(
    pinpadMessage,
    pinpadMacAddress,
    dialogMessage,
    dialogTitle,
    useDefaultUI,
    progressCallbackEventName
  );
}

/**
 * You need to send the pinPad name and mac address, for discovery you can use any other react native bluetooth listing package.
 *
 * @category Bluetooth Pinpad Methods
 * @since 0.1.7
 * @param pinpadName - A friendly name to the PinPad (Usually the bluetooth discover name).
 * @param pinpadMacAddress - Mac Address for this PinPad.
 * @param dialogMessage - Default UI dialog main message.
 * @param dialogTitle - Default UI dialog title.
 * @param useDefaultUI - Wheter to use Stone's default progress dialog or not.
 * @param progressCallbackEventName - The native event name to map with the `useNativeEventListener` hook.
 */
export function connectToPinPad(
  pinpadName: String,
  pinpadMacAddress: String,
  dialogMessage: String | null = null,
  dialogTitle: String | null = null,
  useDefaultUI: Boolean = true,
  progressCallbackEventName: ProgressEventName = 'CONNECT_TO_PINPAD_PROGRESS'
): Promise<boolean> {
  return StonePos.connectToPinPad(
    pinpadName,
    pinpadMacAddress,
    dialogMessage,
    dialogTitle,
    useDefaultUI,
    progressCallbackEventName
  );
}

/**
 * POS Printing Related Methods
 */

/**
 * Prints a receipt when you are using a POS.
 *
 * @category POS Methods
 * @since 0.1.7
 * @param receiptType - Which mode you want to print: `CLIENT` - customer, `MERCHANT` - merchant.
 * @param transactionAtk - Transaction Initiator Key - ATK
 * @param isReprint - If you are printing the receipt again (This will add `REPRINT` in the top corner of the receipt).
 * @param dialogMessage - Default UI dialog main message.
 * @param dialogTitle - Default UI dialog title.
 * @param useDefaultUI - Wheter to use Stone's default progress dialog or not.
 * @param progressCallbackEventName - The native event name to map with the `useNativeEventListener` hook.
 */
export function printReceiptInPOSPrinter(
  receiptType: ReceiptType,
  transactionAtk: String,
  isReprint: Boolean = false,
  dialogMessage: String | null = null,
  dialogTitle: String | null = null,
  useDefaultUI: Boolean = true,
  progressCallbackEventName: ProgressEventName = 'PRINT_RECEIPT_IN_POS_PRINTER_PROGRESS'
): Promise<boolean> {
  return StonePos.printReceiptInPOSPrinter(
    receiptType,
    transactionAtk,
    isReprint,
    dialogMessage,
    dialogTitle,
    useDefaultUI,
    progressCallbackEventName
  );
}

/**
 * We render the HTML in a WebKit out of window browser and do the bitmap splitting and send it to the printer.
 * Please note that we can't guarantee the device has enough memory to execute this.
 *
 * @category POS Methods
 * @since 0.1.7
 * @param htmlContent - The HTML content you want us to render and print.
 * @param dialogMessage - Default UI dialog main message.
 * @param dialogTitle - Default UI dialog title.
 * @param useDefaultUI - Wheter to use Stone's default progress dialog or not.
 * @param progressCallbackEventName - The native event name to map with the `useNativeEventListener` hook.
 */
export function printHTMLInPOSPrinter(
  htmlContent: String,
  dialogMessage: String | null = null,
  dialogTitle: String | null = null,
  useDefaultUI: Boolean = true,
  progressCallbackEventName: ProgressEventName = 'PRINT_RECEIPT_IN_POS_PRINTER_PROGRESS'
): Promise<boolean> {
  return StonePos.printHTMLInPOSPrinter(
    htmlContent,
    dialogMessage,
    dialogTitle,
    useDefaultUI,
    progressCallbackEventName
  );
}
