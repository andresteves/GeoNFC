package com.nfc.geo.geonfc;

import java.io.IOException;
import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.nfc.geo.geonfc.database.InfoEntity;
import com.nfc.geo.geonfc.database.QueryInsertUpdate;
import com.nfc.geo.geonfc.deviceinfo.DeviceInformation;
import com.nfc.geo.geonfc.interfaces.MainInterface;
import com.nfc.geo.geonfc.tcp.TCPConnection;
import com.nfc.geo.geonfc.utils.FileHandler;

public class MainActivity extends Fragment implements MainInterface{

	private TextView text_tag,imei_text,version_text,gps_text, acerque_tel;
	private TCPConnection conn;
	private DeviceInformation dinf;	
	private QueryInsertUpdate queryInsert;
	private Button salir,imei;
	private View vi;
	private FileHandler fileHandler;
	private BroadcastReceiver mNetworkStateIntentReceiver;
	private IntentFilter mNetworkStateChangedFilter;
	private ArrayList<String> fileIdioma;
	private String[] textos_botons;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		vi = inflater.inflate(R.layout.activity_main, null);

		fileHandler = new FileHandler();
		fileIdioma = new ArrayList<String>();
		textos_botons =  new String [8];

		text_tag = (TextView) vi.findViewById(R.id.tag_id);
		acerque_tel = (TextView) vi.findViewById(R.id.tag_leida);

		text_tag.addTextChangedListener(new TextWatcher() {			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			@Override
			public void afterTextChanged(final Editable s) {
				final Handler handler=new Handler();				
				Runnable runnable=new Runnable(){  
					@Override  
					public void run() {  
						s.replace(0, s.length(), "Tag ID:");
					}  
				};				
				handler.postDelayed(runnable, 10000);
			}
		});

		version_text = (TextView) vi.findViewById(R.id.version_number);

		try {
			version_text.setText(""+getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
		} catch (NameNotFoundException e1) {
			e1.printStackTrace();
		}

		imei_text = (TextView) vi.findViewById(R.id.imei_text);
		gps_text = (TextView) vi.findViewById(R.id.gps_text);
		imei = (Button) vi.findViewById(R.id.imei);
		salir = (Button) vi.findViewById(R.id.exit);

		dinf = new DeviceInformation(getActivity(), MainActivity.this);

		imei.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				imei_text.setText(dinf.getDeviceImei(getActivity()));
			}
		});

		salir.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				getActivity().finish();
			}
		});

		queryInsert = new QueryInsertUpdate(getActivity());

		conn = new TCPConnection(getActivity(),dinf);		

		mNetworkStateChangedFilter = new IntentFilter();
		mNetworkStateChangedFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

		mNetworkStateIntentReceiver = new BroadcastReceiver() {			
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
					ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE);
					NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

					if ( activeNetInfo != null ){
						//send info
						queryInsert.open();
						ArrayList<InfoEntity> ii = queryInsert.getInfo();

						if(!ii.isEmpty())
						{
							for (InfoEntity infoEntity : ii) {
								String sendData = "";

								if(infoEntity.getType().equals("NFC"))
									sendData = conn.buildData(0,infoEntity.getTag_id(), infoEntity);
								else
									sendData = conn.buildData(1,null, infoEntity);

								conn.setInfEnti(infoEntity);
								conn.sendInfo(sendData);

								if(conn.isHasSent())
									queryInsert.deleteInfo(infoEntity.getId());
							}

							queryInsert.close();
						}
					}
				}
			}
		};

		/*
		 * Cambio de los textos de acuerdo con los idiomas en lo fichero de config.ini
		 * */		
		try {
			fileIdioma = fileHandler.readFile(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getCanonicalPath());

			if(fileIdioma.size() > 5)
			{
				textos_botons[0]= fileIdioma.get(5).split("  = ")[1];
				textos_botons[1]= fileIdioma.get(6).split("  = ")[1];
				textos_botons[2]= fileIdioma.get(7).split("  = ")[1];
				textos_botons[3]= fileIdioma.get(8).split("  = ")[1];

				salir.setText(""+textos_botons[0]);
				acerque_tel.setText(""+textos_botons[3]);
			}
		} catch (IOException e) {
			//Do nothing
		}

		return vi;
	}

	@Override
	public void onResume() {
		super.onResume();

		getActivity().registerReceiver(mNetworkStateIntentReceiver, mNetworkStateChangedFilter);
		getActivity().setIntent(new Intent());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		System.exit(0);		
	}

	@Override
	public void onPause() {		
		super.onPause();

		text_tag.setText("Tag ID:");
		getActivity().unregisterReceiver(mNetworkStateIntentReceiver);
	}	

	public boolean checkConn()
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService( Context.CONNECTIVITY_SERVICE );
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

	public void changeGPSText(Location loc)
	{
		gps_text.setText(textos_botons[1]+": " + loc.getLatitude() + "\n"+textos_botons[2]+": " + loc.getLongitude());
	}

	public void changeGPSText(String value)
	{
		gps_text.setText(value);
	}

	@Override
	public void changeTagText(String value) 
	{
		text_tag.setText(value);
	}
}
