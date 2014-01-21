package com.ssc.mycarassistant.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 
 * @author wangkun
 *	所有有关的数据库表格列的定义，以及内容uri的定义
 */
public final class CarAssistant {

	/**  */
	public static final String AUTHORITY = "com.ssc.provider.carassistant";
	
	public static final Uri CONTENT_URI = Uri.parse( "content://" + CarAssistant.AUTHORITY );
	
	static final String DATABASE_NAME = "CarRecs.db";
	
	static final int DATABASE_VERSION = 1;
	
	/**
	 * 
	 */
	public static final class ToFuelRecords extends ToFuelRecordColumns implements BaseColumns{
		 /** 访问单条加油记录的内容MIME类型 */
	      public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.ssc.provider.carassistant.tofuel";
	      /** 访问所有加油记录的内容的MIME */
	      public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.ssc.provider.carassistant.tofuel";
	      /** 访问所有加油记录的内容 uri, content://com.ssc.provider.carassistant/ToFuelRecords */
	      public static final Uri CONTENT_URI = Uri.parse( "content://" + CarAssistant.AUTHORITY + "/" + ToFuelRecords.TABLE + "/");
	      /** 访问指定车辆的所有加油记录的内容uri */
	      public static final Uri CONTENT_URI_CARS = Uri.parse( "content://" + CarAssistant.AUTHORITY + "/" + ToFuelRecords.TABLE + "/CARS/");
	      /** 访问指定车辆的指定年份所有加油记录的内容uri */
	      public static final Uri CONTENT_URI_CAR_YEARS = Uri.parse( "content://" + CarAssistant.AUTHORITY + "/" + ToFuelRecords.TABLE + "/CAR_YEARS/");
	      /**  */
	      public static final Uri CONTENT_URI_CAR_INITITEM_URI = Uri.parse( "content://" + CarAssistant.AUTHORITY + "/" + ToFuelRecords.TABLE + "/CAR_INITITEM/");
	      public static final String DEFAULT_SORT_ORDER = ToFuelRecords.DATE /*+ " DESC"*/;	//默认以时间顺序返回加油记录结果
	      
	      public static final String TABLE = "ToFuelRecords";
	      static final String CREATE_STATEMENT = 
	         "CREATE TABLE " + ToFuelRecords.TABLE + "(" + ToFuelRecords._ID + " " + ToFuelRecords._ID_TYPE + 
	                                          "," + ToFuelRecords.VEHICLE + " " + ToFuelRecords.VEHICLE_TYPE + 
	                                          "," + ToFuelRecords.DATE + " " + ToFuelRecords.DATE_TYPE + 
	                                          "," + ToFuelRecords.FUEL + " " + ToFuelRecords.FUEL_TYPE +
	                                          "," + ToFuelRecords.MILEAGE + " " + ToFuelRecords.MILEAGE_TYPE +
	                                          "," + ToFuelRecords.FUEL_DIAL + " " + ToFuelRecords.FUEL_DIAL_TYPE +
	                                          "," + ToFuelRecords.MONEY + " " + ToFuelRecords.MONEY_TYPE + 
	                                          "," + ToFuelRecords.AMOUNT + " " + ToFuelRecords.AMOUNT_TYPE + 
	                                          "," + ToFuelRecords.PRICE + " " + ToFuelRecords.PRICE_TYPE +
	                                          "," + ToFuelRecords.STATION + " " + ToFuelRecords.STATION_TYPE +
	                                          ");";
	      
	      public static int FEILD_COUNT = 9;	//不包括id字段在内的字段数
	}
	
	public static final class FuelClasses extends FuelClassColumns implements BaseColumns{
		
		/** 访问所有油品种类的uri, content://com.ssc.provider.carassistant/FuelClasses */
	    public static final Uri CONTENT_URI = Uri.parse( "content://" + CarAssistant.AUTHORITY + "/" + FuelClasses.TABLE );
		
