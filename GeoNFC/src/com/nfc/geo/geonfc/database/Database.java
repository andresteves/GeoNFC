package com.nfc.geo.geonfc.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper{

	private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_TABLE_NAME = "Stats";
    public static final String TABLE_STATS = "infostats";
    public static final String COLUMN_TAGID = "tag_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_NUMSATT = "num_sat";
    public static final String COLUMN_LAT = "latitude";
    public static final String COLUMN_LONG = "longitude";
    public static final String COLUMN_HDOP = "hdop";
    public static final String COLUMN_SPEED = "speed";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_GPS_FIJO = "gps_fijo";
    
	public Database(Context context) {
		super(context, DATABASE_TABLE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String build_table = "CREATE TABLE " + TABLE_STATS + "("
                +"id INTEGER PRIMARY KEY autoincrement, tag_id TEXT, timestamp TEXT, num_sat INTEGER, latitude DOUBLE, " +
                "longitude DOUBLE, hdop FLOAT, speed FLOAT, type TEXT, gps_fijo TEXT)";
		
		db.execSQL(build_table);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
