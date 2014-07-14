package com.nfc.geo.geonfc;

import java.io.IOException;
import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nfc.geo.geonfc.database.InfoEntity;
import com.nfc.geo.geonfc.database.QueryInsertUpdate;
import com.nfc.geo.geonfc.deviceinfo.DeviceInformation;
import com.nfc.geo.geonfc.interfaces.MainInterface;
import com.nfc.geo.geonfc.tcp.TCPConnection;
import com.nfc.geo.geonfc.utils.FileHandler;

public class Tabs extends FragmentActivity {

	private FragmentTabHost host;
	private NfcAdapter mAdapter;
	private PendingIntent pendingIntent;
	private LinearLayout inicio_fin;
	private QueryInsertUpdate queryInsert;
	private TCPConnection conn;
	private DeviceInformation dinf;
	private SharedPreferences settings;
	private String data_send = "";
	private InfoEntity infEnti;
	private Button inicio,fin,cancelar;
	private MainInterface mainInterface;

	/** The intent filter */
	private static final IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
	private static final IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

	/** The intent filter array */
	private static final IntentFilter[] intentFiltersArray = new IntentFilter[] {tag,tech,};

	/** The tech list */
	private static final String[][] techListsArray = new String[][] { 
		new String[] { NfcF.class.getName() },
		new String[] { NfcA.class.getName() },
		new String[] { NfcB.class.getName() }, 
		new String[] { NfcV.class.getName() },
		new String[] { MifareClassic.class.getName() }, 
		new String[] { MifareUltralight.class.getName() },
		new String[] { IsoDep.class.getName() }, 
		new String[] { NdefFormatable.class.getName() },
		new String[] { Ndef.class.getName() },
		new String[] { IsoDep.class.getName() }
	};

