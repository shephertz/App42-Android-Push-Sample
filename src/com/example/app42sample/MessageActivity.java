/**
 * 
 */
package com.example.app42sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.shephertz.app42.paas.sdk.android.App42Activity;

/**
 * @author Ajay Tiwari
 *
 */
public class MessageActivity extends App42Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		((TextView) findViewById(R.id.page_header)).setText("Message Activty");
		
		String message = getIntent().getStringExtra(GCMIntentService.EXTRA_MESSAGE);    		
		// Create the text view
		((TextView) findViewById(R.id.text)).setText(message);
		
		Log.d("MessageActivity-onCreate", "Message Recieved :"+message);
	}
	
	  public void onClick(View view){
		   super.onClick(view);
		   
		   Intent intent=new Intent(this,MainActivity.class);
		   startActivity(intent);
		  
	   }
	  
	  
	  public void onResume()
	  {
		 super.onResume();
		 String message = getIntent().getStringExtra(GCMIntentService.EXTRA_MESSAGE); 
	     Log.d("MessageActivity-onResume", "Message Recieved :"+message);
	     IntentFilter filter = new IntentFilter(GCMIntentService.DISPLAY_MESSAGE_ACTION);
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
	        	
	            //Right here do what you want in your activity
	        	String message = intent.getStringExtra(GCMIntentService.EXTRA_MESSAGE);
	        	Log.i("MessageActivity-BroadcastReceiver", "Message Recieved " +" : " +message);
	        	((TextView) findViewById(R.id.text)).setText(message);
	        	
	        }
	    };

	

}
