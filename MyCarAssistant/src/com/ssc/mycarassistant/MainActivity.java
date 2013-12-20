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
	
	
	HashMap<Integer,Car> mCars;			//车辆
	
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
//    	if(!readFuelClasses())
//    		return false;
//    	if(!readFuels())
//    		return false;
//    	if(!readCars())
//    		return false;
//    	if(!readStations())
//    		return false;
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
