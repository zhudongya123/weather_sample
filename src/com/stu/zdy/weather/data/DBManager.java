package com.stu.zdy.weather.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.stu.zdy.weather_sample.R;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DBManager {
    private final static int BUFFER_SIZE = 400000;
    public static final String DB_NAME = "data.db"; //
    public static final String PACKAGE_NAME = "com.stu.zdy.weather_sample";
    public static final String DB_PATH = "/data"
            + Environment.getDataDirectory().getAbsolutePath() + "/"
            + PACKAGE_NAME; //

    private SQLiteDatabase database;


    public static SQLiteDatabase copyDataBaseFromRaw(Context context) {
        String dbfile = DB_PATH + "/" + DB_NAME;
        try {
            InputStream is = context.getResources().openRawResource(R.raw.data);
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
        db.close();
        return id;
    }
}
