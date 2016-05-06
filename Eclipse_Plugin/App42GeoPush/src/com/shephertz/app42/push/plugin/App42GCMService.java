/**
 * -----------------------------------------------------------------------
 *     Copyright  2010 ShepHertz Technologies Pvt Ltd. All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.shephertz.app42.push.plugin;

import java.util.HashMap;

import org.json.JSONArray;
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
import android.util.Log;

import com.example.app42Sample.MainActivity;
import com.example.app42Sample.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.shephertz.app42.paas.sdk.android.App42API;
import com.shephertz.app42.paas.sdk.android.App42CallBack;
import com.shephertz.app42.paas.sdk.android.App42Exception;
import com.shephertz.app42.paas.sdk.android.App42Log;
import com.shephertz.app42.paas.sdk.android.event.App42Preferences;
import com.shephertz.app42.paas.sdk.android.push.PushNotificationService;
import com.shephertz.app42.push.fencing.App42FenceManager;
import com.shephertz.app42.push.plugin.App42LocationManager.App42LocationListener;
import com.shephertz.app42.push.plugin.PushMessage.PushType;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 * @author Vishnu Garg
 */

public class App42GCMService extends IntentService {
	
	/** The Constant NotificationId. */
	private static final int NotificationId = 1;
	
	/** The m notification manager. */
	private NotificationManager mNotificationManager;
	
	/** The builder. */
	NotificationCompat.Builder builder;
	
	/** The Constant ExtraMessage. */
	public static final String ExtraMessage = "message";
	
	/** The Constant RichPush. */
	public static final String RichPush = "richPush";
	
	/** The msg count. */
	static int msgCount = 0;
	
	/** The Constant DisplayMessageAction. */
	public static final String DisplayMessageAction = "com.example.app42sample.DisplayMessage";


	/**
	 * Instantiates a new app42 gcm service.
	 */
	public App42GCMService() {
		super("GcmIntentService");
	}

	/** The Constant TAG. */
	public static final String TAG = "App42 Push Demo";

