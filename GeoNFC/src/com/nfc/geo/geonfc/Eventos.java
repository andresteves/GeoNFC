package com.nfc.geo.geonfc;

import java.io.IOException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.nfc.geo.geonfc.deviceinfo.DeviceInformation;
import com.nfc.geo.geonfc.tcp.TCPConnection;
import com.nfc.geo.geonfc.utils.FileHandler;

public class Eventos extends Fragment {

	private FileHandler file;
	private LinearLayout layout;
	private ArrayList<String> eventos;
	private TCPConnection conn;
	private DeviceInformation di;
	private SharedPreferences settings;
	private String[] textos_botons;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View vi = inflater.inflate(R.layout.incidencias, null);

		eventos = new ArrayList<String>();
		layout = (LinearLayout) vi.findViewById(R.id.incidencias_layout);
		file = new FileHandler();
		textos_botons =  new String[3];

		settings = getActivity().getSharedPreferences("gps_status", 0);
		di = new DeviceInformation(getActivity());
		conn = new TCPConnection(getActivity(), di);

		try {
			eventos = file.readFile(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getCanonicalPath());

			int index = 0;

			for (int i = 0; i < eventos.size(); i++) {
				if(eventos.get(i).equals("[EVENTOS]"))
					index = i;
			}

			for (int i = index+1; i < eventos.size(); i++) {
				final String[] aux = eventos.get(i).split(";");

				Button ff = new Button(getActivity());
				ff.setText(""+aux[2]);

				ff.setOnClickListener(new OnClickListener() {					
					@Override
					public void onClick(View v) {
						showAlertDialog(aux[0], aux[2]);
					}
				});

				layout.addView(ff);
			}

		} catch (IOException e) {
			//Do nothing
		}

		if(eventos.size() > 5)
		{
			textos_botons[0]= eventos.get(10).split("  = ")[1];
			textos_botons[1]= eventos.get(11).split(" = ")[1];
			textos_botons[2]= eventos.get(12).split(" = ")[1];
		}
		return vi;
	}

	public void showAlertDialog(final String response,String event)
	{
		final AlertDialog alert = new AlertDialog.Builder(getActivity()).create();
		alert.setTitle("Enviar Evento");
		alert.setButton(AlertDialog.BUTTON_POSITIVE, ""+textos_botons[1], new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String aux = ""+ di.getDeviceImei(getActivity())+",,NFC,"+di.getTimestamp()+
						","+response+settings.getString("ultima_tag", "")+",,,,,,,"+di.getBatteryInfo(getActivity());
				String activarString = "$AN"+aux.length()+","+aux+",\r\n";

				conn.sendInfo(activarString);

				alert.dismiss();
			}
		});
		alert.setButton(AlertDialog.BUTTON_NEGATIVE, ""+textos_botons[2], new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				alert.dismiss();
			}
		});
		alert.setMessage(""+ textos_botons[0]+ " " + event + ".");

		alert.show();

	}
}
