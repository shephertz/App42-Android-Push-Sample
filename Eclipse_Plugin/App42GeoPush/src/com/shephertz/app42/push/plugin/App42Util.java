/**
 * -----------------------------------------------------------------------
 *     Copyright  2010 ShepHertz Technologies Pvt Ltd. All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.shephertz.app42.push.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;

import com.example.app42Sample.R;
import com.shephertz.app42.push.plugin.PushMessage.GeoPush;
import com.shephertz.app42.push.plugin.PushMessage.GeoType;

/**
 * The Class App42Util.
 *
 * @author Vishnu Garg
 */
public class App42Util {
	
	/** The m app42 preference. */
	private static SharedPreferences mApp42Preference = null;
	
	/** The Constant KeyCountry. */
	private static final String KeyCountry = "app42_countryCode";
	
	/** The Constant KeyCity. */
	private static final String KeyCity = "app42_cityName";
	
	/** The Constant KeyState. */
	private static final String KeyState = "app42_stateName";

	/** The Constant KeyCountryName. */
	private static final String KeyCountryName = "app42_countryName";
	
	/** The Constant KeySubDomainArea. */
	private static final String KeySubDomainArea = "app42_subDomain";
	
	/** The Constant KeyLat. */
	public static final String KeyLat = "app42_lat";
	
	/** The Constant KeyLong. */
	public static final String KeyLong = "app42_lng";
	
	/** The Constant KeyRadius. */
	public static final String KeyRadius = "app42_distance";

	/** The Constant App42GeoTag. */
	private static final String App42GeoTag = "app42_geoBase";
	
	/** The Constant AddressBase. */
	private static final String AddressBase = "addressBase";
	
	/** The Constant LocationBase. */
	private static final String LocationBase = "coordinateBase";
	
	/** The Constant KeyApp42Message. */
	private static final String KeyApp42Message = "app42_message";

	/** The Constant KeyMessage. */
	private static final String KeyMessage = "message";
	
	/** The Constant KeyAlert. */
	private static final String KeyAlert = "alert";
	
	/** The Constant KeyTitle. */
	private static final String KeyTitle = "title";
	
	/** The Constant KeyGeoCampTargetting. */
	private static final String KeyGeoCampTargetting = "_App42GeoTargetCoordinates";

	/** The Constant KeyForGeoCampaign. */
	private static final String KeyForGeoCampaign = "_App42GeoCampaign";
	
	/** The Constant KeyForRichPush. */
	private static final String KeyForRichPush = "_app42RichPush";

	/** The Constant KeyPushIdentifier. */
	public static final String KeyPushIdentifier = "_App42CampaignName";
	
	private static final String KeyMultiMap="app42_mapLocation";

	/** The Constant KeyBadge. */
	private static final String KeyBadge = "badge";
	
	/** The Constant KeySound. */
	private static final String KeySound = "sound";

	/** The Constant KeyType. */
	static final String KeyType = "type";
	
	/** The Constant KeyContent. */
	static final String KeyContent = "content";

	/**
	 * Inits the.
	 *
	 * @param context the context
	 */
	private static void init(Context context) {
		mApp42Preference = context.getSharedPreferences("",
				android.content.Context.MODE_PRIVATE);
	}

	/**
	 * Save Location Details for further query.
	 *
	 * @param address the address
	 * @param context the context
	 */
	public static void saveLocationAddress(Address address, Context context) {
		if (mApp42Preference == null)
			init(context);
		Editor editor = mApp42Preference.edit();
		editor.putString(KeyCountry, address.getCountryCode());
		editor.putString(KeyCity, address.getLocality());
		editor.putString(KeyState, address.getAdminArea());
		editor.putString(KeyCountryName, address.getCountryName());
		editor.putString(KeySubDomainArea, address.getSubAdminArea());
		editor.putString(KeyLat, "" + address.getLatitude());
		editor.putString(KeyLong, "" + address.getLongitude());
		editor.commit();

	}

