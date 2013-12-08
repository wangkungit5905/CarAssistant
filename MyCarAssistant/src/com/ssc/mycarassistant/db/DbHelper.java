package com.ssc.mycarassistant.db;

import java.util.Date;

import com.ssc.mycarassistant.COODINATIONTYPE;
import com.ssc.mycarassistant.db.CarAssistant.FuelClassColumns;
import com.ssc.mycarassistant.db.CarAssistant.FuelClasses;
import com.ssc.mycarassistant.db.CarAssistant.FuelColumns;
import com.ssc.mycarassistant.db.CarAssistant.Fuels;
import com.ssc.mycarassistant.db.CarAssistant.ToFuelRecordColumns;
import com.ssc.mycarassistant.db.CarAssistant.ToFuelRecords;
import com.ssc.mycarassistant.db.CarAssistant.ToFuelStationColumns;
import com.ssc.mycarassistant.db.CarAssistant.ToFuelStations;
import com.ssc.mycarassistant.db.CarAssistant.VehicleInfoColumns;
import com.ssc.mycarassistant.db.CarAssistant.VehicleInfos;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {

	private Context mContext;
	private final static String TAG = "MCA.DbHelper";
	
	
	public DbHelper(Context context)
	   {
	      super(context, CarAssistant.DATABASE_NAME, null, CarAssistant.DATABASE_VERSION);
	      this.mContext = context;
	   }
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(FuelClasses.CREATE_STATEMENT);
		db.execSQL(Fuels.CREATE_STATEMENT);
		db.execSQL(VehicleInfos.CREATE_STATEMENT);
		db.execSQL(ToFuelStations.CREATE_STATEMENT);
		db.execSQL(ToFuelRecords.CREATE_STATEMENT);
		
		db.execSQL("insert into FuelClasses(name) values('汽油');");
		db.execSQL("insert into FuelClasses(name) values('柴油');");
		
		db.execSQL("insert into Fuels(FuelClass,gradeName) values(1,'93#');");
		db.execSQL("insert into Fuels(FuelClass,gradeName) values(1,'97#');");
		db.execSQL("insert into Fuels(FuelClass,gradeName) values(2,'0#');");
		
		//long id = initFuelTable();
		//initToFuelRecord(id);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int current, int targetVersion) {
		// TODO Auto-generated method stub

	}
	
	/** 初始化油料表（包括油料种类及其标号） */
	private long initFuelTable(){
		Log.d(TAG, "init fuel table!");
		SQLiteDatabase sqldb = getWritableDatabase();
		ContentValues args = new ContentValues();
		
		//初始化油料种类表
		args.put(FuelClassColumns.NAME, "汽油");
		long gaslineId = sqldb.insert(FuelClasses.TABLE, null, args);
		args.clear();
		args.put(FuelClassColumns.NAME, "柴油");
		long dieselId = sqldb.insert(FuelClasses.TABLE,null,args);
		
		//初始化油料表
		args.clear();
		args.put(FuelColumns.CLASS, gaslineId);
		args.put(FuelColumns.GRADE, "93");
		long g93 = sqldb.insert(Fuels.TABLE,null,args);
		args.clear();
		args.put(FuelColumns.CLASS, gaslineId);
		args.put(FuelColumns.GRADE, "97");
		long g97 = sqldb.insert(Fuels.TABLE,null,args);
		args.clear();
		args.put(FuelColumns.CLASS, dieselId);
		args.put(FuelColumns.GRADE, "0#");
		long g0 = sqldb.insert(Fuels.TABLE,null,args);
		args.clear();
		return g93;
	}
	
	/** 初始化一条加油记录，以便显示历史加油记录 */
	private void initToFuelRecord(long gas93Id){
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		//添加一条加油站记录
		values.put(ToFuelStationColumns.NAME, "宁公运孔浦加油站");
		values.put(ToFuelStationColumns.ADDRESS, "宁波市环城北路东段161号");
		values.put(ToFuelStationColumns.LAT, 29.90714);
		values.put(ToFuelStationColumns.LON, 121.59104);
		values.put(ToFuelStationColumns.COORDINATE_CLASS, COODINATIONTYPE.CT_GPS.toName());
		long staticId = db.insert(ToFuelStations.TABLE, null, values);
		//添加一条车辆记录
		//int fuel = 1;	//93号汽油
		values.clear();
		values.put(VehicleInfoColumns.NUMBER, "浙B-138H6");
		values.put(VehicleInfoColumns.TOTAL_SCALE, 20);
		values.put(VehicleInfoColumns.BOX_VOLUME, 60);
		values.put(VehicleInfoColumns.USE_FUEL, gas93Id);
		long carId = db.insert(VehicleInfos.TABLE, null, values);
		values.clear();
		//添加一条加油记录
		values.put(ToFuelRecordColumns.VEHICLE, carId);
		long currentTime = new Date().getTime();
		values.put(ToFuelRecordColumns.DATE, currentTime);
		values.put(ToFuelRecordColumns.FUEL, gas93Id);
		values.put(ToFuelRecordColumns.MILEAGE, 7);
		values.put(ToFuelRecordColumns.FUEL_DIAL, 4);
		values.put(ToFuelRecordColumns.MILEAGE, 100);
		values.put(ToFuelRecordColumns.AMOUNT, 13.51);
		values.put(ToFuelRecordColumns.PRICE, 7.4);
		long id = db.insert(ToFuelRecords.TABLE, null, values);
		values.clear();
	}

}
