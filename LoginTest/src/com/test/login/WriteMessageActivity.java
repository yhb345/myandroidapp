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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class WriteMessageActivity extends Activity implements OnClickListener {

	private EditText eTitle;
	private EditText eContent;
	private Button btnSubmit;
	private Button btnClear;
	
	private ProgressDialog submitProgress;
	
	private NetworkManager netMgr;
	
	public static final String MESSAGE_URL = "http://10.0.2.2:8080/androidWeb/servlet/MessageCompose";
	public static final int MSG_SUBMIT_RESULT = 0;
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MSG_SUBMIT_RESULT:
				submitProgress.dismiss();
				JSONObject json = (JSONObject) msg.obj;
				handleSubmitResult(json);
				break;
			}
		}
	};
	
	private void handleSubmitResult(JSONObject json) {
		int result_code = 0;
		try {
			result_code = json.getInt("result_code");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(result_code == 0) {
			Toast.makeText(this, "发表留言失败！", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, "发表留言成功！", Toast.LENGTH_LONG).show();
			finish();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.write_message_activity);
		initViews();
		netMgr = new NetworkManager(this);
		netMgr.registerNetworkWatcher();
	}
	
	private void initViews() {
		eTitle     = (EditText) findViewById(R.id.write_msg_title);
		eContent   = (EditText) findViewById(R.id.write_msg_content);
		btnSubmit  = (Button) findViewById(R.id.write_msg_submit);
		btnClear   = (Button) findViewById(R.id.write_msg_clear);
		btnSubmit.setOnClickListener(this);
		btnClear.setOnClickListener(this);
	}
	
	@Override
	protected void onDestroy() {
		netMgr.unRegisterNetworkWatcher();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.write_msg_submit:
			handleSubmit();
		    break;
		case R.id.write_msg_clear:
			handleClear();
			break;
		}
	}

	private void handleSubmit() {
		if(TextUtils.isEmpty(eTitle.getText().toString())) {
			Toast.makeText(this, "标题不能为空！", Toast.LENGTH_LONG).show();
			return;
		}
		submit();
	}

	private void submit() {
		if(!netMgr.isNetworkAvailable()) {
			Toast.makeText(this, "请检查网络连接!", Toast.LENGTH_LONG).show();
			return;
		}
		submitProgress = new ProgressDialog(this);
		submitProgress = submitProgress.show(this, null, "正在提交...");
		new Thread(new Runnable() {
			@Override
			public void run() {
				String username = WriteMessageActivity.this.getIntent().getStringExtra("username");
				String title = eTitle.getText().toString();
				String content = eContent.getText().toString();
				
				HttpClient client = new DefaultHttpClient();
				HttpPost request = new HttpPost(MESSAGE_URL);
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("username", username));
				params.add(new BasicNameValuePair("title", title));
				params.add(new BasicNameValuePair("content", content));
				try {
					request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
					HttpResponse response = client.execute(request);
					if(response.getStatusLine().getStatusCode() == 200) {
						HttpEntity entity = response.getEntity();
						String entityStr = EntityUtils.toString(entity);
						String jsonStr = entityStr.substring(entityStr.indexOf("{"));
						JSONObject json = new JSONObject(jsonStr);
						sendMessage(MSG_SUBMIT_RESULT, json);
					}
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void handleClear() {
		// TODO Auto-generated method stub
		
	}

	private void sendMessage(int what, Object obj) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}
}
