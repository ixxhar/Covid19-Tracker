package com.ixxhar.covid19tracker.helperclass;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NearByDeviceDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "mycovid19.db";
    public static final String TABLE_NAME = "nearby_devices_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "NearbyDeviceIdentifer";
    public static final String COL_3 = "DiscoveredAt";

    public NearByDeviceDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, NearbyDeviceIdentifer TEXT, DiscoveredAt TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    //get all data from nearby devices table
    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        return res;
    }

    //insert new record in nearby devices table
    public boolean insertData(String nearbyDevice, String discoveredAt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, nearbyDevice);
        contentValues.put(COL_3, discoveredAt);
        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

    //delete record from nearby devices table by passing an id
    public Integer deleteData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "ID = ?", new String[]{id});
    }

    //update data in nearby devices table
    public boolean updateData(String id, String nearbyDevice, String phoneNumber, String discoveredAt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, id);
        contentValues.put(COL_2, nearbyDevice);
        contentValues.put(COL_3, discoveredAt);
        db.update(TABLE_NAME, contentValues, "ID = ?", new String[]{id});
        return true;
    }
}