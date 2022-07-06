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

````sh
npm install react-native-stone-pos
```

### Android

#### YOU NEED TO DO THIS

Edit `android/app/build.gradle` ( NOT `android/build.gradle` ) and add the following:

```gradle
apply from: "../../node_modules/react-native-stone-pos/stone-repo.gradle"
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
