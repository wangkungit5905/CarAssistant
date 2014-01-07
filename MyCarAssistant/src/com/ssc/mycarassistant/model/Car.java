package com.ssc.mycarassistant.model;

import android.R.integer;

import com.ssc.mycarassistant.model.Fuel;

public class Car {
	public Car(int id, String number, Fuel fuel, int boxVolume, int total){
		this.id = id;
		mNumber = number;
		mUsedFuel = fuel;
		mBoxVolume = boxVolume;
		mTotalScale = total;
		maintenanceMileage = 0;
		maintenancePeriod=0;
	}
	
	public int ID(){return id;}
	public String number(){return mNumber;}
	public void setNumber(String number){mNumber=number;}
	public Fuel usedFuel(){return mUsedFuel;}
	public void setUsedFuel(Fuel fuel){mUsedFuel=fuel;}
	public int boxVolume(){return mBoxVolume;}
	public void setBoxVolume(int volume){mBoxVolume=volume;}
	public int totalScale(){return mTotalScale;}
	public void setTotalScale(int scale){mTotalScale=scale;}
	public String toString(){return mNumber;}
	
	public int mileage(){return maintenanceMileage;}
	public void setMileage(int mileage){maintenanceMileage = mileage;}
	public int period(){return maintenancePeriod;}
	public void setPeriod(int period){maintenancePeriod = period;}	
	
	private int id;
	private String mNumber;		//车牌号
	private Fuel mUsedFuel;		//所用燃料
	private int mBoxVolume;		//邮箱容积
	private int mTotalScale;	//里程表总刻度
	private int maintenanceMileage;	//保养间隔里程
	private int maintenancePeriod;	//保养间隔周期（以月计）
}
