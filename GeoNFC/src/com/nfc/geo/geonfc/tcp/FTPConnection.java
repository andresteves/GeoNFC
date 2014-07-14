package com.nfc.geo.geonfc.tcp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;

public class FTPConnection {

	private Activity activity;
	private File file;
	private FTPClient ftpClient;
	
	public FTPConnection(Activity act)
	{
		activity = act;		
	}
	
	public void downloadFile(String nameFile)
	{
		new DownloadFTP(activity, nameFile).execute();
	}
	
	class DownloadFTP extends AsyncTask<Void, Void, Void>
	{
		private ProgressDialog progressDialog;
		private String nameFile,replyString;
		
		public DownloadFTP(Context context,String name)
		{
			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage("CARGANDO DATOS, ESPERE POR FAVOR");
			nameFile = name;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			progressDialog.dismiss();
			
			if(!replyString.contains("550 Failed to open file."))
				dataMessage("Eventos cargados correctamente!");
			else
				dataMessage("Error al cargar eventos, reintente por favor.");
		}

		@Override
		protected Void doInBackground(Void... params) {
			
			file = new File(activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "config.ini");
			
			try {
				ftpClient = new FTPClient();
				ftpClient.connect("your-url");
				ftpClient.login("username", "password");
				ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
				
				BufferedOutputStream buffoOut=null;
				buffoOut=new BufferedOutputStream(new FileOutputStream(file));
				ftpClient.enterLocalPassiveMode();
				ftpClient.retrieveFile(nameFile, buffoOut);
				replyString = ftpClient.getReplyString();
				buffoOut.close();
				ftpClient.logout();
				ftpClient.disconnect();			
			} catch (SocketException e) {
				dataMessage("Error al cargar eventos, reintente por favor.");
			} catch (IOException e) {
				dataMessage("Error al cargar eventos, reintente por favor.");
			}
			
			return null;
		}
		
		private void dataMessage(String mensaje)
		{
			final AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
			alertDialog.setTitle("");
			alertDialog.setMessage(mensaje);
			alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					alertDialog.dismiss();
				} });
			alertDialog.show();
		}
	}
}
