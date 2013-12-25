package com.ssc.mycarassistant;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.text.SimpleDateFormat;

import javax.crypto.NullCipher;

import com.ssc.mycarassistant.db.CarAssistant.FuelClasses;
import com.ssc.mycarassistant.db.CarAssistant.Fuels;
import com.ssc.mycarassistant.db.CarAssistant.ToFuelRecordColumns;
import com.ssc.mycarassistant.db.CarAssistant.ToFuelRecords;
import com.ssc.mycarassistant.db.CarAssistant.ToFuelStationColumns;
import com.ssc.mycarassistant.db.CarAssistant.ToFuelStations;
import com.ssc.mycarassistant.db.CarAssistant.VehicleInfoColumns;
import com.ssc.mycarassistant.db.CarAssistant.VehicleInfos;
import com.ssc.mycarassistant.model.Car;
import com.ssc.mycarassistant.model.Fuel;
import com.ssc.mycarassistant.model.FuelStation;
import com.ssc.mycarassistant.model.ToFuelRecord;

import android.net.Uri;
import android.os.Bundle;
import android.R.integer;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.app.LoaderManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.webkit.DateSorter;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
//android.widget.AdapterView.OnItemSelectedListener

/**
 * 
 * @author wangkun
 * 用来显示某辆车的加油记录
 *
 */
