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

	static int QUEST_CODE_MAGAGER = 1;
	private Class<?>[] clazzs;
	
	HashMap<Integer,String> mFuelClasses;  //燃料种类
	HashMap<Integer,Fuel> mFuels;			//燃料
	HashMap<Integer,Car> mCars;			//车辆
	HashMap<Integer,FuelStation> mStations;//加油站
	
	Cursor mCursor;
	
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
    	if(item.getItemId() == R.id.mi_manage){
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
//    	if(!readCars())
//    		return false;
//    	if(!readStations())
//    		return false;
    	return true;     
    }
    
    //读取燃料种类 
    private boolean readFuelClasses(){
    	ContentResolver resolver = getContentResolver();  
    	try
        {        	
        	mCursor = resolver.query(FuelClasses.CONTENT_URI, null, null, null, null);
        	if(mCursor == null){
        		Toast.makeText(MainActivity.this,getText(R.string.title_error),Toast.LENGTH_SHORT).show();
        		return false;
        	}
        	else if(mCursor.getCount() == 0){
        		Toast.makeText(MainActivity.this,getText(R.string.error_info_null_fuelclass),Toast.LENGTH_SHORT).show();
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
    
    private boolean readFuels(){
    	ContentResolver resolver = getContentResolver();  
    	try
        { 
    		mCursor = resolver.query(Fuels.CONTENT_URI, null, null, null, null);
        	if(mCursor == null){
        		Toast.makeText(MainActivity.this,getText(R.string.title_error),Toast.LENGTH_SHORT).show();
        		return false;
        	}
        	else if(mCursor.getCount() == 0){
        		Toast.makeText(MainActivity.this,getText(R.string.error_info_null_fuel),Toast.LENGTH_SHORT).show();
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
    
    private boolean readCars(){
    	ContentResolver resolver = getContentResolver();  
    	try{
    		mCursor = resolver.query(VehicleInfos.CONTENT_URI, null, null, null, null);
        	if(mCursor == null){
        		Toast.makeText(MainActivity.this,getText(R.string.title_error),Toast.LENGTH_SHORT).show();
        		return false;
        	}
        	else if(mCursor.getCount() == 0){
        		Toast.makeText(MainActivity.this,getText(R.string.error_info_null_vehicle),Toast.LENGTH_SHORT).show();
        		return true;
        	}
        	else{
        		if(mCars == null)
        			mCars = new HashMap<Integer,Car>();
        		else
        			mCars.clear();
        		while(mCursor.moveToNext()){
        			if(mCars == null)
        				mCars = new HashMap<Integer,Car>();
        			else
        				mCars.clear();
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
    
    private boolean readStations(){
    	ContentResolver resolver = getContentResolver();  
    	try{
    		mCursor = resolver.query(ToFuelStations.CONTENT_URI, null, null, null, null);
        	if(mCursor == null){
        		Toast.makeText(MainActivity.this,getText(R.string.title_error),Toast.LENGTH_SHORT).show();
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
    
//    @Override 
//    protected void onActivityResult(int requestCode, int resultCode, Intent data){
//    	if(resultCode == 0)
//    		return;
//    	if(requestCode == QUEST_CODE_MAGAGER){
//    		
//    	}
//    }
}
