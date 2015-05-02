package org.wangpeng.ncuweather;

import java.io.BufferedReader;
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
import org.wangpeng.ncuweather.adapters.gridAdapter;
import org.wangpeng.ncuweather.adapters.listAdapter;
import org.wangpeng.ncuweather.classes.WeatherDayInfo;
import org.wangpeng.ncuweather.classes.WeatherHourInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
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

	public static int[] resIds; // ����ͼ��
	public static ArrayList<String> daysName; // δ�����������

	private TextView weatherCityTextv; // �ϲ����������Ŀؼ�
	private ImageView weatherImagev;
	private TextView weatherTepTextv;
	private Button weatherCitySettingBtn;
	private TextView publishtimeTextv;
	private ProgressBar progressbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ��ʼ���ۺ����ݽӿ�
		SDKInitializer.initialize(getApplicationContext());

		setContentView(R.layout.activity_main);

		gridView = (GridView) findViewById(R.id.gridView);
		listView = (ListView) findViewById(R.id.weatherdaysListv);
		weatherCityTextv = (TextView) findViewById(R.id.weatherCityTextv);
		weatherTepTextv = (TextView) findViewById(R.id.weatherTepTextv);
		weatherImagev = (ImageView) findViewById(R.id.weatherImageV);
		weatherCitySettingBtn = (Button) findViewById(R.id.weatherCitySettingBtn);
		publishtimeTextv = (TextView) findViewById(R.id.publishtimeTextv);
		progressbar = (ProgressBar) findViewById(R.id.progressbar);

		daysName = new ArrayList<String>();
		SetResources();

		// ShowUI("101240101","�ϲ�");
		Test();

		weatherCitySettingBtn
				.setOnClickListener(new CitySettingOnClickListener(this));
	}

	// ���ز���ģ����������
	public void Test() {
		String result = getAssetsData("daysdata.json");
		listDayData = Json2list_day_juhe(result);
		listAdapter d_adapter = new listAdapter(MainActivity.this, listDayData);
		listView.setAdapter(d_adapter);

		listHourData = Json2list_hour(getAssetsData("101240101.html"));
		// ����GridView��������ÿ������������
		SetGridParams(listHourData.size(), 80);
		gridAdapter h_adapter = new gridAdapter(MainActivity.this, listHourData);
		gridView.setAdapter(h_adapter);
	}

	public void ShowUI(String AreaId, String CityName) {
		GetHoursData("http://m.weather.com.cn//mpub/hours/" + AreaId + ".html");
		GetDaysData_juhe(CityName);
	}

	// ������ȡδ����������
	public void GetDaysData_juhe(String cityname) {
		Parameters params = new Parameters();
		params.add("cityname", cityname);
		params.add("dtype", "json");
		params.add("format", 1);

		// ��ʾ������
		progressbar.setVisibility(View.VISIBLE);
		// �Ƴ���һ�ε�����
		listDayData.clear();
		listView.setAdapter(null);

		JuheData.executeWithAPI(39, "http://v.juhe.cn/weather/index",
				JuheData.GET, params, new DataCallBack() {
					public void resultLoaded(int err, String reason,
							String result) {
						// �����ݼ��سɹ������ؽ�����
						progressbar.setVisibility(View.GONE);

						if (err == 0) {
							// ����ȡ�����ݴ��뵽�ļ��У�������ʾ
							writeToFile("daysdata.json", result);

							// �������ݲ��������б���
							listDayData = Json2list_day_juhe(result);

							listAdapter d_adapter = new listAdapter(
									MainActivity.this, listDayData);
							listView.setAdapter(d_adapter);
						} else {
							// ��ȡʧ�ܣ���ʾʧ��ԭ��
							Toast.makeText(getApplicationContext(), reason,
									Toast.LENGTH_SHORT).show();
						}
					}
				});
	}

	// ������ȡδ����Сʱ����
	public void GetHoursData(String url) {

		// ʹ���첽������
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

				// ������ȡ�������ݲ������ں����б�GridView��
				String str = new String(arg2);
				listHourData = Json2list_hour(str);

				// ����GridView��������ÿ������������
				SetGridParams(listHourData.size(), 80);
				gridAdapter h_adapter = new gridAdapter(MainActivity.this,
						listHourData);
				gridView.setAdapter(h_adapter);
			}
		});
	}

	// д�ļ�����
	public void writeToFile(String filename, String data) {
		try {
			// ��Ӧ���ڲ��ļ������ļ�������ʱ�ᴴ���������ڻὫ֮ǰ���ݸ���
			FileOutputStream fos = this.openFileOutput(filename,
					Context.MODE_PRIVATE);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			// д����Ϣ���ļ���
			osw.write(data);
			// �������뻺����
			osw.flush();
			osw.close();
			fos.close();
			// Toast.makeText(context.getApplicationContext(), "д���ļ��ɹ�",
			// Toast.LENGTH_SHORT).show();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ��������ͼƬ��Դ
	public void SetResources() {
		// ��ȡ��Դ��
		TypedArray ar = getResources().obtainTypedArray(R.array.weatherimages);
		int len = ar.length();
		resIds = new int[len];
		for (int i = 0; i < len; i++) {
			// �����Դ��ID��������
			resIds[i] = ar.getResourceId(i, 0);
		}
		ar.recycle();
	}

	// ����GridView �Ŀ�Ⱥ���������������������ÿһ��Ŀ��
	public void SetGridParams(int size, int length) {

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		float density = dm.density;
		int gridviewWidth = (int) (size * (length + 4) * density);
		int itemWidth = (int) (length * density);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				gridviewWidth, LinearLayout.LayoutParams.FILL_PARENT);
		gridView.setLayoutParams(params); // �ص�
		gridView.setColumnWidth(itemWidth); // �ص�
		gridView.setHorizontalSpacing(5); // ���
		gridView.setNumColumns(size); // �ص�
	}

	/**
	 * json����ת����ArrayList<WeatherHourInfo>
	 * 
	 * @param json
	 *            ��Ҫת����json�ַ���
	 * @return list
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

	/**
	 * json����ת����ArrayList<WeatherDayInfo>
	 * 
	 * @param json
	 * @return list
	 */
	public ArrayList<WeatherDayInfo> Json2list_day_juhe(String json) {

		ArrayList<WeatherDayInfo> list = new ArrayList<WeatherDayInfo>();
		try {
			JSONObject root = new JSONObject(json);
			JSONObject weatherobj = root.getJSONObject("result");

			JSONObject sk = weatherobj.getJSONObject("sk");
			publishtimeTextv.setText("����:" + sk.getString("time"));

			// ��������
			JSONObject today = weatherobj.getJSONObject("today");
			{
				int weatherId = Integer.parseInt(today.getJSONObject(
						"weather_id").getString("fa"));
				int hour = Integer.parseInt(sk.getString("time")
						.substring(0, 2));

				// ���������ͼƬ���Ϊ32
				if (weatherId == 53) {
					weatherId = 32;
				} // �����ҹ����Ϊҹ���ͼ��
				if (hour > 19 || hour < 6) {
					weatherId += 33;
				}

				weatherImagev.setImageResource(resIds[weatherId + 66]);

				weatherCityTextv.setText(today.getString("city"));
				weatherTepTextv.setText(today.getString("temperature").replace(
						"��~", "~"));
			}
			// δ����������
			JSONObject future = weatherobj.getJSONObject("future");
			{
				// ��ȡfuture�µ��������ڽ��
				JSONArray days = future.names();
				ArrayList<String> days_l = new ArrayList<String>();
				for (int i = 0; i < days.length(); i++) {
					days_l.add(days.get(i).toString());
				}
				// �Ի�ȡ�������нڵ㰴����������
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
			String weeks[] = { "������", "����һ", "���ڶ�", "������", "������", "������", "������" };
			for (int i = 0; i < weeks.length; i++) {
				if (today.getString("week").equals(weeks[i])) {
					week = i;
				}
			}
			// δ������ֻ�����죬��������δ�������е�����
			for (int i = 1; i < 7; i++) {
				daysName.add(weeks[(week + i) % 7]);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * ��ȡAssets�е��ļ����ݣ������ַ���
	 * 
	 * @param filename
	 *            �ļ�·��
	 * @return String �ļ��е��ַ�������
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
		if (id == R.id.about) {

			LayoutInflater flater = getLayoutInflater();
			View mview = flater.inflate(R.layout.aboutdialog, null);
			Dialog dialog = new AlertDialog.Builder(this).setView(mview)
					.create();
			// ��ʾ�Ի���
			dialog.show();
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
