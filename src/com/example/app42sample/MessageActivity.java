/**
 * 
 */
package com.example.app42sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.shephertz.app42.paas.sdk.android.App42API;
import com.shephertz.app42.paas.sdk.android.App42Activity;
import com.shephertz.app42.paas.sdk.android.App42CallBack;

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
	}
	
	  public void onClick(View view){
		   super.onClick(view);
		   
		   Intent intent=new Intent(this,MainActivity.class);
		   startActivity(intent);
		  
	   }

	

}
