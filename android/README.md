# Android Self Embedded-SDK samples

## Clone project

```bash
git@github.com:joinself/self-mobile-embedded-samples.git
```


## API Usages

[Android SelfSDK Usages](USAGE.md)


## Build and run the following apps

### Chat app
This app demonstrates:
- register an account 
- send and receive messages
- liveness check

__Build__
```bash
cd android
./gradlew :chat:assembleDebug
```

### Chat Compose
This app use Jetpack Compose to build the UI

__Build__
```bash
./gradlew :chat-compose:assembleDebug
```


### Sign In
Sign in from existing SelfID from Chat app above.

__Build__
```bash
./gradlew :sign-in:assembleDebug
```