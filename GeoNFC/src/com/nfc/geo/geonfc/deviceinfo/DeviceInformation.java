package com.nfc.geo.geonfc.deviceinfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import com.nfc.geo.geonfc.MainActivity;
import com.nfc.geo.geonfc.database.InfoEntity;
import com.nfc.geo.geonfc.interfaces.DeviceInterface;
import com.nfc.geo.geonfc.interfaces.MainInterface;
import com.nfc.geo.geonfc.tcp.TCPConnection;

public class DeviceInformation implements DeviceInterface{

	private Location loc = null;
	private LocationManager locationManager = null;
	private LocationListener locationListener = null;
	private TCPConnection conn;

	//Timer e TimerTask por la actividad GPS 
	private Timer timer;
	private TimerTask timerTask;
	private MainInterface setTexts;

	private boolean sendFlag = false;

	public DeviceInformation(Activity activity)
	{
		conn = new TCPConnection(activity, this);
	}
	
	public DeviceInformation(Activity activity, MainActivity mainActivity)
	{
		conn = new TCPConnection(activity, this);
		
		setTexts = (MainInterface) mainActivity;
	}

	public Location getCurrentLocation()
	{
		return loc;		
	}

	public void sendGPSInfo()
	{
		timer = new Timer();
		timerTask = new TimerTask() {
			@Override
			public void run() {
				String aux = conn.buildData(1, null, null);
				conn.sendInfo(aux);
			}
		};
		timer.scheduleAtFixedRate(timerTask, 10000, 10000);
	}

	public void stopSendGPSInfo()
	{
		try {
			timer.cancel();
			timer.purge();
		} catch (Exception e) {
		}		
	}

	public Location getLastLocation()
	{
		if(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null)
			return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		else 
			return null;
	}

	public void startLocating(final Activity activity, final int delay)
	{
		String locManager = "";
		locManager = LocationManager.GPS_PROVIDER;

		locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

		if(locationListener == null)
			locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				loc = location;				

				timer.cancel();
				timer.purge();

				handleGpsLoc(location);				
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {}

			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {}
		};

		if(locationListener != null)
			locationManager.requestLocationUpdates(locManager, delay, 0, locationListener);
	}

	public void handleGpsLoc(Location loc)
	{
		if(sendFlag)
		{
			setTexts.changeGPSText(loc);
			enviarData();
		}else{
			if(locationListener != null && locationManager != null)
			{
				locationManager.removeUpdates(locationListener);
				locationListener = null;
				locationManager = null;
			}

		}
	}

	public void stopLocating()
	{
		locationManager.removeUpdates(locationListener);
	}

	public boolean isSendFlag() {
		return sendFlag;
	}

	public void setSendFlag(boolean sendFlag) {
		this.sendFlag = sendFlag;
	}

	private void enviarData()
	{
		InfoEntity infEnti = new InfoEntity(null, getTimestamp(), loc.getLatitude(),loc.getLongitude(), 
				(loc.getAccuracy()/5), loc.getSpeed(), getNumSatt(),"GPS");
		conn.setInfEnti(infEnti);
		String aux = conn.buildData(1, null, infEnti);
		conn.sendInfo(aux);
	}

	public String getDeviceImei(Context ctx)
	{
		TelephonyManager manager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		return manager.getDeviceId();		
	}

	public String getSIMNum(Context ctx)
	{
		TelephonyManager manager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		return manager.getSimSerialNumber();		
	}

	public float getBatteryInfo(Context ctx)
	{
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = ctx.registerReceiver(null, ifilter);

		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

		return level;
	}

	public String getTimestamp()
	{
		SimpleDateFormat s = new SimpleDateFormat("yyyyMMddHHmmss");
		s.setTimeZone(TimeZone.getTimeZone("UTC"));
		String format = s.format(new Date());
		return format;		
	}

	public int getNumSatt()
	{		
		return locationManager.getGpsStatus(null).getMaxSatellites();
	}

}
