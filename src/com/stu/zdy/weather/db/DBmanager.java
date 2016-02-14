package com.stu.zdy.weather.db;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.stu.zdy.weather_sample.R;

public class DBManager {
	private final static int BUFFER_SIZE = 400000;
	public static final String DB_NAME = "data.db"; // 保存的数据库文件名
	public static final String PACKAGE_NAME = "com.stu.zdy.weather_sample";
	public static final String DB_PATH = "/data"
			+ Environment.getDataDirectory().getAbsolutePath() + "/"
			+ PACKAGE_NAME; // 在手机里存放数据库的位置

	private SQLiteDatabase database;
	private Context context;
	private SQLiteDatabase db;

	public DBManager(Context context) {
		this.context = context;
	}

	public void openDatabase() {
//		this.database = this.copyDataBaseFromRaw();
	}

	public static SQLiteDatabase copyDataBaseFromRaw(Context context) {
		String dbfile = DB_PATH + "/" + DB_NAME;
		try {
			Log.v("当前需要导入", "当前需要导入");
			InputStream is = context.getResources().openRawResource(R.raw.data); // 欲导入的数据库
			FileOutputStream fos = new FileOutputStream(dbfile);
			byte[] buffer = new byte[BUFFER_SIZE];
			int count = 0;
			while ((count = is.read(buffer)) > 0) {
				fos.write(buffer, 0, count);
			}
			fos.close();
			is.close();
			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbfile,
					null);
			return db;
		} catch (FileNotFoundException e) {
			Log.e("Database", "File not found");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("Database", "IO exception");
			e.printStackTrace();
		}
		return null;
	}

	public void closeDatabase() {
		this.database.close();
	}

	public static String getIdByCityName(String name) {
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DB_PATH + "/"
				+ DB_NAME, null);
		String id = "";
		Cursor c = db.rawQuery("select * from citylist ", null);
		while (c.moveToNext()) {
			if (c.getString(c.getColumnIndex("cityname")).equals(name)) {
				id = c.getString(c.getColumnIndex("id"));
				break;
			}
		}
		c.close();
		// db.close();
		return id;
	}
}
