/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, {useState, useEffect} from 'react';
import type {PropsWithChildren} from 'react';
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  PermissionsAndroid,
  Text,
  useColorScheme,
  View,
  Button,
  Alert
} from 'react-native';

import {
  Colors,
  DebugInstructions,
  Header,
  LearnMoreLinks,
  ReloadInstructions,
} from 'react-native/Libraries/NewAppScreen';

import {NativeEventEmitter, NativeModules} from 'react-native';
const {SelfSDKRNModule} = NativeModules;


type SectionProps = PropsWithChildren<{
  title: string;
}>;

function Section({children, title}: SectionProps): React.JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';
  return (
    <View style={styles.sectionContainer}>
      <Text
        style={[
          styles.sectionTitle,
          {
            color: isDarkMode ? Colors.white : Colors.black,
          },
        ]}>
        {title}
      </Text>
      <Text
        style={[
          styles.sectionDescription,
          {
            color: isDarkMode ? Colors.light : Colors.dark,
          },
        ]}>
        {children}
      </Text>
    </View>
  );
}

function App(): React.JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  const [selfId, setSelfId] = useState('');

  useEffect(() => {
    console.log('componentDidMount');
    console.log("selfId:" + selfId)

    const eventEmitter = new NativeEventEmitter(NativeModules.ToastExample);
    let eventListener = eventEmitter.addListener('EventSelfId', event => {
      console.log("selfId: " + event.selfId)
      setSelfId(event.selfId)
    });

    SelfSDKRNModule.getSelfId(result => {      
      setSelfId(result)
    });

    // Removes the listener once unmounted
    return () => {
      eventListener.remove();
    };
  }, []);

  const requestLocationPermission = async () => {
    try {
      const granted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
        {
          title: 'Location Permission',
          message: 'SelfSDK needs access to your location',
          buttonNeutral: 'Ask Me Later',
          buttonNegative: 'Cancel',
          buttonPositive: 'OK',
        },
      );
      if (granted === PermissionsAndroid.RESULTS.GRANTED) {        
        SelfSDKRNModule.getLocation(
          error => {
            Alert.alert(error, error);
          },
          result => {
            console.log("location: " + result)
            Alert.alert('Location', result);
        });
      } else {
        console.log('Location permission denied');
      }
    } catch (err) {
      console.warn(err);
    }
  };
  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar
        barStyle={isDarkMode ? 'light-content' : 'dark-content'}
        backgroundColor={backgroundStyle.backgroundColor}
      />
      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        style={backgroundStyle}>
        
        <View
          style={{
            backgroundColor: isDarkMode ? Colors.black : Colors.white,
          }}>

          <View style={styles.container}>
            <Text>
              SelfId: {selfId}
            </Text>
            <Button
              title="Create Account"
              style={styles.button}
              disabled={selfId != ''}
              onPress={() => {
                  SelfSDKRNModule.openLivenessCheck();                
              }}
            />  
            <Button
              title="Request Location"
              style={styles.button}
              disabled={selfId == ''}
              onPress={() => {
                requestLocationPermission()
              }}
            />           
          </View>                  
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'column',
    flex: 1,
    padding: 20,
    margin: 20,
    gap: 10
  },
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
  button: {
      
      alignItems: 'center',
      justifyContent: 'center',
      paddingVertical: 12,
      paddingHorizontal: 32,
      borderRadius: 4,
      elevation: 3,
      marginHorizontal: 16,
      marginVertical: 16,
      marginTop: 24
  }
});

export default App;
