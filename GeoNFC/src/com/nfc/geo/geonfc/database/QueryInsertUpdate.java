package com.nfc.geo.geonfc.database;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class QueryInsertUpdate {

	// Database fields
	private SQLiteDatabase database;
	private Database dbHelper;

	public QueryInsertUpdate(Activity acti)
	{
		dbHelper = new Database(acti);
	}

	public void open()
	{
		database = dbHelper.getWritableDatabase();
	}

	public void close()
	{
		dbHelper.close();
	}

	public void insertInfo(InfoEntity info)
	{
		ContentValues values = new ContentValues();

		if(info.getTag_id() != null)
			values.put(Database.COLUMN_TAGID, info.getTag_id());
		else
			values.put(Database.COLUMN_TAGID, "");

		values.put(Database.COLUMN_TIMESTAMP, info.getTimestamp());
		values.put(Database.COLUMN_NUMSATT, info.getNum_sattelites());
		values.put(Database.COLUMN_LAT, info.getLatitude());
		values.put(Database.COLUMN_LONG, info.getLongitude());
		values.put(Database.COLUMN_HDOP, info.getHdpo());		
		values.put(Database.COLUMN_SPEED, info.getSpeed());
		values.put(Database.COLUMN_TYPE, info.getType());
		if(info.getLatitude() != 0.0 || info.getLatitude() != 0)
			values.put(Database.COLUMN_GPS_FIJO, "A");
		else
			values.put(Database.COLUMN_GPS_FIJO, "V");

		database.insert(Database.TABLE_STATS, null,values);
	}

	public void deleteInfo(long id)
	{
		database.delete(Database.TABLE_STATS,"id= " + id, null);
	}

	public ArrayList<InfoEntity> getInfo()
	{
		Cursor cs = database.rawQuery("SELECT * FROM "+Database.TABLE_STATS, null);
		ArrayList<InfoEntity> inf = new ArrayList<InfoEntity>();
		int cursor_count = cs.getCount();

		while(cursor_count > 0)
		{
			cs.moveToPosition(cursor_count-1);
			inf.add(cursorToInfoEntity(cs));
			cursor_count--;
		}		

		cs.close();
		return inf;
	}

	public void cleanTable()
	{
		database.delete(Database.TABLE_STATS, null, null);
	}

	private InfoEntity cursorToInfoEntity(Cursor cursor) {		

		InfoEntity infoE = new InfoEntity();
		infoE.setId(cursor.getInt(0));
		infoE.setTag_id(cursor.getString(1));
		infoE.setTimestamp(cursor.getString(2));
		infoE.setNum_sattelites(cursor.getInt(3));
		infoE.setLatitude(cursor.getFloat(4));
		infoE.setLongitude(cursor.getFloat(5));
		infoE.setHdpo(cursor.getFloat(6));
		infoE.setSpeed(cursor.getFloat(7));
		infoE.setType(cursor.getString(8));
		if(cursor.getFloat(4) != 0.0 || cursor.getFloat(4) != 0)
			infoE.setGps_fijo("A");
		else
			infoE.setGps_fijo("V");
		
		return infoE;
	}

}
