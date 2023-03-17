/**
 * @since 0.1.7
 */

export type AddressModelType = {
  distric: String;
  city: String;
  street: String;
  doorNumber: String;
  neighborhood: String;
};

/**
 * @since 0.1.7
 */
export type UserModelType = {
  stoneCode: String;
  merchantName: String;
  merchantAddress: AddressModelType;
  merchantDocumentNumber: String;
  saleAffiliationKey: String;
};

/**
 * @since 0.1.7
 */
export type TransactionTypeInfoType = {
  id: Number;
  appLabel: String;
  transTypeEnum: String;
};

/**
 * @since 0.1.7
 */
export type TransSelectedInfoType = {
  brandName: String;
  aid: String;
  transactionTypeInfo: TransactionTypeInfoType;
  cardAppLabel: String;
  paymentBusinessModel: String;
  brandId: Number;
};

/**
 * @since 0.1.7
 */
export type InstalmentTransactionType = {
  count?: Number;
  interest?: Boolean;
  name?: String;
};

/**
 * @since 0.1.14
 */
export type TransactionType = {
  amount: String;
  emailSent?: String;
  timeToPassTransaction?: String;
  initiatorTransactionKey?: String;

  /**
   * Also know as ATK
   */
  acquirerTransactionKey?: String;

  cardHolderNumber?: String;
  cardHolderName?: String;
  date?: String;
  time?: String;
  aid?: String;
  arcq?: String;
  authorizationCode?: String;
  iccRelatedData?: String;
  transactionReference?: String;
  actionCode?: String;
  commandActionCode?: String;
  pinpadUsed?: String;
  saleAffiliationKey?: String;
  cne?: String;
  cvm?: String;
  balance?: String;
  serviceCode?: String;
  subMerchantCategoryCode?: String;
  entryMode?: String;
  cardBrandName?: String;
  instalmentTransaction?: InstalmentTransactionType;
  transactionStatus?:
    | 'UNKNOWN'
    | 'APPROVED'
    | 'DECLINED'
    | 'DECLINED_BY_CARD'
    | 'CANCELLED'
    | 'PARTIAL_APPROVED'
    | 'TECHNICAL_ERROR'
    | 'REJECTED'
    | 'WITH_ERROR'
    | 'PENDING_REVERSAL'
    | 'PENDING'
    | 'REVERSED';
  instalmentType?: String;
  typeOfTransactionEnum?: String;
  cancellationDate?: String;
  shortName?: String;
  subMerchantAddress?: String;
  userModel?: UserModelType;
  cvv?: String;
  isFallbackTransaction?: Boolean;
  subMerchantCity?: String;
  subMerchantTaxIdentificationNumber?: String;
  subMerchantRegisteredIdentifier?: String;
  subMerchantPostalAddress?: String;
  appLabel?: String;
  transAppSelectedInfo?: TransSelectedInfoType;
  cardExpireDate?: String;
  cardSequenceNumber?: String;
  externalId?: String;
  messageFromAuthorizer?: String;
};

/**
 * @since 0.1.7
 */
export type TransactionSetupType = {
  /**
   * Stone requests the amount to be in cents, so, `R$ 1,00` will be `100` here.
   */
  amountInCents: String;

  /**
   * When you have more than one stoneCode activated use this to select which one to use.
   */
  stoneCode?: String;

  /**
   * Bluetooth PinPad Mac Address when you not running in POS mode. Leave this empty to use the first pinpad in the list.
   */
  pinpadMacAddress?: String;

  /**
   * If you set this to `false` you need to call `captureTransaction` later.
   */
  capture: Boolean;

  /**
   * `INSTANT_PAYMENT` is here, but at least as of version `4.1.1` it is not yet implemented in StoneSDK, they ask you to use the `Intents Integration` if you want to use Instant Payments.
   */
  typeOfTransaction: 'DEBIT' | 'CREDIT' | 'VOUCHER' | 'INSTANT_PAYMENT' | 'PIX';

  entryMode?:
    | 'MAGNETIC_STRIPE'
    | 'CHIP_N_PIN'
    | 'CONTACTLESS'
    | 'CONTACTLESS_MAG'
    | 'QRCODE'
    | 'UNKNOWN';

  /**
   * Send anything greater than 1 here to make an installment transaction.
   */
  installmentCount?: Number;

  /**
   * If this is a `Parcelado pelo comprador`, or `Parcelado pelo comerciante`. (Default to false, which means `Parcelado pelo comerciante`)
   */
  installmentHasInterest?: Boolean;

  /**
   * Use this if you are running multiple transactions.
   */
  initiatorTransactionKey?: String;

  /**
   * Usually used by Sub-Acquirer Apps. (This will appear in the statement descriptor)
   */
  shortName?: String;

  /**
   * Used by Sub-Acquirer Apps. (Also know as MCC)
   */
  subMerchantCategoryCode?: String;

  /**
   * Used by Sub-Acquirer Apps.
   */
  subMerchantAddress?: String;

  /**
   * Used by Sub-Acquirer Apps.
   */
  subMerchantCity?: String;

  /**
   * Used by Sub-Acquirer Apps.
   */
  subMerchantPostalAddress?: String;

  /**
   * Used by Sub-Acquirer Apps. (Also know as your internal Merchant ID)
   */
  subMerchantRegisteredIdentifier?: String;

  /**
   * Used by Sub-Acquirer Apps. (Also know as CPF/CNPJ)
   */
  subMerchantTaxIdentificationNumber?: String;

  /**
   * Wheter to use Stone's default progress dialog or not.
   */
  useDefaultUI?: Boolean;

  /**
   * Default UI dialog title.
   */
  dialogTitle?: String;

  /**
   * Default UI dialog main message.
   */
  dialogMessage?: String;
};

/**
 * @since 0.1.7
 */
export type MailContact = {
  email: String;
  name?: String;
};

/**
 * CLIENT means Customer. MERCHANT of course means Merchant.
 *
 * @since 0.1.7
 */
export type ReceiptType = 'CLIENT' | 'MERCHANT';

/**
 * This is used in the `useNativeEventListener` hook.
 *
 * @since 0.1.7
 */
export type ProgressEventName =
  | 'REVERSE_PENDING_TRANSACTIONS_PROGRESS'
  | 'VOID_TRANSACTION_PROGRESS'
  | 'CAPTURE_TRANSACTION_PROGRESS'
  | 'MAKE_TRANSACTION_PROGRESS'
  | 'SEND_TRANSACTION_RECEIPT_MAIL_PROGRESS'
  | 'FETCH_TRANSACTION_FOR_CARD_PROGRESS'
  | 'DISPLAY_MESSAGE_IN_PINPAD_PROGRESS'
  | 'CONNECT_TO_PINPAD_PROGRESS'
  | 'PRINT_RECEIPT_IN_POS_PRINTER_PROGRESS'
  | 'PRINT_HTML_IN_POS_PRINTER_PROGRESS'
  | 'MIFARE_PROGRESS'
  | String;

/**
 * Mifare Key Type
 *
 * @since 0.1.11
 */
export enum MifareKeyType {
  TypeA = 0,
  TypeB = 1,
}
