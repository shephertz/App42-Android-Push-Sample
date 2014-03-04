package com.example.app42sample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.shephertz.app42.paas.sdk.android.App42API;

public class MainActivity extends Activity {


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		((TextView) findViewById(R.id.page_header)).setText("Main Activty");
		 App42API.initialize(
	                this,
	                "<YOUR API KEY>",
	                "<YOUR SECRET KEY>");
	        App42API.setLoggedInUser("<Logged In User>") ;
	        Util.registerWithApp42("<Your Google Project No>");
		
	}


	public void onStart() {
		super.onStart();

	}

	/*
	 * * This method is called when a Activty is stop disable all the events if
	 * occuring (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	public void onStop() {
		super.onStop();

	}

	/*
	 * This method is called when a Activty is finished or user press the back
	 * button (non-Javadoc)
	 * 
	 * @override method of superclass
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	public void onDestroy() {
		super.onDestroy();

	}

	/*
	 * called when this activity is restart again
	 * 
	 * @override method of superclass
	 */
	public void onReStart() {
		super.onRestart();

	}

	/*
	 * called when activity is paused
	 * 
	 * @override method of superclass (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	public void onPause() {
		super.onPause();
		unregisterReceiver(mBroadcastReceiver);

	}

	/*
	 * called when activity is resume
	 * 
	 * @override method of superclass (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	public void onResume() {
		super.onResume();
		String message = getIntent().getStringExtra(GCMIntentService.EXTRA_MESSAGE); 
	        Log.d("MainActivity-onResume", "Message Recieved :"+message);
	        IntentFilter filter = new IntentFilter(GCMIntentService.DISPLAY_MESSAGE_ACTION);
	        filter.setPriority(2);
	        registerReceiver(mBroadcastReceiver, filter);
	}
	
	 final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		  
	        @Override
	        public void onReceive(Context context, Intent intent) {
	        	
	        	String message = intent.getStringExtra(GCMIntentService.EXTRA_MESSAGE);
	        	Log.i("MainActivity-BroadcastReceiver", "Message Recieved " +" : " +message);
	        	((TextView) findViewById(R.id.text)).setText(message);
	        	
	        }
	    };





}
