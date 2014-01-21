package com.ssc.mycarassistant;

import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import com.ssc.mycarassistant.db.CarAssistant;
import com.ssc.mycarassistant.db.CarAssistant.Fuels;
import com.ssc.mycarassistant.db.CarAssistant.ToFuelRecords;
import com.ssc.mycarassistant.db.CarAssistant.ToFuelStations;
import com.ssc.mycarassistant.db.CarAssistant.VehicleInfos;
import com.ssc.mycarassistant.db.DbHelper;
import com.ssc.mycarassistant.db.CarAssistant.FuelClasses;

import android.R.integer;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class CarAssistantProvider extends ContentProvider {
	
	//与内容uri匹配的整数常量
	private static final int FUELCLASSES = 1;
	private static final int FUELCLASS_ITEM = 2;
	private static final int FUELS = 3;
	private static final int FUEL_ITEM = 4;
	private static final int CARS = 5;
	private static final int CAR_ITEM = 6;
	private static final int STATIONS = 7;
	private static final int STATION_ITEM = 8;
	private static final int TOFUELS = 9;
	private static final int TOFUEL_ITEM = 10;			//单条加油记录
	private static final int TOFUEL_CARS = 11;			//针对某辆车的所有加油记录
	private static final int TOFUEL_CARS_YEAR = 12;		//针对某辆车某年的所有加油记录
	private static final int TOFUEL_CAR_INITITEM = 13;	//加油记录初始条目
	
	private static final UriMatcher mUriMatcher;
	private DbHelper mDbHelper;
	
	static{
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(CarAssistant.AUTHORITY, FuelClasses.TABLE, FUELCLASSES);
		mUriMatcher.addURI(CarAssistant.AUTHORITY, FuelClasses.TABLE+"/#", FUELCLASS_ITEM);
		mUriMatcher.addURI(CarAssistant.AUTHORITY, Fuels.TABLE, FUELS);
		mUriMatcher.addURI(CarAssistant.AUTHORITY, Fuels.TABLE+"/#", FUEL_ITEM);
		mUriMatcher.addURI(CarAssistant.AUTHORITY, VehicleInfos.TABLE, CARS);
		mUriMatcher.addURI(CarAssistant.AUTHORITY, VehicleInfos.TABLE+"/#", CAR_ITEM);
		mUriMatcher.addURI(CarAssistant.AUTHORITY, ToFuelStations.TABLE, STATIONS);
		mUriMatcher.addURI(CarAssistant.AUTHORITY, ToFuelStations.TABLE+"/#", STATION_ITEM);
		mUriMatcher.addURI(CarAssistant.AUTHORITY, ToFuelRecords.TABLE, TOFUELS);
		mUriMatcher.addURI(CarAssistant.AUTHORITY, ToFuelRecords.TABLE+"/#", TOFUEL_ITEM);
		mUriMatcher.addURI(CarAssistant.AUTHORITY, ToFuelRecords.TABLE+"/CARS/#", TOFUEL_CARS);
		mUriMatcher.addURI(CarAssistant.AUTHORITY, ToFuelRecords.TABLE+"/CAR_YEARS/#/#", TOFUEL_CARS_YEAR);
		mUriMatcher.addURI(CarAssistant.AUTHORITY, ToFuelRecords.TABLE+"/CAR_INITITEM/#", TOFUEL_CAR_INITITEM);
	}
	
	public CarAssistantProvider() {
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int affected = 0;
		int match = mUriMatcher.match(uri);
		long id = ContentUris.parseId(uri);
		switch (match) {
		case TOFUEL_ITEM:
			affected = mDbHelper.deleteToFuelRecord(id);
			break;
		case TOFUEL_CARS:
			affected = mDbHelper.deleteToFuelRecords((int)id);
			break;
		case CAR_ITEM:
			affected = mDbHelper.deleteVehicle(id);
			break;
		case FUEL_ITEM:
			affected = mDbHelper.deleteFuel(id);
			break;
		default:
			break;
		}
		return affected;
	}

	@Override
	public String getType(Uri uri) {
		// TODO: Implement this to handle requests for the MIME type of the data
		// at the given URI.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Uri insertedUri = null;
		long id;
//		long date;
//		int fuelId,stationId;
//		float money,mileage,price,amount,dial;
		int match = mUriMatcher.match(uri);
		switch (match) {
		case TOFUELS:			
			//if(!values.containsKey(ToFuelRecords.DATE)){
			//	date = Calendar.getInstance().getTimeInMillis();
			//}
			//其他字段的值是否存在的检测忽略
			id = mDbHelper.insertToFuelRecord(values);
			insertedUri = ContentUris.withAppendedId(uri, id);			
			break;
		case STATIONS:
			id = mDbHelper.insertStation(values);
			insertedUri = ContentUris.withAppendedId(uri, id);
		case CARS:
			id = mDbHelper.insertVehicle(values);
			insertedUri = ContentUris.withAppendedId(uri, id);
			break;
		case FUELS:
			id = mDbHelper.insertFuel(values);
			insertedUri = ContentUris.withAppendedId(uri, id);
		case FUELCLASSES:
			id = mDbHelper.insertFuelClass(values);
			insertedUri = ContentUris.withAppendedId(uri, id);
		default:
			break;
		}
		return insertedUri;		
	}

	@Override
	public boolean onCreate() {
		if(mDbHelper == null)
			mDbHelper = new DbHelper(getContext());
		
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		String tableName = null;
		String innerSelection = null;
        String[] innerSelectionArgs = new String[]{};
        String sortorder = sortOrder;
        
        List<String> pathSegments = uri.getPathSegments();
        
        boolean isToFuelRecs = false;
		switch (mUriMatcher.match(uri)) {
			case FUELCLASSES:
				tableName = FuelClasses.TABLE;
				break;
			case FUELCLASS_ITEM:
				tableName = FuelClasses.TABLE;
				innerSelection = FuelClasses._ID + " = ? ";
				break;
			case FUELS:
				tableName = Fuels.TABLE;
				break;
			case FUEL_ITEM:
				tableName = Fuels.TABLE;
				innerSelection = Fuels._ID + " = ? ";
				break;
			case CARS:
				tableName = VehicleInfos.TABLE;
				break;
			case CAR_ITEM:
				tableName = VehicleInfos.TABLE;
				innerSelection = VehicleInfos._ID + " = ? ";
				break;
			case STATIONS:
				tableName = ToFuelStations.TABLE;
				break;
			case STATION_ITEM:
				tableName = ToFuelStations.TABLE;
				innerSelection = ToFuelStations._ID + " = ? ";
				innerSelectionArgs = new String[]{pathSegments.get(1)};
				break;
			case TOFUELS:
				tableName = ToFuelRecords.TABLE;
				break;
			case TOFUEL_ITEM:
				tableName = ToFuelRecords.TABLE;
				innerSelection = ToFuelRecords._ID + " = ? ";
				innerSelectionArgs = new String[]{pathSegments.get(1)};
				break;
			case TOFUEL_CARS:
				isToFuelRecs = true;
				tableName = ToFuelRecords.TABLE;
				innerSelection = ToFuelRecords.VEHICLE + " = ? and " + ToFuelRecords.MONEY + " != 0";
				innerSelectionArgs = new String[]{pathSegments.get(2)};
				break;
			case TOFUEL_CARS_YEAR:
				isToFuelRecs = true;
				tableName = ToFuelRecords.TABLE;
				innerSelection = ToFuelRecords.VEHICLE + " = ? and "+ ToFuelRecords.MONEY + " != 0" + " and (" + 
						ToFuelRecords.DATE + " >= ? and " + ToFuelRecords.DATE + " <= ?)";
				Calendar calendar = Calendar.getInstance();
				int year = Integer.parseInt(pathSegments.get(3));
				calendar.set(year, 0,1);
				long st = calendar.getTime().getTime();
				calendar.set(year,11,31,23,59);
				long et = calendar.getTime().getTime();
				innerSelectionArgs = new String[]{pathSegments.get(2),Long.toString(st),Long.toString(et)};
				break;
			default:
				//throw new UnsupportedOperationException("Not yet implemented");
				return null;
		}
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(tableName);
		if( selection == null )
           selection = innerSelection;
        else if(innerSelection != null)
           selection = "( "+ innerSelection + " ) and " + selection;
        LinkedList<String> allArgs = new LinkedList<String>();
        if( selectionArgs == null )
           allArgs.addAll(Arrays.asList(innerSelectionArgs));
        else
        {
           allArgs.addAll(Arrays.asList(innerSelectionArgs));
           allArgs.addAll(Arrays.asList(selectionArgs));
        }
        selectionArgs = allArgs.toArray(innerSelectionArgs);
        
        //确保加油记录的返回顺序以日期的倒序形式，即最近的加油记录处于最前
        if(isToFuelRecs){
        	if(sortorder == null)
            	sortorder = ToFuelRecords.DATE + " desc";
            else
            	sortorder  = sortorder + "," + ToFuelRecords.DATE + " desc";
        }        

            
        // Make the query.
        SQLiteDatabase mDb = this.mDbHelper.getWritableDatabase();
        Cursor c = qb.query( mDb, projection, selection, selectionArgs, null, null, sortorder  );
        c.setNotificationUri( getContext().getContentResolver(), uri );
        return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,String[] selectionArgs){
		int match = CarAssistantProvider.mUriMatcher.match( uri );
		int id;
		int updates = 0;
		
		switch (match) {
		case TOFUEL_ITEM:
			id = (int)ContentUris.parseId(uri);
			updates = mDbHelper.updateToFuelRecord(id, values);
			break;
		case TOFUEL_CAR_INITITEM:
			id = (int)ContentUris.parseId(uri);
			updates = mDbHelper.updateInitRecord(id, values);
			break;
		case STATION_ITEM:
			id = (int)ContentUris.parseId(uri);
			updates = mDbHelper.updateStation(id, values);
			break;
		case CAR_ITEM:
			id = (int)ContentUris.parseId(uri);
			updates = mDbHelper.updateVehicle(id, values);
			break;
		default:
			break;
		}
		
		return updates;
	}
}
