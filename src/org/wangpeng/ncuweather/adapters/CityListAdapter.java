package org.wangpeng.ncuweather.adapters;

import java.util.ArrayList;

import org.wangpeng.ncuweather.R;
import org.wangpeng.ncuweather.classes.CityInfo;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * 自定义的Adapter，适配城市搜索结果的ListView
 * @author wangpeng
 *
 */
public class CityListAdapter extends BaseAdapter {
	private ArrayList<CityInfo> data;
	private Context context;

	public CityListAdapter(Context context, ArrayList<CityInfo> data) {
		this.data = data;
		this.context = context;
	}

	public int getCount() {
		return data.size();
	}

	public Object getItem(int position) {
		return data.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater flater = ((Activity) context).getLayoutInflater();
		View mview = flater.inflate(R.layout.cityitem, null);
		TextView tv = (TextView) mview.findViewById(R.id.DialogLvItemTextv);
		tv.setText(data.get(position).name);
		return mview;
	}
}
