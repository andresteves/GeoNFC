package com.nfc.geo.geonfc.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

public class EnviarDatos implements Runnable {

	private static final int PUERTO = 5043;
	private static final String IP = "your-url";

	private Socket socket = null;
	private DataOutputStream dataOutputStream = null;
	private DataInputStream dataInputStream = null;
	private String msg;
	private String response="";

	public EnviarDatos(String mensage)
	{
		msg = mensage;
	}

	@Override
	public void run() {
		try {
			socket = new Socket(IP,PUERTO);			
			dataOutputStream = new DataOutputStream(socket.getOutputStream());
			dataInputStream = new DataInputStream(socket.getInputStream());

			dataOutputStream.writeUTF(msg);
			dataOutputStream.flush();

			boolean received = false;

			while(received == false)
			{
				String ax = dataInputStream.readUTF();

				Log.i("Response server", ""+ax);

				if(ax != null && ax.length() > 2)
				{
					setResponse(ax);
					received = true;
				}
			}

			socket.close();
			dataOutputStream.close();
			dataInputStream.close();
		} catch (UnknownHostException e) {
		} catch (IOException e) {
		} 
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}
}
