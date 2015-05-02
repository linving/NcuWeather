package org.wangpeng.ncuweather;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.thinkland.sdk.android.DataCallBack;
import com.thinkland.sdk.android.JuheData;
import com.thinkland.sdk.android.Parameters;
import com.thinkland.sdk.android.SDKInitializer;

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
	private TextView publishtimeTextv;
	
	private String City; // 当前的城市
	private String AreaId; // 当前的城市

	private static SQLiteDatabase database;
	public static final String DATABASE_FILENAME = "weatherdata.db"; // 这个是DB文件名字
	public static final String PACKAGE_NAME = "org.wangpeng.ncuweather"; // 这个是自己项目包路径
	public static final String DATABASE_PATH = "/data"
			+ Environment.getDataDirectory().getAbsolutePath() + "/"
			+ PACKAGE_NAME + "/databases"; // 获取存储位置地址

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 初始化聚合数据接口
		SDKInitializer.initialize(getApplicationContext());

		setContentView(R.layout.activity_main);

		gridView = (GridView) findViewById(R.id.gridView);
		listView = (ListView) findViewById(R.id.weatherdaysListv);
		weatherCityTextv = (TextView) findViewById(R.id.weatherCityTextv);
		weatherTepTextv = (TextView) findViewById(R.id.weatherTepTextv);
		weatherImagev = (ImageView) findViewById(R.id.weatherImageV);
		weatherCitySettingBtn = (Button) findViewById(R.id.weatherCitySettingBtn);
		publishtimeTextv = (TextView) findViewById(R.id.publishtimeTextv);

		daysName = new ArrayList<String>();
		SetResources();

		weatherCitySettingBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "选择城市",
						Toast.LENGTH_SHORT).show();
				LayoutInflater flater = LayoutInflater.from(MainActivity.this);
				View mview = flater.inflate(R.layout.dialog, null);
				EditText editv = (EditText) mview
						.findViewById(R.id.DialogSearchEditv);
				final TextView textv = (TextView) mview
						.findViewById(R.id.DialogSearchCityTextv);
				final ListView lv = (ListView) mview
						.findViewById(R.id.DialogCitysLv);
				final ArrayList<CityInfo> list_citys = new ArrayList<CityInfo>();

				editv.addTextChangedListener(new TextWatcher() {

					public void onTextChanged(CharSequence s, int start,
							int before, int count) {
					}

					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {
					}

					public void afterTextChanged(Editable s) {

						textv.setText("未查询到结果");
						list_citys.clear();
						textv.setVisibility(View.GONE);

						String c_name = s.toString().trim();
						if (!c_name.equals("")) {

							SQLiteDatabase db = openDatabase(MainActivity.this);
							Cursor c = db.rawQuery(
									"select * from citys where name like ?",
									new String[] { "%" + c_name + "%" });
							if (!c.moveToFirst()) {
								System.out.println("未查询到结果");
								textv.setVisibility(View.VISIBLE);
							} else {
								textv.setVisibility(View.GONE);
								while (!c.isAfterLast()) {
									CityInfo city = new CityInfo();
									city.areaid = c.getString(c.getColumnIndex("areaid"));
									city.name = c.getString(c.getColumnIndex("name"));
									list_citys.add(city);
									c.moveToNext();
								}
							}
						}
						lv.setAdapter(new CityListAdapter(list_citys));
					}
				});
				final Dialog dialog = new AlertDialog.Builder(MainActivity.this)
						.setView(mview).create();
				dialog.show();
				
				lv.setOnItemClickListener(new OnItemClickListener() {

					public void onItemClick(AdapterView<?> parent,
							View view, int position, long id) {
						Toast.makeText(getApplicationContext(), list_citys.get(position).areaid, Toast.LENGTH_SHORT).show();
						
						City = list_citys.get(position).name;
						AreaId = list_citys.get(position).areaid;
						
						dialog.dismiss();
						
						ShowUI();
					}
				});
			}
		});
	}
	
	public void ShowUI()
	{
		GetHoursData("http://m.weather.com.cn//mpub/hours/"+AreaId+".html");
	    GetDaysData_juhe(City);
	}

	public class CityListAdapter extends BaseAdapter {
		private ArrayList<CityInfo> data;

		public CityListAdapter(ArrayList<CityInfo> data) {
			this.data = data;
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
			LayoutInflater flater = LayoutInflater.from(MainActivity.this);
			View mview = flater.inflate(R.layout.cityitem, null);
			TextView tv = (TextView) mview.findViewById(R.id.DialogLvItemTextv);
			tv.setText(data.get(position).name);
			return mview;
		}
	}

	public static SQLiteDatabase openDatabase(Context context) {
		try {
			String databaseFilename = DATABASE_PATH + "/" + DATABASE_FILENAME;
			File dir = new File(DATABASE_PATH);
			if (!dir.exists()) {
				dir.mkdir();
			}
			if (!(new File(databaseFilename)).exists()) {
				InputStream is = context.getResources().openRawResource(
						R.raw.weatherdata);
				FileOutputStream fos = new FileOutputStream(databaseFilename);
				byte[] buffer = new byte[8192];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}

				fos.close();
				is.close();
			}
			database = SQLiteDatabase.openOrCreateDatabase(databaseFilename,
					null);
			return database;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void GetDaysData_juhe(String cityname) {
		Parameters params = new Parameters();
		params.add("cityname", cityname);
		params.add("dtype", "json");
		params.add("format", 1);

		JuheData.executeWithAPI(39, "http://v.juhe.cn/weather/index",
				JuheData.GET, params, new DataCallBack() {
					public void resultLoaded(int err, String reason,
							String result) {
						if (err == 0) {
							// tv.setText(result);
							writeToFile(result);

							listDayData = Json2list_day_juhe(result);

							listAdapter d_adapter = new listAdapter();
							listView.setAdapter(d_adapter);
						} else {
							Toast.makeText(getApplicationContext(), reason,
									Toast.LENGTH_SHORT).show();
						}
					}
				});
	}

	public void writeToFile(String data) {
		try {
			// 打开应用内部文件，当文件不存在时会创建，当存在会将之前内容覆盖
			FileOutputStream fos = this.openFileOutput("json.txt",
					Context.MODE_PRIVATE);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			// 写入信息到文件中
			osw.write(data);
			// 清理输入缓冲区
			osw.flush();
			osw.close();
			fos.close();
			// Toast.makeText(context.getApplicationContext(), "写入文件成功",
			// Toast.LENGTH_SHORT).show();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void GetHoursData(String url) {

		AsyncHttpClient asyClient = new AsyncHttpClient();
		asyClient.get(url, new AsyncHttpResponseHandler() {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				// called when response HTTP status is "4XX" (eg. 401, 403, 404)
				System.out.println("failure");
			}

			@Override
			public void onRetry(int retryNo) {
				super.onRetry(retryNo);
				// called when request is retried
				System.out.println("retry");
			}

			@Override
			public void onStart() {
				super.onStart();
				// called before request is started
				System.out.println("start");
			}

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				System.out.println("success");
				String str = new String(arg2);
				listHourData = Json2list_hour(str);

				SetGridParams(listHourData.size(), 80);
				gridAdapter h_adapter = new gridAdapter();
				gridView.setAdapter(h_adapter);
			}
		});
	}

	// 设置天气图片资源
	public void SetResources() {
		// 获取资源组
		TypedArray ar = getResources().obtainTypedArray(R.array.weatherimages);
		int len = ar.length();
		resIds = new int[len];
		for (int i = 0; i < len; i++) {
			// 添加资源的ID到数组中
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

	/**
	 * 自定义的Adapter，适配GridView
	 * 
	 * @author wangpeng
	 * 
	 */
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

	/**
	 * 自定义的Adapter，适配ListView
	 * 
	 * @author wangpeng
	 * 
	 */
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
			textv2.setText(listDayData.get(position).tep);

			// 设置天气图片1
			ImageView imagev = (ImageView) v
					.findViewById(R.id.weatherdayImagev);
			int weatherId = Integer.parseInt(listDayData.get(position).img1);
			// 如果是霾，图片编号为32
			if (weatherId == 53) {
				weatherId = 32;
			}
			imagev.setImageResource(resIds[weatherId]);
			// 设置天气图片2
			ImageView imagev2 = (ImageView) v
					.findViewById(R.id.weatherdayImagev2);
			int weatherId2 = Integer.parseInt(listDayData.get(position).img2);
			if (weatherId2 == 53) {
				weatherId2 = 32;
			}
			imagev2.setImageResource(resIds[weatherId2]);

			if (weatherId == weatherId2) {
				imagev2.setVisibility(View.GONE);
			}

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

	public ArrayList<WeatherDayInfo> Json2list_day_juhe(String json) {

		ArrayList<WeatherDayInfo> list = new ArrayList<WeatherDayInfo>();
		try {
			JSONObject root = new JSONObject(json);
			JSONObject weatherobj = root.getJSONObject("result");

			JSONObject sk = weatherobj.getJSONObject("sk");
			publishtimeTextv.setText("发布:" + sk.getString("time"));

			// 今日天气
			JSONObject today = weatherobj.getJSONObject("today");
			{
				weatherCityTextv.setText(today.getString("city"));
				weatherTepTextv.setText(today.getString("temperature"));
				weatherImagev.setImageResource(resIds[Integer.parseInt(today
						.getJSONObject("weather_id").getString("fa"))]);
			}
			// 未来几天天气
			JSONObject future = weatherobj.getJSONObject("future");
			{
				// 获取future下的所有日期结点
				JSONArray days = future.names();
				ArrayList<String> days_l = new ArrayList<String>();
				for (int i = 0; i < days.length(); i++) {
					days_l.add(days.get(i).toString());
				}
				// 对获取到的所有节点按照名称排序
				Collections.sort(days_l);

				for (int i = 1; i < 7; i++) {
					JSONObject wobj = future.getJSONObject(days_l.get(i));
					WeatherDayInfo w = new WeatherDayInfo();

					w.img1 = wobj.getJSONObject("weather_id").getString("fa");
					w.img2 = wobj.getJSONObject("weather_id").getString("fb");
					w.weather = wobj.getString("weather");
					w.tep = wobj.getString("temperature");

					list.add(w);
				}
			}

			int week = 0;
			// Integer.parseInt(weatherobj.getString("week").substring(3));
			String weeks[] = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
			for (int i = 0; i < weeks.length; i++) {
				if (today.getString("week").equals(weeks[i])) {
					week = i;
				}
			}
			// 未来天气只有六天，用于适配未来天气中的星期
			for (int i = 1; i < 7; i++) {
				daysName.add(weeks[(week + i) % 7]);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

	public class WeatherDayInfo {
		public String weather;
		public String img1;
		public String img2;
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

	public class CityInfo {
		public String areaid;
		public String name;
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
