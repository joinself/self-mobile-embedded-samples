# React Native Self Embedded-SDK samples

React Native sample app that uses Self SDK

## Clone project

```bash
git@github.com:joinself/self-mobile-embedded-samples.git
```

## Step1: Install dependencies


```bash
cd reactnative
yarn install
```

__Install CocoaPod dependencies for iOS__

```bash
cd ios
pod install
```

## Step 2: Start your Application

Start Dev server first, then start android or ios app

### Start Dev Server

```bash
yarn start
```

### For Android

```bash
# using npm
npm run android

# OR using Yarn
yarn android
```

### For iOS

```bash
# using npm
npm run ios

# OR using Yarn
yarn ios
```

#### Open Xcode

- If packages are updated, run
```
# cd self-mobile-embedded-samples/reactnative/ios
pod update
```

- Open project in Xcode `reactnative/ios/reactnative.xcworkspace`


## Get project info

```bash
npx react-native info
```

