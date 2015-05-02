package org.wangpeng.ncuweather;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.wangpeng.ncuweather.adapters.CityListAdapter;
import org.wangpeng.ncuweather.animations.btnAnima;
import org.wangpeng.ncuweather.classes.CityInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 城市设置按钮监听类
 * 
 * @author wangpeng
 * 
 */
public class CitySettingOnClickListener implements OnClickListener {

	// 配置SQLite数据库
	private static SQLiteDatabase database;
	public static final String DATABASE_FILENAME = "weatherdata.db"; // 这个是DB文件名字
	public static final String PACKAGE_NAME = "org.wangpeng.ncuweather"; // 这个是自己项目包路径
	public static final String DATABASE_PATH = "/data"
			+ Environment.getDataDirectory().getAbsolutePath() + "/"
			+ PACKAGE_NAME + "/databases"; // 获取存储位置地址

	// 应用Activity上下文
	private Context context;

	// 构造函数
	public CitySettingOnClickListener(Context context) {
		this.context = context;
	}
	
	btnAnima ani = new btnAnima();

	public void onClick(View v) {
		
		ani.setDuration(800);
		v.startAnimation(ani);
		
		Toast.makeText(context, "选择城市", Toast.LENGTH_SHORT).show();
		// 获取布局管理器
		LayoutInflater flater = ((Activity) context).getLayoutInflater();
		View mview = flater.inflate(R.layout.dialog, null);

		// 搜索框
		EditText editv = (EditText) mview.findViewById(R.id.DialogSearchEditv);
		// “未查找到”提示文本
		final TextView textv = (TextView) mview
				.findViewById(R.id.DialogSearchTipTextv);
		// 查询城市结果列表
		final ListView lv = (ListView) mview.findViewById(R.id.DialogCitysLv);
		// 查询城市结果数据组
		final ArrayList<CityInfo> list_citys = new ArrayList<CityInfo>();

		// EditText内容改变监听
		editv.addTextChangedListener(new TextWatcher() {
			// 当EditText的文本正在改变时执行
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			// 当EditText的文本改变前执行
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			// 当EditText的文本改变后执行
			public void afterTextChanged(Editable s) {

				// 初始化提示框和结果列表
				textv.setText("未查询到结果");
				list_citys.clear();
				textv.setVisibility(View.GONE);

				String c_name = s.toString().trim();
				if (!c_name.equals("")) {
					// 数据库中模糊查找关键词
					SQLiteDatabase db = openDatabase(context);
					Cursor c = db.rawQuery(
							"select * from citys where name like ?",
							new String[] { "%" + c_name + "%" });

					if (!c.moveToFirst()) {// 未查找到结果
						System.out.println("未查询到结果");
						// 显示未找到的提示
						textv.setVisibility(View.VISIBLE);

					} else {// 查询到结果
						textv.setVisibility(View.GONE);
						while (!c.isAfterLast()) {
							CityInfo city = new CityInfo();
							city.areaid = c.getString(c
									.getColumnIndex("areaid"));
							city.name = c.getString(c.getColumnIndex("name"));
							// 将查找到结果存入到备选列表中
							list_citys.add(city);
							c.moveToNext();
						}
					}
				}
				// 将所有结果呈现到列表中
				lv.setAdapter(new CityListAdapter(context, list_citys));
			}
		});

		// 自定义的对话框（自定义View）
		final Dialog dialog = new AlertDialog.Builder(context).setView(mview)
				.create();
		// 显示对话框
		dialog.show();

		// 结果列表点击监听
		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//Toast.makeText(context, list_citys.get(position).areaid,
				//		Toast.LENGTH_SHORT).show();

				// 获取到点击项中的城市名和城市ID
				String City = list_citys.get(position).name;
				String AreaId = list_citys.get(position).areaid;
				// 隐藏对话框
				dialog.dismiss();
				// 根据城市和ID更新UI数据
				((MainActivity) context).ShowUI(AreaId, City);
			}
		});
	}

	// 打开数据库的静态函数
	public static SQLiteDatabase openDatabase(Context context) {
		try {
			String databaseFilename = DATABASE_PATH + "/" + DATABASE_FILENAME;
			File dir = new File(DATABASE_PATH);
			// 如果数据库文件目录不存在，则创建目录
			if (!dir.exists()) {
				dir.mkdir();
			}
			if (!(new File(databaseFilename)).exists()) {
				// 如果数据库文件在内部存储区不存在，则拷贝raw中的数据库文件到内部存储区
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

			// 通过文件创建数据库对象
			database = SQLiteDatabase.openOrCreateDatabase(databaseFilename,
					null);
			return database;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
