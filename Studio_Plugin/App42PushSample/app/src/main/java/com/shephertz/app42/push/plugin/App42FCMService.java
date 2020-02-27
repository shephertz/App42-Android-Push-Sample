/**
 * -----------------------------------------------------------------------
 * Copyright � 2015 ShepHertz Technologies Pvt Ltd. All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.shephertz.app42.push.plugin;

import android.app.Notification;
import android.app.NotificationChannel;
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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.app42Sample.MainActivity;
import com.example.app42Sample.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.shephertz.app42.paas.sdk.android.App42Exception;
import com.shephertz.app42.push.plugin.App42LocationManager.App42LocationListener;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Harendra Singh
 */

public class App42FCMService extends FirebaseMessagingService {
    private static final int NOTIFICATION_ID = 1;
    public static final String ExtraMessage = "message";
    static int msgCount = 0;
    public static final String DisplayMessageAction = "com.example.app42sample.DisplayMessage";
    private static final String App42GeoTag = "app42_geoBase";
    private static final String AddressBase = "addressBase";
    private static final String LocationBase = "coordinateBase";
    private static final String KeyApp42Message = "app42_message";

    public static final String TAG = "App42 Push Demo";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        //        Log.e("onNewToken >>>>>>>>", "newToken:  " + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO (developer):Handle FCM messages here.
        // Not getting messages here?See why this may be:https:
        //goo.gl/39bRNJ

        //        Log.d("Message >>>>>>>>", "remoteMessage:  " + remoteMessage.toString());
        //        Log.d("Message >>>>>>>>", "From:  " + remoteMessage.getFrom());

        // Check if message contains a data payload..
        if (remoteMessage.getData() != null && remoteMessage.getData().size() > 0) {
            //            Log.d("Message >>>>>>>>", "Message data payload:  " + remoteMessage.getData());
            JSONObject object = new JSONObject(remoteMessage.getData());
            try {
                validatePushIfRequired(object.getString("message"), null);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Check if message contains a notification payload..
        //        if (remoteMessage.getNotification() != null) {
        //            Log.d("Message >>>>>>>>", "Message Notification:  " + remoteMessage.getNotification());
        //            Log.d("Message >>>>>>>>", "Message Notification Body:  " + remoteMessage.getNotification().getBody());
        //        }
    }

    private void showNotification(String message, Intent intent) {
        broadCastMessage(message);
        sendNotification(message);
    }

    private void validatePushIfRequired(String message, final Intent intent) {
        try {
            final JSONObject json = new JSONObject(message);
            final String geoBaseType = json.optString(App42GeoTag, null);
            if (geoBaseType == null) {
                showNotification(json.toString(), intent);
            } else {
                App42LocationManager.fetchGPSLocation(this, new App42LocationListener() {

                    @Override
                    public void onLocationFetched(Location location) {
                        // TODO Auto-generated method stub
                        LocationUtils.saveLocation(location, App42FCMService.this);
                        validateGeoBasePush(json, geoBaseType, intent);
                    }

                    @Override
                    public void onLocationException(App42Exception e) {
                        // TODO Auto-generated method stub
                        validateGeoBasePush(json, geoBaseType, intent);
                    }

                    @Override
                    public void onLocationAddressRetrieved(Address address) {
                        // TODO Auto-generated method stub
                        LocationUtils.saveLocationAddress(address, App42FCMService.this);
                        validateGeoBasePush(json, geoBaseType, intent);
                    }
                });
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            showNotification(message, intent);
        }
    }

    /**
     * Validate GeoBase Push Notification
     *
     * @param jsondata
     * @param geotype
     * @param intent
     */
    private void validateGeoBasePush(JSONObject jsondata, String geotype, Intent intent) {
        if (geotype.equals(AddressBase)) {
            if (LocationUtils.isCountryBaseSuccess(jsondata, this))
                showNotification(jsondata.optString(KeyApp42Message, ""), intent);
        } else if (geotype.equals(LocationBase)) {
            if (LocationUtils.isGeoBaseSuccess(jsondata, this))
                showNotification(jsondata.optString(KeyApp42Message, ""), intent);
        }
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a FCM message.

    /**
     * @param msg
     */
    private void sendNotification(String msg) {

        String title = this.getString(R.string.app_name);
        long when = System.currentTimeMillis();

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

        // Set intent so it does not start a new activity..
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(getResources().getString(R.string.default_notification_channel_id), getResources().getString(R.string.default_notification_channel_id), NotificationManager.IMPORTANCE_HIGH);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(mChannel);
            }
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getResources().getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg).setWhen(when).setNumber(++msgCount)
                .setLights(Color.YELLOW, 1, 2).setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(contentIntent);

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        }
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
