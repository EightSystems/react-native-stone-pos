export type AddressModelType = {
  distric: String;
  city: String;
  street: String;
  doorNumber: String;
  neighborhood: String;
};

export type UserModelType = {
  stoneCode: String;
  merchantName: String;
  merchantAddress: AddressModelType;
  merchantDocumentNumber: String;
  saleAffiliationKey: String;
};

export type TransactionTypeInfoType = {
  id: Number;
  appLabel: String;
  transTypeEnum: String;
};

export type TransSelectedInfoType = {
  brandName: String;
  aid: String;
  transactionTypeInfo: TransactionTypeInfoType;
  cardAppLabel: String;
  paymentBusinessModel: String;
  brandId: Number;
};

export type InstalmentTransactionType = {
  count?: Number;
  interest?: Boolean;
  name?: String;
};

export type TransactionType = {
  amount: String;
  emailSent?: String;
  timeToPassTransaction?: String;
  initiatorTransactionKey?: String;
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

export type TransactionSetupType = {
  stoneCode?: String;
  pinpadMacAddress?: String;
  capture: Boolean;
  useDefaultUI?: Boolean;
  amountInCents: String;
  typeOfTransaction: 'DEBIT' | 'CREDIT' | 'VOUCHER' | 'INSTANT_PAYMENT';
  installmentCount?: Number;
  installmentHasInterest?: Boolean;
  initiatorTransactionKey?: String;
  shortName?: String;
  subMerchantCategoryCode?: String;
  subMerchantAddress?: String;
  subMerchantCity?: String;
  subMerchantPostalAddress?: String;
  subMerchantRegisteredIdentifier?: String;
  subMerchantTaxIdentificationNumber?: String;
};

export type MailContact = {
  email: String;
  name?: String;
};

export type ReceiptType = 'CLIENT' | 'MERCHANT';

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
  | String;
