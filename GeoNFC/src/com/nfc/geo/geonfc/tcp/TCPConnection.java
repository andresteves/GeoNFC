package com.nfc.geo.geonfc.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;

import com.nfc.geo.geonfc.database.InfoEntity;
import com.nfc.geo.geonfc.database.QueryInsertUpdate;
import com.nfc.geo.geonfc.deviceinfo.DeviceInformation;

public class TCPConnection{

	private static final int NFC = 0;
	private static final int GPS = 1;
	private static final int PUERTO = 5043;
	private static final String IP = "your-url";
	private DeviceInformation dinfo;
	private Activity acti;
	private QueryInsertUpdate queryInsert;
	private InfoEntity infEnti;
	private boolean hasSent = true;
	Socket socket = null;
	DataOutputStream dataOutputStream = null;
	DataInputStream dataInputStream = null;
	private Handler handler;

	public TCPConnection(Activity act, DeviceInformation dinf)
	{		
		dinfo = dinf;
		acti = act;

		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {			  
				queryInsert = new QueryInsertUpdate(acti);
				queryInsert.open();
				ArrayList<InfoEntity> ii = queryInsert.getInfo();

				boolean isInDB = false;

				if(!ii.isEmpty())
				{
					for (InfoEntity infoEntity : ii) {
						if(infoEntity.getTimestamp().equals(infEnti.getTimestamp()))
							isInDB =true;
					}

					if(!isInDB)
					{
						queryInsert.insertInfo(infEnti);
					}
				}else{
					queryInsert.insertInfo(infEnti);
				}
				queryInsert.close();
				setHasSent(false);
				
				final AlertDialog alertDialog = new AlertDialog.Builder(acti).create();
				alertDialog.setTitle("Erro en lo servidor o no GPRS conexion disponible!");
				alertDialog.setMessage("Activa tu GPRS conexion se no estaba activada!");
				alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						alertDialog.dismiss();
					} });
				alertDialog.show();
			}
		};
	}

	/**
	 * Método para el envío de los datos
	 * @param info Los datos a serem enviados
	 * @return true si server responde, false si ninguna respuesta de server
	 */
	public void sendInfo(String info)
	{
		Thread a = new Thread(new SendData(info));
		a.start();
	}

	class SendData implements Runnable {

		private String inf = "";

		public SendData(String ii){
			inf = ii;
		}

		@Override
		public void run(){
			try {		
				socket = new Socket(IP,PUERTO); 

				dataOutputStream = new DataOutputStream(socket.getOutputStream());

				dataInputStream = new DataInputStream(socket.getInputStream());

				dataOutputStream.writeUTF(inf);				

				socket.close();
				dataOutputStream.close();
				dataInputStream.close();
				setHasSent(true);
			} catch (UnknownHostException e) {
				handler.sendEmptyMessage(0);
			} catch (IOException e) {
				handler.sendEmptyMessage(0);
			}
		}
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

	public InfoEntity getInfEnti() {
		return infEnti;
	}

	public void setInfEnti(InfoEntity infEnti) {
		this.infEnti = infEnti;
	}

	public boolean isHasSent() {
		return hasSent;
	}

	public void setHasSent(boolean hasSent) {
		this.hasSent = hasSent;
	}
}
