package com.nfc.geo.geonfc.utils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class FileHandler {

	private static final String FILE_NAME = "config.ini";
	
	public FileHandler()
	{}

	public ArrayList<String> readFile(String path){
		
		File file = new File(path, FILE_NAME);
		
		ArrayList<String> events = new ArrayList<String>();

		DataInputStream out;
		try {
			out = new DataInputStream(new FileInputStream(file));

			String aux = "";
			while((aux = out.readLine()) != null)
				events.add(aux);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return events;
	}
}
