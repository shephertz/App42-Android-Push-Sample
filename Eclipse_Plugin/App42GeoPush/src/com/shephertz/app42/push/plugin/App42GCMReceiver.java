/**
 * -----------------------------------------------------------------------
 *     Copyright  2010 ShepHertz Technologies Pvt Ltd. All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.shephertz.app42.push.plugin;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * The Class App42GCMReceiver.
 *
 * @author Vishnu Garg
 */
public class App42GCMReceiver  extends WakefulBroadcastReceiver {
	
	 /* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	    public void onReceive(Context context, Intent intent) {
	        ComponentName comp = new ComponentName(context.getPackageName(),
	        		App42GCMService.class.getName());
	        startWakefulService(context, (intent.setComponent(comp)));
	        setResultCode(Activity.RESULT_OK);
	}
}