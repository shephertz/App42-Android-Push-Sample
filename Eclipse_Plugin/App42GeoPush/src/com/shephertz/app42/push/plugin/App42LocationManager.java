/**
 * -----------------------------------------------------------------------
 *     Copyright  2010 ShepHertz Technologies Pvt Ltd. All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.shephertz.app42.push.plugin;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

import com.shephertz.app42.paas.sdk.android.App42API;
import com.shephertz.app42.paas.sdk.android.App42Exception;

/**
 * The Class App42LocationManager.
 *
 * @author Vishnu Garg
 */
public class App42LocationManager {
	
	/**
	 * Fetch gps location.
	 *
	 * @param context the context
	 * @param callback the callback
	 */
	public static void fetchGPSLocation(Context context,App42LocationListener callback) {
		// Getting LocationManager object
	try{
		Location location = null;
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		if (locationManager == null)
			callback.onLocationException(new App42Exception(
					"PLease Enabale Location Service"));
		List<String> strList = locationManager.getAllProviders();
		if (strList != null && strList.size() > 0) {
			for (String provider : strList) {
				// Get the location from the given provider
				location = locationManager.getLastKnownLocation(provider);
				if (location != null) {
					break;
				}
			}//40.7127837,-74.00594130000002
		}
		if (location == null)
			callback.onLocationException(new App42Exception("GPS is Disable"));
		else
			getLocationAddress(location, callback);
	}catch(Throwable w){
			callback.onLocationException(new App42Exception("User denied for location permission"));
		}
	}

	/**
	 * Gets the location address.
	 *
	 * @param location the location
	 * @param callback the callback
	 * @return the location address
	 */
	private static void getLocationAddress(final Location location,
			final App42LocationListener callback) {
		new Thread() {
			@Override
			public void run() {
				try {
					Geocoder geocoder = new Geocoder(App42API.appContext,
							Locale.getDefault());
					List<Address> addresses = null;
					addresses = geocoder.getFromLocation(
							location.getLatitude(), location.getLongitude(), 1);
					if (addresses != null && addresses.size() > 0){
						Address address=addresses.get(0);
					
						callback.onLocationAddressRetrived(addresses.get(0));
					}
					else
						callback.onLocationFetched(location);
				} catch (Exception ex) {
					ex.printStackTrace();
					callback.onLocationException(new App42Exception(ex
							.getMessage()));
				}
			}
		}.start();
	}
	
	/**
	 * The listener interface for receiving app42Location events.
	 * The class that is interested in processing a app42Location
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addApp42LocationListener<code> method. When
	 * the app42Location event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see App42LocationEvent
	 */
	public interface App42LocationListener {
		
		/**
		 * On location address retrived.
		 *
		 * @param address the address
		 */
		public void onLocationAddressRetrived(Address address);
		
		/**
		 * On location exception.
		 *
		 * @param e the e
		 */
		public void onLocationException(App42Exception e);
		
		/**
		 * On location fetched.
		 *
		 * @param location the location
		 */
		public void onLocationFetched(Location location);
	}

}
