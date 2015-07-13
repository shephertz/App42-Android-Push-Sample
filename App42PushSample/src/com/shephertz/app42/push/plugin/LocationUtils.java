/**
 * -----------------------------------------------------------------------
 *     Copyright © 2015 ShepHertz Technologies Pvt Ltd. All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.shephertz.app42.push.plugin;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.location.Location;

/**
 * @author Vishnu Garg
 * 
 */
public class LocationUtils {
	private static SharedPreferences mApp42Preference = null;
	private static final String KeyCountry = "app42_countryCode";
	private static final String KeyCity = "app42_cityName";
	private static final String KeyLat = "app42_lat";
	private static final String KeyLong = "app42_lng";
	private static final String KeyRadius = "app42_distance";

	/**
	 * @param context
	 */
	private static void init(Context context) {
		mApp42Preference = context.getSharedPreferences("",
				android.content.Context.MODE_PRIVATE);
	}

	/** Save Location Details for further query
	 * @param address
	 * @param context
	 */
	public static void saveLocationAddress(Address address, Context context) {
		if (mApp42Preference == null)
			init(context);
		Editor editor = mApp42Preference.edit();
		editor.putString(KeyCountry, address.getCountryCode());
		editor.putString(KeyCity, address.getLocality());
		editor.putString(KeyLat, "" + address.getLatitude());
		editor.putString(KeyLong, "" + address.getLongitude());
		editor.commit();

	}

	/** Save Location Latitude and Longitude
	 * @param location
	 * @param context
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
	 * Validate if device is eligible for GeoBase Push
	 * @param jsonData
	 * @param context
	 * @return
	 */
	public static boolean isGeoBaseSuccess(JSONObject jsonData, Context context) {

		try {
			boolean isSuccess = false;
			double myLattitude = Double.parseDouble(mApp42Preference.getString(
					KeyLat, "0.0"));
			double myLong = Double.parseDouble(mApp42Preference.getString(
					KeyLong, "0.0"));
			if (myLattitude > 0 || myLong > 0) {
				double app42Lat = jsonData.getDouble(KeyLat);
				double app42Long = jsonData.getDouble(KeyLong);
				double radius = jsonData.getDouble(KeyRadius);
				Location targetLocation = getLocation(app42Lat, app42Long);
				if (mApp42Preference == null)
					init(context);
				Location myLocation = getLocation(myLattitude, myLong);
				float distanceInMeters = (targetLocation.distanceTo(myLocation) * 1000);
				if (distanceInMeters <= radius)
					isSuccess = true;
			}
			return isSuccess;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * Validate if device is eligible for Country base Push
	 * @param lattitude
	 * @param longtitude
	 * @return
	 */
	private static Location getLocation(double lattitude, double longtitude) {
		Location location = new Location("");// provider name is unecessary
		location.setLatitude(lattitude);// your coords of course
		location.setLongitude(longtitude);
		return location;
	}

	/** Validate City
	 * @param city
	 * @return
	 */
	private static boolean isCityValid(String city) {
		if (city == null)
			return true;
		else
			return city.equals(mApp42Preference.getString(KeyCity, ""));
	}

	/**
	 * Validate eligibility for Country base Push
	 * @param jsonData
	 * @param context
	 * @return
	 */
	public static boolean isCountryBaseSuccess(JSONObject jsonData,
			Context context) {
		try {
			boolean isSuccess = false;
			String countryCode = jsonData.optString(KeyCountry, null);
			String cityName = jsonData.optString(KeyCity, null);
			if (countryCode != null) {
				if (mApp42Preference == null)
					init(context);
				String myContryCode = mApp42Preference
						.getString(KeyCountry, "");
				if (myContryCode.equals(countryCode)) {
					isSuccess = isCityValid(cityName);
				}
			}
			return isSuccess;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
}
