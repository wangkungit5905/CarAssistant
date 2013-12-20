package com.ssc.mycarassistant.model;

import android.R.integer;

public class ToFuelRecord {

	public ToFuelRecord(int id, Car car, long date, Fuel fuel, int mileage, Float fuelDial, float money, float amount, 
			float price, FuelStation station){
		this.id = id;
		mCar = car;
		mDate = date;
		mFuel = fuel;
		mFuelDial = fuelDial;
		mMileage = mileage;
		mMoney = money;
		mFuelAmount = amount;
		mPrice = price;
		mStation = station;
	}
	
	public int ID(){return id;}
	public Car car(){return mCar;}
	public void setCar(Car car){mCar=car;}
	public long date(){return mDate;}
	public void setDate(long date){mDate=date;}
	public Fuel Fuel(){return mFuel;}
	public void setFuel(Fuel fuel){mFuel=fuel;}
	public int mileage(){return mMileage;}
	public void setMileage(int m){mMileage=m;}
	public float fuelDial(){return mFuelDial;}
	public void setFuelDial(Float fuelDial){mFuelDial=fuelDial;}
	public float money(){return mMoney;}
	public void setMoney(float money){mMoney=money;}
	public float amount(){return mFuelAmount;}
	public void setAmount(float amount){mFuelAmount=amount;}
	public float price(){return mPrice;}
	public void setPrice(float price){mPrice=price;}
	public FuelStation station(){return mStation;}
	public void setStateion(FuelStation station){mStation=station;}
	
	int id;
	Car mCar;				//所属车辆
	long mDate;				//加油日期
	Fuel mFuel;				//所加燃料
	int mMileage;			//里程表读数
	float mFuelDial;		//油表读数
	float mMoney;			//加注金额
	float mFuelAmount;		//加注油量
	float mPrice;			//单价
	FuelStation mStation;	//加油站
}