	/**
	 * Save Location Latitude and Longitude.
	 *
	 * @param location the location
	 * @param context the context
	 */
	public static void saveLocation(Location location, Context context) {
		if (mApp42Preference == null)
			init(context);
		Editor editor = mApp42Preference.edit();
		editor.putString(KeyLat, "" + location.getLatitude());
		editor.putString(KeyLong, "" + location.getLongitude());
		editor.commit();
	}

	/**
	 * Validategeo push.
	 *
	 * @param context the context
	 * @param geoPushList the geo push list
	 * @return true, if successful
	 */
	public static boolean validategeoPush(Context context,
			ArrayList<GeoPush> geoPushList) {
		if (geoPushList == null || geoPushList.size() == 0)
			return false;
		boolean isValid = false;
		for (GeoPush geoPush : geoPushList) {
			if (geoPush.getGeoType() == GeoType.LocationBased) {
				isValid = islocationBaseSuccess(context, geoPush);
				if (isValid)
					return isValid;
			} else if (geoPush.getGeoType() == GeoType.AddressBased) {
				isValid = isAddressBaseSuccess(geoPush, context);
				if (isValid)
					return isValid;
			}
		}
		return isValid;
	}

	/**
	 * Validate if device is eligible for GeoBase Push.
	 *
	 * @param context the context
	 * @param geoPush the geo push
	 * @return true, if is location base success
	 */
	private static boolean islocationBaseSuccess(Context context,
			GeoPush geoPush) {
		boolean isSuccess = false;
		if (mApp42Preference == null)
			init(context);
		double myLattitude = Double.parseDouble(mApp42Preference.getString(
				KeyLat, "0.0"));
		double myLong = Double.parseDouble(mApp42Preference.getString(KeyLong,
				"0.0"));
		if (myLattitude > 0 || myLong > 0) {
			Location targetLocation = getLocation(geoPush.getLattitude(),
					geoPush.getLongtitude());
			Location myLocation = getLocation(myLattitude, myLong);
			float distanceInKM = (targetLocation.distanceTo(myLocation) / 1000);
			if (distanceInKM <= geoPush.getRadius())
				isSuccess = true;
		}
		return isSuccess;
	}

	/**
	 * Validate if device is eligible for Country base Push.
	 *
	 * @param lattitude the lattitude
	 * @param longtitude the longtitude
	 * @return the location
	 */
	private static Location getLocation(double lattitude, double longtitude) {
		Location location = new Location("");// provider name is unecessary
		location.setLatitude(lattitude);// your coords of course
		location.setLongitude(longtitude);
		return location;
	}

	/**
	 * Validate City.
	 *
	 * @param city the city
	 * @return true, if is city valid
	 */
	private static boolean isCityValid(String city) {
		if (isEmptyOrNull(city))
			return true;
		else
			return city.equals(mApp42Preference.getString(KeyCity, ""))
					|| city.equals(mApp42Preference.getString(KeySubDomainArea,
							""));
	}

	/**
	 * Checks if is country valid.
	 *
	 * @param country the country
	 * @return true, if is country valid
	 */
	private static boolean isCountryValid(String country) {
		if (isEmptyOrNull(country))
			return true;
		else
			return country.equals(mApp42Preference.getString(KeyCountry, ""))
					|| country.equals(mApp42Preference.getString(
							KeyCountryName, ""));
	}

	/**
	 * Checks if is state valid.
	 *
	 * @param state the state
	 * @return true, if is state valid
	 */
	private static boolean isStateValid(String state) {
		if (isEmptyOrNull(state))
			return true;
		else
			return state.equals(mApp42Preference.getString(KeyState, ""));
	}

	/**
	 * Checks if is empty or null.
	 *
	 * @param value the value
	 * @return true, if is empty or null
	 */
	private static boolean isEmptyOrNull(String value) {
		return (value == null) || value.equals("");
	}

