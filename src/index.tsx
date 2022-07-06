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

export const { IS_RUNNING_IN_POS } = StonePos.getConstants();

const eventEmitter = new NativeEventEmitter(NativeModules.StonePos);

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

export function initSDK(appName: String): Promise<boolean> {
  return StonePos.initSDK(appName);
}

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

export function getActivatedCodes(): Promise<UserModelType> {
  return StonePos.getActivatedCodes();
}

export function getAllTransactionsOrderByIdDesc(): Promise<TransactionType[]> {
  return StonePos.getAllTransactionsOrderByIdDesc();
}

export function getLastTransaction(): Promise<TransactionType | null> {
  return StonePos.getLastTransaction();
}

export function findTransactionWithAuthorizationCode(
  authorizationCode: String
): Promise<TransactionType | null> {
  return StonePos.findTransactionWithAuthorizationCode(authorizationCode);
}

export function findTransactionWithInitiatorTransactionKey(
  initiatorTransactionKey: String
): Promise<TransactionType | null> {
  return StonePos.findTransactionWithInitiatorTransactionKey(
    initiatorTransactionKey
  );
}

export function findTransactionWithId(
  transactionId: Number
): Promise<TransactionType | null> {
  return StonePos.findTransactionWithId(transactionId);
}

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
 * You need to pass a progressCallbackEventName as a string,
 * and then use the `useNativeEventListener` for listening to the status updates
 * As we can't call callbacks from NativeModules this was the used solution.
 *
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
 * In order for a transaction to be captured you need to pass `capture` = false at the `makeTransaction` method, and then get the ATK and call this method.
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
 * You need to pass a progressCallbackEventName as a string,
 * and then use the `useNativeEventListener` for listening to the status updates
 * As we can't call callbacks from NativeModules this was the used solution.
 *
 * Stone uses an `InstalmentTransactionEnum` to decide on how the installment plan will be,
 * we decided to make it simplier, just pass installmentCount, and installmentHasInterest,
 * that way we try to figure out the enum we need to use.
 *
 * @param transactionSetup TransactionSetupType
 * @param progressCallbackEventName String
 * @returns Promise<TransactionType>
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