	private FileHandler fileHandler;
	private ArrayList<String> fileIdioma;
	private String[] text_tabs = new String[7];

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.tabs_main);

		fileHandler = new FileHandler();
		fileIdioma = new ArrayList<String>();

		inicio_fin = (LinearLayout) findViewById(R.id.inicio_fin_layout);
		inicio_fin.setVisibility(View.VISIBLE);
		inicio_fin.setBackgroundColor(Color.rgb(0, 18, 73));
		inicio_fin.setClickable(false);
		inicio_fin.setEnabled(false);

		host = (FragmentTabHost)findViewById(android.R.id.tabhost);
		host.setup(this, getSupportFragmentManager() , R.id.realtabcontent);

		host.addTab(host.newTabSpec("main").setIndicator(""+(text_tabs[0] == null ? "Lecturas" : text_tabs[0]),null),
				MainActivity.class, null);
		host.addTab(host.newTabSpec("events").setIndicator(""+(text_tabs[1] == null ? "Eventos" : text_tabs[1]),null),
				Eventos.class, null);
		host.addTab(host.newTabSpec("settings").setIndicator(""+(text_tabs[2] == null ? "Configuración" : text_tabs[2]),null),
				Settings.class, null);
		host.addTab(host.newTabSpec("gps").setIndicator("GPS",null),
				GPSPestana.class, null);

		pendingIntent = PendingIntent.getActivity(
				this, 0, new Intent(getApplicationContext(), getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT);

		mAdapter = NfcAdapter.getDefaultAdapter(this);
		
		inicio = (Button) findViewById(R.id.inicio);
		fin = (Button) findViewById(R.id.fin);
		cancelar = (Button) findViewById(R.id.cancelar);
		
		inicio.setVisibility(View.INVISIBLE);
		fin.setVisibility(View.INVISIBLE);
		cancelar.setVisibility(View.INVISIBLE);
		
		try {
			fileIdioma = fileHandler.readFile(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getCanonicalPath());

			if(fileIdioma.size() > 5)
			{
				text_tabs[0]= fileIdioma.get(2).split("  = ")[1];
				text_tabs[1]= fileIdioma.get(3).split("  = ")[1];
				text_tabs[2]= fileIdioma.get(4).split("  = ")[1];
				text_tabs[3]= fileIdioma.get(9).split("  = ")[1];
				text_tabs[5]= fileIdioma.get(12).split(" = ")[1];
				text_tabs[4]= fileIdioma.get(18).split(" = ")[1];				
				text_tabs[6]= fileIdioma.get(19).split(" = ")[1];
				
				inicio.setText(""+text_tabs[4]);
				fin.setText(""+text_tabs[6]);
				cancelar.setText(""+text_tabs[5]);
			}
		} catch (IOException e) {
			//Do nothing
		}
		
		queryInsert = new QueryInsertUpdate(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);

		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {

			dinf = new DeviceInformation(this, (MainActivity) getSupportFragmentManager().findFragmentByTag("main"));
			conn = new TCPConnection(this,dinf);
			mainInterface = (MainInterface) getSupportFragmentManager().findFragmentByTag("main");
			String msg = "";
			try {
				// Checkar SCHEDULE mensage
				Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

				if (rawMsgs != null) {
					NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
					for (int i = 0; i < rawMsgs.length; i++) {
						msgs[i] = (NdefMessage) rawMsgs[i];
					}

					msg = new String(msgs[0].getRecords()[0].getPayload());

					if(msg.substring(3).equals("SCHEDULE"))
					{
						inicio_fin.setBackgroundColor(Color.CYAN);
						inicio.setVisibility(View.VISIBLE);
						fin.setVisibility(View.VISIBLE);
						cancelar.setVisibility(View.VISIBLE);
						buildButtonsSchedule();
					}
				}
				// Fin de SCHEDULE
			} catch (Exception e) {
				//Do nothing
			}finally{

				byte[] tag_id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
				String id_tag = bytesToHexString(tag_id);
				String id_tag_mayusculas = id_tag.substring(2).toUpperCase();

				mainInterface.changeTagText("Tag ID:"+id_tag_mayusculas);

				/**
				 * Guardar la ultima tag leida
				 * */
				settings = getSharedPreferences("gps_status", 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("ultima_tag", id_tag_mayusculas);
				editor.apply();

				/**
				 * Custom mensage de información al usuario indicando que se ha leído el TAG
				 */
				LayoutInflater inflater = getLayoutInflater();
				View vw = inflater.inflate(R.layout.toast_tag, null);
				TextView text_leida = (TextView) vw.findViewById(R.id.tag_leida);
				text_leida.setText(""+(text_tabs[3] == null ? "NFC Tag leida" : text_tabs[3]));
				Toast makeToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
				makeToast.setGravity(Gravity.BOTTOM, 0,100);
				makeToast.setView(vw);
				makeToast.show();

				if(msg.equals(""))
				{
					//Build a string with gps if enable and nfc tag info
					infEnti = new InfoEntity(id_tag_mayusculas, dinf.getTimestamp(), 0,0,0,0,0,"NFC");
					data_send = conn.buildData(0, id_tag_mayusculas, infEnti);

					if(checkConn())
					{
						conn.sendInfo(data_send);
					}else{
						queryInsert.open();
						queryInsert.insertInfo(infEnti);
						queryInsert.close();
					}
				}			
			}
		}
	}

	@Override
	protected void onResume() {
		if(mAdapter != null)
			mAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);

		super.onResume();
	}

	@Override
	protected void onPause() {
		if(mAdapter != null)
			mAdapter.disableForegroundDispatch(this);

		super.onPause();
	}

	public boolean checkConn()
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService( Context.CONNECTIVITY_SERVICE );
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

		if(activeNetInfo != null)
		{
			if(activeNetInfo.getState() == NetworkInfo.State.CONNECTED)
				return true;
			else
				return false;
		}			
		else		
			return false;
	}

	private String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("0x");
		if (src == null || src.length <= 0) {
			return null;
		}

		char[] buffer = new char[2];
		for (int i = 0; i < src.length; i++) {
			buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);  
			buffer[1] = Character.forDigit(src[i] & 0x0F, 16);  
			stringBuilder.append(buffer);
		}

		return stringBuilder.toString();
	}

	public void buildButtonsSchedule()
	{		
		mainInterface = (MainInterface) getSupportFragmentManager().findFragmentByTag("main");
		
		inicio.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {				
				InfoEntity infEnti = new InfoEntity("ST"+settings.getString("ultima_tag", ""), dinf.getTimestamp(), 0,0,0,0,0,"NFC");
				String data = conn.buildData(0, "ST"+settings.getString("ultima_tag", ""), infEnti);

				conn.sendInfo(data);

				mainInterface.changeTagText("Tag ID:"+settings.getString("ultima_tag", ""));

				inicio_fin.setBackgroundColor(Color.rgb(0, 18, 73));
				inicio.setVisibility(View.INVISIBLE);
				fin.setVisibility(View.INVISIBLE);
				cancelar.setVisibility(View.INVISIBLE);
			}
		});

		fin.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {				
				InfoEntity infEnti = new InfoEntity("EN"+settings.getString("ultima_tag", ""), dinf.getTimestamp(), 0,0,0,0,0,"NFC");
				String data = conn.buildData(0, "EN"+settings.getString("ultima_tag", ""), infEnti);

				conn.sendInfo(data);

				mainInterface.changeTagText("Tag ID:"+settings.getString("ultima_tag", ""));
				
				inicio_fin.setBackgroundColor(Color.rgb(0, 18, 73));
				inicio.setVisibility(View.INVISIBLE);
				fin.setVisibility(View.INVISIBLE);
				cancelar.setVisibility(View.INVISIBLE);
			}
		});

		cancelar.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				InfoEntity infEnti = new InfoEntity("CA"+settings.getString("ultima_tag", ""), dinf.getTimestamp(), 0,0,0,0,0,"NFC");
				String data = conn.buildData(0, "CA"+settings.getString("ultima_tag", ""), infEnti);

				conn.sendInfo(data);

				mainInterface.changeTagText("Tag ID:"+settings.getString("ultima_tag", ""));
				
				inicio_fin.setBackgroundColor(Color.rgb(0, 18, 73));
				inicio.setVisibility(View.INVISIBLE);
				fin.setVisibility(View.INVISIBLE);
				cancelar.setVisibility(View.INVISIBLE);
			}
		});
	}
}
