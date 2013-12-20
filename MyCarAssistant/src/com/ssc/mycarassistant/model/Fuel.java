package com.ssc.mycarassistant.model;

public class Fuel {

	public Fuel(int id, long fuelClsId, String fuelClsName, String grade){
		this.id = id;
		mFuelClsId = (int)fuelClsId;
		mFuelName = fuelClsName;
		mGrade = grade;
	}
	
	public int ID(){return id;}
	public String toString(){
		return mFuelName + "（" + mGrade + "）";
	}
	
	
	private int mFuelClsId;				//燃料种类id
	private String mGrade,mFuelName;		//燃料标号和燃料名
	private int id;
}