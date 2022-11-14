import * as React from 'react';
import {
  Alert,
  ScrollView,
  StyleSheet,
  Pressable,
  View,
  Text,
  ActivityIndicator,
} from 'react-native';
import DeviceInfo from 'react-native-device-info';
import AlertPrompt from 'react-native-prompt-android';
import * as StonePOS from 'react-native-stone-pos';
import type { TransactionType } from 'react-native-stone-pos';
import packageJson from '../package.json';
import { Spacer } from './Spacer';

type TestTransactionData = {
  [key: string]: TransactionType | null;
};

type TestLoadingStatus = {
  [key: string]: boolean | null;
};

type ButtonProps = {
  title: string;
  onPress: () => void;
  isLoading: boolean;
};

const Button = (props: ButtonProps) => {
  return (
    <Pressable
      onPress={props.onPress}
      disabled={props.isLoading}
      style={{
        backgroundColor: 'blue',
        opacity: props.isLoading ? 0.75 : 1,
        minHeight: 50,
        width: '90%',
        justifyContent: 'center',
        alignItems: 'center',
        flexDirection: 'row',
        borderRadius: 5,
      }}
    >
      <Text style={{ fontSize: 16, color: 'white', textAlign: 'center' }}>
        {props.title}
      </Text>

      {props.isLoading ? (
        <ActivityIndicator
          size={'small'}
          color={'white'}
          style={{ marginLeft: 5 }}
        />
      ) : null}
    </Pressable>
  );
};

