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
 * �������ð�ť������
 * 
 * @author wangpeng
 * 
 */
public class CitySettingOnClickListener implements OnClickListener {

	// ����SQLite���ݿ�
	private static SQLiteDatabase database;
	public static final String DATABASE_FILENAME = "weatherdata.db"; // �����DB�ļ�����
	public static final String PACKAGE_NAME = "org.wangpeng.ncuweather"; // ������Լ���Ŀ��·��
	public static final String DATABASE_PATH = "/data"
			+ Environment.getDataDirectory().getAbsolutePath() + "/"
			+ PACKAGE_NAME + "/databases"; // ��ȡ�洢λ�õ�ַ

	// Ӧ��Activity������
	private Context context;

	// ���캯��
	public CitySettingOnClickListener(Context context) {
		this.context = context;
	}
	
	btnAnima ani = new btnAnima();

	public void onClick(View v) {
		
		ani.setDuration(800);
		v.startAnimation(ani);
		
		Toast.makeText(context, "ѡ�����", Toast.LENGTH_SHORT).show();
		// ��ȡ���ֹ�����
		LayoutInflater flater = ((Activity) context).getLayoutInflater();
		View mview = flater.inflate(R.layout.dialog, null);

		// ������
		EditText editv = (EditText) mview.findViewById(R.id.DialogSearchEditv);
		// ��δ���ҵ�����ʾ�ı�
		final TextView textv = (TextView) mview
				.findViewById(R.id.DialogSearchTipTextv);
		// ��ѯ���н���б�
		final ListView lv = (ListView) mview.findViewById(R.id.DialogCitysLv);
		// ��ѯ���н��������
		final ArrayList<CityInfo> list_citys = new ArrayList<CityInfo>();

		// EditText���ݸı����
		editv.addTextChangedListener(new TextWatcher() {
			// ��EditText���ı����ڸı�ʱִ��
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			// ��EditText���ı��ı�ǰִ��
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			// ��EditText���ı��ı��ִ��
			public void afterTextChanged(Editable s) {

				// ��ʼ����ʾ��ͽ���б�
				textv.setText("δ��ѯ�����");
				list_citys.clear();
				textv.setVisibility(View.GONE);

				String c_name = s.toString().trim();
				if (!c_name.equals("")) {
					// ���ݿ���ģ�����ҹؼ���
					SQLiteDatabase db = openDatabase(context);
					Cursor c = db.rawQuery(
							"select * from citys where name like ?",
							new String[] { "%" + c_name + "%" });

					if (!c.moveToFirst()) {// δ���ҵ����
						System.out.println("δ��ѯ�����");
						// ��ʾδ�ҵ�����ʾ
						textv.setVisibility(View.VISIBLE);

					} else {// ��ѯ�����
						textv.setVisibility(View.GONE);
						while (!c.isAfterLast()) {
							CityInfo city = new CityInfo();
							city.areaid = c.getString(c
									.getColumnIndex("areaid"));
							city.name = c.getString(c.getColumnIndex("name"));
							// �����ҵ�������뵽��ѡ�б���
							list_citys.add(city);
							c.moveToNext();
						}
					}
				}
				// �����н�����ֵ��б���
				lv.setAdapter(new CityListAdapter(context, list_citys));
			}
		});

		// �Զ���ĶԻ����Զ���View��
		final Dialog dialog = new AlertDialog.Builder(context).setView(mview)
				.create();
		// ��ʾ�Ի���
		dialog.show();

		// ����б�������
		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//Toast.makeText(context, list_citys.get(position).areaid,
				//		Toast.LENGTH_SHORT).show();

				// ��ȡ��������еĳ������ͳ���ID
				String City = list_citys.get(position).name;
				String AreaId = list_citys.get(position).areaid;
				// ���ضԻ���
				dialog.dismiss();
				// ���ݳ��к�ID����UI����
				((MainActivity) context).ShowUI(AreaId, City);
			}
		});
	}

	// �����ݿ�ľ�̬����
	public static SQLiteDatabase openDatabase(Context context) {
		try {
			String databaseFilename = DATABASE_PATH + "/" + DATABASE_FILENAME;
			File dir = new File(DATABASE_PATH);
			// ������ݿ��ļ�Ŀ¼�����ڣ��򴴽�Ŀ¼
			if (!dir.exists()) {
				dir.mkdir();
			}
			if (!(new File(databaseFilename)).exists()) {
				// ������ݿ��ļ����ڲ��洢�������ڣ��򿽱�raw�е����ݿ��ļ����ڲ��洢��
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

			// ͨ���ļ��������ݿ����
			database = SQLiteDatabase.openOrCreateDatabase(databaseFilename,
					null);
			return database;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
