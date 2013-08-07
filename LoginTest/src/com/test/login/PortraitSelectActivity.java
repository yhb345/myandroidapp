package com.test.login;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class PortraitSelectActivity extends Activity implements OnItemClickListener {

	private GridView portraitsView;
	private ArrayList<HashMap<String, Object>> portraitList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.portrait_gridview);
		
		portraitsView = (GridView)findViewById(R.id.gridView1);
		portraitsView.setOnItemClickListener(this);
		getPortraitFromResource();
		portraitsView.setAdapter(new DataAdapter());
	}
	
	private void getPortraitFromResource() {
		portraitList = new ArrayList<HashMap<String, Object>>();
		AssetManager am = getAssets();
		String[] fileNames = null;
		try {
			fileNames = am.list("portraits");
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(int i = 0; i < fileNames.length; i++) {
			InputStream is = null;
			try {
				is = am.open("portraits" + "/" + fileNames[i]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Bitmap bitmap = BitmapFactory.decodeStream(is);
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("fileName", fileNames[i]);
			map.put("bitmap", bitmap);
			portraitList.add(map);
			Log.d("yanghongbing", "bitmap = " + bitmap);
		}
		
	}

	private class DataAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return portraitList.size();
		}

		@Override
		public Object getItem(int position) {
			HashMap<String, Object> map = portraitList.get(position);
			Bitmap bitmap = (Bitmap) map.get("bitmap");
			return bitmap;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(PortraitSelectActivity.this).inflate(R.layout.gridview_item, null);
			ImageView iv = (ImageView) convertView.findViewById(R.id.imageView1);
			iv.setImageBitmap((Bitmap) getItem(position));
			return convertView;
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		
		
	}
}
