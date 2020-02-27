/**
 * -----------------------------------------------------------------------
 * Copyright 2015 ShepHertz Technologies Pvt Ltd. All rights reserved.
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

import com.shephertz.app42.paas.sdk.android.App42Exception;

/**
 * @author Vishnu Garg
 *
 */
public class App42LocationManager {

    public static void fetchGPSLocation(Context context, App42LocationListener callback) {
        // Getting LocationManager object
        try {
            Location location = null;
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager == null)
                callback.onLocationException(new App42Exception(
                        "PLease Enable Location Service"));
            List<String> strList = locationManager.getAllProviders();
            if (strList != null && strList.size() > 0) {
                for (String provider : strList) {
                    // Get the location from the given provider
                    location = locationManager.getLastKnownLocation(provider);
                    if (location != null) {
                        break;
                    }
                }
            }
            if (location == null)
                callback.onLocationException(new App42Exception("GPS is Disable"));
            else
                getLocationAddress(location, callback, context);
        } catch (Throwable w) {
            callback.onLocationException(new App42Exception("User denied for location permission"));
        }
    }

    /**
     * @param jsonArray
     */
    private static void getLocationAddress(final Location location,
                                           final App42LocationListener callback, final Context context) {
        new Thread() {
            @Override
            public void run() {
                try {
                    Geocoder geocoder = new Geocoder(context,
                            Locale.getDefault());
                    List<Address> addresses = null;
                    addresses = geocoder.getFromLocation(
                            location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && addresses.size() > 0) {
                        Address address = addresses.get(0);
                        System.out.println(address);
                        System.out.println(address.getCountryCode());
                        System.out.println(address.getLocality());
                        System.out.println(address.getLongitude());
                        System.out.println(address.getLatitude());
                        callback.onLocationAddressRetrieved(addresses.get(0));
                    } else
                        callback.onLocationFetched(location);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    callback.onLocationFetched(location);
                }
            }
        }.start();
    }

    public interface App42LocationListener {
        public void onLocationAddressRetrieved(Address address);

        public void onLocationException(App42Exception e);

        public void onLocationFetched(Location location);
    }

}
