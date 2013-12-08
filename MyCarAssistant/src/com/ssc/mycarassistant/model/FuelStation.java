package com.ssc.mycarassistant.model;

import com.ssc.mycarassistant.COODINATIONTYPE;

public class FuelStation {

	public FuelStation(int id, String name, String address){
		this.id = id;
		mName = name;
		mAddress = address;
		mLat = 0; mLon = 0;
		mCoType = COODINATIONTYPE.CT_GPS;
	}
	public String name(){return mName;}
	public void setName(String name){mName = name;}
	public String address(){return mAddress;}
	public void setAddress(String address){mAddress = address;}
	public double latitude(){return mLat;}
	public double longitude(){return mLon;}
	public void setCoodination(double lat, double lon){mLat=lat;mLon=lon;}
	public COODINATIONTYPE coodinationType(){return mCoType;}
	public void setCoodinationType(COODINATIONTYPE type){mCoType = type;}
	
	int id;
	String mName, mAddress;		//名称及其地址
	double mLat,mLon;			//经纬度坐标
	COODINATIONTYPE mCoType;	//经纬坐标类型
}
