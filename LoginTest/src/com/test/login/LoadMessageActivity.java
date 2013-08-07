package com.test.login;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LoadMessageActivity extends Activity 
                implements OnTouchListener, OnScrollListener {
	
	private ListView mListView;
	private LinearLayout mTopFresh;
	private View mBottomFresh;
	
	private TextView mFootView;
	private TextView mHeadView;
	
	private NetworkManager netMgr;
	private ArrayList<HashMap<String, Object>> dataList;
	private DataAdapter adapter;
	
	public static final String MESSAGE_READ_URL = "http://10.0.2.2:8080/androidWeb/servlet/MessageRead";
	
	public static final String ACTION_FIRST_REFRESH = "first_refresh";
	public static final String ACTION_REFRSSH = "refresh";
	
    public static final int MSG_READ_RESULT = 1;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MSG_READ_RESULT:
				mTopFresh.setVisibility(View.GONE);
				JSONArray json = (JSONArray) msg.obj;
				handleLoadedData(json);
				break;
			}
		};
	};
	
	private void handleLoadedData(JSONArray json) {
		if(json.length() == 0) {
			mListView.removeFooterView(mFootView);
			return;
		}
		mFootView.setText("点击加载更多");
		for(int i = 0; i < json.length(); i++) {
			JSONObject jsonObj = null;
			HashMap<String, Object> map = null;
			try {
				jsonObj = json.getJSONObject(i);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(jsonObj != null) {
				map = new HashMap<String, Object>();
				try {
					map.put("username", jsonObj.getString("username"));
					map.put("time", jsonObj.getString("time"));
					map.put("title", jsonObj.getString("title"));
					map.put("content", jsonObj.getString("content"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dataList.add(map);
				adapter.notifyDataSetChanged();
			}
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.load_message_activity);
		initViews();
		netMgr = new NetworkManager(this);
		netMgr.registerNetworkWatcher();
		dataList = new ArrayList<HashMap<String, Object>>();
		adapter = new DataAdapter(this);
		mFootView = new TextView(this);
		mFootView.setText("点击加载更多");
		mFootView.setGravity(Gravity.CENTER);
		mListView.addFooterView(mFootView);
		/*
		mHeadView = new TextView(this);
		mHeadView.setText("下拉刷新");
		mHeadView.setHeight(30);
		mHeadView.setGravity(Gravity.CENTER);
		mListView.addHeaderView(mHeadView);
	    */
	    mListView.setAdapter(adapter);
	    mListView.setSelection(1);
	    mListView.setOnTouchListener(this);
	    mListView.setOnScrollListener(this);
	    
		loadMessages();
		
		mFootView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {	
				doFresh();
			}});
	}
	
	@Override
	protected void onDestroy() {
		netMgr.unRegisterNetworkWatcher();
		super.onDestroy();
	}
	

	private void loadMessages() {
		if(!netMgr.isNetworkAvailable()) {
			Toast.makeText(this, "请检查网络连接", Toast.LENGTH_LONG).show();
			return;
		}
		mTopFresh.setVisibility(View.VISIBLE);
		new Thread(new Runnable(){
			@Override
			public void run() {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("action", ACTION_FIRST_REFRESH));
				JSONArray json = getJsonFromNetwork(params);
				sendMessage(MSG_READ_RESULT, json);
			}
		}).start();
	}
	
	private JSONArray getJsonFromNetwork(List<NameValuePair> params) {
		Log.d("yanghongbing", "network start!");
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(MESSAGE_READ_URL);
		JSONArray json = null;
		try {
			request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			String entityStr = EntityUtils.toString(entity);
			Log.d("yanghongbing", "entityStr = " + entityStr);
			String jsonStr = entityStr.substring(entityStr.indexOf("["));
			json = new JSONArray(jsonStr);
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
		return json;
		
	}

	private void initViews() {
		mListView = (ListView)findViewById(R.id.listView);
		mTopFresh = (LinearLayout)findViewById(R.id.load_top_refresh);
		mBottomFresh = LayoutInflater.from(this).inflate(R.layout.listview_footview, null);
		mBottomFresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {	
				doFresh();
			}});
	}
	
	protected void doFresh() {
		final int start = dataList.size();
		mFootView.setText("加载中...");
		new Thread(new Runnable() {
			@Override
			public void run() {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("action", ACTION_REFRSSH));
				params.add(new BasicNameValuePair("start", String.valueOf(start)));
				JSONArray json = getJsonFromNetwork(params);
				sendMessage(MSG_READ_RESULT, json);
			}
		}).start();
	}

	private class DataAdapter extends BaseAdapter {
		ViewHolder holder;
		Context context;
		
		class ViewHolder {
			TextView username;
			TextView time;
			TextView title;
			TextView content;
			TextView count;
		}
		
		public DataAdapter(Context context) {
			this.context = context;
			holder = new ViewHolder();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return dataList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return dataList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup viewGroup) {
			//if(convertView == null) {
				convertView = LayoutInflater.from(context).inflate(R.layout.message_item, null);
				initHolder(convertView);
				convertView.setTag(holder);
				Log.d("yanghongbing", "holder 1 = " + holder);
			//} else {
				//holder = (ViewHolder)convertView.getTag();
				//Log.d("yanghongbing", "holder 2 = " + holder);
			//}
			Log.d("yanghongbing", "position = " + position);
			HashMap<String, Object> map = ((HashMap<String, Object>)getItem(position));
			Log.d("yanghongbing", "map = " + map);
			holder.username.setText((String)map.get("username"));
			holder.time.setText((String)map.get("time"));
			holder.title.setText((String)map.get("title"));
			holder.content.setText((String)map.get("content"));
			holder.count.setText(String.valueOf(position+1));
			return convertView;
		}
		
		private void initHolder(View view) {
			holder.username = (TextView)view.findViewById(R.id.item_username);
			holder.time = (TextView)view.findViewById(R.id.item_time);
			holder.title = (TextView)view.findViewById(R.id.item_title);
			holder.content = (TextView)view.findViewById(R.id.item_content);
			holder.count = (TextView)view.findViewById(R.id.item_count);
		}
		
	}
	
	private void sendMessage(int what, Object obj) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.d("yanghongbing", "event = " + event.getAction());
		int posistion = mListView.getLastVisiblePosition();
		Log.d("yanghongbing", "getLastVisiblePosition = " + posistion);
		return false;
	}
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		Log.d("yanghongbing", "firstVisibleItem = " + firstVisibleItem + ", visibleItemCount = " + visibleItemCount + ", totalItemCount = " + totalItemCount);
	    //Log.d("yanghongbing", );
		if(mListView.getLastVisiblePosition() == totalItemCount) {
	    	mBottomFresh.setVisibility(View.VISIBLE);
	    } else {
	    	mBottomFresh.setVisibility(View.GONE);
	    }
	}
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		Log.d("yanghongbing", "scrollState = " + scrollState);
		
	}
}