export default function App() {
  const [isSDKInitialized, setSDKInitialized] = React.useState<boolean>(false);
  const [testTransactionData, setTestTransactionData] =
    React.useState<TestTransactionData>({});
  const [testLoadingStatus, setTestLoadingStatus] =
    React.useState<TestLoadingStatus>({});

  StonePOS.useNativeEventListener('MAKE_TRANSACTION_PROGRESS', (data: any) => {
    console.log('Payment', data);
  });

  StonePOS.useNativeEventListener('VOID_TRANSACTION_PROGRESS', (data: any) => {
    console.log('Void', data);
  });

  const executeTestWithLoadingStatuses = async (
    testId: string,
    executionCallback: () => void
  ) => {
    setTestLoadingStatus({
      ...testLoadingStatus,
      [testId]: true,
    });

    try {
      await executionCallback();
    } catch (e) {
      console.error(e);
    }

    setTestLoadingStatus({
      ...testLoadingStatus,
      [testId]: false,
    });
  };

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
    return new Promise((resolve) => {
      AlertPrompt(
        'Input',
        'Enter your activation code',
        async (text: string) => {
          if (text.length > 0) {
            try {
              const hasActivated = await StonePOS.activateCode(
                text,
                'Testing accepted activation code',
                'Activating...',
                true
              );

              resolve(true);

              if (hasActivated) {
                Alert.alert('Success', 'Your test passed, code activated');
              } else {
                Alert.alert('Error', 'Oops, something went wrong');
              }
            } catch (e) {
              resolve(false);

              Alert.alert(
                'Error',
                `Your test failed, no code activated: ${JSON.stringify(e)}`
              );
            }
          } else {
            resolve(true);
          }
        }
      );
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

  const mifareDetectCardTest = async () => {
    try {
      const cardUuid = await StonePOS.mifareDetectCard();

      Alert.alert(
        'Success',
        `Your test passed, card detected: ${cardUuid.join(',')}`
      );
    } catch (e) {
      Alert.alert(
        'Error',
        `Your test failed, no card detected: ${JSON.stringify(e)}`
      );
    }
  };

  const mifareAuthenticateSectorTest = () => {
    return new Promise((resolve) => {
      AlertPrompt(
        'Input',
        'Enter in the following format: sector/key',
        async (text: string) => {
          if (text.length > 0 && text.split('/').length === 2) {
            try {
              const [sector, hexKey] = text.split('/', 2);

              const sectorAuthenticator =
                await StonePOS.mifareAuthenticateSector(
                  StonePOS.MifareKeyType.TypeA,
                  parseInt(sector as string),
                  hexKey as string
                );

              resolve(true);

              console.log(sectorAuthenticator);

              Alert.alert('Success', 'Your test passed, code activated');
            } catch (e) {
              resolve(false);

              Alert.alert(
                'Error',
                `Your test failed, no code activated: ${JSON.stringify(e)}`
              );
            }
          } else {
            resolve(true);
          }
        },
        {
          defaultValue: '1/FFFFFFFFFFFF',
        }
      );
    });
  };

  const mifareReadSectorBlockTest = () => {
    return new Promise((resolve) => {
      AlertPrompt(
        'Input',
        'Enter in the following format: sector/block/key',
        async (text: string) => {
          if (text.length > 0 && text.split('/').length === 3) {
            try {
              const [sector, block, hexKey] = text.split('/', 3);

              const blockValue = await StonePOS.mifareReadBlock(
                StonePOS.MifareKeyType.TypeA,
                parseInt(sector as string),
                parseInt(block as string),
                hexKey as string
              );

              resolve(true);

              Alert.alert(
                'Success',
                `Your test passed, block value: ${blockValue.join(', ')}`
              );
            } catch (e) {
              resolve(false);

              Alert.alert(
                'Error',
                `Your test failed, read block exception: ${JSON.stringify(e)}`
              );
            }
          } else {
            resolve(true);
          }
        },
        {
          defaultValue: '1/1/FFFFFFFFFFFF',
        }
      );
    });
  };

  const mifareWriteSectorBlockTest = () => {
    return new Promise((resolve) => {
      AlertPrompt(
        'Input',
        'Enter in the following format: sector/block/value/key',
        async (text: string) => {
          if (text.length > 0 && text.split('/').length === 4) {
            try {
              const [sector, block, value, hexKey] = text.split('/', 4);

              await StonePOS.mifareWriteBlock(
                StonePOS.MifareKeyType.TypeA,
                parseInt(sector as string),
                parseInt(block as string),
                value as string,
                hexKey as string
              );

              resolve(true);

              Alert.alert(
                'Success',
                `Your test passed, block written, new block value: ${value}`
              );
            } catch (e) {
              resolve(false);

              Alert.alert(
                'Error',
                `Your test failed, write block exception: ${JSON.stringify(e)}`
              );
            }
          } else {
            resolve(true);
          }
        },
        {
          defaultValue: '1/1/HELLO WORLD 1234/FFFFFFFFFFFF',
        }
      );
    });
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
            onPress={() =>
              executeTestWithLoadingStatuses(
                'TST_001',
                async () => await deviceInfoTest()
              )
            }
            isLoading={testLoadingStatus['TST_001'] ? true : false}
          />
          <Spacer />
          <Button
            title="Test 002 - Denied Activation"
            onPress={() =>
              executeTestWithLoadingStatuses(
                'TST_002',
                async () => await wrongCodeActivationTest()
              )
            }
            isLoading={testLoadingStatus['TST_002'] ? true : false}
          />
          <Spacer />
          <Button
            title="Test 003 - Approved Activation"
            onPress={() =>
              executeTestWithLoadingStatuses(
                'TST_003',
                async () => await approvedCodeActivationTest()
              )
            }
            isLoading={testLoadingStatus['TST_003'] ? true : false}
          />
          <Spacer />
          <Button
            title="Test 003/1 - Get Activated Codes"
            onPress={() =>
              executeTestWithLoadingStatuses(
                'TST_0031',
                async () => await getActivatedCodesTest()
              )
            }
            isLoading={testLoadingStatus['TST_0031'] ? true : false}
          />
          <Spacer />
          <Button
            title="Test 004 - 1X Credit Denied"
            onPress={() =>
              executeTestWithLoadingStatuses(
                'TST_004',
                async () => await oneInstallmentCreditDeniedTest()
              )
            }
            isLoading={testLoadingStatus['TST_004'] ? true : false}
          />
          <Spacer />
          <Button
            title="Test 005 - Transaction Data"
            onPress={() =>
              executeTestWithLoadingStatuses(
                'TST_005',
                async () => await lastTransactionDataTest()
              )
            }
            isLoading={testLoadingStatus['TST_005'] ? true : false}
          />
          <Spacer />
          <Button
            title="Test 006 - 1x Credit Denied (Wrong Password)"
            onPress={() =>
              executeTestWithLoadingStatuses(
                'TST_006',
                async () => await wrongPasswordCreditTest()
              )
            }
            isLoading={testLoadingStatus['TST_006'] ? true : false}
          />
          <Spacer />
          <Button
            title="Test 007 - 1x Credit Approved"
            onPress={() =>
              executeTestWithLoadingStatuses(
                'TST_007',
                async () => await approvedCreditTest()
              )
            }
            isLoading={testLoadingStatus['TST_007'] ? true : false}
          />
          <Spacer />
          <Button
            title="Test 008 - Transaction Data"
            onPress={() =>
              executeTestWithLoadingStatuses(
                'TST_008',
                async () => await lastTransactionDataTest()
              )
            }
            isLoading={testLoadingStatus['TST_008'] ? true : false}
          />
          <Spacer />
          <Button
            title="Test 009 - Void Approved"
            onPress={() =>
              executeTestWithLoadingStatuses(
                'TST_009',
                async () => await voidTransactionFromTest('TST_007')
              )
            }
            isLoading={testLoadingStatus['TST_009'] ? true : false}
          />
          <Spacer />
          <Button
            title="Test 010 - Transaction Data"
            onPress={() =>
              executeTestWithLoadingStatuses(
                'TST_010',
                async () => await lastTransactionDataTest()
              )
            }
            isLoading={testLoadingStatus['TST_010'] ? true : false}
          />
          <Spacer />
          <Button
            title="Test 011 - 2x Credit Approved"
            onPress={() =>
              executeTestWithLoadingStatuses(
                'TST_011',
                async () =>
                  await approvedCreditTest(
                    'CREDIT',
                    '10000',
                    'TST_011',
                    2,
                    false
                  )
              )
            }
            isLoading={testLoadingStatus['TST_011'] ? true : false}
          />
          <Spacer />
          <Button
            title="Test 012 - Void Approved"
            onPress={() =>
              executeTestWithLoadingStatuses(
                'TST_012',
                async () => await voidTransactionFromTest('TST_011')
              )
            }
            isLoading={testLoadingStatus['TST_012'] ? true : false}
          />
          <Spacer />
          <Button
            title="Test 013 - Transaction Data"
            onPress={() =>
              executeTestWithLoadingStatuses(
                'TST_013',
                async () => await lastTransactionDataTest()
              )
            }
            isLoading={testLoadingStatus['TST_013'] ? true : false}
          />
          <Spacer />
          <Button
            title="Test 014 - Debit Approved"
            onPress={() =>
              executeTestWithLoadingStatuses(
                'TST_014',
                async () =>
                  await approvedCreditTest('DEBIT', '8000', 'TST_014', 1, false)
              )
            }
            isLoading={testLoadingStatus['TST_014'] ? true : false}
          />
          <Spacer />
          <Button
            title="Test 015 - Void Approved"
            onPress={() =>
              executeTestWithLoadingStatuses(
                'TST_015',
                async () => await voidTransactionFromTest('TST_014')
              )
            }
            isLoading={testLoadingStatus['TST_015'] ? true : false}
          />
          <Spacer />
          <Button
            title="Test 016 - Transaction Data"
            onPress={() =>
              executeTestWithLoadingStatuses(
                'TST_016',
                async () => await lastTransactionDataTest()
              )
            }
            isLoading={testLoadingStatus['TST_016'] ? true : false}
          />

          <Spacer />
          <Button
            title="Test 017 - Print HTML"
            onPress={() =>
              executeTestWithLoadingStatuses(
                'TST_017',
                async () => await htmlPrintTest()
              )
            }
            isLoading={testLoadingStatus['TST_017'] ? true : false}
          />

          <Spacer />
          <Button
            title="Test 018 - Detect Mifare Card"
            onPress={() =>
              executeTestWithLoadingStatuses(
                'TST_018',
                async () => await mifareDetectCardTest()
              )
            }
            isLoading={testLoadingStatus['TST_018'] ? true : false}
          />

          <Spacer />
          <Button
            title="Test 019 - Authenticate Mifare Sector"
            onPress={() =>
              executeTestWithLoadingStatuses(
                'TST_019',
                async () => await mifareAuthenticateSectorTest()
              )
            }
            isLoading={testLoadingStatus['TST_019'] ? true : false}
          />

          <Spacer />
          <Button
            title="Test 020 - Read Mifare Sector Block"
            onPress={() =>
              executeTestWithLoadingStatuses(
                'TST_020',
                async () => await mifareReadSectorBlockTest()
              )
            }
            isLoading={testLoadingStatus['TST_020'] ? true : false}
          />

          <Spacer />
          <Button
            title="Test 021 - Write Mifare Sector Block"
            onPress={() =>
              executeTestWithLoadingStatuses(
                'TST_021',
                async () => await mifareWriteSectorBlockTest()
              )
            }
            isLoading={testLoadingStatus['TST_021'] ? true : false}
          />
        </ScrollView>
      ) : (
        <Button
          title="Initialize SDK"
          onPress={() =>
            executeTestWithLoadingStatuses(
              'INIT',
              async () => await sdkInitialize()
            )
          }
          isLoading={testLoadingStatus['INIT'] ? true : false}
        />
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
    alignItems: 'center',
  },
  container: {
    flex: 1,
    alignItems: 'center',
    width: '100%',
    justifyContent: 'center',
  },
});
