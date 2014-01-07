package com.ssc.mycarassistant;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.ssc.mycarassistant.R.id;
import com.ssc.mycarassistant.db.CarAssistant.ToFuelRecords;		
import com.ssc.mycarassistant.db.CarAssistant.ToFuelStations;
import com.ssc.mycarassistant.model.Car;
import com.ssc.mycarassistant.model.Fuel;
import com.ssc.mycarassistant.model.FuelStation;
import com.ssc.mycarassistant.model.ToFuelRecord;

import android.net.Uri;
import android.os.Bundle;
import android.R.bool;
import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class ToFuelRecordEditor extends Activity implements DatePickerDialog.OnDateSetListener,
StationChoiceFragment.OnSelectStationListener{
	
	//第三次测试pull操作
	
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
	private long mDate,mDateOld;
	private int mStationId,mFuelId,mStationIdOld,mFuelIdOld,mMileage;
	private float mMoney,mFuelDial,mFuelAmount,mPrice;
	private Car mDefCar;
	private FuelStation mDefStation;
	//private Intent mResultIntent;	//记录编辑的状态（加油记录的哪些部分被修改了）
	private FieldAutoCal mAutoCalListener;	//
	
	TextView mDateView,mStationView,mFuelView;
	EditText mMoneyEdit,mMileageEdit,mAmountEdit,mDialEdit,mPriceEdit;
	//ListView mStationView;
	Button mBtnOk,mBtnCancel,mBtnNew;
	
	private StationChoiceFragment mStationFragment = null;
	
	private SimpleDateFormat TOFUELREC_DATE_FORMATER;	
	
	/** 这个类用来当用户点按燃料字段时，给用户选择所加注的燃料 */
	public class FuelTypeListener implements OnClickListener{
		Fuel[]  fuels;
		int pos;		//找到当前加油记录所对应的燃料的索引
		ArrayAdapter<Fuel> adapter;
		
		public FuelTypeListener(){
			//this.parent=parent;			
			pos = -1;
			Object[] items = MainActivity.mFuels.values().toArray();
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
	
	/** 这个类用来实现当用户点按加油站字段时，弹出一个窗口给用户选择 */
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
			.setTitle("请选择加油站")
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
	
	/** 这个类用来实现当用户点按加油站字段时，在当前视图的下部显示一个选择加油站的视图分段 */
	public class StationListener2 implements OnClickListener{
		private Activity parent;
		
		public StationListener2(Activity parent){
			this.parent = parent;
		}

		@Override
		public void onClick(View arg0) {
			mBtnNew.setVisibility(View.VISIBLE);
			FragmentManager fragmentManager = parent.getFragmentManager();
			FragmentTransaction transaction = fragmentManager.beginTransaction();
			if(mStationFragment == null){
				//FuelStation[] stations = (FuelStation[]) ToFuelMgr.mStations.values().toArray();
				mStationFragment = StationChoiceFragment.getInstance();
			}
			transaction.add(R.id.tofuel_editor_selector, mStationFragment);
			transaction.commit();
			mStationFragment.setStationId(mStationId);
			//mStationFragment.setChoicedItem(mStationId);
		}
		
	}
	
	/**
	 * 
	 * @author wangkun
	 *	这个类用来当输入金额和单价后字段计算加注油量并填充
	 */
	public class FieldAutoCal implements OnEditorActionListener{

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if(v == mMoneyEdit){
				inspect();
			}
			else if(v == mPriceEdit){
				inspect();
			}
			return false;
		}	
		
		private boolean inspect(){
			float price,money;
			if(mPriceEdit.getText().toString().isEmpty() || 
					mMoneyEdit.getText().toString().isEmpty())
				return false;
			else{
				price = Float.parseFloat(mPriceEdit.getText().toString());
				money = Float.parseFloat(mMoneyEdit.getText().toString());
			}
			if(price != 0){
				float amount = money / price;
				mAmountEdit.setText(String.format("%.2f", amount));
			}
			return true;
		}
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		TOFUELREC_DATE_FORMATER = new SimpleDateFormat("yyyy-M-d");	
		mRowId = intent.getLongExtra(ToFuelMgr.TF_BUNDLE_KEY_ROWID, 0);
		if(mRowId == 0){
			int id = intent.getIntExtra(ToFuelMgr.TF_BUNDLE_KEY_DEF_CAR, 1);
			mDefCar = MainActivity.mCars.get(id);
			id = intent.getIntExtra(ToFuelMgr.TF_BUNDLE_KEY_DEF_STATION, 1);
			mDefStation = ToFuelMgr.mStations.get(id);
		}
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
        mBtnNew = (Button)findViewById(R.id.tf_editor_btn_new_station);
        mBtnNew.setVisibility(View.INVISIBLE);
        
        
		if(editMode){	
			mAutoCalListener = new FieldAutoCal();
			mMoneyEdit.setOnEditorActionListener(mAutoCalListener);
			mPriceEdit.setOnEditorActionListener(mAutoCalListener);
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
			//mStationView.setOnClickListener(new StationListener());		
			mStationView.setOnClickListener(new StationListener2(this));
	        mBtnOk.setOnClickListener(
	        		new OnClickListener() {						
						@Override
						public void onClick(View v) {
							if(saveRecord()){
								setResult(RESULT_OK);
								finish();
							}							
						}
					});
	        mBtnCancel.setOnClickListener(
	        		new OnClickListener() {						
						@Override
						public void onClick(View v) {
							setResult(RESULT_CANCELED);	
							testContentObserver();
							finish();
						}
					});
	        mBtnNew.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(mStationFragment == null)
						return;
					mStationFragment.swithNewStationView();
				}
			});
		}
		fillDatas();		
	}
	
	//一个测试函数，通过改变id为1的加油站的名称，来测试内容观察者是否会得到通知
	void testContentObserver(){
		Uri uri = ToFuelStations.CONTENT_URI;
		//FuelStation station = ToFuelMgr.mStations.get(1);
		uri = ContentUris.withAppendedId(uri, 1);
		ContentValues values = new ContentValues();
		values.put(ToFuelStations.NAME, "测试加油站的名称修改");
		ContentResolver resolver = getContentResolver();
		resolver.update(uri, values, null, null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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
			mFuelId = mDefCar.usedFuel().ID();		//这两个的初始值要取决于当前选择的车辆默认使用的燃料
			mStationId = mDefStation.ID();		//
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
		mDateOld = mDate;
		mMoney = cursor.getFloat(cursor.getColumnIndex(ToFuelRecords.MONEY));
		mMileage = cursor.getInt(cursor.getColumnIndex(ToFuelRecords.MILEAGE));
		mFuelDial = cursor.getFloat(cursor.getColumnIndex(ToFuelRecords.FUEL_DIAL));
		mPrice = cursor.getFloat(cursor.getColumnIndex(ToFuelRecords.PRICE));
		mFuelId = cursor.getInt(cursor.getColumnIndex(ToFuelRecords.FUEL));
		mFuelIdOld = mFuelId;
		mFuelAmount = cursor.getFloat(cursor.getColumnIndex(ToFuelRecords.AMOUNT));
		mStationId = cursor.getInt(cursor.getColumnIndex(ToFuelRecords.STATION)); 
		mStationIdOld = mStationId;
		return true;
	}
	
	/** 填充加油记录数据 */
	private void fillDatas(){
		if(!readToFuelRec()){
			Toast.makeText(this, R.string.error_info_record_not_exist, Toast.LENGTH_SHORT).show();			
			return;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(mDate);
		mDateView.setText(TOFUELREC_DATE_FORMATER.format(calendar.getTime()));
		if(mMoney != 0)
			mMoneyEdit.setText(String.format("%.2f", mMoney));
		if(mMileage != 0)
			mMileageEdit.setText(String.format("%d", mMileage));
		if(mFuelAmount != 0)
			mAmountEdit.setText(String.format("%.2f", mFuelAmount));
		if(mFuelDial != 0)
			mDialEdit.setText(String.format("%.2f", mFuelDial));
		if(mPrice != 0)
			mPriceEdit.setText(String.format("%.2f", mPrice));
        mFuelView.setText(MainActivity.mFuels.get(mFuelId).toString());
        mStationView.setText(ToFuelMgr.mStations.get(mStationId).toString());
	}
	
	/** 验证并保存记录 */
	private  boolean saveRecord(){		
		if(mMoneyEdit.getText().toString().isEmpty() ||
				mPriceEdit.getText().toString().isEmpty() ||
				mAmountEdit.getText().toString().isEmpty()) {
			Toast.makeText(this, "多个必填字段未设置", Toast.LENGTH_SHORT).show();
			return false;
		}
		String errInfo = "";
		float money = Float.parseFloat(mMoneyEdit.getText().toString());
		if(money == 0)
			errInfo = "金额 ";
		float price = Float.parseFloat(mPriceEdit.getText().toString());
		if(price == 0)
			errInfo = errInfo + "单价 ";
		float amount = Float.parseFloat(mAmountEdit.getText().toString());
		if(amount == 0)
			errInfo = errInfo + "油量";
		if(!errInfo.isEmpty()){
			Toast.makeText(this, String.format("数据不全（%s）", errInfo), Toast.LENGTH_SHORT).show();
			return false;
		}
		float dial = Float.parseFloat(mDialEdit.getText().toString());
		int mileage = Integer.parseInt(mMileageEdit.getText().toString());
		
		boolean editTag = false;
		ContentResolver resolver;
		ContentValues values = new ContentValues();
		
		
		if(mRowId != 0){
			if(mDate != mDateOld){
				editTag = true;
				values.put(ToFuelRecords.DATE, mDate);
			}
			if(mFuelId != mFuelId){
				editTag = true;
				values.put(ToFuelRecords.FUEL, mFuelId);
			}
			if(mStationId != mStationIdOld){
				editTag = true;
				values.put(ToFuelRecords.STATION, mStationId);
			}
			if(money != mMoney){
				editTag = true;
				values.put(ToFuelRecords.MONEY, money);
			}
			if(amount != mFuelAmount){
				editTag = true;
				values.put(ToFuelRecords.AMOUNT, amount);
			}
			if(price != mPrice){
				editTag = true;
				values.put(ToFuelRecords.PRICE, price);
			}
			if(dial != mFuelDial){
				editTag = true;
				values.put(ToFuelRecords.FUEL_DIAL, dial);
			}
			if(mileage != mMileage){
				editTag = true;
				values.put(ToFuelRecords.MILEAGE, mileage);
			}
			if(!editTag)
				return true;
			resolver = getContentResolver();
			Uri uri = ToFuelRecords.CONTENT_URI;
			uri = ContentUris.withAppendedId(uri, mRowId);		
			resolver.update(uri, values, null, null);
		}
		else{
			mMoney = Float.parseFloat(mMoneyEdit.getText().toString());
			mPrice = Float.parseFloat(mPriceEdit.getText().toString());
			mFuelAmount = Float.parseFloat(mAmountEdit.getText().toString());
			mFuelDial = Float.parseFloat(mDialEdit.getText().toString());
			mMileage = Integer.parseInt(mMileageEdit.getText().toString());
			
			resolver = getContentResolver();
			Uri uri = ToFuelRecords.CONTENT_URI;
			values.put(ToFuelRecords.VEHICLE, mDefCar.ID());
			values.put(ToFuelRecords.DATE, mDate);
			values.put(ToFuelRecords.MONEY, mMoney);
			values.put(ToFuelRecords.MILEAGE, mMileage);
			values.put(ToFuelRecords.FUEL_DIAL, mFuelDial);
			values.put(ToFuelRecords.AMOUNT, mFuelAmount);
			values.put(ToFuelRecords.PRICE, mPrice);
			values.put(ToFuelRecords.FUEL, mFuelId);
			values.put(ToFuelRecords.STATION, mStationId);
			resolver.insert(uri, values);
		}
		
		return true;
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {		
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, monthOfYear, dayOfMonth);
		mDate = calendar.getTimeInMillis();
		mDateView.setText(TOFUELREC_DATE_FORMATER.format(calendar.getTime()));		
	}
	
	/** 一些返回编辑过的数据的方法 */
	public long getDate(){return mDate;}
	public int getSationId(){return mStationId;}
	public int getFuelId(){return mFuelId;}
	public float getMoney(){return mMoney;}
	public int getMileage(){return mMileage;}
	public float getFuelDial(){return mFuelDial;}
	public float getFualAmount(){return mFuelAmount;}
	public float getPrice(){return mPrice;}

	@Override
	public void onSelectChanged(int id) {
		if(id != mStationId){
			mStationId = id;
			mStationView.setText(ToFuelMgr.mStations.get(mStationId).name());
		}
		FragmentManager manager = getFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		transaction.remove(mStationFragment);
		transaction.commit();
	}

	
	
}
