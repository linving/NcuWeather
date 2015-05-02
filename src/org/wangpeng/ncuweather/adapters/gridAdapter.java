package org.wangpeng.ncuweather.adapters;

import java.util.ArrayList;

import org.wangpeng.ncuweather.MainActivity;
import org.wangpeng.ncuweather.R;
import org.wangpeng.ncuweather.classes.WeatherHourInfo;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 自定义的Adapter，适配未来几小时天气的GridView
 * 
 * @author wangpeng
 * 
 */
public class gridAdapter extends BaseAdapter {

	private ArrayList<WeatherHourInfo> listHourData;
	private Context context;
	public gridAdapter(Context context, ArrayList<WeatherHourInfo> listHourData){
		this.listHourData = listHourData;
		this.context = context;
	}
	public int getCount() {
		return listHourData.size();
	}

	public Object getItem(int position) {
		return listHourData.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		View v = inflater.inflate(R.layout.griditem, null);
		TextView textv = (TextView) v.findViewById(R.id.weatheritemTextv);
		textv.setText(listHourData.get(position).jf.substring(8, 10) + "时");

		ImageView imagev = (ImageView) v
				.findViewById(R.id.weatheritemImagev);

		int hour = Integer.parseInt(listHourData.get(position).jf
				.substring(8, 10));

		int weatherId = Integer.parseInt(listHourData.get(position).ja);

		// System.out.println(hour);
		// System.out.println(weatherId);
		// 如果是霾，图片编号为32
		if (weatherId == 53) {
			weatherId = 32;
		} // 如果是夜晚，换为夜晚的图标
		if (hour > 19 || hour < 6) {
			weatherId += 33;
		}
		imagev.setImageResource(MainActivity.resIds[weatherId]);

		TextView textv2 = (TextView) v.findViewById(R.id.weatheritemTextv2);
		textv2.setText(listHourData.get(position).jb + "℃");

		return v;
	}

}
