package com.nfc.geo.geonfc;

import java.io.IOException;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.nfc.geo.geonfc.tcp.FTPConnection;
import com.nfc.geo.geonfc.utils.FileHandler;

public class Settings extends Fragment{

	private Button actualizar;
	private EditText nombre_file;
	private FTPConnection ftpConn;

	private ArrayList<String> fileIdioma;
	private FileHandler fileHandler;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View vi = inflater.inflate(R.layout.settings, null);

		fileHandler = new FileHandler();
		fileIdioma = new ArrayList<String>();
		actualizar = (Button) vi.findViewById(R.id.actualizar);
		nombre_file = (EditText) vi.findViewById(R.id.eventos_file);
		ftpConn = new FTPConnection(getActivity());

		actualizar.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(!nombre_file.getText().toString().isEmpty())
				{
					ftpConn.downloadFile(nombre_file.getText().toString());
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
				actualizar.setText(""+fileIdioma.get(15).split(" = ")[1]);
			}
		} catch (IOException e) {
			//Do nothing
		}

		return vi;
	}
}
