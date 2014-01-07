package com.ssc.mycarassistant;

import com.ssc.mycarassistant.db.CarAssistant.VehicleInfos;
import com.ssc.mycarassistant.model.Car;
import com.ssc.mycarassistant.model.Fuel;
import com.ssc.widgets.SingelChoiceDialog;

import android.R.integer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CarInfoEditor extends Activity implements DialogInterface.OnDismissListener{
	public static final String CAR_INFO_TAG_CARID = "carId";
	public static final String CAR_INFO_TAG_READONLY = "readonly";
	
	private EditText mNumberEdit,mVolumeEdit,mAmountEdit,mMileageEdit,mMonthEdit;
	private TextView mUsedFuelView;
	private Button mBtnOk, mBtnCancel;
	private SingelChoiceDialog dlg;
	
	private Car mCar;
	private boolean mReadonly;
	private int mCarId;
	private String mNumber;
	private Fuel mUsedFuel;
	private int mTotalScale, mBoxVolume,mMileage,mMonth;
	
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
		}
		else{
			mNumberEdit.setEnabled(false);
			mAmountEdit.setEnabled(false);
			mVolumeEdit.setEnabled(false);
			mMileageEdit.setEnabled(false);
			mMonthEdit.setEnabled(false);
		}
		mCarId = getIntent().getIntExtra(CAR_INFO_TAG_CARID, 0);
		mCar = MainActivity.mCars.get(mCarId);
		//mReadonly = getIntent().getBooleanExtra(CAR_INFO_TAG_READONLY, false);
		fillDatas();
	}

	private void fillDatas(){
		if(mCar == null){
			if(MainActivity.mFuels.containsKey(1))
				mUsedFuel = MainActivity.mFuels.get(1);
		}
		else{
			mUsedFuel = mCar.usedFuel();
			mUsedFuelView.setText(mCar.usedFuel().toString());
			mNumberEdit.setText(mCar.number());
			mVolumeEdit.setText(Integer.toString(mCar.boxVolume()));
			mAmountEdit.setText(Integer.toString(mCar.totalScale()));
			mMileageEdit.setText(Integer.toString(mCar.mileage()));
			mMonthEdit.setText(Integer.toString(mCar.period()));
			
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
					mMonthEdit.getText().toString().isEmpty())
				result = false;
			else{
				mNumber = mNumberEdit.getText().toString();
				mTotalScale = Integer.parseInt(mAmountEdit.getText().toString());
				mBoxVolume = Integer.parseInt(mVolumeEdit.getText().toString());
				mMileage = Integer.parseInt(mMileageEdit.getText().toString());
				mMonth = Integer.parseInt(mMonthEdit.getText().toString());
			}
			
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
			MainActivity.mCars.put(mCarId, mCar);
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
	
	

}