	/**
	 * Validate eligibility for Country base Push.
	 *
	 * @param geoPush the geo push
	 * @param context the context
	 * @return true, if is address base success
	 */
	private static boolean isAddressBaseSuccess(GeoPush geoPush, Context context) {
		boolean isSuccess = false;
		if (mApp42Preference == null)
			init(context);
		isSuccess = isCountryValid(geoPush.getCountry())
				&& isStateValid(geoPush.getState())
				&& isCityValid(geoPush.getCity());
		return isSuccess;
	}

	/**
	 * Parses the push message.
	 *
	 * @param intent the intent
	 * @param context the context
	 * @return the push message
	 */
	public static PushMessage parsePushMessage(Intent intent, Context context) {
		String message = getPushMessage(intent);
		if (message == null)
			return null;
		PushMessage pushMessage = getMessageObject(message, context,
				getTitle(intent, context));
		Bundle bundle = intent.getExtras();

		if (bundle != null) {
			HashMap<String, Object> pushExtras = new HashMap<String, Object>();
			Set<String> keys = bundle.keySet();
			Iterator<String> it = keys.iterator();
			while (it.hasNext()) {
				String key = it.next();
				if (isExtraKey(key)) {
					try {
						if (key.equalsIgnoreCase(KeyForGeoCampaign)) {
							JSONObject geoCampJson = new JSONObject(
									bundle.getString(key));
							setGeoCampaign(pushMessage, geoCampJson);
						} else if (key.equalsIgnoreCase(KeyForRichPush)) {
							JSONObject richPush = new JSONObject(
									bundle.getString(key));
							Iterator<?> richExtraKeys = richPush.keys();
							while (richExtraKeys.hasNext()) {
								String extraKey = (String) richExtraKeys.next();
								if (!extraKey.equalsIgnoreCase(KeyContent)
										&& !extraKey.equalsIgnoreCase(KeyType)) {
									pushExtras.put(extraKey,
											richPush.get(extraKey));
								}
							}
							setRichPush(pushMessage, richPush);
						} else if (key.equalsIgnoreCase(KeyPushIdentifier)) {
							pushMessage.setPushCampIdentifer(bundle
									.getString(key));
						} else {
							pushExtras.put(key, bundle.get(key));
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			pushMessage.setExtraData(pushExtras);
		}
		return pushMessage;
	}

	/**
	 * Checks if is extra key.
	 *
	 * @param key the key
	 * @return true, if is extra key
	 */
	private static boolean isExtraKey(String key) {
		if (key.equalsIgnoreCase(KeyAlert) || key.equalsIgnoreCase(KeyMessage)
				|| key.equalsIgnoreCase(KeyTitle))
			return false;
		return true;
	}

	/**
	 * Gets the message object.
	 *
	 * @param message the message
	 * @param context the context
	 * @param title the title
	 * @return the message object
	 */
	private static PushMessage getMessageObject(String message,
			Context context, String title) {
		PushMessage pushMessage = null;
		try {
			JSONObject jsonMessage = new JSONObject(message);
			final String geoBaseType = jsonMessage.optString(App42GeoTag, null);
			final String richContent = jsonMessage.optString(KeyContent, null);
			if (geoBaseType == null) {
				pushMessage = new PushMessage(message, title);
			} else {
				pushMessage = getGeoTypePush(jsonMessage, title, geoBaseType);
			}
			if (richContent != null) {
				setRichPush(pushMessage, jsonMessage);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			pushMessage = new PushMessage(message, title);
		}
		return pushMessage;
	}

	/**
	 * Gets the geo type push.
	 *
	 * @param jsonMessage the json message
	 * @param title the title
	 * @param geoType the geo type
	 * @return the geo type push
	 */
	private static PushMessage getGeoTypePush(JSONObject jsonMessage,
			String title, String geoType) {
		PushMessage pushMessage = new PushMessage(jsonMessage.optString(
				KeyApp42Message, ""), title);
		if (geoType.equals(AddressBase))
			pushMessage.setGeoAddressPush(
					jsonMessage.optString(KeyCountry, null),
					jsonMessage.optString(KeyState, null),
					jsonMessage.optString(KeyCity, null), GeoType.AddressBased);
		else if (geoType.equals(LocationBase)){
			JSONArray mapsArr=jsonMessage.optJSONArray(KeyMultiMap);
			if(mapsArr!=null){
				for(int i=0;i<mapsArr.length();i++){
					try {
						JSONObject cordsJson=mapsArr.getJSONObject(i);
						pushMessage.setGeoLocationPush(cordsJson.optDouble("lat", 0.0),
								cordsJson.optDouble("lng", 0.0),
								cordsJson.optDouble("radius", 0.0),
								GeoType.LocationBased);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			else
			pushMessage.setGeoLocationPush(jsonMessage.optDouble(KeyLat, 0.0),
					jsonMessage.optDouble(KeyLong, 0.0),
					jsonMessage.optDouble(KeyRadius, 0.0),
					GeoType.LocationBased);
		}

		return pushMessage;
	}

	/**
	 * Sets the rich push.
	 *
	 * @param pushMessage the push message
	 * @param jsonMessage the json message
	 * @throws JSONException the JSON exception
	 */
	private static void setRichPush(PushMessage pushMessage,
			JSONObject jsonMessage) throws JSONException {
		if (jsonMessage.optString(KeyTitle, null) != null)
			pushMessage.setTitle(jsonMessage.getString(KeyTitle));
		pushMessage.setRichPush(jsonMessage.optString(KeyType, "text"),
				jsonMessage.optString(KeyContent, ""));
	}

	/**
	 * Sets the geo campaign.
	 *
	 * @param pushMessage the push message
	 * @param jsonMessage the json message
	 */
	private static void setGeoCampaign(PushMessage pushMessage,
			JSONObject jsonMessage) {
		JSONArray cordsArr=jsonMessage.optJSONArray(KeyGeoCampTargetting);
		
		final String geoBaseType = jsonMessage.optString(App42GeoTag, null);
		String campaignName=jsonMessage.optString(KeyPushIdentifier, null);
		if(campaignName!=null){
			pushMessage.setPushCampIdentifer(campaignName);
		}
		if(cordsArr==null||cordsArr.length()==0)
			return;
		for(int i=0;i<cordsArr.length();i++){
			JSONObject cordsJson;
			try {
				cordsJson = cordsArr.getJSONObject(i);
				if (geoBaseType.equals(AddressBase))
					pushMessage.setGeoAddressPush(
							cordsJson.optString(KeyCountry, null),
							cordsJson.optString(KeyState, null),
							cordsJson.optString(KeyCity, null), GeoType.AddressBased);
				else if (geoBaseType.equals(LocationBase)){
					
						pushMessage.setGeoLocationPush(cordsJson.optDouble(KeyLat, 0.0),
								cordsJson.optDouble(KeyLong, 0.0),
								cordsJson.optDouble(KeyRadius, 0.0),
								GeoType.LocationBased);
					}
					
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
	}

	/**
	 * Gets the title.
	 *
	 * @param intent the intent
	 * @param context the context
	 * @return the title
	 */
	private static String getTitle(Intent intent, Context context) {
		String title = intent.getExtras().getString(KeyTitle);
		if (title == null)
			title = context.getString(R.string.app_name);
		return title;
	}

	/**
	 * Gets the push message.
	 *
	 * @param intent the intent
	 * @return the push message
	 */
	private static String getPushMessage(Intent intent) {
		String message = intent.getExtras().getString(KeyMessage,null);
		if (message == null) {
			message = intent.getExtras().getString(KeyAlert,null);
			if (message == null) 
				message = intent.getExtras().getString(KeyApp42Message,null);
		}
		return message;
	
	}

}
