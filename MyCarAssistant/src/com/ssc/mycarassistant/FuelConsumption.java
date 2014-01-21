package com.ssc.mycarassistant;

import java.util.Calendar;
import java.util.Vector;

import javax.crypto.spec.IvParameterSpec;

import com.ssc.mycarassistant.db.CarAssistant.ToFuelRecords;
import com.ssc.mycarassistant.model.Car;
import com.ssc.widgets.chart.ChartView;

import android.R.anim;
import android.R.integer;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class FuelConsumption extends Activity {

	private static final float DEF_AVERAGE = 8.5f;
	private Spinner mSpinCar,mSpinRange;
	private TextView mTotalMoneyView;
	private ChartView mChartView;
	private Car[] mCars;
	private Car mCar;
	private int mYear,mStartYear,mEndYear;
	private String[] mYears;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fuel_consumption);
		mSpinCar = (Spinner)findViewById(R.id.stat_spin_cars);
		mSpinRange = (Spinner)findViewById(R.id.stat_spin_range);
		mTotalMoneyView = (TextView)findViewById(R.id.stat_total_money);
		mChartView = (ChartView)findViewById(R.id.stat_chartView);
		
		int carNums = MainActivity.mCars.size();
		mCars = new Car[carNums];
		if(carNums == 0){
			Toast.makeText(this, "还未配置任何车辆", Toast.LENGTH_SHORT);
			return;
		}
		MainActivity.mCars.values().toArray(mCars);
		mCar = mCars[0];
		ArrayAdapter<Car> adapter = new ArrayAdapter<Car>(this, android.R.layout.simple_spinner_dropdown_item,mCars);
		mSpinCar.setAdapter(adapter);
		updateYearRange();
		
		mSpinCar.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
				mCar = mCars[position];	
				updateYearRange();
			}
			@Override
			public void onNothingSelected(AdapterView<?> v) {
			}
		});
		
		mSpinRange.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
				if(position == 0)
					mYear = 0;
				else 
					mYear = Integer.parseInt(mYears[position]);
				viewStatChart();
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.fuel_consumption, menu);
		return true;
	}
	
	/** 更新指定车辆的年份的取值范围 */
	private void updateYearRange(){
		Calendar calendar = Calendar.getInstance();
		int curYear = calendar.get(Calendar.YEAR);
		ContentResolver resolver = getContentResolver();
		String[] projection = new String[2];
		projection[0] = String.format("max(%s)", ToFuelRecords.DATE);
		projection[1] = String.format("min(%s)", ToFuelRecords.DATE);
		String where = ToFuelRecords.VEHICLE + " = ? ";
		String[] argus = new String[]{Integer.toString(mCar.ID())};
		Cursor cursor = resolver.query(ToFuelRecords.CONTENT_URI, projection, where, argus, null);
		if(cursor != null){
			cursor.moveToNext();
			long y1 = cursor.getLong(0);
			long y2 = cursor.getLong(1);	
			if(y1 == 0 && y2 == 0){
				mStartYear = curYear;
				mEndYear = curYear;
				mYear = curYear;
				mCar = null;
			}
			else{
				calendar.setTimeInMillis(y1);
				mEndYear = calendar.get(Calendar.YEAR);
				calendar.setTimeInMillis(y2);
				mStartYear = calendar.get(Calendar.YEAR);
				mYear = mEndYear;
			}
		}
		mYears = null;
		mYears = new String[mEndYear-mStartYear+2];
		mYears[0] = "全部";
		int y = mStartYear;
		for(int i = 1; i < mYears.length; ++i,++y){
			mYears[i] = Integer.toString(y);
		}
		ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,mYears);
		mSpinRange.setAdapter(adapter2);
		mSpinRange.setSelection(mYears.length-1);
	}	
	
	/** 显示统计图 */
	private void viewStatChart(){
		//显示统计图，包括油耗、金额等
		if(mCar == null)
			return;
		ContentResolver resolver = getContentResolver();
		String where;
		String args[];		
		long date_upper;
		Cursor cursor;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(mCar.getInitDate());
		int carStartYear = calendar.get(Calendar.YEAR);  //车辆开始使用年份
		
		//首先获取所选时间范围之前的平均耗油率
		float init_average = 0; //初始平均油耗
		float totalVolume=0;		//之前的总耗油量		
		int totalMiles=0;			//之前的总里程
		if(mYear == 0 || mYear == carStartYear){
			init_average = DEF_AVERAGE;
		}
		else{
			calendar.set(mYear, 0, 1, 0, 0, 0);
			date_upper = calendar.getTimeInMillis();
			where = ToFuelRecords.VEHICLE + " = ? and " + ToFuelRecords.MONEY + " != 0 and " + 
					ToFuelRecords.DATE + " < ?";
			args = new String[]{Integer.toString(mCar.ID()), Long.toString(date_upper)};
			cursor = resolver.query(ToFuelRecords.CONTENT_URI, new String[]{"sum("+ ToFuelRecords.AMOUNT + ")"},
					where, args, null);
			if(cursor == null){
				Toast.makeText(this, "查询出错", Toast.LENGTH_SHORT);
				return;
			}
			cursor.moveToNext();
			totalVolume = cursor.getInt(0);
		}
		
		String projections[] = new String[]{ToFuelRecords.MONEY,ToFuelRecords.MILEAGE,
											ToFuelRecords.AMOUNT,ToFuelRecords.FUEL_DIAL,
											ToFuelRecords.DATE};
		where = ToFuelRecords.VEHICLE + " = ?";
		String order = ToFuelRecords.DATE;
		if(mYear == 0){
			args = new String[]{Integer.toString(mCar.ID())};			
		}
		else{
			calendar = Calendar.getInstance();
			calendar.set(mYear, 0, 1, 0, 0, 0);
			long startTime = calendar.getTimeInMillis();
			calendar.set(mYear, 11, 31, 23, 59, 59);
			long endTime = calendar.getTimeInMillis();
			where = where + " and (" + ToFuelRecords.DATE + " >= ? and " + ToFuelRecords.DATE + " <= ? )";
			args = new String[]{Integer.toString(mCar.ID()),Long.toString(startTime),Long.toString(endTime)};
		}
		cursor = resolver.query(ToFuelRecords.CONTENT_URI, projections, where, args, order);
		if(cursor == null){
			Toast.makeText(this, "查询出错", Toast.LENGTH_SHORT);
			return;
		}
		if(cursor.getCount() < 3){
			Toast.makeText(this, "统计油耗至少需要2条记录！", Toast.LENGTH_SHORT);
			return;
		}
		
		
		
		calendar.setTimeInMillis(mCar.getInitDate());
		int carStatrYear = calendar.get(Calendar.YEAR); //车辆开始使用的年份
		float unitAmount = mCar.getUnit();
		int nums;										//加油记录数
		
		if(mYear == 0 || mYear == carStatrYear){
			nums = cursor.getCount() - 2;
			cursor.moveToNext();	//第一条记录是初始记录跳过
		}
		else
			nums = cursor.getCount()-1;
		float[] averages = new float[nums+1];
		float[] mileages = new float[nums];
		float[] consumptions = new float[nums];
		int[][] dates = new int[nums][2];
		cursor.moveToNext();					//移动到第一条属于选定年份内的有效加油记录
		float moneys = cursor.getFloat(0);		//第一条加油记录的金额
		float remainDial = cursor.getFloat(3);	//第一次加油时的油表刻度（上次遗留量）
		float remainVolume = mCar.getVolumeByDial(remainDial);//第一次加油时的油表刻度（上次遗留量）
		float initVolume = cursor.getFloat(2);	//第一次加注的油量
		initVolume += remainVolume;				//加上遗留的油量=第一次加油后邮箱的油量
		totalVolume += initVolume;				//从开始使用到此时所消耗的油量
		int initMileage = cursor.getInt(1);		//第一条加油记录的里程
		if(mYear != 0 && mYear != carStartYear){
			totalMiles = initMileage;
			init_average = (totalVolume/totalMiles)*100; //这是一个估算值，未考虑初始油量和开始油量之间的差值
		}
		averages[0] = init_average;
		
		int i = 0;
		while(cursor.moveToNext()){
			long date = cursor.getLong(4);			   //加注日期
			calendar.setTimeInMillis(date);
			dates[i][0] = calendar.get(Calendar.MONTH)+1;
			dates[i][1] = calendar.get(Calendar.DAY_OF_MONTH);
			float money = cursor.getFloat(0);		   //加注金额
			int mileage = cursor.getInt(1);			   //加注时的里程表读数
			float amount = cursor.getFloat(2);		   //加注油量
			float dial = cursor.getFloat(3);		   //剩余油量
			float volume = mCar.getVolumeByDial(dial); //第二次加注前剩余油量
			moneys += money;
			int dm = mileage - initMileage; //第一和第二次加油间隔所行驶的里程
			float dv = initVolume - volume; //第一和第二次加油间隔所消耗的油量
			if(dm < 0){
				Toast.makeText(this, "加油记录里程设置有误，里程应该是递增的！", Toast.LENGTH_SHORT);
				break;
			}
			if(dv < 0){
				Toast.makeText(this, "加油记录油量设置有误！", Toast.LENGTH_SHORT);
				break;			
			}
			consumptions[i] = (dv/dm)*100;
			initVolume = volume + amount;
			initMileage = mileage;	
			
			averages[i+1] = (totalVolume/mileage) * 100;
			totalVolume += amount;
			i++;
		}
		mChartView.setTotalMoney(moneys);
		mChartView.setTopRange(15);
		mChartView.setBaseCoast(8.5f);
		mChartView.setWarningCoast(10);
		mChartView.setData(consumptions, dates,averages);
	}
}
