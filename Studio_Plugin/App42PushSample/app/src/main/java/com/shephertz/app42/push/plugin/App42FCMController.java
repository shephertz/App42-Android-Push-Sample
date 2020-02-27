/**
 * -----------------------------------------------------------------------
 * Copyright 2015 ShepHertz Technologies Pvt Ltd. All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.shephertz.app42.push.plugin;

/**
 * @author Harendra Singh
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.shephertz.app42.paas.sdk.android.App42API;
import com.shephertz.app42.paas.sdk.android.App42CallBack;
import com.shephertz.app42.paas.sdk.android.App42Response;

/**
 * @author Harendra Singh
 */
public class App42FCMController {
    private static final int PlayServiceResolutionRequest = 9000;
    private static final String Tag = "App42PushNotification";
    public static final String KeyRegId = "registration_id";
    private static final String KeyAppVersion = "appVersion";
    private static final String PrefKey = "App42PushSample";
    private static final String KeyRegisteredOnApp42 = "app42_register";

    /**
     * This function checks for GooglePlay Service availability
     *
     * @param activity
     * @return
     */
    public static boolean isPlayServiceAvailable(Activity activity) {

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, PlayServiceResolutionRequest).show();
            } else {
                Log.i(Tag, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    /**
     * This function used to get FCM Token from New API
     *
     * @param context
     * @return
     */
    @SuppressLint("NewApi")
    public static String getFCMToken(Context context) {
        final SharedPreferences prefs = getFCMPreferences(context);
        String registrationId = prefs.getString(KeyRegId, "");
        if (registrationId.isEmpty()) {
            Log.i(Tag, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(KeyAppVersion,
                Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(Tag, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @param context
     * @return
     */
    private static SharedPreferences getFCMPreferences(Context context) {
        return context.getSharedPreferences(PrefKey, Context.MODE_PRIVATE);
    }

    /**
     * Get AppVersion
     *
     * @param context
     * @return
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Store registartion Id from FCM in preferences
     *
     * @param context
     * @param regId
     */
    public static void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getFCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(Tag, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KeyRegId, regId);
        editor.putInt(KeyAppVersion, appVersion);
        editor.commit();
    }

    /**
     * Validate if registered
     *
     * @param context
     * @return
     */
    public static boolean isApp42Registerd(Context context) {
        return getFCMPreferences(context).getBoolean(KeyRegisteredOnApp42, false);
    }

    /**
     * @param context
     */
    public static void storeApp42Success(Context context) {
        final SharedPreferences prefs = getFCMPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KeyRegisteredOnApp42, true);
        editor.commit();
    }

    /**
     * @param context
     * @param callBack
     */
    @SuppressLint("NewApi")
    public static void getFCMToken(Context context, App42FCMListener callBack) {

        String fcmToken = App42FCMController.getFCMToken(context);
        if (fcmToken.isEmpty()) {
            registerOnFCM(context, callBack);
        } else {
            callBack.onFCMTokenFetch(fcmToken);
        }
    }


    /**
     * Used to register device on FCM
     *
     * @param context
     * @param callback
     */
    public static void registerOnFCM(final Context context, final App42FCMListener callback) {

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                try {
                    Log.d("InstanceIdResult >>>>>>", task.toString());
                    if (!task.isSuccessful()) {
                        Log.e("InstanceId FAILED >>>>>", "getInstanceId failed:  ", task.getException());
                        return;
                    }

                    // Get new Instance ID token and send to server..
                    String token = task.getResult().getToken();
                    if (callback != null) {
                        callback.onFCMTokenFetch(token);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * Send a PushMessage to desired User
     *
     * @param userName
     * @param message
     * @param callBack
     */
    public static void sendPushToUser(String userName, String message, final App42FCMListener callBack) {
        App42API.buildPushNotificationService().sendPushMessageToUser(userName, message, new App42CallBack() {

            @Override
            public void onSuccess(Object arg0) {
                // TODO Auto-generated method stub
                App42Response response = (App42Response) arg0;
                callBack.onApp42Response(response.getStrResponse());
            }

            @Override
            public void onException(Exception arg0) {
                // TODO Auto-generated method stub
                callBack.onApp42Response(arg0.getMessage());
            }
        });
    }

    /**
     * This function used to register FCM device Token on AppHQ
     *
     * @param userName
     * @param deviceToen
     * @param callBack
     */
    public static void registerOnApp42(String userName, String deviceToen, final App42FCMListener callBack) {
        App42API.buildPushNotificationService().storeDeviceToken(userName, deviceToen, new App42CallBack() {
            @Override
            public void onSuccess(Object arg0) {
                App42Response response = (App42Response) arg0;
                callBack.onRegisterApp42(response.getStrResponse());
            }

            @Override
            public void onException(Exception arg0) {
                // TODO Auto-generated method stub
                callBack.onApp42Response(arg0.getMessage());
            }
        });
    }

    /**
     * CallBack Listener
     */
    public interface App42FCMListener {
        public void onError(String errorMsg);

        public void onFCMTokenFetch(String gcmRegId);

        public void onApp42Response(String responseMessage);

        public void onRegisterApp42(String responseMessage);

    }
}
