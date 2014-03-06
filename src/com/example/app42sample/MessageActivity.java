/**
 * 
 */
package com.example.app42sample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * @author Ajay Tiwari
 * 
 */
public class MessageActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		((TextView) findViewById(R.id.page_header)).setText("Message Activty");

		String message = getIntent().getStringExtra(
				App42GCMService.EXTRA_MESSAGE);
		App42GCMService.resetMsgCount();
		((TextView) findViewById(R.id.text)).setText(message);

		Log.d("MessageActivity-onCreate", "Message Recieved :" + message);
	}

	public void onClick(View view) {
	finish();
	}

	public void onResume() {
		super.onResume();
		String message = getIntent().getStringExtra(
				App42GCMService.EXTRA_MESSAGE);
		Log.d("MessageActivity-onResume", "Message Recieved :" + message);
		IntentFilter filter = new IntentFilter(
				App42GCMService.DISPLAY_MESSAGE_ACTION);
		filter.setPriority(2);
		registerReceiver(mBroadcastReceiver, filter);
	}

	@Override
	public void onPause() {
		unregisterReceiver(mBroadcastReceiver);
		super.onPause();
	}

	final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			// Right here do what you want in your activity
			String message = intent
					.getStringExtra(App42GCMService.EXTRA_MESSAGE);
			Log.i("MessageActivity-BroadcastReceiver", "Message Recieved "
					+ " : " + message);
			((TextView) findViewById(R.id.text)).setText(message);
			App42GCMService.resetMsgCount();

		}
	};

}
