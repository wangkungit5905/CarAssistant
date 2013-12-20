package com.ssc.mycarassistant;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.ssc.mycarassistant.R.id;
import com.ssc.mycarassistant.db.CarAssistant.ToFuelRecords;		
import com.ssc.mycarassistant.model.Fuel;
import com.ssc.mycarassistant.model.FuelStation;

import android.net.Uri;
import android.os.Bundle;
import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ToFuelRecordEditor extends Activity implements DatePickerDialog.OnDateSetListener {
	
	//测试git 代码更新到远程仓库是否正确
	
	//public static final String TF_KEY_ROWID			= "rowId";
	public static final String TF_KEY_DATE 			= "date";
	public static final String TF_KEY_MONEY 		= "money";
	public static final String TF_KEY_MILEAGE 		= "mileage";
	public static final String TF_KEY_FUELDIAL 		= "fuelDial";
	public static final String TF_KEY_FUELAMOUNT 	= "fuelAmount";
	public static final String TF_KEY_FUEL 			= "fuel";
	public static final String TF_KEY_PRICE 		= "price";
	public static final String TF_KEY_STATION 		= "station";

	private long mRowId;
	private boolean editMode;
	private long mDate;
	private int mStationId,mFuelId;
	private float mMoney,mFuelDial,mFuelAmount,mPrice,mMileage;
	
	TextView mDateView,mStationView,mFuelView;
	EditText mMoneyEdit,mMileageEdit,mAmountEdit,mDialEdit,mPriceEdit;
	//ListView mStationView;
	Button mBtnOk,mBtnCancel;
	
	public class FuelTypeListener implements OnClickListener{
		Fuel[]  fuels;
		int pos;		//找到当前加油记录所对应的燃料的索引
		ArrayAdapter<Fuel> adapter;
		
		public FuelTypeListener(){
			//this.parent=parent;			
			pos = -1;
			Object[] items = ToFuelMgr.mFuels.values().toArray();
			fuels = new Fuel[items.length];				
			for(int i = 0; i < items.length; ++i)
				fuels[i] = (Fuel)items[i];
			adapter = new ArrayAdapter<Fuel>(ToFuelRecordEditor.this, android.R.layout.select_dialog_singlechoice,fuels);
		}
		
		@Override
		public void onClick(View v) {						
			for(int i = 0; i < fuels.length; ++i){
				if(fuels[i].ID() == mFuelId){
					pos = i;  
					break;
				}
			}		
			AlertDialog.Builder builder = new AlertDialog.Builder(ToFuelRecordEditor.this)
			.setTitle("请选择加注的燃料")
			.setSingleChoiceItems(adapter, pos, new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(which != -1 && which != pos){
						mFuelId = fuels[which].ID();
						mFuelView.setText(fuels[which].toString());
					}
					dialog.dismiss();
				}
			});
			builder.create().show();
		}
	}
	
	public class StationListener implements OnClickListener{
		FuelStation[] stations;
		int pos;
		ArrayAdapter<FuelStation> adapter;
		
		public StationListener(){
			pos = -1;
			Object[] items = ToFuelMgr.mStations.values().toArray();
			stations = new FuelStation[items.length];
			for(int i = 0; i < items.length; ++i)
				stations[i] = (FuelStation)items[i]; 
			adapter = new ArrayAdapter<FuelStation>(ToFuelRecordEditor.this, android.R.layout.select_dialog_singlechoice,stations);
		}
		
		@Override
		public void onClick(View v) {
			for(int i = 0; i < stations.length; ++i){
				if(stations[i].ID() == mStationId){
					pos = i;
					break;
				}
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(ToFuelRecordEditor.this)
			.setTitle("请选择加注的燃料")
			.setSingleChoiceItems(adapter, pos, new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(which != -1 && which != pos){
						mStationId = stations[which].ID();
						mStationView.setText(stations[which].toString());
					}
					dialog.dismiss();
				}
			});
			builder.create().show();
		}
		
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		mRowId = intent.getLongExtra(ToFuelMgr.TF_BUNDLE_KEY_ROWID, 0);
		editMode = intent.getBooleanExtra(ToFuelMgr.TF_BUNDLE_KEY_EDITMODE, false);
		setContentView(R.layout.tofuel_editor);
		mDateView = (TextView)findViewById(R.id.tf_editor_date);
		mMoneyEdit = (EditText)findViewById(R.id.tf_editor_money);
        mMileageEdit = (EditText)findViewById(R.id.tf_editor_mileage);
        mAmountEdit = (EditText)findViewById(R.id.tf_editor_fuelAmount);
        mDialEdit = (EditText)findViewById(R.id.tf_editor_fuelDial);
        mPriceEdit = (EditText)findViewById(R.id.tf_editor_price);
        mStationView = (TextView)findViewById(R.id.tf_editor_station);
        mFuelView = (TextView)findViewById(R.id.tf_editor_fuel);
        mBtnOk = (Button)findViewById(R.id.tf_editor_bnt_ok);
        mBtnCancel = (Button)findViewById(R.id.tf_editor_btn_cancel);
        
        
		
        
		if(editMode){			
			mDateView.setOnClickListener(
					new OnClickListener() {						
						@Override
						public void onClick(View v) {
							Calendar calendar = Calendar.getInstance();
							calendar.setTimeInMillis(mDate);
							int year = calendar.get(calendar.YEAR);
							int month = calendar.get(Calendar.MONTH);
							int day = calendar.get(Calendar.DAY_OF_MONTH);
							DatePickerDialog dlg = new DatePickerDialog(ToFuelRecordEditor.this,ToFuelRecordEditor.this,year,month,day);
							dlg.show();
						}
					});
			mFuelView.setOnClickListener(new FuelTypeListener());		
			mStationView.setOnClickListener(new StationListener());		
	        mBtnOk.setOnClickListener(
	        		new OnClickListener() {						
						@Override
						public void onClick(View v) {
							saveRecord();		
							setResult(RESULT_OK);
							finish();
						}
					});
	        mBtnCancel.setOnClickListener(
	        		new OnClickListener() {						
						@Override
						public void onClick(View v) {
							setResult(RESULT_CANCELED);	
							finish();
						}
					});
		}
