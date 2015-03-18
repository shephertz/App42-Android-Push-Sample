# About Application
1. This application shows how can we integrate PushNotification using App42 Android SDK in Android application.
2. Here We are using new Google Cloud Messaging API provided by Google as we know old one are depricated.
3. How we can send PushNotification using App42 PushNotification API.

# Running Sample
This is a Android sample app made by using App42 API. It uses PushNotification API of App42 platform.
Here are the few easy steps to run this sample app.

1. [Register] (https://apphq.shephertz.com/register) with App42 platform.
2. Create an app once, you are on Quick start page after registration.
3. If you are already registered, login to [AppHQ] (http://apphq.shephertz.com) console and create an app from App Manager Tab.
4. Create a project and get your Project Id from [Google developer console] (https://cloud.google.com/console/project). It would be available in Overview section of your created project..
5. Select your created project and click on APIs option in Google developer console and enable Google Cloud Messaging for Android service.
6. Click on Credentials from left menu -> Create New Key -> Server Key.
7. Keep Accept requests from these server IP addresses as blank and click on Create button
8. Go to [AppHQ] (http://apphq.shephertz.com) console and click on PushNotification and select Android Settings in Settings option.
9. Select your app, select the GCM under Provider section and copy server key under push key section which is generated in Google developer console in above step and submit it.
10. [Download] (https://github.com/VishnuGShephertz/Push-Notification-using-GCM-New-API/archive/master.zip) sample project  and import it in the eclipse.
11. Add google Play Service as a library project.
10. Open MainActivity.java  file in sample project and make following changes.

```
A. Replace api-Key and secret-Key that you have received in step 32 or 33 at line number 51.
B. Replace your user-id by which you want to register your application for PushNotification at line number 34.
C. Replace your user-id by which you want to register your application for PushNotification at line number 19.

```
12.Build your android application and install on your android device.

__Test and verify PushNotification from AppHQ console__
 
```
A. After registering for PushNotification go to AppHQ console and click on PushNotification and select
  application after selecting User tab.
B. Select desired user from registered User-list and click on Send Message Button.
C. Send appropriate message to user by clicking Send Button.

```
# Design Details:
__Initializing App42API to send PushNotification :__ To Send PushNotification using APP42 API we have to initialize first using Api-Key and Secret-Key in MainActivity.java file.
 
```
	function intializeApp42API() {
			App42.initialize('<YOUR API KEY>', '<YOUR SECRET KEY>');
			App42.setLoggedInUser('<Your User Id>');
		}

```

__Check for GooglePlayService Availability :__ To Use Google Cloud Messaging API, first we need to check for google play service availability, This code is written in App42GCMController.
 
```
	public static boolean isPlayServiceAvailable(Activity activity) {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(activity);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
						PlayServiceResolutionRequest).show();
			} else {
				Log.i(Tag, "This device is not supported.");
			}
			return false;
		}
		return true;
	}

```

__Get Registration Id:__ For Push Notifiction we require GCM Registration Id from Local if save else from Google.
 
```
	public static void getRegistrationId(Context context,
			String googleProjectNo, App42GCMListener callBack) {
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
		String regid = App42GCMController.getRegistrationId(context);
		if (regid.isEmpty()) {
			registeronGCM(context, googleProjectNo, gcm, callBack);
		} else
			callBack.onGCMRegistrationId(regid);
	}

```
__Registartion on GCM:__ If GCM Registartion Id is not saved in local we have to register app on GCM first to get registration Id
 
```
		public static void registeronGCM(final Context context,
			final String googleProjectNo, final GoogleCloudMessaging gcm,
			final App42GCMListener callback) {
		final Handler callingThreadHandler = new Handler();
		new Thread() {
			@Override
			public void run() {
				try {
					final String regid = gcm.register(googleProjectNo);
					callingThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							if (callback != null) {
								callback.onGCMRegistrationId(regid);
							}
						}
					});
				} catch (final Exception ex) {
					callingThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							if (callback != null) {
								callback.onError(ex.getMessage());
							}
						}
					});
				}
			}
		}.start();
	}

```
__App42 Push Registration :__ Once we get GCM registartion Id we have to store it on AppHQ for App42 PushNotification.
 
```
public static void registerOnApp42(String userName,String deviceToen,final App42GCMListener callBack){
		App42API.buildPushNotificationService().storeDeviceToken(userName, deviceToen, new App42CallBack() {
			@Override
			public void onSuccess(Object arg0) {
				App42Response response=(App42Response) arg0;
				callBack.onRegisterApp42(response.getStrResponse());
			}
			
			@Override
			public void onException(Exception arg0) {
				// TODO Auto-generated method stub
				callBack.onApp42Response(arg0.getMessage());
			}
		});
	}

```
__Send Push Message to user:__ You can also send Push Notification message to user as well those are 
 
```
public static void sendPushToUser(String userName,String message,final App42GCMListener callBack){
		App42API.buildPushNotificationService().sendPushMessageToUser(userName, message, new App42CallBack() {
			
			@Override
			public void onSuccess(Object arg0) {
				// TODO Auto-generated method stub
				App42Response response=(App42Response) arg0;
				callBack.onApp42Response(response.getStrResponse());
			}
			
			@Override
			public void onException(Exception arg0) {
				// TODO Auto-generated method stub
				callBack.onApp42Response(arg0.getMessage());
			}
		});
	}
```
__Customize PushNotification Message:__ You can also customize your PushNotification message by changing following code in App42GCMService.java file accordingly.
 
```
     notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_stat_gcm)
				.setContentTitle(title)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg).setWhen(when).setNumber(++msgCount)
				.setLights(Color.YELLOW, 1, 2).setAutoCancel(true)
				.setDefaults(Notification.DEFAULT_SOUND)
				.setDefaults(Notification.DEFAULT_VIBRATE);

```




__AndroidManifest.xml file Changes:__ If you are customizing your own Android application.
So make following changes in your AndroidManifest.xml using this sample's AndroidManifest.xml file.

1. Add following permission in your AndroidManifest.xml file.

```
   <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.VIBRATE" />

  
    <uses-permission android:name="android.permission.WAKE_LOCK" />
	  <!-- Required for Location based Push Notification. -->
	 <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <!--
     Creates a custom permission so only this app can receive its messages.

     NOTE: the permission *must* be called PACKAGE.permission.C2D_MESSAGE,
           where PACKAGE is the application's package name.
    -->
    <permission
        android:name="com.example.app42sample.permission.C2D_MESSAGE"
        android:protectionLevel="signature" />

    <uses-permission android:name="com.example.app42sample.permission.C2D_MESSAGE" />

    <!-- This app has permission to register and receive data message. -->
    <uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />

```

2.Add Receiver component in your Androidmanifest.xml file.

```
     <receiver
            android:name="com.shephertz.app42.push.plugin.App42GCMReceiver"
            android:permission="com.google.android.c2dm.permission.SEND" >
            <intent-filter>
                <!-- Receives the actual messages. -->
                <action android:name="com.google.android.c2dm.intent.RECEIVE" />
                <!-- Receives the registration id. -->
                <action android:name="com.google.android.c2dm.intent.REGISTRATION" />
                <!-- Your package name here -->
                <category android:name="com.example.app42sample" />
            </intent-filter>
        </receiver>

```
3.Declare Service in your AndroidManifest.xml file.

```
 <service android:name="com.shephertz.app42.push.plugin.App42GCMService" >
        </service>
```
4.Replace "com.example.app42sample" with your application package name in AndroidManifest.xml file.
5. Add Meta Data Details in AndroidManifest.xml file.
```
  <meta-data
            android:name="com.google.android.gms.version"
            android:value="@integer/google_play_services_version" />
        <meta-data
            android:name="onMessageOpen"
            android:value="com.example.app42sample.MainActivity" />
```
