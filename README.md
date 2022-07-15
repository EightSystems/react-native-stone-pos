# react-native-stone-pos

Stone Android POS Native Module

## Installation

Stone has a private packageCloud repository, so we need to have your token before hand in order to communicate with package cloud.

So you have two options:

### Environment variable

Add a environment variable called `StonePos_packageCloudToken` to your environment with the token they gave you.

### Project Properties

Add a variable called `StonePos_packageCloudToken` at your `gradle.properties` in your `android` folder with the token they gave you.

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

#### Troubleshooting

##### Manifest merger failed : Attribute application@allowBackup value=(false)

You might get an error when you are using the default React Native Manifest, which is:

```
Manifest merger failed : Attribute application@allowBackup value=(false) from AndroidManifest.xml:12:7-34
  	is also present at [br.com.stone:stone-sdk:4.1.1] AndroidManifest.xml:20:9-35 value=(true).
  	Suggestion: add 'tools:replace="android:allowBackup"' to <application> element at AndroidManifest.xml:7:5-12:19 to override.
```

So, as per the suggestion, you can add `tools:replace="android:allowBackup"` to your `<application>` element at your `android/app/src/main/AndroidManifest.xml` file, like this:

```
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

```
packagingOptions {
  exclude 'META-INF/api_release.kotlin_module'
  exclude 'META-INF/client_release.kotlin_module'
}
```

## Usage

```js
import StonePOS from "react-native-stone-pos";

// ...

const result = await StonePOS.initSDK('My Awesome App');
````

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
