import * as React from 'react';
import { Alert, Button, ScrollView, StyleSheet, View } from 'react-native';
import DeviceInfo from 'react-native-device-info';
import AlertPrompt from 'react-native-prompt-android';
import * as StonePOS from 'react-native-stone-pos';
import type { TransactionType } from 'src/types';
import packageJson from '../package.json';
import { Spacer } from './Spacer';

type TestTransactionData = {
  [key: string]: TransactionType | null;
};

export default function App() {
  const [isSDKInitialized, setSDKInitialized] = React.useState<boolean>(false);
  const [testTransactionData, setTestTransactionData] =
    React.useState<TestTransactionData>({});

  StonePOS.useNativeEventListener('MAKE_TRANSACTION_PROGRESS', (data: any) => {
    console.log('Payment', data);
  });

  StonePOS.useNativeEventListener('VOID_TRANSACTION_PROGRESS', (data: any) => {
    console.log('Void', data);
  });

  const deviceInfoTest = async () => {
    Alert.alert(
      'Info',
      `Version Name: ${DeviceInfo.getVersion()}\nPackage Name: ${DeviceInfo.getBundleId()}\nIs Running in POS: ${
        StonePOS.IS_RUNNING_IN_POS
      }`
    );
  };

  const wrongCodeActivationTest = async () => {
    try {
      const hasActivated = await StonePOS.activateCode(
        '111111111',
        'Testing wrong activation code',
        'Activating...',
        true
      );
      if (hasActivated) {
        Alert.alert(
          'Error',
          "Oops, something went wrong, this code wasn't supposed to be activateable"
        );
      } else {
        Alert.alert('Success', 'Your test passed, no code activated');
      }
    } catch (e) {
      Alert.alert(
        'Success',
        `Your test passed, no code activated: ${JSON.stringify(e)}`
      );
    }
  };

  const approvedCodeActivationTest = () => {
    AlertPrompt('Input', 'Enter your activation code', async (text: string) => {
      if (text.length > 0) {
        try {
          const hasActivated = await StonePOS.activateCode(
            text,
            'Testing accepted activation code',
            'Activating...',
            true
          );
          if (hasActivated) {
            Alert.alert('Success', 'Your test passed, code activated');
          } else {
            Alert.alert('Error', 'Oops, something went wrong');
          }
        } catch (e) {
          Alert.alert(
            'Error',
            `Your test failed, no code activated: ${JSON.stringify(e)}`
          );
        }
      }
    });
  };

  const getActivatedCodesTest = async () => {
    try {
      const activatedCodes = await StonePOS.getActivatedCodes();
      Alert.alert(
        'Success',
        `Activated Codes: ${JSON.stringify(activatedCodes)}`
      );
    } catch (e) {
      Alert.alert(
        'Error',
        `Your test failed, no code activated: ${JSON.stringify(e)}`
      );
    }
  };

  const oneInstallmentCreditDeniedTest = async () => {
    try {
      const transactionStatus = await StonePOS.makeTransaction({
        installmentCount: 1,
        installmentHasInterest: false,
        typeOfTransaction: 'CREDIT',
        amountInCents: '51',
        shortName: 'TST_004',
        capture: true,
        useDefaultUI: true,
      });

      if (transactionStatus.transactionStatus == 'DECLINED') {
        Alert.alert(
          'Success',
          `Your test passed\nTransaction Date: ${transactionStatus.date}\nTransaction ATK: ${transactionStatus.acquirerTransactionKey}\nMessage From Authorizer: ${transactionStatus.messageFromAuthorizer}`
        );
      } else {
        Alert.alert(
          'Error',
          `Your test failed: ${transactionStatus.transactionStatus}`
        );
      }
    } catch (e) {
      Alert.alert('Error', `Your test failed: ${JSON.stringify(e)}`);
    }
  };

  const wrongPasswordCreditTest = async () => {
    try {
      const transactionStatus = await StonePOS.makeTransaction({
        installmentCount: 1,
        installmentHasInterest: false,
        typeOfTransaction: 'CREDIT',
        amountInCents: '55',
        shortName: 'TST_006',
        capture: true,
        useDefaultUI: true,
      });

      if (transactionStatus.transactionStatus == 'DECLINED') {
        Alert.alert(
          'Success',
          `Your test passed\nTransaction Date: ${transactionStatus.date}\nTransaction ATK: ${transactionStatus.acquirerTransactionKey}\nMessage From Authorizer: ${transactionStatus.messageFromAuthorizer}`
        );
      } else {
        Alert.alert(
          'Error',
          `Your test failed: ${transactionStatus.transactionStatus}`
        );
      }
    } catch (e) {
      Alert.alert('Error', `Your test failed: ${JSON.stringify(e)}`);
    }
  };

  const approvedCreditTest = async (
    typeOfTransaction = 'CREDIT',
    amountInCents: String = '500',
    shortName: String = 'TST_007',
    installmentCount: Number = 1,
    installmentHasInterest: Boolean = false
  ) => {
    try {
      const transactionStatus = await StonePOS.makeTransaction({
        installmentCount,
        installmentHasInterest,
        typeOfTransaction: typeOfTransaction as any,
        amountInCents,
        shortName,
        capture: true,
        useDefaultUI: true,
      });

      if (transactionStatus.transactionStatus == 'APPROVED') {
        console.log(
          'Has printed merchant',
          await StonePOS.printReceiptInPOSPrinter(
            'MERCHANT',
            transactionStatus.acquirerTransactionKey!,
            false,
            'Printing merchant slip...'
          )
        );

        console.log(
          'Has printed client',
          await StonePOS.printReceiptInPOSPrinter(
            'CLIENT',
            transactionStatus.acquirerTransactionKey!,
            false,
            'Printing client slip...'
          )
        );

        Alert.alert(
          'Success',
          `Your test passed\nTransaction Date: ${transactionStatus.date}\nTransaction ATK: ${transactionStatus.acquirerTransactionKey}\nMessage From Authorizer: ${transactionStatus.messageFromAuthorizer}`
        );

        setTestTransactionData({
          ...testTransactionData,
          [shortName as string]: transactionStatus,
        });
      } else {
        Alert.alert(
          'Error',
          `Your test failed: ${transactionStatus.transactionStatus}`
        );
      }
    } catch (e) {
      Alert.alert('Error', `Your test failed: ${JSON.stringify(e)}`);
    }
  };

  const voidTransactionFromTest = async (testId: string) => {
    if (testTransactionData[testId]?.acquirerTransactionKey) {
      try {
        const transactionStatus = await StonePOS.voidTransaction(
          testTransactionData[testId]?.acquirerTransactionKey!
        );

        if (transactionStatus.transactionStatus == 'CANCELLED') {
          Alert.alert(
            'Success',
            `Your test passed\nTransaction Date: ${transactionStatus.date}\nTransaction ATK: ${transactionStatus.acquirerTransactionKey}\nMessage From Authorizer: ${transactionStatus.messageFromAuthorizer}`
          );

          setTestTransactionData({
            ...testTransactionData,
            [testId]: null,
          });
        } else {
          Alert.alert(
            'Error',
            `Your test failed: ${transactionStatus.transactionStatus}`
          );
        }
      } catch (e) {
        Alert.alert('Error', `Your test failed: ${JSON.stringify(e)}`);
      }
    } else {
      Alert.alert('Error', 'Your test failed: No previous transaction found');
    }
  };

  const lastTransactionDataTest = async () => {
    try {
      const transactionStatus = await StonePOS.getLastTransaction();

      if (transactionStatus) {
        Alert.alert(
          'Success',
          `Your test passed\nTransaction Data: ${JSON.stringify(
            transactionStatus
          )}`
        );
      } else {
        Alert.alert('Error', `Your test failed, no transaction data`);
      }
    } catch (e) {
      Alert.alert(
        'Error',
        `Your test failed, no transaction data: ${JSON.stringify(e)}`
      );
    }
  };

  const htmlPrintTest = async () => {
    try {
      var htmlMapped: String[] = Array(2)
        .fill(0)
        .map((_, index) => {
          return `<p>Hello world, this is a HTML print ${index}</p><b>Hello in bold</b><center>Hello in center</center>`;
        });

      const transactionStatus = await StonePOS.printHTMLInPOSPrinter(
        `<html><head><style type='text/css'>body { font-size: 22pt; }</style></head><body>${htmlMapped.join(
          '\n'
        )}</body>`
      );

      if (transactionStatus) {
        Alert.alert(
          'Success',
          `Your test passed\nTransaction Data: ${JSON.stringify(
            transactionStatus
          )}`
        );
      } else {
        Alert.alert('Error', `Your test failed, no transaction data`);
      }
    } catch (e) {
      Alert.alert(
        'Error',
        `Your test failed, no transaction data: ${JSON.stringify(e)}`
      );
    }
  };

  const sdkInitialize = async () => {
    try {
      const sdkInitializeResult = await StonePOS.initSDK(packageJson.name);

      if (sdkInitializeResult) {
        setSDKInitialized(true);
      } else {
        Alert.alert('Error', `Failed to initialize`);
      }
    } catch (e) {
      Alert.alert('Error', `Failed to initialize ${JSON.stringify(e)}`);
    }
  };

  return (
    <View style={styles.container}>
      {isSDKInitialized ? (
        <ScrollView
          style={styles.scroll}
          contentContainerStyle={styles.scrollContent}
        >
          <Button
            title="Test 001 - Application Info"
            onPress={() => deviceInfoTest()}
          />
          <Spacer />
          <Button
            title="Test 002 - Denied Activation"
            onPress={() => wrongCodeActivationTest()}
          />
          <Spacer />
          <Button
            title="Test 003 - Approved Activation"
            onPress={() => approvedCodeActivationTest()}
          />
          <Spacer />
          <Button
            title="Test 003/1 - Get Activated Codes"
            onPress={() => getActivatedCodesTest()}
          />
          <Spacer />
          <Button
            title="Test 004 - 1X Credit Denied"
            onPress={() => oneInstallmentCreditDeniedTest()}
          />
          <Spacer />
          <Button
            title="Test 005 - Transaction Data"
            onPress={() => lastTransactionDataTest()}
          />
          <Spacer />
          <Button
            title="Test 006 - 1x Credit Denied (Wrong Password)"
            onPress={() => wrongPasswordCreditTest()}
          />
          <Spacer />
          <Button
            title="Test 007 - 1x Credit Approved"
            onPress={() => approvedCreditTest()}
          />
          <Spacer />
          <Button
            title="Test 008 - Transaction Data"
            onPress={() => lastTransactionDataTest()}
          />
          <Spacer />
          <Button
            title="Test 009 - Void Approved"
            onPress={() => voidTransactionFromTest('TST_007')}
          />
          <Spacer />
          <Button
            title="Test 010 - Transaction Data"
            onPress={() => lastTransactionDataTest()}
          />
          <Spacer />
          <Button
            title="Test 011 - 2x Credit Approved"
            onPress={() =>
              approvedCreditTest('CREDIT', '10000', 'TST_011', 2, false)
            }
          />
          <Spacer />
          <Button
            title="Test 012 - Void Approved"
            onPress={() => voidTransactionFromTest('TST_011')}
          />
          <Spacer />
          <Button
            title="Test 013 - Transaction Data"
            onPress={() => lastTransactionDataTest()}
          />
          <Spacer />
          <Button
            title="Test 014 - Debit Approved"
            onPress={() =>
              approvedCreditTest('DEBIT', '8000', 'TST_014', 1, false)
            }
          />
          <Spacer />
          <Button
            title="Test 015 - Void Approved"
            onPress={() => voidTransactionFromTest('TST_014')}
          />
          <Spacer />
          <Button
            title="Test 016 - Transaction Data"
            onPress={() => lastTransactionDataTest()}
          />

          <Spacer />
          <Button
            title="Test 017 - Print HTML"
            onPress={() => htmlPrintTest()}
          />
        </ScrollView>
      ) : (
        <Button title="Initialize SDK" onPress={() => sdkInitialize()} />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  scroll: {
    width: '100%',
    //marginBottom: 10,
  },
  scrollContent: {
    padding: 20,
  },
  container: {
    flex: 1,
    alignItems: 'center',
    width: '100%',
    justifyContent: 'center',
  },
});