public class ToFuelMgr extends Activity  implements OnItemSelectedListener,
SimpleCursorAdapter.ViewBinder, AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>{

	/** 传递给加油记录数据的键名 */
	public static final String TF_KEY_READONLY		= "readonly";
	//public static final String TF_KEY_ISNEW			= "isNew";
	public static final String TF_KEY_ISCHANGED		= "isChanged";	
	
	
	/** 保存表示加油记录的哪些字段被更新了的数组中对应的索引 */
	public static final String TF_EDIT_TAG_DATE		= "date";
	public static final String TF_EDIT_TAG_MONEY 	= "money";
	public static final String TF_EDIT_TAG_MILEAGE	= "mileage";
	public static final String TF_EDIT_TAG_FUELDIAL	= "fuelDial";
	public static final String TF_EDIT_TAG_FUALAMOUNT= "fuelAmount";
	public static final String TF_EDIT_TAG_FUAL		= "fuel";
	public static final String TF_EDIT_TAG_PRICE	= "price";
	public static final String TF_EDIT_TAG_STATION	= "station";	
	
	public static final String TF_BUNDLE_KEY_ROWID	  = "com.ssc.mycarassistant.tf_record_id";	//要显示或编辑的记录id，对于新记录，则为0
	public static final String TF_BUNDLE_KEY_EDITMODE = "com.ssc.mycarassistant.editMOde";	    //编辑模式（true：编辑，flase：只读显示）
	public static final String TF_BUNDLE_KEY_DEF_CAR = "com.ssc.mycarassistant.def_car";		//默认车辆
	public static final String TF_BUNDLE_KEY_DEF_STATION = "com.ssc.mycarassistant.def_station";//默认加油站id
	
	
	public static final int REQUEST_CODE_EDIT = 1;		//请求编辑操作
	public static final int REQUEST_CODE_NEW  = 2;		//请求新增操作
	
	public static HashMap<Integer,String> mFuelClasses;  //燃料种类
	public static HashMap<Integer,Fuel> mFuels;			//燃料
	public static HashMap<Integer,FuelStation> mStations;//加油站
	
	private int mStartYear,mEndYear;	//加油记录的起始/终止年份
	private String[] mSpanYear;			//加油记录所跨越的年份	
	private int mYear;
	
	private Spinner mCarNumber,mYearSpin;
	private ListView mToFuelRecList;
	int mScreenOrientation;					//当前屏幕方向（ORIENTATION_LANDSCAPE, ORIENTATION_PORTRAIT）
	public static HashMap<Integer,Car> mCars;
	public Car mCar;		//当前选择的车辆
	//Car[] mCarArray;	
	//ToFuelRecord[] mToFuelRecords;
	private Cursor mCursor;
	private View mTitlePort,mTitleLand;		//标题条
	
	//ToFuelRecordsAdapter adapterRec;
	SimpleCursorAdapter adapterRec;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_to_fuel_mgr);
		mScreenOrientation = getResources().getConfiguration().orientation;
		mCarNumber = (Spinner)findViewById(R.id.carNumbers);
		mYearSpin = (Spinner)findViewById(R.id.toFuel_years);
		mCarNumber.setOnItemSelectedListener(this);
		mYearSpin.setOnItemSelectedListener(this);
		mToFuelRecList = (ListView)findViewById(R.id.toFuelRecords);
		mToFuelRecList.setOnItemClickListener(this);
		//mToFuelRecList.setOnItemLongClickListener(this);
		
		LayoutInflater inflater = LayoutInflater.from(this);
		mTitlePort = inflater.inflate(R.layout.tofue_table_title_port, mToFuelRecList, false);
		mTitleLand = inflater.inflate(R.layout.tofue_table_title_land, mToFuelRecList, false);
		
		if(!init())
			return;
		
		String[] fromFields = getFromFields();
		int[] toViews = getToViews();
		
		//String uriStr = ToFuelRecords.CONTENT_URI_CAR_YEARS.toString();
		//uriStr = uriStr + "/" + Integer.toString(1) + "/" + Integer.toString(2013);
		//Uri uri = Uri.parse(uriStr);
		
		//这里使用了直接在UI线程中读取加油记录并将返回的光标绑定到列表
		//ContentResolver resolver = getContentResolver();  		
		//mCursor = resolver.query(uri, null, null, null, null);
    	//if(mCursor == null){
    	//	Toast.makeText(this,getText(R.string.title_error),Toast.LENGTH_SHORT).show();
    	//	return;
    	//}
    	//else if(mCursor.getCount() == 0)        		
    	//	Toast.makeText(this,"无记录",Toast.LENGTH_SHORT).show(); 
    	
    	registerForContextMenu(mToFuelRecList);
				
		//初始化车辆列表
		//mCarArray = mCars.values().toArray(new Car[mCars.size()]);		
		ArrayAdapter<Car> adapterCar = new ArrayAdapter<Car>(this,android.R.layout.simple_spinner_item,
				mCars.values().toArray(new Car[mCars.size()]));
		//adapterCar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mCarNumber.setAdapter(adapterCar);
		
		mStartYear = 2010;	//这个应该从实际的数据库中读取到
		mEndYear = 2013;
		int count = mEndYear - mStartYear + 1;
		int year = mEndYear;
		mSpanYear = new String[count];
		for(int i=0;i<count;i++,year--)
			mSpanYear[i] = Integer.toString(year);
		ArrayAdapter<String> adapterYear = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,mSpanYear);
		mYearSpin.setAdapter(adapterYear);
		
		//初始化已有加油记录的年份列表
		
		//初始化加油记录列表
		if(mScreenOrientation == Configuration.ORIENTATION_PORTRAIT)
			mToFuelRecList.addHeaderView(mTitlePort);
		else {
			mToFuelRecList.addHeaderView(mTitleLand);
		}
		//adapterRec = new SimpleCursorAdapter(this, R.layout.tofuelrec_listitem, mCursor, fromFields, toViews, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		adapterRec = new SimpleCursorAdapter(this, R.layout.tofuelrec_listitem, null, fromFields, toViews, 0);
		adapterRec.setViewBinder(this);
		mToFuelRecList.setAdapter(adapterRec);
		
		mYear = 2013;
		mCar = mCars.get(1);
		getLoaderManager().initLoader(0, null, this);
		
		//ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>(); 
		//SimpleAdapter mSchedule
	}
	
	@Override
	public void onConfigurationChanged (Configuration newConfig){
		mScreenOrientation = newConfig.orientation;
		//adapterRec.setViewResource(R.layout.tofuelrec_listitem);
		if(mScreenOrientation == Configuration.ORIENTATION_PORTRAIT){
			mToFuelRecList.removeHeaderView(mTitleLand);
			mToFuelRecList.addHeaderView(mTitlePort);
		}
		else{
			mToFuelRecList.removeHeaderView(mTitlePort);
			mToFuelRecList.addHeaderView(mTitleLand);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.tofuelmgr, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.tf_mi_new:
	        	editToFuelRecord(0);
	        	return true;	        
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
//		if(parent == mCarNumber){
//			Car car = mCarArray[pos];
//			if(!readToFuelRecordsForCar(car.ID()))
//				return;
//			adapterRec.notifyDataSetChanged();
//			//显示加油记录内容......
//		}
		if(parent == mCarNumber)
			mCar = (Car)parent.getItemAtPosition(pos);
		if(parent == mYearSpin)
			mYear = Integer.parseInt(mSpanYear[pos]);
		getLoaderManager().restartLoader(0, null, this);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
	
	private boolean init(){	
		if(!readFuelClasses())
			return false;
		if(!readFuels())
			return false;
		if(!readCars())
			return false;
		if(!readStations())
			return false;
		//if(!readToFuelRecords())
		//	return false;
		return true;
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data){
		int i = 0;
		if(requestCode == REQUEST_CODE_EDIT && resultCode == RESULT_OK){			
			i = 0;				
		}
		else if(requestCode == REQUEST_CODE_NEW && resultCode == RESULT_OK){
			i--;
		}
	}
	
	
	 //读取燃料种类 
    private boolean readFuelClasses(){
    	ContentResolver resolver = getContentResolver();  
    	try
        {        	
        	mCursor = resolver.query(FuelClasses.CONTENT_URI, null, null, null, null);
        	if(mCursor == null){
        		Toast.makeText(this,getText(R.string.title_error),Toast.LENGTH_SHORT).show();
        		return false;
        	}
        	else if(mCursor.getCount() == 0){
        		Toast.makeText(this,getText(R.string.error_info_null_fuelclass),Toast.LENGTH_SHORT).show();
        		return false;
        	}
        	else{
        		if(mFuelClasses == null)
        			mFuelClasses = new HashMap<Integer,String>();
        		else
        			mFuelClasses.clear();
        		while(mCursor.moveToNext()){
        			int id = mCursor.getInt(0);
        			String fuelName = mCursor.getString(1);
        			mFuelClasses.put(id, fuelName);
        		}
        	}        	
        }
        finally
        {
           if (mCursor != null)
              mCursor.close();           
        }
        return true;
    }
    
    /** 读取可用燃料 */
    private boolean readFuels(){
    	ContentResolver resolver = getContentResolver();  
    	try
        { 
    		mCursor = resolver.query(Fuels.CONTENT_URI, null, null, null, null);
        	if(mCursor == null){
        		Toast.makeText(this,getText(R.string.title_error),Toast.LENGTH_SHORT).show();
        		return false;
        	}
        	else if(mCursor.getCount() == 0){
        		Toast.makeText(this,getText(R.string.error_info_null_fuel),Toast.LENGTH_SHORT).show();
        		return false;
        	}
        	else{
        		if(mFuels == null)
        			mFuels = new HashMap<Integer,Fuel>();
        		else
        			mFuels.clear();
        		while(mCursor.moveToNext()){
        			int id = mCursor.getInt(0);
        			int fcId = mCursor.getInt(1);
        			String grade = mCursor.getString(2);
        			Fuel fuel = new Fuel(id,fcId,mFuelClasses.get(fcId),grade);
        			mFuels.put(id, fuel);
        		}
        	}
        }
    	finally
        {
           if (mCursor != null)
              mCursor.close();           
        }
    	return true;
    }
	
	/** 读取所有的车辆填充cars数组 */
	private boolean readCars(){		
    	ContentResolver resolver = getContentResolver();  
    	try{
    		mCursor = resolver.query(VehicleInfos.CONTENT_URI, null, null, null, null);
        	if(mCursor == null){
        		Toast.makeText(ToFuelMgr.this,getText(R.string.title_error),Toast.LENGTH_SHORT).show();
        		return false;
        	}
        	else if(mCursor.getCount() == 0){
        		Toast.makeText(ToFuelMgr.this,getText(R.string.error_info_null_vehicle),Toast.LENGTH_SHORT).show();
        		return true;
        	}
        	else{
        		int carNumber = mCursor.getCount();
        		if(mCars == null)
        			mCars = new HashMap<Integer, Car>();
        		else 
        			mCars.clear();
        		int row = 0;
        		while(mCursor.moveToNext()){        			
        			int id = mCursor.getInt(0);
        			String number = mCursor.getString(mCursor.getColumnIndex(VehicleInfoColumns.NUMBER));
        			int fuelId = mCursor.getInt(mCursor.getColumnIndex(VehicleInfoColumns.USE_FUEL));
        			int boxVolume = mCursor.getInt(mCursor.getColumnIndex(VehicleInfoColumns.BOX_VOLUME));
        			int mileage = mCursor.getInt(mCursor.getColumnIndex(VehicleInfoColumns.TOTAL_SCALE));
        			Car car = new Car(id,number,mFuels.get(fuelId),boxVolume,mileage);
        			mCars.put(id, car);
        		}
        	}
    	}
    	finally
        {
           if (mCursor != null)
              mCursor.close();           
        }
    	return true;
    }

	/** 读取加油站 */
	private boolean readStations(){
    	ContentResolver resolver = getContentResolver();  
    	try{
    		mCursor = resolver.query(ToFuelStations.CONTENT_URI, null, null, null, null);
        	if(mCursor == null){
        		Toast.makeText(this,getText(R.string.title_error),Toast.LENGTH_SHORT).show();
        		return false;
        	}
        	else if(mCursor.getCount() == 0)        		
        		return true;        	
        	else{
        		if(mStations == null)
        			mStations = new HashMap<Integer,FuelStation>();
        		else
        			mStations.clear();
        		while(mCursor.moveToNext()){
        			int id = mCursor.getInt(0);
        			String name = mCursor.getString(mCursor.getColumnIndex(ToFuelStationColumns.NAME));
        			String addr = mCursor.getString(mCursor.getColumnIndex(ToFuelStationColumns.ADDRESS));
        			double lat = mCursor.getDouble(mCursor.getColumnIndex(ToFuelStationColumns.LAT));
        			double lon = mCursor.getDouble(mCursor.getColumnIndex(ToFuelStationColumns.LON));
        			//COODINATIONTYPE type;  //对于坐标类型，要看保存到数据库的实际内容来定
        			FuelStation station = new FuelStation(id,name,addr);
        			station.setCoodination(lat, lon);
        			station.setCoodinationType(COODINATIONTYPE.CT_GPS);
        			mStations.put(id, station);
        		}        			
        	}
    	}
    	finally
        {
           if (mCursor != null)
              mCursor.close();           
        }
    	return true;
    }
	
	
	
	
	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		// 将来自光标的数据绑定到视图
		TextView v = (TextView)view;
		switch (columnIndex){
		case 2:		//日期
			SimpleDateFormat TOFUELREC_DATE_FORMATER = new SimpleDateFormat("M-d");
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(cursor.getLong(columnIndex));
			v.setText(TOFUELREC_DATE_FORMATER.format(calendar.getTime()));
			break;
		case 3:		//油品种类
			int fuelId = cursor.getInt(columnIndex);
			v.setText(mFuels.get(fuelId).toString());
			break;
		case 4:		//里程表读数
			double mileage = cursor.getDouble(columnIndex);
			v.setText(Integer.toString(cursor.getInt(columnIndex)));
			break;
		case 5:		//油表读数
		case 6:		//加注金额
		case 7:		//加注油量
		case 8:		//单价
			float value = cursor.getFloat(columnIndex);
			v.setText(Float.toString(value));
			break;
		case 9:		//加油站
			int stationId = cursor.getInt(columnIndex);
			v.setText(mStations.get(stationId).name());
			break;
		}
		return true;
	}
	
	/** 返回由光标适配器使用的来源字段 */
	private String[] getFromFields(){
		String[] from = new String[ToFuelRecords.FEILD_COUNT-1];
		from[0] = ToFuelRecords.DATE; 
		from[1] = ToFuelRecords.MONEY;
		from[2] = ToFuelRecords.AMOUNT;
		from[3] = ToFuelRecords.MILEAGE;
		from[4] = ToFuelRecords.FUEL_DIAL;
		from[5] = ToFuelRecords.PRICE;
		from[6] = ToFuelRecords.FUEL;			//燃料种类
		from[7] = ToFuelRecords.STATION;
		return from;
	}
	
	/** 返回由光标适配器使用的目的视图 */
	private int[] getToViews(){
		int[] to = new int[ToFuelRecords.FEILD_COUNT-1];
		to[0] = R.id.tf_listitem_date;		
		to[1] = R.id.tf_listitem_money;		
		to[2] = R.id.tf_listitem_fuelAmount;		
		to[3] = R.id.tf_listitem_mileage;		
		to[4] = R.id.tf_listitem_fuelDial;		
		to[5] = R.id.tf_listitem_price;	
		to[6] = R.id.tf_listitem_fuelType;
		to[7] = R.id.tf_listitem_station;
		return to;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		//创建加油记录管理的上下文菜单
	    super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.tofuelmgr, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		//处理上下文菜单
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    switch (item.getItemId()) {
	        case R.id.tf_mi_view:
	        	viewToFuelRecord(info.id);
	            return true;
	        case R.id.tf_mi_edit:
	        	editToFuelRecord(info.id);
	            return true;
	        case R.id.tf_mi_delete:
	        	deleteToFuelRecord(info.id);
	        	return true;
	        case R.id.tf_mi_new:
	        	editToFuelRecord(0);
	        	return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
	
	
	
	/** 显示加油记录详情  */
	private void viewToFuelRecord(long id){	
		Intent intent = new Intent(ToFuelMgr.this,ToFuelRecordEditor.class);
		intent.putExtra(TF_BUNDLE_KEY_ROWID, id);
		intent.putExtra(TF_BUNDLE_KEY_EDITMODE, false);
		startActivity(intent);
	}
	
	/** 编辑已有记录 */
	private void editToFuelRecord(long id){
		Intent intent = new Intent(ToFuelMgr.this,ToFuelRecordEditor.class);
		if(id != 0){
			intent.putExtra(TF_BUNDLE_KEY_ROWID, id);
			intent.putExtra(TF_BUNDLE_KEY_EDITMODE, true);
			startActivityForResult(intent, REQUEST_CODE_EDIT);
		}
		else{
			intent.putExtra(TF_BUNDLE_KEY_ROWID, 0);
			intent.putExtra(TF_BUNDLE_KEY_EDITMODE, true);
			intent.putExtra(TF_BUNDLE_KEY_DEF_CAR, mCar.ID());
			intent.putExtra(TF_BUNDLE_KEY_DEF_STATION, mStations.get(1).ID());
			startActivityForResult(intent, REQUEST_CODE_NEW);
		}		
	}
	
//	private void newToFuelRecord(){
//		Intent intent = new Intent(ToFuelMgr.this,ToFuelRecordEditor.class);
//		
//		
//	}
	
	/** 删除记录 */
	void deleteToFuelRecord(long id){
		Uri uri = ToFuelRecords.CONTENT_URI;
		uri = ContentUris.withAppendedId(uri, id);
		ContentResolver resolver = getContentResolver();
		resolver.delete(uri, null, null);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		//单击加油记录，显示详情
		viewToFuelRecord(id);
	}

	
	@Override
	public Loader onCreateLoader(int id, Bundle args) {
		//利用光标装载器来实现加油记录的后台装载
		String uriStr = ToFuelRecords.CONTENT_URI_CAR_YEARS.toString();
		uriStr = uriStr + "/" + Integer.toString(mCar.ID()) + "/" + Integer.toString(mYear);
		Uri uri = Uri.parse(uriStr);
		return new CursorLoader(this,uri,null,null,null,null);
	}

	@Override
	public void onLoadFinished(Loader loader, Cursor cursor) {
		adapterRec.swapCursor(cursor);	
	}

	@Override
	public void onLoaderReset(Loader loader) {
		adapterRec.swapCursor(null);		
	}
		
	
	
	
//	public class ToFuelRecordsAdapter extends BaseAdapter {
//
//		TextView mDate,mFuel,mStation,mMileage,mFuelDial,mPrice,mMoney,mAmount;
//		public final SimpleDateFormat TOFUELREC_DATE_FORMATER = new SimpleDateFormat("yyyy-MM-dd");
//		
//		@Override
//		public int getCount() {
//			if(mToFuelRecords == null)
//				return 0;
//			else
//				return mToFuelRecords.length;
//		}
//
//		@Override
//		public Object getItem(int position) {
//			return mToFuelRecords[position];
//		}
//
//		@Override
//		public long getItemId(int position) {
//			ToFuelRecord record = (ToFuelRecord)mToFuelRecords[position];
//			return record.ID();
//		}
//
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			if(convertView == null) {
//	            LayoutInflater inflater = LayoutInflater.from(ToFuelMgr.this);
//	            convertView = inflater.inflate(R.layout.tofuelrec_listitem, parent, false);
//	            mDate = (TextView)convertView.findViewById(R.id.tf_listitem_date);
//	            mMoney = (TextView)convertView.findViewById(R.id.tf_listitem_money);	            
//	            mAmount = (TextView)convertView.findViewById(R.id.tf_listitem_fuelAmount);
//	            mMileage = (TextView)convertView.findViewById(R.id.tf_listitem_mileage);
//	               
//	            //如果是横屏，则有更多的显示元素可以利用
//	            if(mScreenOrientation == Configuration.ORIENTATION_LANDSCAPE){
//	            	mStation = (TextView)convertView.findViewById(R.id.tf_listitem_station);
//	            	mPrice = (TextView)convertView.findViewById(R.id.tf_listitem_price);
//	            	mFuelDial = (TextView)convertView.findViewById(R.id.tf_listitem_fuelDial);	
//	            	//mFuel = (TextView)convertView.findViewById(R.id.tf_listitem_fuelType); 
//	            }
//	            else{
//	            	mStation = null;
//	            	mPrice = null;
//	            	mFuelDial = null;
//	            	mFuel = null;
//	            }
//	        }
//			ToFuelRecord rec = mToFuelRecords[position];
//			Calendar calendar = Calendar.getInstance();
//			calendar.setTimeInMillis(rec.date());
//			mDate.setText(TOFUELREC_DATE_FORMATER.format(calendar.getTime()));
//			mMoney.setText(Float.toString(rec.money()));
//			mAmount.setText(Float.toString(rec.amount()));
//			mMileage.setText(Integer.toString(rec.mileage()));			
//			if(mScreenOrientation == Configuration.ORIENTATION_LANDSCAPE){
//				mStation.setText(rec.station().name());
//				mPrice.setText(Float.toString(rec.price()));
//				mFuelDial.setText(Float.toString(rec.fuelDial()));
//				//mFuel.setText(rec.Fuel().toString());
//			}
//			return convertView;
//		}		
//	}
	
//	private boolean readToFuelRecords(){
//		
//	}
}

/** 读取加油记录的光标适配器类  */
//public class ToFuelRecordsAdapter extends SimpleCursorAdapter {
//	private Cursor m_cursor;
//	private Context m_context;
//
//	@Override
//	public View newView(Context context, Cursor cursor, ViewGroup parent) {
//		final View view = super.newView(context, cursor, parent);
//		ViewHolder holder = new ViewHolder();
////		holder.titleView = (TextView) view.findViewById(R.id.title);
////		holder.linkView = (TextView) view.findViewById(R.id.Link);
////		holder.descriptionView = (TextView) view.findViewById(R.id.Description);
////		holder.categoryView = (TextView) view.findViewById(R.id.Category);
////		holder.authorView = (TextView) view.findViewById(R.id.Author);
////		holder.pubDateView = (TextView) view.findViewById(R.id.PubDate);
//
//		view.setTag(holder);
//		return view;
//	}
//
//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//		// TODO Auto-generated method stub
//		return super.getView(position, convertView, parent);
//	}
//
//	public ToFuelRecordsAdapter(Context context, int layout, Cursor c,String[] from, int[] to) {
//		super(context, layout, c, from, to, FLAG_REGISTER_CONTENT_OBSERVER);
//		m_cursor = c;
//		m_context = context;
//	}
//
//	@Override
//	public void bindView(View view, Context context, Cursor cursor) {
//		ViewHolder holder = (ViewHolder) view.getTag();
//		setViewText(holder.titleView, cursor.getString(cursor
//				.getColumnIndex(Items.TITLE)));
//		setViewText(holder.linkView, cursor.getString(cursor
//				.getColumnIndex(Items.LINK)));
//		setViewText(holder.descriptionView, cursor.getString(cursor
//				.getColumnIndex(Items.DESCRIPTION)));
//		setViewText(holder.categoryView, cursor.getString(cursor
//				.getColumnIndex(Items.CATEGORY)));
//		setViewText(holder.authorView, cursor.getString(cursor
//				.getColumnIndex(Items.AUTHOR)));
//		setViewText(holder.pubDateView, cursor.getString(cursor
//				.getColumnIndex(Items.PUBDATE)));
//		super.bindView(view, context, cursor);
//	}
//
//	final static class ViewHolder {
//		//仅竖屏
//		public TextView date;
//		public TextView money;
//		public TextView amount;
//		public TextView mileage;
//		//仅横屏
//		public TextView fuelDial;
//		public TextView price;
//		public TextView station;
//		public TextView fuel;
//	}
//}
