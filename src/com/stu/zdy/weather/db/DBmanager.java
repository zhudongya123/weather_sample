package com.stu.zdy.weather.db;

import java.io.File;
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

public class DBmanager {
	private final int BUFFER_SIZE = 400000;
	public static final String DB_NAME = "data.db"; // 文件名
	public static final String PACKAGE_NAME = "com.stu.zdy.weather_sample";// 包名
	public static final String DB_PATH = "/data" + Environment.getDataDirectory().getAbsolutePath() + "/"
			+ PACKAGE_NAME; // 应用程序绝对路径

	private SQLiteDatabase database;
	private Context context;
	private SQLiteDatabase db;

	public DBmanager(Context context) {
		this.context = context;
	}

	public void openDatabase() {
		this.database = this.openDatabase(DB_PATH + "/" + DB_NAME);
	}

	private SQLiteDatabase openDatabase(String dbfile) {
		try {
			if (!(new File(dbfile).exists())) {// 当数据库文件存在的时候
				InputStream is = this.context.getResources().openRawResource(R.raw.data); // 搞到数据库文件的输入字节流（从文件读）
				FileOutputStream fos = new FileOutputStream(dbfile);// 新建一个输出字节流（写到文件中）
				byte[] buffer = new byte[BUFFER_SIZE];// 缓冲区大小400000
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}
				fos.close();
				is.close();
			}
			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbfile, null);
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

	public String getIdByCityName(String name) {
		db = SQLiteDatabase.openOrCreateDatabase(DBmanager.DB_PATH + "/" + DBmanager.DB_NAME, null);
		String id = "";
		Cursor c = db.rawQuery("select * from citylist ", null);
		while (c.moveToNext()) {
			if (c.getString(c.getColumnIndex("cityname")).equals(name)) {
				id = c.getString(c.getColumnIndex("id"));
				break;
			}
		}
		c.close();
		db.close();
		return id;
	}
}
