package com.nfc.geo.geonfc.builder;

import android.app.Activity;

import com.nfc.geo.geonfc.database.InfoEntity;
import com.nfc.geo.geonfc.deviceinfo.DeviceInformation;

public class DataBuilder {

	private static final int NFC = 0;
	private static final int GPS = 1;
	
	private InfoEntity infEnti;
	private DeviceInformation dinfo;
	private Activity acti;
	
	public DataBuilder(Activity activity)
	{
		acti = activity;
		dinfo = new DeviceInformation(activity);
	}
	
	/**
	 * Método para la construcción de la info
	 * @param id El id si es nfc o gps
	 * @param tag_id El ID tag
	 * 
	 * @return String con la informacion
	 */
	public String buildData(int id, String tag_id, InfoEntity infoe)
	{
		String pre_info = "";

		if(infoe != null)
			infEnti = infoe;

		switch (id) {
		case NFC:
			pre_info = ""+ dinfo.getDeviceImei(acti)+",,NFC,"+infEnti.getTimestamp()+","
					+infEnti.getTag_id()+",,,,,,,"+dinfo.getBatteryInfo(acti)+",";
			infEnti = new InfoEntity(tag_id, dinfo.getTimestamp(),0,0,0,0,0,"NFC");
			String nfc_info = "$AN"+pre_info.length()+","+pre_info+"\r\n";
			return nfc_info;
		case GPS:
			if(infoe != null)
			{
				if(infoe.getLatitude() != 0.0)
					pre_info = ""+ dinfo.getDeviceImei(acti)+",,GPS,"+infoe.getTimestamp()+
					",,"+infoe.getLatitude()+","+infoe.getLongitude()+",A,"+infoe.getHdpo()+","+infoe.getNum_sattelites()+","
					+infoe.getSpeed()+","+dinfo.getBatteryInfo(acti)+",";
				else
					pre_info = ""+ dinfo.getDeviceImei(acti)+",,GPS,"+infoe.getTimestamp()+
					",,"+infoe.getLatitude()+","+infoe.getLongitude()+",V,"+infoe.getHdpo()+","+infoe.getNum_sattelites()+","
					+infoe.getSpeed()+","+dinfo.getBatteryInfo(acti)+",";

				infEnti = new InfoEntity(null, infoe.getTimestamp(), infoe.getLatitude(),infoe.getLongitude(),infoe.getHdpo()
						, infoe.getSpeed(), infoe.getNum_sattelites(),"GPS");
			}else{
				pre_info = ""+ dinfo.getDeviceImei(acti)+",,GPS,"+dinfo.getTimestamp()+",,,,V,,,,,"+dinfo.getBatteryInfo(acti)+",";
				infEnti = new InfoEntity(tag_id, dinfo.getTimestamp(),0,0,0,0,0,"GPS");
			}
			String gps_info = "$AN"+pre_info.length()+","+pre_info+"\r\n";
			return gps_info;
		default:
			break;
		}
		return null;
	}
}
