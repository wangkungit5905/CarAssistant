package com.ssc.mycarassistant;

import java.util.HashMap;
import java.util.Iterator;

import com.ssc.mycarassistant.db.CarAssistant.FuelClasses;
import com.ssc.mycarassistant.db.CarAssistant.Fuels;
import com.ssc.mycarassistant.db.CarAssistant.ToFuelRecords;
import com.ssc.mycarassistant.db.CarAssistant.VehicleInfoColumns;
import com.ssc.mycarassistant.db.CarAssistant.VehicleInfos;
import com.ssc.mycarassistant.model.Car;
import com.ssc.mycarassistant.model.Fuel;

import android.R.integer;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private GridView mGridView;
	private Class<?>[] clazzs;
	public static HashMap<Integer,String> mFuelClasses; //燃料种类
	public static HashMap<Integer,Fuel> mFuels;			//燃料
	public static HashMap<Integer,Car> mCars;			//车辆
	
	public class ImageAdapter extends BaseAdapter {
	    private Context mContext;

	    public ImageAdapter(Context c) {
	        mContext = c;
	    }

	    public int getCount() {
	        return mThumbIds.length;
	    }

	    public Object getItem(int position) {
	        return null;
	    }

	    public long getItemId(int position) {
	        return 0;
	    }
	    
	    public View getView(int position, View convertView, ViewGroup parent) {
	        ImageView imageView;
	        if (convertView == null) {  // if it's not recycled, initialize some attributes
	            imageView = new ImageView(mContext);
	            imageView.setLayoutParams(new GridView.LayoutParams(100, 100));
	            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
	            imageView.setPadding(10, 10, 10, 10);
	        } else {
	            imageView = (ImageView) convertView;
	        }

	        imageView.setImageResource(mThumbIds[position]);
	        return imageView;
	    }

	    // references to our images
	    private Integer[] mThumbIds = {
	    		R.drawable.tofuelmgr,
	    		R.drawable.fuel_consumption,
	    		R.drawable.maintance	    		
	    };
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity2);
		init();
		
	    
		mGridView = (GridView)findViewById(R.id.gridview);
		ImageAdapter adapter = new ImageAdapter(this);
		mGridView.setAdapter(adapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Intent intent = new Intent(MainActivity.this, clazzs[position]);
				startActivity(intent);
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
        return true;
	}
	
	@Override
    public boolean	 onOptionsItemSelected(MenuItem item){
    	if(item.getItemId() == R.id.action_settings){
    		startManagerView();
    		return true;
    	}
    	else
    		return super.onOptionsItemSelected(item);
    }
	
	/** 启动管理界面 */
    private void startManagerView(){
    	Intent intent = new Intent(MainActivity.this, ManagerActivity.class);
    	startActivity(intent);
    }
	
	private boolean init(){
		clazzs = new Class<?>[3];
	    clazzs[0] = ToFuelMgr.class;
	    clazzs[1] = FuelConsumption.class;
	    clazzs[2] = RepailMgr.class;
	    
    	if(!readFuelClasses())
    		return false;
    	if(!readFuels())
    		return false;
    	if(!readCars())
    		return false;
    	return true;     
    }
	
	//读取燃料种类 
    private boolean readFuelClasses(){
    	ContentResolver resolver = getContentResolver();  
    	Cursor cursor = null;
    	try
        {        	
        	cursor = resolver.query(FuelClasses.CONTENT_URI, null, null, null, null);
        	if(cursor == null){
        		Toast.makeText(this,getText(R.string.title_error),Toast.LENGTH_SHORT).show();
        		return false;
        	}
        	else if(cursor.getCount() == 0){
        		Toast.makeText(this,getText(R.string.error_info_null_fuelclass),Toast.LENGTH_SHORT).show();
        		return false;
        	}
        	else{
        		if(mFuelClasses == null)
        			mFuelClasses = new HashMap<Integer,String>();
        		else
        			mFuelClasses.clear();
        		while(cursor.moveToNext()){
        			int id = cursor.getInt(0);
        			String fuelName = cursor.getString(1);
        			mFuelClasses.put(id, fuelName);
        		}
        	}        	
        }
        finally
        {
           if (cursor != null)
              cursor.close();           
        }
        return true;
    }
    
    /** 读取可用燃料 */
    private boolean readFuels(){
    	ContentResolver resolver = getContentResolver();   
    	Cursor cursor = null;
    	try
        { 
    		cursor = resolver.query(Fuels.CONTENT_URI, null, null, null, null);
        	if(cursor == null){
        		Toast.makeText(this,getText(R.string.title_error),Toast.LENGTH_SHORT).show();
        		return false;
        	}
        	else if(cursor.getCount() == 0){
        		Toast.makeText(this,getText(R.string.error_info_null_fuel),Toast.LENGTH_SHORT).show();
        		return false;
        	}
        	else{
        		if(mFuels == null)
        			mFuels = new HashMap<Integer,Fuel>();
        		else
        			mFuels.clear();
        		while(cursor.moveToNext()){
        			int id = cursor.getInt(0);
        			int fcId = cursor.getInt(1);
        			String grade = cursor.getString(2);
        			Fuel fuel = new Fuel(id,fcId,mFuelClasses.get(fcId),grade);
        			mFuels.put(id, fuel);
        		}
        	}
        }
    	finally
        {
           if (cursor != null)
              cursor.close();           
        }
    	return true;
    }
	
	/** 读取所有的车辆填充cars数组 */
	private boolean readCars(){		
		if(mCars == null)
			mCars = new HashMap<Integer, Car>();
		else 
			mCars.clear();
    	ContentResolver resolver = getContentResolver();  
    	Cursor cursor = null;
    	try{
    		cursor = resolver.query(VehicleInfos.CONTENT_URI, null, null, null, null);
        	if(cursor == null){
        		Toast.makeText(this,getText(R.string.title_error),Toast.LENGTH_SHORT).show();
        		return false;
        	}
        	else if(cursor.getCount() == 0){
        		Toast.makeText(this,getText(R.string.error_info_null_vehicle),Toast.LENGTH_SHORT).show();
        		return true;
        	}
        	else{
        		int carNumber = cursor.getCount();        		
        		int row = 0;
        		while(cursor.moveToNext()){        			
        			int id = cursor.getInt(0);
        			String number = cursor.getString(cursor.getColumnIndex(VehicleInfoColumns.NUMBER));
        			int fuelId = cursor.getInt(cursor.getColumnIndex(VehicleInfoColumns.USE_FUEL));
        			int boxVolume = cursor.getInt(cursor.getColumnIndex(VehicleInfoColumns.BOX_VOLUME));
        			int mileage = cursor.getInt(cursor.getColumnIndex(VehicleInfoColumns.TOTAL_SCALE));
        			int period_mileage = cursor.getInt(cursor.getColumnIndex(VehicleInfos.MAINTENANCE_MILEAGE));
        			int period_month = cursor.getInt(cursor.getColumnIndex(VehicleInfos.MAINTENANCE_MONTH));
        			Car car = new Car(id,number,mFuels.get(fuelId),boxVolume,mileage);
        			car.setMileage(period_mileage);
        			car.setPeriod(period_month);
        			car.setInitFuelVolume(10);	//临时代码，设置初始油量和里程数据
        			car.setInitMileage(7);
        			mCars.put(id, car);
        		}
        	}
        	
        	//读取车辆的初始信息
        	Iterator<Car> iterator = mCars.values().iterator();
        	String[] projection = new String[]{ToFuelRecords.DATE,ToFuelRecords.MILEAGE,ToFuelRecords.FUEL_DIAL};        	
        	String where;        
        	String[] args = new String[1];
        	while (iterator.hasNext()) {
				Car car = iterator.next();
				where = ToFuelRecords.VEHICLE + " = ? and " + ToFuelRecords.MONEY + " = 0";
				args[0] = Integer.toString(car.ID());
				cursor = resolver.query(ToFuelRecords.CONTENT_URI, projection, where, args, null);
				if(cursor == null){
					Toast.makeText(this, "查询出错！", Toast.LENGTH_SHORT);
					break;
				}
				else if(cursor.getCount() > 1){
					Toast.makeText(this, "出现多个初始记录！", Toast.LENGTH_SHORT);
					break;
				}
				else{
					cursor.moveToNext();
					long date = cursor.getLong(0);
					int initMileage = cursor.getInt(1);
					float initDial = cursor.getFloat(2);
					car.setInitMileage(initMileage);
					car.setInitFuelDial(initDial);
					car.setInitDate(date);
				}
			}
    	}
    	finally
        {
           if (cursor != null)
              cursor.close();           
        }
    	return true;
    }

}
