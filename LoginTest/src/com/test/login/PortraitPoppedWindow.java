package com.test.login;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class PortraitPoppedWindow extends Builder {
	Activity activity;
    private String result;
    
    private GridView portraitsView;
    private View view;
    
    private ArrayList<HashMap<String, Object>> iconList;
    
    private Dialog dialog;
    
	public PortraitPoppedWindow(Activity activity) {
		super(activity);
		this.activity = activity;	
		view = LayoutInflater.from(activity).inflate(R.layout.portrait_gridview, null);
		portraitsView = (GridView) view.findViewById(R.id.gridView1);
		iconList = new ArrayList<HashMap<String, Object>>();
		getIconList();
		portraitsView.setAdapter(new DataAdapter());
		
		setView(view);
		dialog = this.show();
		setAttributes();	
	}
	
	private void setAttributes() {
		WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();  
        layoutParams.width =  LayoutParams.FILL_PARENT;  
        layoutParams.height = 500;  
        dialog.getWindow().setAttributes(layoutParams);  
	}
	
	public void setOnItemClickListener(final ImageView iv) {
		portraitsView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position,
					long id) {
				HashMap<String, Object> map = iconList.get(position);
				Bitmap bitmap = (Bitmap) map.get("bitmap");
				String fileName = (String) map.get("fileName");
				iv.setImageBitmap(bitmap);
				iv.setContentDescription(fileName);
				dialog.dismiss();
			}
		});
	}
	
	private void getIconList() {
				AssetManager am = activity.getAssets();
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
					iconList.add(map);
					Log.d("yanghongbing", "bitmap = " + bitmap);
				}
			
		
	}

	public String getResult() {
		return result;
	}
    	
	private class DataAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return iconList.size();
		}

		@Override
		public Object getItem(int position) {
			HashMap<String, Object> map = iconList.get(position);
			Bitmap bitmap = (Bitmap) map.get("bitmap");
			return bitmap;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(activity).inflate(R.layout.gridview_item, null);
			ImageView iv = (ImageView) convertView.findViewById(R.id.imageView1);
			iv.setImageBitmap((Bitmap) getItem(position));
			return convertView;
		}
		
	}

	
}
