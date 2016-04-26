/**
 * -----------------------------------------------------------------------
 *     Copyright © 2015 ShepHertz Technologies Pvt Ltd. All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.shephertz.app42.push.plugin;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.example.app42Sample.MainActivity;
import com.example.app42Sample.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.shephertz.app42.paas.sdk.android.App42Exception;
import com.shephertz.app42.paas.sdk.android.App42Log;
import com.shephertz.app42.push.plugin.App42LocationManager.App42LocationListener;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 * @author Vishnu Garg
 */

public class App42GCMService extends IntentService {
	private static final int NotificationId = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	public static final String ExtraMessage = "message";
	static int msgCount = 0;
	public static final String DisplayMessageAction = "com.example.app42sample.DisplayMessage";
	private static final String App42GeoTag = "app42_geoBase";
	private static final String AddressBase = "addressBase";
	private static final String LocationBase = "coordinateBase";
	private static final String KeyApp42Message = "app42_message";

	public App42GCMService() {
		super("GcmIntentService");
	}

	public static final String TAG = "App42 Push Demo";

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);
		if (!extras.isEmpty()) { // has effect of unparcelling Bundle
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				App42Log.debug("Send error: " + extras.toString());
				App42GCMReceiver.completeWakefulIntent(intent);
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				App42Log.debug("Deleted messages on server: "
						+ extras.toString());
				App42GCMReceiver.completeWakefulIntent(intent);
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {
				String message = intent.getExtras().getString("message");
				App42Log.debug("Received: " + extras.toString());
				App42Log.debug("Message: " + message);
				validatePushIfRequired(message, intent);
				
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.

	}

	private void showNotification(String message,Intent intent) {
		broadCastMessage(message);
		sendNotification(message);
		 App42GCMReceiver.completeWakefulIntent(intent);
	}

	private void validatePushIfRequired(String message,final Intent intent) {
		try {
			final JSONObject json = new JSONObject(message);
			final String geoBaseType = json.optString(App42GeoTag, null);
			if (geoBaseType == null) {
				showNotification(json.toString(),intent);
			}
			else{
				 App42LocationManager.fetchGPSLocation(this,new App42LocationListener() {
					
					@Override
					public void onLocationFetched(Location location) {
						// TODO Auto-generated method stub
						LocationUtils.saveLocation(location, App42GCMService.this);
						validateGeobasePush(json, geoBaseType,intent);
					}
					
					@Override
					public void onLocationException(App42Exception e) {
						// TODO Auto-generated method stub
						validateGeobasePush(json, geoBaseType,intent);
					}
					
					@Override
					public void onLocationAddressRetrived(Address address) {
						// TODO Auto-generated method stub
						LocationUtils.saveLocationAddress(address, App42GCMService.this);
						validateGeobasePush(json, geoBaseType,intent);
					}
				});
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			showNotification(message,intent);
		}
	}
	
	/**
	 *  Validate GeoBase Push Notification
	 * @param jsondata
	 * @param geotype
	 * @param intent
	 */
	private void validateGeobasePush(JSONObject jsondata,String geotype,Intent intent){
		if(geotype.equals(AddressBase)){
			if(LocationUtils.isCountryBaseSuccess(jsondata, this))
				showNotification(jsondata.optString(KeyApp42Message, ""),intent);
		}
		else if(geotype.equals(LocationBase)){
			if(LocationUtils.isGeoBaseSuccess(jsondata, this))
				showNotification(jsondata.optString(KeyApp42Message, ""),intent);
		}
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	/**
	 * @param msg
	 */
	private void sendNotification(String msg) {
		String title = this.getString(R.string.app_name);
		long when = System.currentTimeMillis();
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent notificationIntent;
		try {
			notificationIntent = new Intent(this,
					Class.forName(getActivityName()));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			notificationIntent = new Intent(this, MainActivity.class);
		}
		notificationIntent.putExtra("message_delivered", true);
		notificationIntent.putExtra(ExtraMessage, msg);

		// set intent so it does not start a new activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_stat_gcm)
				.setContentTitle(title)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg).setWhen(when).setNumber(++msgCount)
				.setLights(Color.YELLOW, 1, 2).setAutoCancel(true)
				.setDefaults(Notification.DEFAULT_SOUND)
				.setDefaults(Notification.DEFAULT_VIBRATE);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NotificationId, mBuilder.build());
	}

	/**
     * 
     */
	public static void resetMsgCount() {
		msgCount = 0;
	}

	/**
	 * @param message
	 */
	public void broadCastMessage(String message) {
		Intent intent = new Intent(DisplayMessageAction);
		intent.putExtra(ExtraMessage, message);
		this.sendBroadcast(intent);
	}

	/**
	 * @return
	 */
	private String getActivityName() {
		ApplicationInfo ai;
		try {
			ai = this.getPackageManager().getApplicationInfo(
					this.getPackageName(), PackageManager.GET_META_DATA);
			Bundle aBundle = ai.metaData;
			return aBundle.getString("onMessageOpen");
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "MainActivity";
		}
	}

}
