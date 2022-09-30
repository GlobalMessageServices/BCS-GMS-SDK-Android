# Android PushSDK
## Setting up your project to work with the SDK

### Table of contents
1. [Add Firebase cloud messaging to your project](https://github.com/GlobalMessageServices/BCS-GMS-SDK-Android/blob/main/README.md#add-firebase-cloud-messaging-to-your-project)
1. [Get credentials for your app using Android Studio](https://github.com/GlobalMessageServices/BCS-GMS-SDK-Android/blob/main/README.md#get-credentials-for-your-app-using-android-studio)
1. [Add the SDK to your project](https://github.com/GlobalMessageServices/BCS-GMS-SDK-Android/blob/main/README.md#add-the-sdk-to-your-project)
1. [Extend the PushKFirebaseService](https://github.com/GlobalMessageServices/BCS-GMS-SDK-Android/blob/main/README.md#extend-the-pushkfirebaseservice)
1. [Start using the SDK](https://github.com/GlobalMessageServices/BCS-GMS-SDK-Android/blob/main/README.md#start-using-the-sdk)
1. [SDK functions description](https://github.com/GlobalMessageServices/BCS-GMS-SDK-Android/blob/main/README.md#sdk-functions-description)

***
## Add Firebase cloud messaging to your project

### You need to connect your app to your firebase project
*	Create a project at https://console.firebase.google.com/ (or use an existing one)
![image](https://user-images.githubusercontent.com/112561176/193265894-76cd91fb-d4dd-4eef-9781-8831e62e498f.png)
*	Add required dependencies to your android studio project (either manually or use Firebase Assistant plugin for Android Studio)
![image](https://user-images.githubusercontent.com/112561176/193265943-afda92f5-9c9d-4a49-8a27-286f1f379d8c.png)
![image](https://user-images.githubusercontent.com/112561176/193265959-f869e2b2-3602-4322-9dbc-1d4472a6983e.png)
![image](https://user-images.githubusercontent.com/112561176/193265973-9def30cb-bc73-403a-ac1e-fa8b03f09508.png)

### Recommended dependency versions to use within your app (or use whatever would be compatible with what is used within the SDK):
*	Project (top-level) build.gradle:
 ```
buildscript {
    dependencies {
        ...
        classpath 'com.google.gms:google-services:4.3.4'
    }
}
```
* Module (app-level) build.gradle:
```
plugins {
    ...
    id 'com.google.gms.google-services'
}

dependencies {
    ...
    implementation 'com.google.firebase:firebase-messaging:21.0.0'
}
```
* Make sure you add proper google-services.json file to the root of your app module
Read more at: https://firebase.google.com/docs/android/learn-more
!!!Starting with Gradle 7 repositoriesMode in settings.gradle (Project Settings) should be changed as showed below: 
```
	dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
		repositories{
                            
		}
	}
```
## Get credentials for your app using Android Studio
### Provide your firebase project's cloud messaging server key and application fingerpint to the PushSDK representative, and obtain required credentials to work with the SDK
![image](https://user-images.githubusercontent.com/112561176/193266741-102adcdc-9492-44e6-abc7-82116f4e775f.png)
![image](https://user-images.githubusercontent.com/112561176/193266760-a19e6e64-79aa-4534-ab8e-2beb866769ea.png)
### Follow the next steps to get app fingerprint:
Generate key and keystore
1.	In the menu bar, click Build > Generate Signed Bundle/APK.
2.	In the Generate Signed Bundle or APK dialog, select Android App Bundle  and click Next.
3.	Below the field for Key store path, click Create new.
4.	On the New Key Store window, provide the following information for your keystore and key.
5.	Keystore<br>
	*	Key store path: Select the location where your keystore should be created. Also, a file name should be added to the end of the location path with the .jks extension.
	*	Password: Create and confirm a secure password for your keystore.
6.	Key<br>
	*	Alias: Enter an identifying name for your key.
	*	Password: Create and confirm a secure password for your key. This should be the same as your keystore password. 
	*	Validity (years): Set the length of time in years that your key will be valid. Your key should be valid for at least 25 years, so you can sign app updates with the same key through the lifespan of your app.
	*	Certificate: Enter some information about yourself for your certificate. This information is not displayed in your app, but is included in your certificate as part of the APK.<br> 
![image](https://user-images.githubusercontent.com/112561176/193267545-8afbb5fa-a3cf-4c3e-aee9-e7106cd35b3f.png)
7.	Once you complete the form, click OK.

### Sign your app with your key
To sign your app using Android Studio, and export an existing app signing key, follow these steps:
1.	If you don’t currently have the Generate Signed Bundle or APK dialog open, click Build > Generate Signed Bundle/APK.
2.	In the Generate Signed Bundle or APK dialog, select either Android App Bundle click Next.
3.	Select a module from the drop down.
4.	Specify the path to your keystore, the alias for your key, and enter the passwords for both.<br>
![image](https://user-images.githubusercontent.com/112561176/193268333-dc9395c5-3848-46b8-9db8-7f209cc56d12.png)
5.	Click Next
6.	In the next window, select a destination folder for your signed app, select the build variant. <br>
![image](https://user-images.githubusercontent.com/112561176/193268373-376c147b-99b3-4ebc-aff6-50f0f68cd33a.png)
7.	Click Finish.
### To get the certificate fingerprint run the keytool utility provided with Java:
```
keytool -list -v -keystore C:\Users\user_name\debug-keystore.jks
```
where C:\Users\user_name\debug-keystore.jks is key store path.<br>
Read more at: https://developer.android.com/studio/publish/app-signing#generate-key and https://developers.google.com/android/guides/client-auth

## Add the SDK to your project
Make sure you have declared maven repository in your project (top-level) build.gradle
```
allprojects {
    repositories {
        ...
        maven {
            url 'https://jitpack.io'
        }
    }
}
```
Add SDK dependency to your module (app-level) build.gradle. The latest version 1.1.0
```
dependencies {
    ...
    //or use a newer version if available
    'com.github.GlobalMessageServices:Hyber-GMS-SDK-Android:1.1.0'
}
```
To use http protocol instead of https, add android:usesCleartextTraffic="true" to your application tag inside android manifest
```
<application
        ...

        android:usesCleartextTraffic="true"
        >
```

## Extend the PushKFirebaseService
Create a class that extends PushKFirebaseService<br>
Specify title and text which may be displayed by the system in the "summary notification".<br>
Read more about receiving push messages and displaying notifications using the PushSDK [here](https://github.com/GlobalMessageServices/BCS-GMS-SDK-Android/wiki/Receiving-push-messages-and-showing-notifications).

```
class MyPushKFirebaseService : PushKFirebaseService(
    summaryNotificationTitleAndText = Pair("title", "text")
) {

    override fun setNotificationStyle(
        notificationConstruct: NotificationCompat.Builder,
        data: Map<String, String>,
        notificationStyle: NotificationStyle
    ) {
        super.setNotificationStyle(notificationConstruct, data, notificationStyle)
    }

    /**
     * Called when data push is received from the Messaging Hub
     */
    override fun onReceiveDataPush(
        appIsInForeground: Boolean,
        isDoNotDisturbModeActive: Boolean,
        areNotificationsEnabled: Boolean,
        isNotificationChannelMuted: Boolean,
        remoteMessage: RemoteMessage
    ) {
        super.onReceiveDataPush(
            appIsInForeground,
            isDoNotDisturbModeActive,
            areNotificationsEnabled,
            isNotificationChannelMuted,
            remoteMessage
        )

        //can be used to configure, for example set "isDoNotDisturbModeActive" to false,
        // to send notifications in "Do not disturb mode" anyways
//        super.onReceiveDataPush(
//            appIsInForeground = appIsInForeground,
//            isDoNotDisturbModeActive = false,
//            areNotificationsEnabled = areNotificationsEnabled,
//            isNotificationChannelMuted = isNotificationChannelMuted,
//            remoteMessage = remoteMessage
//        )
    }

    /**
     * Callback - when notification is sent
     */
    override fun onNotificationSent(
        appIsInForeground: Boolean,
        isDoNotDisturbModeActive: Boolean,
        areNotificationsEnabled: Boolean,
        isNotificationChannelMuted: Boolean,
        remoteMessage: RemoteMessage
    ) {
        super.onNotificationSent(
            appIsInForeground,
            isDoNotDisturbModeActive,
            areNotificationsEnabled,
            isNotificationChannelMuted,
            remoteMessage
        )
    }

   

 /**
     * Callback - when notification will not be sent
     */
    override fun onNotificationWontBeSent(
        appIsInForeground: Boolean,
        isDoNotDisturbModeActive: Boolean,
        areNotificationsEnabled: Boolean,
        isNotificationChannelMuted: Boolean,
        remoteMessage: RemoteMessage
    ) {
        super.onNotificationWontBeSent(
            appIsInForeground,
            isDoNotDisturbModeActive,
            areNotificationsEnabled,
            isNotificationChannelMuted,
            remoteMessage
        )
    }
    /**
     * Prepares NotificationCompat.Builder object for showing
     */
    override fun prepareNotification(data: Map<String, String>): NotificationCompat.Builder? {
        return super.prepareNotification(data)
        //can customize NotificationCompat.Builder object here, e.g.:
//        val notificationConstruct = pushSdkNotificationManager.constructNotification(data, PushSdkNotificationManager.NotificationStyle.BIG_TEXT)
//        notificationConstruct?.apply {
//            setContentText("some new text")
//        }
//        return notificationConstruct
    }
}
```
Add the service to your AndroidManifest.xml
```
<application
...>
<service
    android:name=".MyPushKFirebaseService"
    android:enabled="true"
    android:exported="true"
    android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"
    android:stopWithTask="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
</application>
```
## Start using the SDK
Make sure you have extended the PushKFirebaseService and added it to the AndroidManifest.xml
### Init the SDK with the following basic parameters:
*	context - the context you would like to use
*	baseApiUrl - base URL path to the API
```
val pushSDK = PushSDK(
    context = this,
    baseApiUrl = "https://yourapilink.com/version/3.0" // API url that you would be provided with
)
```
### Register your device/application:
*	Either register your device by using:
```
val answer1 = pushSDK.registerNewDevice(
    "clientAPIKey",  //API key that you would be provided with
    "appFingerprint", //APP fingerprint 
    "88002000600", //Device's phone number
    "UserPassword" //password, associated with Device's phone number (legacy - it is unused, you can put any value))
Log.d("TAG", answer1.toString())
```
Which would produce a similar output if successful:
```
D/TAG: PushKFunAnswerRegister(code=200, result=Ok, description=Success, deviceId=1066, token=c71b4bc05ee24f8eac007cc63d8ff2c4, userId=82, userPhone=88002000600, createdAt=2020-10-29T08:12:33.194942+00)
```
Then update registration:
```
val answer2 = pushSDK.updateRegistration()
Log.d("TAG", answer2.toString())
```
Which would produce a similar output if successful:
```
D/TAG: PushKFunAnswerGeneral(code=200, result=OK, description=Success, body={"deviceId": 1066})
```
This will register the device within the system and automatically pick up your current firebase cloud messaging token
*	Or register your device by manually specifying your firebase cloud messaging token:
```
val answer1 = pushSDK.registerNewDevice(
    "clientAPIKey",  //API key that you would be provided with
    "appFingerprint", //APP fingerprint 
    "userMsisdn", //Device's phone number
    "userPassword", //password, associated with Device's phone number (legacy - it is unused, you can put any value)
    "firebaseToken" //your firebase cloud messaging token  (Optional)
)
Log.d("TAG", answer1.toString())
```
This will register the device within the system and use the specified firebase cloud messaging token<br>
Note: In case you are trying out this code for the first time, you may want to unregister all devices before registering a new one, so that registration would pass every time. You may do so by adding the following code before registering a device:
```
val answer0 = pushSDK.unregisterAllDevices()
Log.d("TAG", answer0.toString())
```
###Your application should now be able to receive push messages from the api
Note: By default, notifications would only appear when your application is in background.
### For more usage information - please see:
•	[Receiving push messages and showing notifications](https://github.com/GlobalMessageServices/BCS-GMS-SDK-Android/wiki/Receiving-push-messages-and-showing-notifications)


# SDK functions description

All this functions are available from PushSDK class. For using it, create this class new instance first.

***
* new device registration. Register your device on push server
```
fun registerNewDevice(
        clientAPIKey: String,
        appFingerprint: String,
        userMsisdn: String,
        userPassword: String,
        firebaseToken: String = ""
    ): PushServerAnswerRegister
```
* update firebase token on push server
```
fun updateRegistration(): PushServerAnswerGeneral
```
* clear local device on server. This function clear on push server only locally saved device id
```
fun unregisterCurrentDevice(): PushServerAnswerGeneral
```
* clear all devices registered with current msisdn
```
fun unregisterAllDevices(): PushServerAnswerGeneral
```
* get message history. Returns all messages for specific period in seconds
```
fun getMessageHistory(periodInSeconds: Int): PushServerAnswerGeneral
```
* get all devices from server
```
fun getAllRegisteredDevices(): PushServerAnswerGeneral
```
* message callback to server
```
fun sendMessageCallback(
        messageId: String,
        messageText: String
    ): PushServerAnswerGeneral
```
* send delivery report to server
```
fun sendMessageDeliveryReport(messageId: String): PushServerAnswerGeneral
```
* check message queue
```
fun checkMessageQueue(): PushServerAnswerGeneral
```