	/* (non-Javadoc)
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);
		System.out.println("Messsss Type"+messageType);
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
				String fenceCampData=intent.getExtras().getString("_App42GeoFenceData", null);		
				Log.i("App42 Message Received : ", intent.getExtras().toString());
				if(fenceCampData!=null){
					handleSilentGeoPush(intent);
				}
				else{
					Log.i("App42 Message Received : ", intent.getExtras().toString());
				PushMessage pushMessage=App42Util.parsePushMessage(intent, this);
				if(pushMessage==null)
					return;
				validatePushMessage(pushMessage, intent);
				}
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
	}
	
	/**
	 * Handle silent geo push.
	 *
	 * @param intent the intent
	 */
	private void handleSilentGeoPush(Intent intent){
		String fenceData = intent.getExtras().getString("_App42GeoFenceCoordinates", null);
		String fenceCampData=intent.getExtras().getString("_App42GeoFenceData", null);	
		if(fenceData==null|fenceCampData==null)
			return;
		try {
			JSONArray fenceArr=new JSONArray(fenceData);
			JSONObject fenceCampaign=new JSONObject(fenceCampData);
			App42FenceManager mManager=App42FenceManager.getInstance(this);
			mManager.connect();
			mManager.setGeoFences(fenceCampaign, fenceArr);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Show notification.
	 *
	 * @param pushMessage the push message
	 * @param intent the intent
	 */
	private void showNotification(PushMessage pushMessage,Intent intent) {	
		try{
			broadCastMessage(pushMessage.getMessage());
			sendNotification(pushMessage);
		   App42GCMReceiver.completeWakefulIntent(intent);
		   trackOpenRate(pushMessage);
		}
		catch(Throwable th){
			
		}
	}
	
	/**
	 * Track open rate.
	 *
	 * @param pushMessage the push message
	 */
	private void trackOpenRate(PushMessage pushMessage){
		String campaignName=pushMessage.getPushCampIdentifer();
		if(campaignName==null||campaignName.isEmpty())
			return;
		HashMap<String, String> otherMetaHeaders = new HashMap<String, String>();
		otherMetaHeaders.put("pushIdentifier", "Diwali");
		  if (App42API.appApiKey == null
					|| App42API.appApiKey.equals(""))
				App42Preferences.instance().resetApp42API(this);
		  PushNotificationService pushService=App42API.buildPushNotificationService();
		  pushService.setOtherMetaHeaders(otherMetaHeaders);
		  pushService.trackPush(new App42CallBack() {
			
			@Override
			public void onSuccess(Object arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onException(Exception arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	/**
	 * Validate push message.
	 *
	 * @param pushMessage the push message
	 * @param intent the intent
	 */
	private void validatePushMessage(final PushMessage pushMessage,final Intent intent) {
		if(pushMessage.isGeoPush()){
			 App42LocationManager.fetchGPSLocation(this,new App42LocationListener() {
				@Override
				public void onLocationFetched(Location location) {
					// TODO Auto-generated method stub
					App42Util.saveLocation(location, App42GCMService.this);
					validateGeobasePush(pushMessage,intent);
				}
				@Override
				public void onLocationException(App42Exception e) {
					// TODO Auto-generated method stub
					validateGeobasePush(pushMessage,intent);
				}
				@Override
				public void onLocationAddressRetrived(Address address) {
					// TODO Auto-generated method stub
					App42Util.saveLocationAddress(address, App42GCMService.this);
					validateGeobasePush(pushMessage,intent);
				}
			});
		}
		else{
			showNotification(pushMessage, intent);
		}
	}
	
	/**
	 *  Validate GeoBase Push Notification.
	 *
	 * @param pushMessage the push message
	 * @param intent the intent
	 */
	private void validateGeobasePush(PushMessage pushMessage,Intent intent){
	       if(App42Util.validategeoPush(this, pushMessage.getGeoPushList()))
	    	   showNotification(pushMessage, intent);
	}

	/**
	 * Gets the notification intent.
	 *
	 * @param pushMessage the push message
	 * @return the notification intent
	 */
	private Intent getNotificationIntent(PushMessage pushMessage){
		Intent notificationIntent;
		try {
			if(pushMessage.getPushType()==PushType.Rich){
				notificationIntent = new Intent(this, MessageActivity.class);
				notificationIntent.putExtra(RichPush, pushMessage.getRichPushJson().toString());
			}
			else{
			notificationIntent = new Intent(this,
					Class.forName(getActivityName()));
			}
			if(pushMessage.getPushCampIdentifer()!=null)
				notificationIntent.putExtra("_App42CampaignName", pushMessage.getPushCampIdentifer());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			notificationIntent = new Intent(this, MainActivity.class);
		}
		notificationIntent.putExtra("message_delivered", true);
		notificationIntent.putExtra(ExtraMessage, pushMessage.getMessage());
		return notificationIntent;
	}
	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	/**
	 * Send notification.
	 *
	 * @param pushMessage the push message
	 */
	private void sendNotification(PushMessage pushMessage) {
		
		long when = System.currentTimeMillis();
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent notificationIntent=getNotificationIntent(pushMessage);

		// set intent so it does not start a new activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_stat_gcm)
				.setContentTitle(pushMessage.getTitle())
				.setStyle(new NotificationCompat.BigTextStyle().bigText(pushMessage.getMessage()))
				.setContentText(pushMessage.getMessage()).setWhen(when).setNumber(++msgCount)
				.setLights(Color.YELLOW, 1, 2).setAutoCancel(true)
				.setDefaults(Notification.DEFAULT_SOUND)
				.setDefaults(Notification.DEFAULT_VIBRATE);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NotificationId, mBuilder.build());
	}

	/**
	 * Reset msg count.
	 */
	public static void resetMsgCount() {
		msgCount = 0;
	}

	/**
	 * Broad cast message.
	 *
	 * @param message the message
	 */
	public void broadCastMessage(String message) {
		Intent intent = new Intent(DisplayMessageAction);
		intent.putExtra(ExtraMessage, message);
		this.sendBroadcast(intent);
	}

	/**
	 * Gets the activity name.
	 *
	 * @return the activity name
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
