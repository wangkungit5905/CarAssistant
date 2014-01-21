package com.ssc.mycarassistant.model;

import java.io.Flushable;

import android.R.integer;

import com.ssc.mycarassistant.model.Fuel;

public class Car {
	public static final float BOXVOLUME_FACTOR = 0.8f;	//油表刚满刻度时所代表的油量占邮箱总容积的百分比
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
	public float getUnit(){return ((float)mBoxVolume * BOXVOLUME_FACTOR) /(float)mTotalScale;} //返回每刻度所代表的油量
	public float getInitFuelVolume(){return mInitFuelVolume;}
	public void setInitFuelVolume(float volume){mInitFuelVolume=volume;}
	public void setInitFuelDial(float dial){mInitFuelVolume=((mBoxVolume * BOXVOLUME_FACTOR)/mTotalScale)*dial;}
	public float getInitDial(){return (mInitFuelVolume / ((float)mBoxVolume * BOXVOLUME_FACTOR))*mTotalScale;}//返回初始油表刻度
	public float getVolumeByDial(float dial){return (dial/mTotalScale)*mBoxVolume*BOXVOLUME_FACTOR;} //返回指定油表刻度所代表的油量（升）
	public int getInitMileage(){return mInitMileage;}
	public void setInitMileage(int m){mInitMileage=m;}
	public long getInitDate(){return mInitDate;}
	public void setInitDate(long date){mInitDate=date;}
	
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
	
	private float mInitFuelVolume;	//初始油量（升）
	private int mInitMileage;		//初始里程
	private long mInitDate;			//初始日期
}
