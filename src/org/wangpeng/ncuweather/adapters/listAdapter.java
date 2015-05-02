package org.wangpeng.ncuweather.adapters;

import java.util.ArrayList;

import org.wangpeng.ncuweather.MainActivity;
import org.wangpeng.ncuweather.R;
import org.wangpeng.ncuweather.classes.WeatherDayInfo;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 自定义的Adapter，适配未来几天天气的ListView
 * 
 * @author wangpeng
 * 
 */
public class listAdapter extends BaseAdapter {
	private ArrayList<WeatherDayInfo> listDayData;
	private Context context;
	public listAdapter(Context context, ArrayList<WeatherDayInfo> listDayData){
		this.listDayData = listDayData;
		this.context = context;
	}

	public int getCount() {
		return listDayData.size();
	}

	public Object getItem(int position) {
		return listDayData.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		View v = inflater.inflate(R.layout.weatherdays, null);

		TextView textv = (TextView) v.findViewById(R.id.weatherdayTextv);
		textv.setText(MainActivity.daysName.get(position));
		TextView textv2 = (TextView) v.findViewById(R.id.weatherdayTextv2);
		textv2.setText(listDayData.get(position).tep);

		// 设置天气图片1
		ImageView imagev = (ImageView) v
				.findViewById(R.id.weatherdayImagev);
		int weatherId = Integer.parseInt(listDayData.get(position).img1);
		// 如果是霾，图片编号为32
		if (weatherId == 53) {
			weatherId = 32;
		}
		imagev.setImageResource(MainActivity.resIds[weatherId]);
		// 设置天气图片2
		ImageView imagev2 = (ImageView) v
				.findViewById(R.id.weatherdayImagev2);
		int weatherId2 = Integer.parseInt(listDayData.get(position).img2);
		if (weatherId2 == 53) {
			weatherId2 = 32;
		}
		imagev2.setImageResource(MainActivity.resIds[weatherId2]);

		if (weatherId == weatherId2) {
			imagev2.setVisibility(View.GONE);
		}

		return v;
	}

}
