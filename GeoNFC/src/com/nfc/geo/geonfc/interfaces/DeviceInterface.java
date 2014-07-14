package com.nfc.geo.geonfc.interfaces;

import android.app.Activity;

public interface DeviceInterface {
	
	public void sendGPSInfo();
	public void stopSendGPSInfo();
	public void startLocating(final Activity activity, final int delay);
	public void stopLocating();
}
