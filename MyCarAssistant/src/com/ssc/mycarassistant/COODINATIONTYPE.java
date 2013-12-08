package com.ssc.mycarassistant;

/** 经纬度坐标类型 */
public enum COODINATIONTYPE {
	CT_GPS("GPS"),CT_GD("GAO DE"), CT_BD("BAI DU");
	
	public String toName(){
		return type;
	}
	
	private COODINATIONTYPE(String type){
		this.type = type;
	}
	private String type;
}
