package com.test.login;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class CreateUserActivity extends Activity implements OnClickListener {
	
	public static final String CREATE_ACCOUNT_URL = "http://10.0.2.2:8080/androidWeb/servlet/NewAccount";
	public static final int MSG_CREATE_RESULT = 1;
	public static final int MSG_DISMISS_DIALOG = 2;
    public static final String PORTRAIT_DEFAULT = "NULL";
	
	private EditText eUsername;
	private EditText ePwd1;
	private EditText ePwd2;
	private RadioGroup rGender;
	private EditText eAge;
	private EditText ePhone;
	private EditText eEmail;
	private ImageView iPortrait;
	
	private Button btnSubmit;
	private Button btnReset;
	
	ProgressDialog progress;
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MSG_CREATE_RESULT:
				JSONObject json = (JSONObject) msg.obj;
				hanleCreateAccountResult(json);
				break;
			case MSG_DISMISS_DIALOG:
				progress.dismiss();
				break;
			}
		}	
	};
	
	private void hanleCreateAccountResult(JSONObject json) {
		/*
		 *   result_code: 
		 * 0  ע��ɹ�
		 * 1  �û����Ѵ���
		 * 2 ���ݿ�����쳣
		 * */
		int result;
		try {
			result = json.getInt("result_code");
		} catch (JSONException e) {
			Toast.makeText(this, "û�л�ȡ���������Ӧ��", Toast.LENGTH_LONG).show();
			e.printStackTrace();
			return;
		}
		
		if(result == 1) {
			Toast.makeText(this, "�û����Ѵ��ڣ�", Toast.LENGTH_LONG).show();
			return;
		}
		
		if(result == 2) {
			Toast.makeText(this, "ע��ʧ�ܣ�����˳����쳣��", Toast.LENGTH_LONG).show();
			return;
		}
		
		if(result == 0) {
			Toast.makeText(this, "ע��ɹ���ǰ����½���棡", Toast.LENGTH_LONG).show();
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_user_activity);
		
		initViews();
		AssetManager am = getResources().getAssets();
		try {
			String[] files = am.list("portraits");
			for(int i = 0; i < files.length; i++) {
				Log.d("yanghongbing", "file[" + i + "] = " + files[i]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void initViews() {
		eUsername = (EditText)findViewById(R.id.new_username);
		ePwd1 = (EditText)findViewById(R.id.new_password_1);
		ePwd2 = (EditText)findViewById(R.id.new_password_2);
		rGender = (RadioGroup)findViewById(R.id.new_radio_group_gender);
		eAge = (EditText)findViewById(R.id.new_age);
		ePhone = (EditText)findViewById(R.id.new_phone);
		eEmail = (EditText)findViewById(R.id.new_email);
		iPortrait = (ImageView)findViewById(R.id.new_portrait);
		iPortrait.setContentDescription("NULL");
		iPortrait.setOnClickListener(this);
		btnSubmit = (Button)findViewById(R.id.new_btn_submit);
		btnReset = (Button)findViewById(R.id.new_btn_reset);
		btnSubmit.setOnClickListener(this);
		btnReset.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.new_btn_submit:
			handleCreateAccount();
			break;
		case R.id.new_btn_reset:
			handleReset();
			break;
		case R.id.new_portrait:
			createSelectPortraitWindow();
			break;
		}
		
	}

	private void createSelectPortraitWindow() {
		PortraitPoppedWindow portraitSelectWindow = new PortraitPoppedWindow(this);
		portraitSelectWindow.setOnItemClickListener(iPortrait);
	}

	private void handleCreateAccount() {
		boolean isUsernameValid = checkUsername();
		if(!isUsernameValid) {
			Toast.makeText(this, "�û�������ȷ������������", Toast.LENGTH_LONG).show();
			return;
		}
		
		int pwdResult = checkPassword();
		if(pwdResult == 1) {
			Toast.makeText(this, "������������벻һ�£���ȷ�ϣ�", Toast.LENGTH_LONG).show();
			return;
		} 
		if (pwdResult == 2) {
			Toast.makeText(this, "���벻��Ϊ�գ�", Toast.LENGTH_LONG).show();
			return;
		}
		
		int isAgeValid = checkAge();
		if(isAgeValid == -1) {
			Toast.makeText(this, "���䲻��Ϊ�գ�", Toast.LENGTH_LONG).show();
			return;
		}
		if(isAgeValid == -2) {
			Toast.makeText(this, "���䳬����Χ(1~100)��", Toast.LENGTH_LONG).show();
			return;
		}
		if(isAgeValid == -3) {
			Toast.makeText(this, "�����ʽ��������벻Ҫ������ĸ�����ŵ������ַ�����", Toast.LENGTH_LONG).show();
			return;
		}
		
		if(TextUtils.isEmpty(ePhone.getText().toString())) {
			Toast.makeText(this, "������绰���룡", Toast.LENGTH_LONG).show();
			return;
		}
		
		if(TextUtils.isEmpty(eEmail.getText().toString())) {
			Toast.makeText(this, "���������䣡", Toast.LENGTH_LONG).show();
			return;
		}
		
		createAccount();
	}
	
	/*

	private void createAccount() {
		progress = new ProgressDialog(this);
		progress.setCancelable(false);
		progress.setCanceledOnTouchOutside(false);
		progress.show(this, null, "ע����...");
		new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d("yanghongbing", "Start Network!");
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(CREATE_ACCOUNT_URL);
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("username", eUsername.getText().toString()));
				params.add(new BasicNameValuePair("password", ePwd1.getText().toString()));
				RadioButton selectedGender = (RadioButton)CreateUserActivity.this.findViewById(rGender.getCheckedRadioButtonId());
				params.add(new BasicNameValuePair("gender", 
						selectedGender.getText().toString()));
				params.add(new BasicNameValuePair("age", eAge.getText().toString()));
				params.add(new BasicNameValuePair("phone", ePhone.getText().toString()));
				params.add(new BasicNameValuePair("email", eEmail.getText().toString()));
				params.add(new BasicNameValuePair("portrait", iPortrait.getContentDescription().toString()));
			
				MultipartEntity mEntity = new MultipartEntity();
				
				try {			
					httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
					HttpResponse httpResponse = httpClient.execute(httpPost);
					
					if(httpResponse.getStatusLine().getStatusCode() == 200) {
						Log.d("yanghongbing", "Network OK!");
						HttpEntity entity = httpResponse.getEntity();
						String entityStr = EntityUtils.toString(entity);
						String jsonStr = entityStr.substring(entityStr.indexOf("{"));
						JSONObject json = new JSONObject(jsonStr);
						sendMessage(MSG_CREATE_RESULT, json);
						
					}
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
		
	}
	
	*/
	
	private void createAccount() {
		progress = new ProgressDialog(this);
		progress.setCancelable(false);
		progress.setCanceledOnTouchOutside(false);
		progress = progress.show(this, null, "ע����...");
		new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d("yanghongbing", "Start Network!");
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(CREATE_ACCOUNT_URL);
				MultipartEntity mEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE,
						null, Charset.forName(HTTP.UTF_8));
				
				String params = getParams().toString();
				try {
					StringBody sb = new StringBody(params, Charset.forName(HTTP.UTF_8));
					mEntity.addPart("params", sb);
					File portraitFile = BitmapToFile(iPortrait);
					if(portraitFile != null) {
						String filePath = portraitFile.getPath();
						String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
					    FileBody fileBody = new FileBody(portraitFile);
					    mEntity.addPart(fileName, fileBody);
					}
					httpPost.setEntity(mEntity);
					HttpResponse response = httpClient.execute(httpPost);
					HttpEntity entity = response.getEntity();
					String entityStr = EntityUtils.toString(entity);
					String jsonStr = entityStr.substring(entityStr.indexOf("{"));
					Log.d("yanghongbing", "jsonStr = " + jsonStr);
					JSONObject json = new JSONObject(jsonStr);
					sendMessage(MSG_CREATE_RESULT, json);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				} finally {
					sendMessage(MSG_DISMISS_DIALOG, null);
				}
			}
		}).start();
	}
	
	private boolean checkUsername() {
		String username = eUsername.getText().toString().trim();
		if(TextUtils.isEmpty(username)) {
			return false;
		}
		return true;
	}

	private int checkPassword() {
		/*
		 * return value:
		 * 0 password valid
		 * 1 password not equal 2 inputs
		 * 2 password empty
		 * */
		String pwd1 = ePwd1.getText().toString().trim();
		String pwd2 = ePwd2.getText().toString().trim();
		if(!pwd1.equals(pwd2)) {
			return 1;
		} else if(TextUtils.isEmpty(pwd1)) {
			return 2;
		} else {
			return 0;
		}
	}
	
	private int checkAge() {
		/*
		 * return value
		 * 0 ����Ϸ�
		 * -1 ����Ϊ��
		 * -2����Ϊ����
		 * -3����Ϊ����ֵ�ַ��������С��
		 * */
		int ageNum;
		String age = eAge.getText().toString().trim();
		if(TextUtils.isEmpty(age)) {
			return -1;
		}
		try {
			ageNum = Integer.parseInt(age);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return -3;
		}
		if(ageNum <= 0 || ageNum > 100) {
			return -2;
		}
		return 0;
	}

	private void handleReset() {
		eUsername.setText("");
		ePwd1.setText("");
		ePwd2.setText("");
		((RadioButton)(rGender.getChildAt(0))).setChecked(true);
		eAge.setText("");
		ePhone.setText("");
		eEmail.setText("");
		iPortrait.setImageResource(R.drawable.default_portrait);
	}
	
	private void sendMessage(int what, Object obj) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}
	
	private JSONObject getParams() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("username", eUsername.getText().toString().trim());
		map.put("password", ePwd1.getText().toString().trim());
		RadioButton selectedGender = (RadioButton)CreateUserActivity.this.findViewById(rGender.getCheckedRadioButtonId());
		map.put("gender", selectedGender.getText().toString());
		map.put("age", eAge.getText().toString().trim());
		map.put("phone", ePhone.getText().toString().trim());
		map.put("email", eEmail.getText().toString().trim());
		return (new JSONObject(map));
	}
	
	private File BitmapToFile(ImageView iv) {
		iv.setDrawingCacheEnabled(true);
		Bitmap bm = iv.getDrawingCache();
		String fileName = iv.getContentDescription().toString();
		if(PORTRAIT_DEFAULT.equals(fileName)) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		String rootPath = Environment.getExternalStorageDirectory().getPath();
        File fileDir = new File(rootPath + "/temp");
        if(!fileDir.exists()) {
        	fileDir.mkdir();
        }
		String filePath = fileDir.getPath() + "/" + fileName;
		Log.d("yanghongbing", "filePath = " + filePath);
		File file = new File(filePath);
		if(file.exists()) {
			file.delete();
			try {
				file.createNewFile();	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			FileOutputStream fos = new FileOutputStream(file);
			byte[] bytes = new byte[1024];
            while(is.read(bytes) != -1) {
				fos.write(bytes);
			}
            fos.flush();
            fos.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}
	
	

}