		public static final String TABLE = "FuelClasses";
		static final String CREATE_STATEMENT = 
				"CREATE TABLE " + FuelClasses.TABLE + "(" + FuelClasses._ID + " " + FuelClasses._ID_TYPE + 
				"," + FuelClasses.NAME + " " + FuelClasses.NAME_TYPE + 
                ");";
	}
	
	public static final class Fuels extends FuelColumns implements BaseColumns{
		/** 访问所有燃料的uri, content://com.ssc.provider.carassistant/Fuels */
	    public static final Uri CONTENT_URI = Uri.parse( "content://" + CarAssistant.AUTHORITY + "/" + Fuels.TABLE );
		public static final String TABLE = "Fuels";
		static final String CREATE_STATEMENT = 
				"CREATE TABLE " + Fuels.TABLE + "(" + Fuels._ID + " " + Fuels._ID_TYPE + 
				"," + Fuels.CLASS + " " + Fuels.CLASS_TYPE + 
				"," + Fuels.GRADE + " " + Fuels.GRADE_TYPE + 
                ");";
	}
	
	public static class VehicleInfos extends VehicleInfoColumns implements BaseColumns{
		/** 访问所有车辆的uri, content://com.ssc.provider.carassistant/VehicleInfos */
	    public static final Uri CONTENT_URI = Uri.parse( "content://" + CarAssistant.AUTHORITY + "/" + VehicleInfos.TABLE );
		public static final String TABLE = "VehicleInfos";
		static final String CREATE_STATEMENT = 
				"CREATE TABLE " + VehicleInfos.TABLE + "(" + VehicleInfos._ID + " " + VehicleInfos._ID_TYPE + 
				"," + VehicleInfos.NUMBER + " " + VehicleInfos.NUMBER_TYPE + 
				"," + VehicleInfos.TOTAL_SCALE + " " + VehicleInfos.TOTAL_TYPE + 
				"," + VehicleInfos.BOX_VOLUME + " " + VehicleInfos.VOLUME_TYPE + 
				"," + VehicleInfos.USE_FUEL + " " + VehicleInfos.USE_FUEL_TYPE + 
				"," + VehicleInfos.MAINTENANCE_MILEAGE + " " + VehicleInfos.MILEAGE_TYPE + 
				"," + VehicleInfos.MAINTENANCE_MONTH + " " + VehicleInfos.MONTH_TYPE + 
				");";
	}
	
	public static class ToFuelStations extends ToFuelStationColumns implements BaseColumns{
		/** 访问所有加油站的uri, content://com.ssc.provider.carassistant/ToFuelStations */
	    public static final Uri CONTENT_URI = Uri.parse( "content://" + CarAssistant.AUTHORITY + "/" + ToFuelStations.TABLE );
		public static final String TABLE = "ToFuelStations";
		static final String CREATE_STATEMENT = 
				"CREATE TABLE " + ToFuelStations.TABLE + "(" + ToFuelStations._ID + " " + ToFuelStations._ID_TYPE + 
				"," + ToFuelStations.NAME + " " + ToFuelStations.NAME_TYPE + 
				"," + ToFuelStations.ADDRESS + " " + ToFuelStations.ADDRESS_TYPE + 
				"," + ToFuelStations.LAT + " " + ToFuelStations.LAT_TYPE + 
				"," + ToFuelStations.LON + " " + ToFuelStations.LON_TYPE + 
				"," + ToFuelStations.COORDINATE_CLASS + " " + ToFuelStations.COORDINATE_CLASS_TYPE + 
				");";
	}
	
	
	
	/**	燃料种类表 */
	public static class FuelClassColumns{	      
	      public static final String NAME     = "name";				//油料名            
	      static final String NAME_TYPE       = "TEXT NOT NULL";
	      static final String _ID_TYPE        = "INTEGER PRIMARY KEY AUTOINCREMENT";
	   }
	
