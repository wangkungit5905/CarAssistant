package com.ssc.mycarassistant;

import java.util.HashMap;

import com.ssc.mycarassistant.db.CarAssistant.FuelClasses;
import com.ssc.mycarassistant.db.CarAssistant.Fuels;
import com.ssc.mycarassistant.db.CarAssistant.ToFuelStationColumns;
import com.ssc.mycarassistant.db.CarAssistant.ToFuelStations;
import com.ssc.mycarassistant.db.CarAssistant.VehicleInfoColumns;
import com.ssc.mycarassistant.db.CarAssistant.VehicleInfos;
import com.ssc.mycarassistant.model.Car;
import com.ssc.mycarassistant.model.Fuel;
import com.ssc.mycarassistant.model.FuelStation;

import android.app.LauncherActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class MainActivity extends LauncherActivity {

	public static HashMap<Integer,String> mFuelClasses; //燃料种类
	public static HashMap<Integer,Fuel> mFuels;			//燃料
	public static HashMap<Integer,Car> mCars;			//车辆
	
	static int QUEST_CODE_MAGAGER = 1;
	private Class<?>[] clazzs;
	
	
	
	
	//Cursor mCursor;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

		//定义两个Activity的名称  
	    String[] names = {"加油记录", "维保记录"}; 
	    //定义两个Activity对应的实现类  
	    clazzs = new Class<?>[2];
	    clazzs[0] = ToFuelMgr.class;
	    clazzs[1] = RepailMgr.class;
	    //clazzs = {ToFuelMgr.class, RepailMgr.class};
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1 
                , names); 
	    setListAdapter(adapter); 
	    init();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
    
    //根据列表项返回指定Activity对应的Intent  
    @Override 
    protected Intent intentForPosition(int position) { 
        // TODO Auto-generated method stub  
        return new Intent(MainActivity.this, clazzs[position]); 
    } 
    
    /** 启动管理界面 */
    private void startManagerView(){
    	Intent intent = new Intent(MainActivity.this, ManagerActivity.class);
    	//Bundle data = new Bundle();
    	startActivity(intent);
    }
    
    private boolean init(){
    	if(!readFuelClasses())
    		return false;
    	if(!readFuels())
    		return false;
    	if(!readCars())
    		return false;
    	return true;     
    }
    
   
    
    
    
    
    
    
    
//    @Override 
//    protected void onActivityResult(int requestCode, int resultCode, Intent data){
//    	if(resultCode == 0)
//    		return;
//    	if(requestCode == QUEST_CODE_MAGAGER){
//    		
//    	}
//    }
    
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
        		if(mCars == null)
        			mCars = new HashMap<Integer, Car>();
        		else 
        			mCars.clear();
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
        			mCars.put(id, car);
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
