package com.ssc.mycarassistant;

import java.util.Arrays;
import java.util.LinkedList;

import com.ssc.mycarassistant.db.CarAssistant;
import com.ssc.mycarassistant.db.CarAssistant.Fuels;
import com.ssc.mycarassistant.db.CarAssistant.ToFuelRecords;
import com.ssc.mycarassistant.db.CarAssistant.ToFuelStations;
import com.ssc.mycarassistant.db.CarAssistant.VehicleInfos;
import com.ssc.mycarassistant.db.DbHelper;
import com.ssc.mycarassistant.db.CarAssistant.FuelClasses;

import android.content.ContentProvider;
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
	private static final int TOFUEL_ITEM = 10;
	
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
	}
	
	public CarAssistantProvider() {
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// Implement this to handle requests to delete one or more rows.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public String getType(Uri uri) {
		// TODO: Implement this to handle requests for the MIME type of the data
		// at the given URI.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO: Implement this to handle requests to insert a new row.
		throw new UnsupportedOperationException("Not yet implemented");
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
				innerSelection = FuelClasses._ID + " = ? ";
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
        
        // Make the query.
        SQLiteDatabase mDb = this.mDbHelper.getWritableDatabase();
        Cursor c = qb.query( mDb, projection, selection, selectionArgs, null, null, sortorder  );
        c.setNotificationUri( getContext().getContentResolver(), uri );
        return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO: Implement this to handle requests to update one or more rows.
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
