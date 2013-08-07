package com.test.login;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class UserInfoActivity extends Activity implements OnClickListener {
	private TextView tvUsername;
	private TextView tvGender;
	private TextView tvAge;
	private TextView tvPhone;
	private TextView tvEmail;
	private ImageView ivPortrait;
	
	private Button btnWriteMsg;
	private Button btnViewMsg;
	
	private final String SITE_ADDRESS = "http://10.0.2.2:8080/androidWeb";
	
	public static final int MSG_DISPLAY_IMAGE = 1;
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MSG_DISPLAY_IMAGE:
				Bitmap bitmap = (Bitmap) msg.obj;
				displayPortrait(bitmap);
			}
		}

		private void displayPortrait(Bitmap bitmap) {
			ivPortrait.setImageBitmap(bitmap);			
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_info_activity);
		initViews();
		Intent intent = getIntent();
		displayUserInfo(intent);
	}

	private void initViews() {
		tvUsername = (TextView) findViewById(R.id.usr_info_username);
		tvGender   = (TextView) findViewById(R.id.usr_info_gender);
		tvAge      = (TextView) findViewById(R.id.usr_info_age);
		tvPhone    = (TextView) findViewById(R.id.usr_info_phone);
		tvEmail    = (TextView) findViewById(R.id.usr_info_email);
		ivPortrait = (ImageView)findViewById(R.id.usr_info_portrait);
		btnWriteMsg = (Button) findViewById(R.id.usr_info_write_message);
		btnViewMsg  = (Button) findViewById(R.id.usr_info_view_message);
		btnWriteMsg.setOnClickListener(this);
		btnViewMsg.setOnClickListener(this);
	}
	
	private void displayUserInfo(Intent intent) {
		String username = intent.getStringExtra("username");
		String gender   = intent.getStringExtra("gender");
		int age         = intent.getIntExtra("age", -1);
		String phone    = intent.getStringExtra("phone");
		String email    = intent.getStringExtra("email");
		String portrait = intent.getStringExtra("portrait");
		
		tvUsername.setText(username);
		tvGender.setText(gender);
		tvAge.setText(String.valueOf(age));
		tvPhone.setText(phone);
		tvEmail.setText(email);
		downloadAndDisplayPortrait(portrait);
	}

	private void downloadAndDisplayPortrait(String pAddress) {
		if(CreateUserActivity.PORTRAIT_DEFAULT.equals(pAddress)) {
			return;
		}
		final String portraitUrl = SITE_ADDRESS + pAddress;
		Log.d("yanghongbing", "portraitUrl = " + portraitUrl);
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost request = new HttpPost(portraitUrl);
				try {
					HttpResponse response = httpClient.execute(request);
					HttpEntity entity = response.getEntity();
					InputStream is = entity.getContent();
					Bitmap bitmap = BitmapFactory.decodeStream(is);
					sendMessage(MSG_DISPLAY_IMAGE, bitmap);
					
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.usr_info_write_message:
			openSubmitActivity();
			break;
		case R.id.usr_info_view_message:
			openMessageActivity();
			break;
		}
		
	}

	private void openMessageActivity() {
		Intent intent = new Intent(this, LoadMessageActivity.class);
		startActivity(intent);	
	}

	private void openSubmitActivity() {
		Intent intent = new Intent(this, WriteMessageActivity.class);
		intent.putExtra("username", tvUsername.getText().toString());
		startActivity(intent);
	}
	
	private void sendMessage(int what, Object obj) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}

}
