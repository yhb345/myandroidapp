package com.test.login;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {
    private EditText loginUsername;
    private EditText loginPassword;
    private Button loginButton;
    private Button createButton;
    
    private ProgressDialog loginProgress;
    
    private NetworkManager netMgr;
    
    public static final int MSG_LOGIN_RESULT = 0;
    public static final int MSG_NETWORK_UNAVAILABLE = 1;
    public static final int MSG_SERVER_UNAVAILABLE = 2;
    
    public String serverUrl = "http://10.0.2.2:8080/androidWeb/servlet/loadMessage";
    
    private Handler mHandler = new Handler() {
    	public void handleMessage(Message msg) {
    		switch(msg.what) {
    		case MSG_LOGIN_RESULT:
    			loginProgress.dismiss();
    			JSONObject json = (JSONObject) msg.obj;
    			handleLoginResult(json);
    			break;
    		case MSG_NETWORK_UNAVAILABLE:
    			loginProgress.dismiss();
    			Toast.makeText(LoginActivity.this, "网络未连接，请检查网络！", Toast.LENGTH_LONG).show();
    			break;
    		case MSG_SERVER_UNAVAILABLE:
    			loginProgress.dismiss();
    			Toast.makeText(LoginActivity.this, "无法连接到服务器！", Toast.LENGTH_LONG).show();
    			break;
    		}
    	};
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        initViews();
        netMgr = new NetworkManager(this);
        netMgr.registerNetworkWatcher();
    }
	private void initViews() {
		loginUsername = (EditText)findViewById(R.id.login_username);
		loginPassword = (EditText)findViewById(R.id.login_password);
		loginButton   = (Button)findViewById(R.id.login);
		createButton  = (Button)findViewById(R.id.create_count);
		
		loginButton.setOnClickListener(this);
		createButton.setOnClickListener(this);
	}
	
	@Override
	protected void onDestroy() {
		netMgr.unRegisterNetworkWatcher();
		super.onDestroy();
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.login:
			handleLogin();
			break;
		case R.id.create_count:
			handleCreateCount();
			break;
		default:
			break;	
		}
		
	}
	private void handleLogin() {
		String username = loginUsername.getText().toString();
		String password = loginPassword.getText().toString();
		if(TextUtils.isEmpty(username) || (TextUtils.isEmpty(password))) {
			Toast.makeText(this, "用户名和密码不能为空！", Toast.LENGTH_LONG).show();
			return;
		}
		login(username, password);
	}

	private void login(final String username, final String password) {
		if(!netMgr.isNetworkAvailable()) {
			Toast.makeText(this, "请检查网络连接！", Toast.LENGTH_LONG).show();
			return;
		}
		loginProgress = new ProgressDialog(this);
		loginProgress.setCancelable(false);
		loginProgress.setCanceledOnTouchOutside(false);
		loginProgress = loginProgress.show(this, null, "登陆中...");
		new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d("yanghongbing", "start network!");
				HttpClient client = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(serverUrl);
				List<NameValuePair> params = new ArrayList<NameValuePair>(); 
				params.add(new BasicNameValuePair("username", username));
				params.add(new BasicNameValuePair("password", password));
				
				HttpResponse httpResponse = null;
				try {
					httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
					httpResponse = client.execute(httpPost);
					if(httpResponse.getStatusLine().getStatusCode() == 200) {
						Log.d("yanghongbing", "network OK!");
						HttpEntity entity = httpResponse.getEntity();
						String entityString = EntityUtils.toString(entity);
						String jsonString = entityString.substring(entityString.indexOf("{"));
						Log.d("yanghongbing", "entity = " + jsonString);
						JSONObject json = new JSONObject(jsonString);
						sendMessage(MSG_LOGIN_RESULT, json);
						Log.d("yanghongbing", "json = " + json);
					} 
				} catch (UnsupportedEncodingException e) {
					Log.d("yanghongbing", "UnsupportedEncodingException");
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					Log.d("yanghongbing", "ClientProtocolException");
					e.printStackTrace();
				} catch (IOException e) {
					Log.d("yanghongbing", "IOException");
					sendMessage(MSG_SERVER_UNAVAILABLE, null);
					e.printStackTrace();
				} catch (JSONException e) {
					Log.d("yanghongbing", "IOException");
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
		
	}
	private void handleCreateCount() {
		Intent intent = new Intent(this, CreateUserActivity.class);
		startActivity(intent);
		finish();
	}
	
	private void handleLoginResult(JSONObject json){
		/*
		 * login_result:
		 * -1：登陆失败，未知错误！
		 * 0: 登陆成功！
		 * 1：登陆失败，用户名或密码错误！
		 * 2：登陆失败，用户名不存在！
		 * */
		int resultCode = -1;
		try {
			resultCode = json.getInt("result_code");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		switch(resultCode) {
		case 0:
			onLoginSuccess(json);
			break;
		case 1:
			Toast.makeText(this, "用户名或密码错误！", Toast.LENGTH_LONG).show();
			break;
		case 2:
			Toast.makeText(this, "用户名不存在！", Toast.LENGTH_LONG).show();
			break;
		case -1:
		default:
			Toast.makeText(this, "登陆失败！未知错误！", Toast.LENGTH_LONG).show();
			break;
		}
	}
	
	private void onLoginSuccess(JSONObject json) {
		Intent intent = new Intent(this, UserInfoActivity.class);
		
		try {
			intent.putExtra("username", json.getString("username"));
			intent.putExtra("gender", json.getString("gender"));
			intent.putExtra("age", json.getInt("age"));
			intent.putExtra("phone", json.getString("phone"));
			intent.putExtra("email", json.getString("email"));
			intent.putExtra("portrait", json.getString("portrait"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		startActivity(intent);
		finish();
	}
	private void sendMessage(int what, Object obj) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}
}