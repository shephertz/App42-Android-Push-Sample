/**
 * -----------------------------------------------------------------------
 *     Copyright  2010 ShepHertz Technologies Pvt Ltd. All rights reserved.
 * -----------------------------------------------------------------------
 */

package com.shephertz.app42.push.fencing;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.location.GeofenceStatusCodes;

/**
 * Geofence error codes mapped to error messages.
 */
public class GeofenceErrorMessages {
	
	/** The Constant Not_Available. */
	private static final String Not_Available="Geofence service is not available now";
	
	/** The Constant Too_Many. */
	private static final String Too_Many="Your app has registered too many geofences";
	
	/** The Constant Too_Many_Pending. */
	private static final String Too_Many_Pending="You have provided too many PendingIntents to the addGeofences() call";
	
	/** The Constant UnKnown_Error. */
	private static final String UnKnown_Error=" Unknown error: the Geofence service is not available now";
	
    /**
     * Prevents instantiation.
     */
    private GeofenceErrorMessages() {}

    /**
     * Returns the error string for a geofencing error code.
     *
     * @param context the context
     * @param errorCode the error code
     * @return the error string
     */
    public static String getErrorString(Context context, int errorCode) {
        Resources mResources = context.getResources();
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return Not_Available;
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return Too_Many;
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return Too_Many_Pending;
            default:
                return UnKnown_Error;
        }
    }
}
