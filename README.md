# About Application
1. This application shows how can we integrate PushNotification using App42 Android SDK in Android application.
2. Here We are using new Firebase Cloud Messaging API.
3. How we can send PushNotification using App42 PushNotification API.
4. Please do add location permission check for security in case of location based Push Notifications. 

# Running Sample
This is a Android sample app made by using App42 API. It uses PushNotification API of App42 platform.
Here are the few easy steps to run this sample app.

1. [Register] (https://apphq.shephertz.com/register) with App42 platform.
2. Create an app once, you are on Quick start page after registration.
3. If you are already registered, login to [AppHQ] (http://apphq.shephertz.com) console and create an app from App Manager Tab.
4. Next step is to [Add Firebase to your Android project] (https://firebase.google.com/docs/android/setup). Follow all the steps mentioned in the link to create Firebase project and to connect with your Android app.
5. Now follow all the steps mentioned in the link to [Set up Firebase Cloud Messaging client app on Android] (https://firebase.google.com/docs/cloud-messaging/android/client).
6. Go to [Firebase console] (https://console.firebase.google.com/) and select your project.
7. Click on ‘Project settings’ under Settings icon & copy ‘Legacy server key’ under ‘Cloud Messaging’ tab.
8. Go to [AppHQ] (http://apphq.shephertz.com) console and click on PushNotification and select Android Settings in Settings option.
9. Select your app, click on Add Key and insert the server key that was generated in Firebase console and submit it.
10. [Download Sample Project] (https://github.com/VishnuGShephertz/Push-Notification-using-GCM-New-API/archive/master.zip) and import it in the Android Studio.
11. Download ‘google-services.json’ file from your project’s firebase console and paste it at ‘app’ directory of your Android Studio project.
10. Open MainActivity.java file in sample project and make following changes.

```
A. Replace 'api-Key' and 'secret-Key' that you have received from your App42 project's AppHQ console.
B. Replace your 'user-id' by which you want to register your application for PushNotification (It must be unique for each user. You can use your unique login credendial or deviceId).

```
12.Build your android application and install on your android device.

__Test and verify PushNotification from AppHQ console__
 
```
A. After registering for PushNotification go to AppHQ console and click on PushNotification and select
  application after selecting User tab.
B. Select desired user from registered User-list and click on Send Message Button.
C. Send appropriate message to user by clicking Send Button.

```
__For more detailed info please click on the link [App42 Push Notification Detailed Doc] (http://api.shephertz.com/tutorial/Push-Notification-Android/?index=pn-android) __