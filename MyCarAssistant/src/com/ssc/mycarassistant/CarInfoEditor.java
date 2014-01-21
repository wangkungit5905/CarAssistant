package com.ssc.mycarassistant;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.ssc.mycarassistant.db.CarAssistant.ToFuelRecords;
import com.ssc.mycarassistant.db.CarAssistant.VehicleInfos;
import com.ssc.mycarassistant.model.Car;
import com.ssc.mycarassistant.model.Fuel;
import com.ssc.widgets.SingelChoiceDialog;

import android.R.integer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CarInfoEditor extends Activity implements DialogInterface.OnDismissListener,OnDateSetListener{
	public static final String CAR_INFO_TAG_CARID = "carId";
	public static final String CAR_INFO_TAG_READONLY = "readonly";
	
	private final SimpleDateFormat DATE_FORMATER = new SimpleDateFormat("yyyy-M-d");	
	
	private EditText mNumberEdit,mVolumeEdit,mAmountEdit,mMileageEdit,mMonthEdit;
	private EditText mInitMileageEdit,mInitFuelEdit;
	private TextView mUsedFuelView,mInitDateView;
	private Button mBtnOk, mBtnCancel;
	private SingelChoiceDialog dlg;
	
	private Car mCar;
	private boolean mReadonly;
	private int mCarId;
	private String mNumber;
	private Fuel mUsedFuel;
	private int mTotalScale, mBoxVolume,mMileage,mMonth;
	private int mInitMileage;	//初始里程
	private float mInitFuel;	//初始剩余油量
	private long mInitDate;		//开始使用日期
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mReadonly = getIntent().getBooleanExtra(CAR_INFO_TAG_READONLY, true);
		setContentView(R.layout.activity_car_info_editor);
		mNumberEdit = (EditText)findViewById(R.id.car_info_number);
		mVolumeEdit = (EditText)findViewById(R.id.car_info_volume);
		mAmountEdit = (EditText)findViewById(R.id.car_info_amount);
		mMileageEdit = (EditText)findViewById(R.id.car_info_period_mileage);
		mMonthEdit = (EditText)findViewById(R.id.car_info_period_month);
		mUsedFuelView = (TextView)findViewById(R.id.car_info_usedfuel);		
		mInitMileageEdit = (EditText)findViewById(R.id.car_info_init_mileage);
		mInitFuelEdit = (EditText)findViewById(R.id.car_info_init_fuel);
		mInitDateView = (TextView)findViewById(R.id.car_info_init_date);
		
		mBtnOk = (Button)findViewById(R.id.car_info_btn_ok);
		mBtnOk.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(!mReadonly){
					if(!inspectData())
						return;
					updateCarInfo();
				}					
				setResult(RESULT_OK);
				finish();				
			}
		});
		mBtnCancel = (Button)findViewById(R.id.car_info_btn_cancel);
		mBtnCancel.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();				
			}
		});
		
		mCarId = getIntent().getIntExtra(CAR_INFO_TAG_CARID, 0);
		mCar = MainActivity.mCars.get(mCarId);	
		if(!mReadonly){
			mUsedFuelView.setOnClickListener(new OnClickListener() {			
				@Override
				public void onClick(View v) {
					Fuel[] items = new Fuel[MainActivity.mFuels.size()];
					MainActivity.mFuels.values().toArray(items);
					SingelChoiceDialog dlg = new SingelChoiceDialog(CarInfoEditor.this, items);
					dlg.getInstance("选择燃料种类", CarInfoEditor.this, null).show();
				}
			});
			mInitDateView.setOnClickListener(new OnClickListener() {			
				@Override
				public void onClick(View v) {
					Calendar calendar = Calendar.getInstance();
					if(mCar != null)
						calendar.setTimeInMillis(mCar.getInitDate());
					int year = calendar.get(calendar.YEAR);
					int month = calendar.get(Calendar.MONTH);
					int day = calendar.get(Calendar.DAY_OF_MONTH);
					DatePickerDialog dlg = new DatePickerDialog(CarInfoEditor.this,CarInfoEditor.this,year,month,day);
					dlg.show();				
				}
			});
		}
		else{
			mNumberEdit.setEnabled(false);
			mAmountEdit.setEnabled(false);
			mVolumeEdit.setEnabled(false);
			mMileageEdit.setEnabled(false);
			mMonthEdit.setEnabled(false);
			mInitFuelEdit.setEnabled(false);
			mInitMileageEdit.setEnabled(false);
		}
			
		fillDatas();
	}

	private void fillDatas(){
		if(mCar == null){
			if(MainActivity.mFuels.containsKey(1))
				mUsedFuel = MainActivity.mFuels.get(1);
		}
		else{
			mInitDate = mCar.getInitDate();
			mUsedFuel = mCar.usedFuel();
			mUsedFuelView.setText(mCar.usedFuel().toString());
			mNumberEdit.setText(mCar.number());
			mVolumeEdit.setText(Integer.toString(mCar.boxVolume()));
			mAmountEdit.setText(Integer.toString(mCar.totalScale()));
			mMileageEdit.setText(Integer.toString(mCar.mileage()));
			mMonthEdit.setText(Integer.toString(mCar.period()));
			//初始里程、油量和开始使用日期
			mInitFuelEdit.setText(Float.toString(mCar.getInitFuelVolume()));
			mInitMileageEdit.setText(Integer.toString(mCar.getInitMileage()));
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(mCar.getInitDate());
			mInitDateView.setText(DATE_FORMATER.format(calendar.getTime()));
		}
		mUsedFuelView.setText(mUsedFuel.toString());
	}
	
	/**
	 * 检测参数的有效性
	 * @return
	 */
	private boolean inspectData(){
		boolean result = true;		
		try{
			if(mAmountEdit.getText().toString().isEmpty() || 
					mVolumeEdit.getText().toString().isEmpty() ||
					mMileageEdit.getText().toString().isEmpty() ||
					mMonthEdit.getText().toString().isEmpty() ||
					mInitDateView.getText().toString().isEmpty())
				result = false;
			else{
				mNumber = mNumberEdit.getText().toString();
				mTotalScale = Integer.parseInt(mAmountEdit.getText().toString());
				mBoxVolume = Integer.parseInt(mVolumeEdit.getText().toString());
				mMileage = Integer.parseInt(mMileageEdit.getText().toString());
				mMonth = Integer.parseInt(mMonthEdit.getText().toString());				
			}
			if(mInitMileageEdit.getText().toString().isEmpty())
				mInitMileage = 0;
			else
				mInitMileage = Integer.parseInt(mInitMileageEdit.getText().toString());
			if(mInitFuelEdit.getText().toString().isEmpty())
				mInitFuel = 0;
			else 
				mInitFuel = Float.parseFloat(mInitFuelEdit.getText().toString());			
			
		}
		catch(NumberFormatException e){
			result = false;
		}
		finally{
			if(!result)
				Toast.makeText(this, "有不可接受的车辆信息参数！", Toast.LENGTH_SHORT);
		}
		return result;
	}
	
	/**
	 * 更新车辆信息
	 */
	private void updateCarInfo(){
		ContentValues values = new ContentValues();
		ContentResolver resolver = getContentResolver();
		if(mCarId == 0){
			values.put(VehicleInfos.NUMBER, mNumber);
			values.put(VehicleInfos.USE_FUEL, mUsedFuel.ID());
			values.put(VehicleInfos.TOTAL_SCALE, mTotalScale);
			values.put(VehicleInfos.BOX_VOLUME, mBoxVolume);
			values.put(VehicleInfos.MAINTENANCE_MILEAGE, mMileage);
			values.put(VehicleInfos.MAINTENANCE_MONTH, mMonth);
			Uri uri = resolver.insert(VehicleInfos.CONTENT_URI, values);
			mCarId = (int)ContentUris.parseId(uri);			
			mCar = new Car(mCarId, mNumber, mUsedFuel, mBoxVolume, mTotalScale);
			mCar.setMileage(mMileage);
			mCar.setPeriod(mMonth);
			mCar.setInitMileage(mInitMileage);
			mCar.setInitFuelVolume(mInitFuel);
			mCar.setInitDate(mInitDate);
			MainActivity.mCars.put(mCarId, mCar);
			
			//将初始里程、油量等信息加入到加油记录表中
			values.clear();
			values.put(ToFuelRecords.VEHICLE, mCarId);
			values.put(ToFuelRecords.MILEAGE, mInitMileage);
			values.put(ToFuelRecords.DEFAULT_SORT_ORDER, mInitDate);
			values.put(ToFuelRecords.FUEL_DIAL, mCar.getInitDial());
			values.put(ToFuelRecords.MONEY, 0);			
			values.put(ToFuelRecords.PRICE, 0);
			values.put(ToFuelRecords.FUEL, 0);
			values.put(ToFuelRecords.STATION, 0);			
			values.put(ToFuelRecords.AMOUNT, 0);
			uri = resolver.insert(ToFuelRecords.CONTENT_URI, values);
			long id = ContentUris.parseId(uri);
			id++;
		}
		else{
			if(mNumber.compareTo(mCar.number()) != 0 ){
				mCar.setNumber(mNumber);
				values.put(VehicleInfos.NUMBER, mNumber);
			}
			if(mTotalScale != mCar.totalScale()){
				mCar.setTotalScale(mTotalScale);
				values.put(VehicleInfos.TOTAL_SCALE, mTotalScale);
			}
			if(mBoxVolume != mCar.boxVolume()){
				mCar.setBoxVolume(mBoxVolume);
				values.put(VehicleInfos.BOX_VOLUME, mBoxVolume);
			}
			if(mUsedFuel.ID() != mCar.usedFuel().ID()){
				mCar.setUsedFuel(mUsedFuel);
				values.put(VehicleInfos.USE_FUEL, mUsedFuel.ID());
			}
			if(mMileage != mCar.mileage()){
				mCar.setMileage(mMileage);
				values.put(VehicleInfos.MAINTENANCE_MILEAGE, mMileage);
			}
			if(mMonth != mCar.period()){
				mCar.setPeriod(mMonth);
				values.put(VehicleInfos.MAINTENANCE_MONTH, mMonth);
			}
			if(values.size() > 0){
				Uri uri = ContentUris.withAppendedId(VehicleInfos.CONTENT_URI, mCarId);			
				resolver.update(uri, values, null, null);
			}			
			values.clear();
			if(mInitMileage != mCar.getInitMileage()){
				mCar.setInitMileage(mInitMileage);
				values.put(ToFuelRecords.MILEAGE, mInitMileage);
			}
			if(mInitFuel != mCar.getInitFuelVolume()){
				mCar.setInitFuelVolume(mInitFuel);
				values.put(ToFuelRecords.FUEL_DIAL, mCar.getInitDial());
			}
			if(mInitDate != mCar.getInitDate()){
				mCar.setInitDate(mInitDate);
				values.put(ToFuelRecords.DATE, mInitDate);
			}
			if(values.size() > 0){
				Uri uri = ContentUris.withAppendedId(ToFuelRecords.CONTENT_URI_CAR_INITITEM_URI, mCar.ID()) ;				
				resolver.update(uri, values, null, null);
			}
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		SingelChoiceDialog dlg = (SingelChoiceDialog)dialog;
		if(dlg == null)
			return;
		mUsedFuel = (Fuel)dlg.getChoiceItem();
		mUsedFuelView.setText(mUsedFuel.toString());
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {		
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, monthOfYear, dayOfMonth);
		mInitDate = calendar.getTimeInMillis();
		mInitDateView.setText(DATE_FORMATER.format(calendar.getTime()));	
	}
	
	

}
