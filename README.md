# react-native-stone-pos

Stone Android POS Native Module

## Installation

Stone has a private packageCloud repository, so we need to have your token before hand in order to communicate with package cloud.

So you have two options:

### Environment variable / Project properties

We support both options, as adding the variables to your `gradle.properties` in your `android` folder, or setting it as a global environment variable. (This is useful when you are building it from CI)

The most important variable you need is: `StonePos_packageCloudToken`. This variable is used to authenticate with Stone private package Cloud repo, so we can download our dependencies.

Other variables you might want:

| Name                     | Default Value | Description                                                                                                                                                         |
| ------------------------ | ------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| StonePos_posMode         | pos           | Set this to anything other than `pos` to remove the `HAL` dependencies all together, this is useful if you are building an app to mobile devices other than the POS |
| StonePos_includeIngenico | true          | Set this to anything other than `true` to remove the `Ingenico` hal dependencies                                                                                    |
| StonePos_includeSunmi    | true          | Set this to anything other than `true` to remove the `Sunmi` hal dependencies                                                                                       |
| StonePos_includeGertec   | true          | Set this to anything other than `true` to remove the `Gertec` hal dependencies                                                                                      |

#### Install Steps:

```sh
npm install react-native-stone-pos
```

### Android

#### YOU NEED TO DO THIS

Edit `android/app/build.gradle` ( NOT `android/build.gradle` ) and add the following:

```gradle
apply from: "../../node_modules/react-native-stone-pos/android/stone-repo.gradle"
```

If you are building with the POS in mind, you will also need to apply this here:

```gradle
apply from: "../../node_modules/react-native-stone-pos/android/dynamic-hal.gradle"
```

This will enable the HAL modules of your choosing, or all of them as long as you are in POS mode, or the `StonePos_include*` variables are `true`

#### Troubleshooting

##### Manifest merger failed : Attribute application@allowBackup value=(false)

You might get an error when you are using the default React Native Manifest, which is:

```
Manifest merger failed : Attribute application@allowBackup value=(false) from AndroidManifest.xml:12:7-34
  	is also present at [br.com.stone:stone-sdk:4.1.1] AndroidManifest.xml:20:9-35 value=(true).
  	Suggestion: add 'tools:replace="android:allowBackup"' to <application> element at AndroidManifest.xml:7:5-12:19 to override.
```

So, as per the suggestion, you can add `tools:replace="android:allowBackup"` to your `<application>` element at your `android/app/src/main/AndroidManifest.xml` file, like this:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
  package="com.yourapp.here">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>

    <application
      android:name=".MainApplication"
      android:label="@string/app_name"
      android:icon="@mipmap/ic_launcher"
      android:allowBackup="true"
      tools:replace="android:allowBackup"
      android:theme="@style/AppTheme">
      <activity
        android:name=".MainActivity"
        android:label="@string/app_name"
        android:configChanges="keyboard|keyboardHidden|orientation|screenSize|uiMode"
        android:launchMode="singleTask"
        android:windowSoftInputMode="adjustResize">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
      </activity>
    </application>
</manifest>
```

##### More than one file was found with OS independent path 'META-INF/client_release.kotlin_module'

If you get this error:

```
More than one file was found with OS independent path 'META-INF/client_release.kotlin_module'
```

Try modifying `android/app/build.gradle` and add this inside the `android` section:

```gradle
packagingOptions {
  exclude 'META-INF/api_release.kotlin_module'
  exclude 'META-INF/client_release.kotlin_module'
}
```

## Usage

```js
import * as StonePOS from 'react-native-stone-pos';

// ...

const isRunningInPOS = StonePOS.IS_RUNNING_IN_POS;
const activationResult = await StonePOS.initSDK('My Awesome App');

if (!isRunningInPos) {
  //Ask user to select the bluetooth pinpad (use any library you like), and then you can connect using:
  const pinpadConnectResult = await StonePOS.connectToPinPad(
    'My beauty pinpad',
    '00:11:22:33:44'
  );
}
```

## Example

Checkout the [example project](https://github.com/EightSystems/react-native-stone-pos/tree/main/example), which was made with the intention of running all the required homologation tests. There you will see how to void, execute a charge, print, and so on.

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
