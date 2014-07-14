package com.nfc.geo.geonfc;

import java.io.IOException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.nfc.geo.geonfc.deviceinfo.DeviceInformation;
import com.nfc.geo.geonfc.interfaces.MainInterface;
import com.nfc.geo.geonfc.tcp.EnviarDatos;
import com.nfc.geo.geonfc.utils.FileHandler;

public class GPSPestana extends Fragment{

	private Button save,activar_gps;
	private EditText delay;
	private TextView frecuencia;
	private SharedPreferences settings;
	private ArrayList<String> fileIdioma;
	private String[] textos_botons;
	private FileHandler fileHandler;
	private MainInterface mainInt;
	private EnviarDatos conn;
	private DeviceInformation di;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View vi = inflater.inflate(R.layout.pestana_gps, null);

		frecuencia = (TextView) vi.findViewById(R.id.frecuencia);
		save = (Button) vi.findViewById(R.id.save_settings);		
		activar_gps = (Button) vi.findViewById(R.id.activar_gps);
		delay = (EditText) vi.findViewById(R.id.delay_text);

		fileHandler = new FileHandler();
		fileIdioma = new ArrayList<String>();
		textos_botons =  new String [3];

		di = new DeviceInformation(getActivity(),(MainActivity) getFragmentManager().findFragmentByTag("main"));
		settings = getActivity().getSharedPreferences("gps_status", 0);
		mainInt = (MainInterface) getFragmentManager().findFragmentByTag("main");

		delay.setEnabled(false);
		save.setEnabled(false);

		save.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				SharedPreferences.Editor editor = settings.edit();
				int delayTime = Integer.parseInt(delay.getText().toString());
				editor.putInt("delay_gps", delayTime*1000);
				editor.apply();
				editor.commit();
			}
		});

		activar_gps.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {

				if(!checkGPS())
					;//Enviar mensage a indicar que lo GPS de movil no esta activado

				if(!di.isSendFlag())
				{
					String aux = ""+di.getDeviceImei(getActivity()) + ",ALL,"+di.getTimestamp();
					String activarString = "$AN"+aux.length()+","+aux+",\r\n";			
					conn = new EnviarDatos(activarString);

					try {
						new Thread(conn).start();

						String res = "";

						int i=0;
						while((res = conn.getResponse()).isEmpty() && i < 1000000)
							i++;

						Log.i("DATOS_GPS", "Response:"+res);

						if(res.equals("True"))
						{
							enableGPSSend(true);

							delay.setEnabled(true);
							save.setEnabled(true);
						}

						if(res != null)
							showAlertDialog(res);
					} catch (Exception e) {
						noConexion();
					}					
				}else{
					enableGPSSend(false);
				}
			}
		});


		/*
		 * Cambio de los textos de acuerdo con los idiomas en lo fichero de config.ini
		 * */		
		try {
			fileIdioma = fileHandler.readFile(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getCanonicalPath());

			if(fileIdioma.size() > 5)
			{
				textos_botons[0]= fileIdioma.get(13).split(" = ")[1];
				textos_botons[1]= fileIdioma.get(14).split(" = ")[1];
				textos_botons[2]= fileIdioma.get(16).split(" = ")[1];

				activar_gps.setText(""+textos_botons[0]);
				frecuencia.setText(""+textos_botons[1]);
				save.setText(""+textos_botons[2]);
			}
		} catch (IOException e) {
			//Do nothing
		}

		return vi;
	}

	public void showAlertDialog(String response)
	{
		final AlertDialog alert = new AlertDialog.Builder(getActivity()).create();
		alert.setTitle("GPS Transmisión");
		alert.setButton(AlertDialog.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				alert.dismiss();
			}
		});
		if(response.equals("True"))
			alert.setMessage("Transmisión GPS habilitáda.");
		else
			alert.setMessage("Ninguna transmisión GPS iniciada.");

		alert.show();

	}

	public void enableGPSSend(boolean status)
	{
		if(status)
		{
			int delayTime = Integer.parseInt(delay.getText().toString()) * 1000;

			if(delayTime != 0)
			{
				di.startLocating(getActivity(),delayTime);
				di.sendGPSInfo();
				activar_gps.setText("Desactivar GPS");
				di.setSendFlag(true);	
				mainInt.changeGPSText("Latitud:0.0\nLongitud:0.0");
			}						
		}else{			
			activar_gps.setText("Activar GPS");
			di.stopSendGPSInfo();
			di.setSendFlag(false);
		}
	}

	private void noConexion(){
		final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
		alertDialog.setTitle("Erro en lo servidor o no GPRS conexión disponible!");
		alertDialog.setMessage("Activa tu GPRS conexión se no estaba activada!");
		alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				alertDialog.dismiss();
			} });
		alertDialog.show();
	}

	private boolean checkGPS()
	{
		LocationManager locationManager =
				(LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		return gpsEnabled;
	}
}
