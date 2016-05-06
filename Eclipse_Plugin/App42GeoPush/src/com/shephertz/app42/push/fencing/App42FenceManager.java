/**
 * -----------------------------------------------------------------------
 *     Copyright  2010 ShepHertz Technologies Pvt Ltd. All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.shephertz.app42.push.fencing;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.shephertz.app42.paas.sdk.android.App42API;
import com.shephertz.app42.paas.sdk.android.App42CallBack;
import com.shephertz.app42.paas.sdk.android.App42Response;
import com.shephertz.app42.paas.sdk.android.timer.Timer;
import com.shephertz.app42.push.plugin.App42GCMController;
import com.shephertz.app42.push.plugin.App42Util;

/**
 * The Class App42FenceManager.
 * 
 * @author Vishnu
 */
public class App42FenceManager implements ConnectionCallbacks,
		OnConnectionFailedListener, ResultCallback<Status> {

	/** The Constant TAG. */
	protected static final String TAG = "App42 GeoFencing";

	/** The Constant KeyEntry. */
	private static final String KeyEntry = "app42_entry";

	/** The Constant KeyExit. */
	private static final String KeyExit = "app42_exit";

	/** The Constant KeyDwell. */
	private static final String KeyDwell = "app42_dual";

	/** The Constant KeyInitialDelay. */
	private static final String KeyInitialDelay = "app42_intialdelay";

	/** The Constant KeyStatus. */
	private static final String KeyStatus = "status";
	
	/** The Constant KeyReqIds. */
	private static final String KeyReqIds = "requestIds";

	/** The Constant KeyIsValid. */
	private static final String KeyIsValid = "isValid";

	/** The m google api client. */
	protected GoogleApiClient mGoogleApiClient;

	/** The geofence list. */
	private ArrayList<Geofence> geofenceList;

	/** The m geofence pending intent. */
	private PendingIntent mGeofencePendingIntent;

	/** The m context. */
	private static Context mContext;

	/** The m instance. */
	private static App42FenceManager mInstance;
	
	/** The handler. */
	private Handler handler = new Handler();
	
	/** The fence campaign json. */
	private JSONObject fenceCampaignJson = new JSONObject();
	
	/** The current campaign. */
	private String currentCampaign = null;

	/**
	 * Gets the current campaign.
	 *
	 * @return the current campaign
	 */
	public String getCurrentCampaign() {
		return currentCampaign;
	}


	/**
	 * Gets the single instance of App42FenceManager.
	 *
	 * @param context the context
	 * @return single instance of App42FenceManager
	 */
	public static App42FenceManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new App42FenceManager(context);
			mContext = context;
		}
		return mInstance;
	}

	
	/**
	 * Instantiates a new app42 fence manager.
	 * 
	 * @param context
	 *            the context
	 */
	private App42FenceManager(Context context) {
		geofenceList = new ArrayList<Geofence>();

		mGoogleApiClient = new GoogleApiClient.Builder(context)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();
	}

	/**
	 * Connect.
	 */
	public void connect() {
		if (!mGoogleApiClient.isConnected())
			mGoogleApiClient.connect();
	}

	/**
	 * Dis connect.
	 */
	public void disConnect() {
		mGoogleApiClient.disconnect();
	}

	/**
	 * Sets the geo fences.
	 *
	 * @param campaignData the campaign data
	 * @param fencingCords the fencing cords
	 */
	public void setGeoFences(final JSONObject campaignData,
			final JSONArray fencingCords) {

//		
//		Thread t = new Thread(new Runnable() {
//			public void run() {
//
//				handler.postDelayed(new Runnable() {
//					public void run() {
						buildGeoFences(campaignData, fencingCords);
//					}
//				}, 5000);
//
//			}
//		});
//		t.start();
	}
	/**
	 * Builds the fences.
	 * 
	 * @param campaignData
	 *            the campaign data
	 * @param fencingCords
	 *            the fencing cords
	 */
	private void buildGeoFences(JSONObject campaignData, JSONArray fencingCords) {
		// Code to create List of fences
		if (!mGoogleApiClient.isConnected()) {
			Log.i(TAG, "GoogleApiClient is Not Connected");
			return;
		}
		String campaignName = campaignData.optString(
				App42Util.KeyPushIdentifier, "");
		boolean isOnEntry = campaignData.optBoolean(KeyEntry, false);
		boolean isOnExit = campaignData.optBoolean(KeyExit, false);
		boolean isDwell = campaignData.optBoolean(KeyDwell, false);
		long delay = campaignData.optLong(KeyInitialDelay, 0);
		JSONArray requestIdArr = new JSONArray();
		for (int i = 0; i < fencingCords.length(); i++) {
			JSONObject fenceJson;
			try {
				fenceJson = fencingCords.getJSONObject(i);
				long id = fenceJson.optLong("app42_geoFenceId", 0);
				String requestId = getRequestId(id, campaignName);
				requestIdArr.put(id);

				geofenceList
						.add(new Geofence.Builder()
								.setRequestId(requestId)
								.setCircularRegion(
										fenceJson.optDouble(App42Util.KeyLat,
												0.0),
										fenceJson.optDouble(App42Util.KeyLong,
												0.0),
										(float) fenceJson.optDouble(
												App42Util.KeyRadius, 0.0))
								.setExpirationDuration(Geofence.NEVER_EXPIRE)
								.setTransitionTypes(
										getTransitionState(isOnEntry, isOnExit,
												isDwell))
								.setLoiteringDelay(
										fenceJson
												.optInt("app42_intialdelay", 0))
								.build());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {

			addGeoFences(campaignName);
			fenceCampaignJson.put(campaignName, requestIdArr);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Gets the geo props.
	 * 
	 * @param requestId
	 *            the request id
	 * @param geoEvent
	 *            the geo event
	 * @return the geo props
	 * @throws JSONException
	 *             the JSON exception
	 */
	private static JSONObject getGeoProps(String requestId, String geoEvent)
			throws JSONException {
		int sepIndex = requestId.indexOf("-");
		if (sepIndex > 0) {
			JSONObject props = new JSONObject();
			props.put("event", "dwell");
			props.put("campaignName", requestId.substring(0, sepIndex));
			props.put("geoFenceId", requestId.substring(sepIndex));
			return props;
		}
		return null;
	}

	/**
	 * Track geo fence.
	 * 
	 * @param requestID
	 *            the request id
	 * @param transitionEvent
	 *            the transition event
	 */
	public void trackGeoFence(final String requestID, String transitionEvent) {
		JSONObject geoProps;
		try {
			geoProps = getGeoProps(requestID, transitionEvent);
			if (geoProps == null)
				return;
		} catch (JSONException e) {
			return;
		}

		App42API.buildEventService().trackGeoFencing(geoProps,
				new App42CallBack() {

					@Override
					public void onSuccess(Object response) {
						// TODO Auto-generated method stub
						System.out.println("#######" + response.toString());
						App42Response app42Response = (App42Response) response;
						checkFenceValidity(app42Response.getStrResponse(),
								requestID);
					}

					@Override
					public void onException(Exception ex) {
						// TODO Auto-generated method stub
						System.out.println("#######" + ex.getMessage());
					}
				});
	}

	/**
	 * Check fence validity.
	 *
	 * @param response the response
	 * @param requestId the request id
	 */
	private void checkFenceValidity(String response, String requestId) {
		try {
			JSONObject responseJson = new JSONObject(response);
			boolean status = responseJson.optBoolean(KeyIsValid, false);
			if (status == false) {
				int sepIndex = requestId.indexOf("-");
				if (sepIndex > 0) {
					String campaignName = requestId.substring(0, sepIndex);
					JSONObject fenceJson = getFenceJsonFromPref(campaignName);
					if (fenceJson != null && fenceJson.length() > 0) {
						removeGeoFencing(fenceJson.getJSONArray(KeyReqIds),
								campaignName);
						removeFenceFromPref(campaignName);
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Gets the request id.
	 * 
	 * @param fenceId
	 *            the fence id
	 * @param campName
	 *            the camp name
	 * @return the request id
	 */
	private String getRequestId(long fenceId, String campName) {
		return campName + "-" + fenceId;
	}

	/**
	 * Gets the transition state.
	 * 
	 * @param isOnEntry
	 *            the is on entry
	 * @param isOnExit
	 *            the is on exit
	 * @param onDwell
	 *            the on dwell
	 * @return the transition state
	 */
	private int getTransitionState(boolean isOnEntry, boolean isOnExit,
			boolean onDwell) {
		int entry = 0, exit = 0, dwell = 0;
		if (isOnEntry)
			entry = Geofence.GEOFENCE_TRANSITION_ENTER;
		if (isOnExit)
			exit = Geofence.GEOFENCE_TRANSITION_EXIT;
		if (onDwell)
			dwell = Geofence.GEOFENCE_TRANSITION_DWELL;
		int state = entry | exit | dwell;
		return state;
	}

	/**
	 * Adds the geo fences.
	 *
	 * @param campaignName the campaign name
	 */
	public void addGeoFences(String campaignName) {
		try {
			currentCampaign = campaignName;
			LocationServices.GeofencingApi.addGeofences(mGoogleApiClient,
			// The GeofenceRequest object.
					getGeofencingRequest(),
					// A pending intent that that is reused when calling
					// removeGeofences(). This
					// pending intent is used to generate an intent when a
					// matched geofence
					// transition is observed.
					getGeofencePendingIntent()).setResultCallback(this);
			// Result processed in onResult().
			//
		} catch (SecurityException securityException) {
			// Catch exception generated if the app does not use
			// ACCESS_FINE_LOCATION permission.
			logSecurityException(securityException);
		}
	}

	/**
	 * Gets the geofencing request.
	 * 
	 * @return the geofencing request
	 */
	private GeofencingRequest getGeofencingRequest() {
		GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
		builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
		builder.addGeofences(geofenceList);
		return builder.build();
	}

	/**
	 * Gets the geofence pending intent.
	 * 
	 * @return the geofence pending intent
	 */
	private PendingIntent getGeofencePendingIntent() {
		// Reuse the PendingIntent if we already have it.
		if (mGeofencePendingIntent != null) {
			return mGeofencePendingIntent;
		}
		Intent intent = new Intent(mContext,
				GeofenceTransitionsIntentService.class);
		// We use FLAG_UPDATE_CURRENT so that we get the same pending intent
		// back when calling
		// addGeofences() and removeGeofences().
		return PendingIntent.getService(mContext, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}

	/**
	 * Removes the geo fencing.
	 *
	 * @param requestIds the request ids
	 * @param campignName the campign name
	 */
	public void removeGeoFencing(JSONArray requestIds, String campignName) {
		if (!mGoogleApiClient.isConnected()) {
			return;
		}
		try {
			if (requestIds != null && requestIds.length() > 0) {
				ArrayList<String> ids = new ArrayList<String>();
				for (int i = 0; i < requestIds.length(); i++) {
					ids.add(campignName + "-" + requestIds);
				}
				LocationServices.GeofencingApi.removeGeofences(
						mGoogleApiClient,
						// This is the same pending intent that was used in
						// addGeofences().
						ids); // Result
			}
			// Remove geofences.

		} catch (SecurityException securityException) {
			// Catch exception generated if the app does not use
			// ACCESS_FINE_LOCATION permission.
			logSecurityException(securityException);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.android.gms.common.api.ResultCallback#onResult(com.google.
	 * android.gms.common.api.Result)
	 */
	@Override
	public void onResult(Status status) {
		// TODO Auto-generated method stub
		if (status.isSuccess()) {
			// Update state and save in shared preferences.
			// Code to save preference against that campaign
			Log.i(TAG, "Geo Fence added successfully");
			if (currentCampaign != null) {
				JSONArray requestIds = fenceCampaignJson
						.optJSONArray(currentCampaign);
				if (requestIds == null || requestIds.length() == 0) {
					// Code to save status true against CampainName and Ids
					JSONObject fenceJson = new JSONObject();
					try {
						fenceJson.put(KeyStatus, true);
						fenceJson.put(KeyReqIds, requestIds);
						storeFenceCampign(fenceJson, currentCampaign);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		} else {
			// Get the status code for the error and log it using a
			// user-friendly message.
			String errorMessage = GeofenceErrorMessages.getErrorString(
					mContext, status.getStatusCode());
			Log.e(TAG, errorMessage);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
	 * #onConnectionFailed(com.google.android.gms.common.ConnectionResult)
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub
		Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
				+ result.getErrorCode());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
	 * #onConnected(android.os.Bundle)
	 */
	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		Log.i(TAG, "Connected to GoogleApiClient");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
	 * #onConnectionSuspended(int)
	 */
	@Override
	public void onConnectionSuspended(int arg0) {

		Log.i(TAG, "Connection suspended");
	}

	/**
	 * Log security exception.
	 * 
	 * @param securityException
	 *            the security exception
	 */
	private void logSecurityException(SecurityException securityException) {
		Log.e(TAG, "Invalid location permission. "
				+ "You need to use ACCESS_FINE_LOCATION with geofences",
				securityException);
	}

	/**
	 * Gets the fence json from pref.
	 *
	 * @param campaignName the campaign name
	 * @return the fence json from pref
	 */
	public JSONObject getFenceJsonFromPref(String campaignName) {
		if (mContext == null)
			return null;
		final SharedPreferences prefs = App42GCMController
				.getGCMPreferences(mContext);
		String fenceJson = prefs.getString(campaignName, "{}");
		JSONObject json = null;
		try {
			json = new JSONObject(fenceJson);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}

	/**
	 * Removes the fence from pref.
	 *
	 * @param campaignName the campaign name
	 */
	private void removeFenceFromPref(String campaignName) {
		if (mContext == null)
			return;
		final SharedPreferences prefs = App42GCMController
				.getGCMPreferences(mContext);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(campaignName);
		editor.commit();
	}

	/**
	 * Store fence campign.
	 *
	 * @param fenseJson the fense json
	 * @param campaignName the campaign name
	 */
	private void storeFenceCampign(JSONObject fenseJson, String campaignName) {
		if (mContext == null)
			return;
		final SharedPreferences prefs = App42GCMController
				.getGCMPreferences(mContext);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(campaignName, fenseJson.toString());
		editor.commit();
	}
}