	/** 燃料表（id，燃料种类id，标号名称）*/
	public static class FuelColumns{
		public static final String  CLASS = "FuelClass";		//油料种类
		static final String CLASS_TYPE    = "INTEGER NOT NULL";
		public static final String GRADE  = "gradeName";		//油料标号名
		static final String GRADE_TYPE	  = "TEXT NOT NULL";
		static final String _ID_TYPE        = "INTEGER PRIMARY KEY AUTOINCREMENT";
	}
	
	/** 车辆信息表 */
	public static class VehicleInfoColumns{
		//车牌号，......油表刻度总格数、邮箱容积
		public static final String  NUMBER 		= "number";				//牌照号
		static final String NUMBER_TYPE 		= "TEXT NOT NULL";		
		public static String TOTAL_SCALE 		= "totalScale";			//油表总刻度
		static final String	TOTAL_TYPE 			= "INTEGER";
		public static final String BOX_VOLUME 	= "boxVolume";			//油箱容积
		static final String VOLUME_TYPE		 	= "INTEGER NOT NULL";
		public static final String USE_FUEL		= "useFuel";			//使用的燃料类型
		static final String USE_FUEL_TYPE		= "INTEGER NOT NULL";
		public static final String MAINTENANCE_MILEAGE		= "maintenanceMileage";			//保养间隔里程
		static final String MILEAGE_TYPE		= "INTEGER NOT NULL";
		public static final String MAINTENANCE_MONTH		= "maintenanceMonth";			//保养间隔月份数
		static final String MONTH_TYPE		= "INTEGER NOT NULL";
		static final String _ID_TYPE        	= "INTEGER PRIMARY KEY AUTOINCREMENT";
	}
	
	/** 加油站表 */
	public static class ToFuelStationColumns{
		public static final String NAME 			= "name";			//加油站名
		static final String NAME_TYPE 				= "TEXT";
		public static final String ADDRESS 			= "address";		//地址
		static final String ADDRESS_TYPE 			= "TEXT";
		public static final String LAT 				= "latitude";		//纬度
		static final String LAT_TYPE 				= "REAL";
		public static final String LON 				= "longitude";		//经度
		static final String LON_TYPE 				= "REAL";
		public static final String COORDINATE_CLASS	= "coordinateType"; //经纬度的坐标类型
		static final String COORDINATE_CLASS_TYPE	= "TEXT NOT NULL";
		static final String _ID_TYPE        = "INTEGER PRIMARY KEY AUTOINCREMENT";
	}
	
	/** 加油记录表 */
	public static class ToFuelRecordColumns{
		public static final String VEHICLE 		= "vehicle";			//车辆id
		static final String VEHICLE_TYPE 		= "INTEGER NOT NULL";
		public static final String DATE 		= "date";				//日期
		static final String DATE_TYPE 			= "INTEGER NOT NULL";
		public static final String FUEL 		= "fuel";				//加注的油品种类
		static final String FUEL_TYPE 			= "INTEGER NOT NULL";
		public static final String MILEAGE 		= "mileage";			//加注时的里程表读数
		static final String MILEAGE_TYPE 		= "INTEGER NOT NULL";
		public static final String FUEL_DIAL 	= "fuelDial";			//加注时油表的读数
		static final String FUEL_DIAL_TYPE 		= "REAL NOT NULL";
		public static final String MONEY 		= "money";				//加注金额
		static final String MONEY_TYPE 			= "REAL NOT NULL";
		public static final String AMOUNT 		= "fuelAmount";			//加注油量
		static final String AMOUNT_TYPE 		= "REAL NOT NULL";
		public static final String PRICE		= "price";				//单价
		static final String PRICE_TYPE			= "REAL NOT NULL";
		public static final String STATION		= "station";			//加油站
		static final String STATION_TYPE		= "INTEGER";
		static final String _ID_TYPE        = "INTEGER PRIMARY KEY AUTOINCREMENT";
	}
	
	
}