//		else {
//					
//		}
		fillDatas();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.to_fuel_record_editor, menu);
		return true;
	}
	
	/** 读取单个加油记录 */
	private boolean readToFuelRec(){
		if(mRowId == 0){
			mDate = Calendar.getInstance().getTimeInMillis();	//默认当前日期
			mMoney = 0;
			mPrice = 0;
			mFuelAmount = 0;
			mFuelDial = 0;
			mMileage = 0;
			mFuelId = 1;		//这两个的初始值要取决于当前选择的车辆默认使用的燃料
			mStationId = 1;		//
			return true;
		}
		Uri uri = ContentUris.withAppendedId(ToFuelRecords.CONTENT_URI, mRowId);
		ContentResolver resolver = getContentResolver();  		
		Cursor cursor = resolver.query(uri, null, null, null, null);
    	if(cursor == null)    		
    		return false;    	
    	if(cursor.getCount() == 0)
    		return false;
    	cursor.moveToFirst();
		mDate = cursor.getLong(cursor.getColumnIndex(ToFuelRecords.DATE));
		mMoney = cursor.getFloat(cursor.getColumnIndex(ToFuelRecords.MONEY));
		mMileage = cursor.getFloat(cursor.getColumnIndex(ToFuelRecords.MILEAGE));
		mFuelDial = cursor.getFloat(cursor.getColumnIndex(ToFuelRecords.FUEL_DIAL));
		mPrice = cursor.getFloat(cursor.getColumnIndex(ToFuelRecords.PRICE));
		mFuelId = cursor.getInt(cursor.getColumnIndex(ToFuelRecords.FUEL));
		mFuelAmount = cursor.getFloat(cursor.getColumnIndex(ToFuelRecords.AMOUNT));
		mStationId = cursor.getInt(cursor.getColumnIndex(ToFuelRecords.STATION)); 
		return true;
	}
	
	/** 填充加油记录数据 */
	private void fillDatas(){
		if(!readToFuelRec()){
			Toast.makeText(this, R.string.error_info_record_not_exist, Toast.LENGTH_SHORT);
			return;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(mDate);
		SimpleDateFormat TOFUELREC_DATE_FORMATER = new SimpleDateFormat("M-d");
		
		//if(editMode){
			mDateView.setText(TOFUELREC_DATE_FORMATER.format(calendar.getTime()));
	        mMoneyEdit.setText(Float.toString(mMoney));
	        mMileageEdit.setText(Float.toString(mMileage));
	        mAmountEdit.setText(Float.toString(mFuelAmount));
	        mDialEdit.setText(Float.toString(mFuelDial));
	        mPriceEdit.setText(Float.toString(mPrice));
	        mFuelView.setText(ToFuelMgr.mFuels.get(mFuelId).toString());
	        mStationView.setText(ToFuelMgr.mStations.get(mStationId).toString());
		//}
		//else {
//			TextView tv = (TextView)findViewById(R.id.tf_view_date);			
//			tv.setText(TOFUELREC_DATE_FORMATER.format(calendar.getTime()));
//			tv = (TextView)findViewById(R.id.tf_view_money);
//			tv.setText(Float.toString(mMoney));
//			tv = (TextView)findViewById(R.id.tf_view_fuelAmount);
//			tv.setText(Float.toString(mFuelAmount));
//			tv = (TextView)findViewById(R.id.tf_view_fuelDial);
//			tv.setText(Float.toString(mFuelDial));
//			tv = (TextView)findViewById(R.id.tf_view_price);
//			tv.setText(Float.toString(mPrice));
//			tv = (TextView)findViewById(R.id.tf_view_mileage);
//			tv.setText(Float.toString(mMileage));
//			tv = (TextView)findViewById(R.id.tf_view_fuel);
//			tv.setText(ToFuelMgr.mFuels.get(mFuelId).toString());
//			tv = (TextView)findViewById(R.id.tf_view_station);
//			tv.setText(ToFuelMgr.mStations.get(mStationId).name());
//		}
	}
	
	private void saveRecord(){
		
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {		
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, monthOfYear, dayOfMonth);
		mDate = calendar.getTimeInMillis();
		SimpleDateFormat TOFUELREC_DATE_FORMATER = new SimpleDateFormat("M-d");
		mDateView.setText(TOFUELREC_DATE_FORMATER.format(calendar.getTime()));
	}

}
