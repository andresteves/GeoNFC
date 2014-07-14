package com.nfc.geo.geonfc.database;


public class InfoEntity {

	private long id = 0L;
	
	private String tag_id = "";
	
	private String timestamp = "";
	
	private double latitude = 0.0f;
	
	private double longitude = 0.0f;
	
	private float hdpo = 0.0f;
	
	private float speed = 0.0f;
	
	private int num_sattelites = 0;
	
	private String type = "";
	
	private String gps_fijo = "";
	
	public InfoEntity()
	{		
	}

	public InfoEntity(String tag_id, String timestamp, double latitude,
			double longitude, float hdpo, float speed, int num_sattelites,
			String type) {
		super();
		this.tag_id = tag_id;
		this.timestamp = timestamp;
		this.latitude = latitude;
		this.longitude = longitude;
		this.hdpo = hdpo;
		this.speed = speed;
		this.num_sattelites = num_sattelites;
		this.type = type;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTag_id() {
		return tag_id;
	}

	public void setTag_id(String tag_id) {
		this.tag_id = tag_id;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public float getHdpo() {
		return hdpo;
	}

	public void setHdpo(float hdpo) {
		this.hdpo = hdpo;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public int getNum_sattelites() {
		return num_sattelites;
	}

	public void setNum_sattelites(int num_sattelites) {
		this.num_sattelites = num_sattelites;
	}

	public String getGps_fijo() {
		return gps_fijo;
	}

	public void setGps_fijo(String gps_fijo) {
		this.gps_fijo = gps_fijo;
	}
}
