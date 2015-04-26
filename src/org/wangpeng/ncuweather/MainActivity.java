package org.wangpeng.ncuweather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private GridView gridView;
	private ArrayList<WeatherHourInfo> listHourData;
	private ArrayList<WeatherDayInfo> listDayData;
	private ListView listView;

	private int[] resIds; // 天气图标
	private ArrayList<String> daysName; // 未来几天的名称

	private TextView weatherCityTextv; // 上部今日天气的控件
	private ImageView weatherImagev;
	private TextView weatherTepTextv;
	private Button weatherCitySettingBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		gridView = (GridView) findViewById(R.id.gridView);
		listView = (ListView) findViewById(R.id.weatherdaysListv);
		weatherCityTextv = (TextView) findViewById(R.id.weatherCityTextv);
		weatherTepTextv = (TextView) findViewById(R.id.weatherTepTextv);
		weatherImagev = (ImageView) findViewById(R.id.weatherImageV);
		weatherCitySettingBtn = (Button) findViewById(R.id.weatherCitySettingBtn);

		daysName = new ArrayList<String>();
		SetResources();
		
		String str = getAssetsData("101240101.html");
		listHourData = Json2list_hour(str);
		String str2 = getAssetsData("101010100.html");
		listDayData = Json2list_day(str2);

		SetGridParams(listHourData.size(), 80);
		gridAdapter h_adapter = new gridAdapter();
		gridView.setAdapter(h_adapter);

		listAdapter d_adapter = new listAdapter();
		listView.setAdapter(d_adapter);

	}

	public void SetResources() {
		TypedArray ar = getResources().obtainTypedArray(R.array.weatherimages);
		int len = ar.length();
		resIds = new int[len];
		for (int i = 0; i < len; i++) {
			resIds[i] = ar.getResourceId(i, 0);
		}
		ar.recycle();
	}

	// 参数：总项数，每一项的宽度
	public void SetGridParams(int size, int length) {

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		float density = dm.density;
		int gridviewWidth = (int) (size * (length + 4) * density);
		int itemWidth = (int) (length * density);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				gridviewWidth, LinearLayout.LayoutParams.FILL_PARENT);
		gridView.setLayoutParams(params); // 重点
		gridView.setColumnWidth(itemWidth); // 重点
		gridView.setHorizontalSpacing(5); // 间距
		gridView.setNumColumns(size); // 重点
	}

	public class gridAdapter extends BaseAdapter {

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
			LayoutInflater inflater = getLayoutInflater();
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
			imagev.setImageResource(resIds[weatherId]);

			TextView textv2 = (TextView) v.findViewById(R.id.weatheritemTextv2);
			textv2.setText(listHourData.get(position).jb + "℃");

			return v;
		}

	}

	public class listAdapter extends BaseAdapter {

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
			LayoutInflater inflater = getLayoutInflater();
			View v = inflater.inflate(R.layout.weatherdays, null);

			TextView textv = (TextView) v.findViewById(R.id.weatherdayTextv);
			textv.setText(daysName.get(position));
			TextView textv2 = (TextView) v.findViewById(R.id.weatherdayTextv2);
			TextView textv3 = (TextView) v.findViewById(R.id.weatherdayTextv3);
			textv3.setText(listDayData.get(position).weather);
			textv2.setText(listDayData.get(position).tep);
			
			ImageView imagev = (ImageView) v
					.findViewById(R.id.weatherdayImagev);
			int weatherId = Integer.parseInt(listDayData.get(position).img);
			// 如果是霾，图片编号为32
			if (weatherId == 53) {
				weatherId = 32;
			}
			imagev.setImageResource(resIds[weatherId]);

			return v;
		}

	}

	/**
	 * 获取Assets中的文件内容，返回字符串
	 * 
	 * @param filename
	 *            文件路径
	 * @return String 文件中的字符串内容
	 */
	public String getAssetsData(String filename) {

		String data = "";
		try {
			InputStream is = getResources().getAssets().open(filename);
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			BufferedReader bfr = new BufferedReader(isr);

			String line = "";
			while ((line = bfr.readLine()) != null) {
				data += line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	/**
	 * json数据转化到ArrayList项
	 * 
	 * @param json
	 *            需要转化的json字符串
	 * @return list 存储数据的List列表项
	 */
	public ArrayList<WeatherHourInfo> Json2list_hour(String json) {

		ArrayList<WeatherHourInfo> list = new ArrayList<WeatherHourInfo>();
		try {
			JSONObject root = new JSONObject(json);
			JSONArray arr = root.getJSONArray("jh");

			for (int i = 0; i < arr.length(); i++) {
				WeatherHourInfo news = new WeatherHourInfo();
				JSONObject obj = arr.getJSONObject(i);

				news.ja = obj.getString("ja");
				news.jb = obj.getString("jb");
				news.jc = obj.getString("jc");
				news.jd = obj.getString("jd");
				news.je = obj.getString("je");
				news.jf = obj.getString("jf");

				list.add(news);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

	public ArrayList<WeatherDayInfo> Json2list_day(String json) {

		ArrayList<WeatherDayInfo> list = new ArrayList<WeatherDayInfo>();
		try {
			JSONObject root = new JSONObject(json);
			JSONObject weatherobj = root.getJSONObject("weatherinfo");

			for (int i = 2; i <= 6; i++) {
				WeatherDayInfo w = new WeatherDayInfo();
				w.img = weatherobj.getString("img" + (i * 2 - 1));
				w.weather = weatherobj.getString("weather" + i);
				w.tep = weatherobj.getString("temp" + i);

				list.add(w);
			}

			weatherCityTextv.setText(weatherobj.getString("city"));
			weatherTepTextv.setText(weatherobj.getString("temp1"));
			weatherImagev.setImageResource(resIds[Integer.parseInt(weatherobj
					.getString("img1"))]);

			int week = 0;
			// Integer.parseInt(weatherobj.getString("week").substring(3));
			String weeks[] = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
			for (int i = 0; i < weeks.length; i++) {
				if (weatherobj.getString("week").equals(weeks[i])) {
					week = i;
				}
			}
			daysName.add(weeks[(week+1)%7]);
			daysName.add(weeks[(week+2)%7]);
			daysName.add(weeks[(week+3)%7]);
			daysName.add(weeks[(week+4)%7]);
			daysName.add(weeks[(week+5)%7]);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

	public class WeatherDayInfo {
		public String weather;
		public String img;
		public String tep;

	}

	public class WeatherHourInfo {
		public String ja;
		public String jb;
		public String jc;
		public String jd;
		public String je;
		public String jf;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
