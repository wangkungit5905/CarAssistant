package com.ssc.mycarassistant;

import java.util.Iterator;

import com.ssc.mycarassistant.db.CarAssistant.FuelClasses;
import com.ssc.mycarassistant.db.CarAssistant.Fuels;
import com.ssc.mycarassistant.db.CarAssistant.ToFuelRecords;
import com.ssc.mycarassistant.db.CarAssistant.VehicleInfos;
import com.ssc.mycarassistant.model.Car;
import com.ssc.mycarassistant.model.Fuel;

import android.R.integer;
import android.R.layout;
import android.net.Uri;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ManagerActivity extends Activity /*implements OnNavigationListener */{
	//boolean isFuelMgr = true;
	
	private static final int 	REQUEST_CODE_CAR_INFO  = 1;
	private static final int 	REQUEST_CODE_FUEL_INFO  = 2;
	private static final int  RESULT_CODE_OK = 1;
	private static final int  RESULT_CODE_CANCEL = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//if(isFuelMgr)
		setContentView(R.layout.activity_manager);
		
		final ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        bar.addTab(bar.newTab()
        		.setText(R.string.menu_mgr_fuel)
        		.setTabListener(new TabListener<FuelMgrFragment>(this, "fuel", FuelMgrFragment.class)));
        bar.addTab(bar.newTab()
        		.setText(R.string.menu_mgr_car)
        		.setTabListener(new TabListener<CarMgrFragment>(this, "car", CarMgrFragment.class)));
        if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        }
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.manager, menu);
//		return true;
//	}
	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item){
//		switch (item.getItemId()) {
//		case R.id.mnu_fuel_mgr:
//			
//			break;
//		case R.id.mnu_car_mgr:
//			
//			break;
//
//		default:
//			break;
//		}
//		return true;
//	}
	
	public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
		private final Activity mActivity;
		private final String mTag;
		private final Class<T> mClass;
		//private final Bundle mArgs;
		private Fragment mFragment;

        public TabListener(Activity activity, String tag, Class<T> clz) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
            
            mFragment = mActivity.getFragmentManager().findFragmentByTag(mTag);
            if (mFragment != null && !mFragment.isDetached()) {
                FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
                ft.detach(mFragment);
                ft.commit();
            }
            
            
    	}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			
			//Toast.makeText(mActivity, "Reselected!", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			if (mFragment == null) {
                mFragment = Fragment.instantiate(mActivity, mClass.getName(), null);
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                ft.attach(mFragment);
            }				
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			if (mFragment != null) {
                ft.detach(mFragment);
            }				
		}
	 }
	
	/**
	 * 管理燃料信息的Fragment
	 * @author wangkun
	 *
	 */
	public static class FuelMgrFragment extends Fragment{
		private ListView mFuelClsView,mFuelTypeView;
		private ArrayAdapter<String> mFuelClsAdapter;
		private ArrayAdapter<Fuel> mFuelTypeAdapter;
		private Fuel[] mFuels;
		private String[] mFuelClasses;
		private int[] mFuelClsIds;
		
		public FuelMgrFragment(){	
			setHasOptionsMenu(true);					
		}
		 
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view;
			view = inflater.inflate(R.layout.activity_manager_fuel, container,false);
			mFuelClsView = (ListView)view.findViewById(R.id.mgr_fuel_class_lv);
			mFuelTypeView = (ListView)view.findViewById(R.id.mgr_fuel_type_lv);
			registerForContextMenu(mFuelTypeView);			
			updateFuelClassList();
			updatFuelList();			
			return view;
		}
		
		public void updateFuelClassList(){
			mFuelClsIds = null;
			mFuelClasses = null;
			mFuelClsAdapter = null;
			int size = MainActivity.mFuelClasses.size();
			mFuelClasses = new String[size];		
			mFuelClsIds = new int[size];
			Iterator<Integer> iterator = MainActivity.mFuelClasses.keySet().iterator();
			int i = 0;
			while(iterator.hasNext()){
				int id = iterator.next();
				mFuelClsIds[i] = id;
				mFuelClasses[i] = MainActivity.mFuelClasses.get(id);
				i++;
			}		
			mFuelClsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,
					mFuelClasses);			
			mFuelClsView.setAdapter(mFuelClsAdapter);
		}
		
		public void updatFuelList(){
			mFuels = null;
			mFuelTypeAdapter = null;
			mFuels = new Fuel[MainActivity.mFuels.size()];
			MainActivity.mFuels.values().toArray(mFuels);	
			mFuelTypeAdapter = new ArrayAdapter<Fuel>(getActivity(), android.R.layout.simple_list_item_1,mFuels);
			mFuelTypeView.setAdapter(mFuelTypeAdapter);
		}
				
		
		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
			inflater.inflate(R.menu.manager_menu_option_fuel, menu);
		}
		
		@Override
		public boolean onOptionsItemSelected (MenuItem item){
			switch (item.getItemId()) {
			case R.id.mnu_mgr_addfuel:
				addFuel();
				break;
			case R.id.mnu_mgr_addfuel_class:
				addFuelClass();
				break;
			default:
				break;
			}
			return true;
		}
		
		@Override
		public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
			super.onCreateContextMenu(menu, v, menuInfo);
		    MenuInflater inflater = getActivity().getMenuInflater();
		    inflater.inflate(R.menu.manager_menu_context_fuel, menu);
		}
		
		@Override
		public boolean onContextItemSelected (MenuItem item){
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			int id = mFuels[info.position].ID();
			switch (item.getItemId()) {
			case R.id.mnu_mgr_removefuel:				
				removeFuel(id);
				break;
			default:
				break;
			}
			return true;
		}
		
		/**
		 * 添加燃料种类
		 */
		private void addFuel(){
			final Spinner fuelClsSpinner = new Spinner(getActivity());
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), 
					android.R.layout.simple_spinner_item, mFuelClasses);
			fuelClsSpinner.setAdapter(adapter);
			LinearLayout layout1 = new LinearLayout(getActivity());
			LinearLayout layoutMain = new LinearLayout(getActivity());
			layoutMain.setOrientation(1);
			TextView textView = new TextView(getActivity());
			textView.setText("燃料标号");
			final EditText gradeEdit = new EditText(getActivity());
			layout1.addView(textView,ViewGroup.LayoutParams.WRAP_CONTENT);
			layout1.addView(gradeEdit,ViewGroup.LayoutParams.MATCH_PARENT);
			layoutMain.addView(fuelClsSpinner);
			layoutMain.addView(layout1);
			
			
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
			.setTitle("添加燃料种类")
			.setView(layoutMain)
			.setPositiveButton(R.string.btn_text_ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String grade = gradeEdit.getText().toString();
					if(grade.isEmpty()){
						Toast.makeText(getActivity(), "未填写燃料标号", Toast.LENGTH_SHORT);
						return;
					}
					int curFuelClsId = mFuelClsIds[fuelClsSpinner.getSelectedItemPosition()];
					ContentResolver resolver = getActivity().getContentResolver();
					ContentValues values = new ContentValues();
					values.put(Fuels.CLASS, curFuelClsId);
					values.put(Fuels.GRADE, grade);
					Uri uri = resolver.insert(Fuels.CONTENT_URI, values);
					int id = (int)ContentUris.parseId(uri);
					Fuel fuel = new Fuel(id, curFuelClsId,MainActivity.mFuelClasses.get(curFuelClsId), grade);
					MainActivity.mFuels.put(id, fuel);
					updatFuelList();
				}
			})
			.setNegativeButton(R.string.btn_text_cancel, new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}			
			});
			builder.create().show();
		}
		
		/** 添加燃料类别 */
		private void addFuelClass(){
			final EditText editText = new EditText(getActivity());
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
			.setTitle("输入燃料类别名称")
			.setView(editText)
			.setPositiveButton(R.string.btn_text_ok, new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String name = editText.getText().toString();
					if(name.isEmpty()){
						Toast.makeText(getActivity(), "名称不能为空", Toast.LENGTH_SHORT);
						return;
					}
					ContentResolver resolver = getActivity().getContentResolver();
					ContentValues values = new ContentValues();
					values.put(FuelClasses.NAME, name);
					Uri uri = resolver.insert(FuelClasses.CONTENT_URI, values);
					int id = (int)ContentUris.parseId(uri);
					MainActivity.mFuelClasses.put(id, name);
					updateFuelClassList();
				}
			})
			.setNegativeButton(R.string.btn_text_cancel, new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			builder.create().show();
		}
		
		private void removeFuel(int id){
			ContentResolver resolver = getActivity().getContentResolver();
			Uri uri = ContentUris.withAppendedId(Fuels.CONTENT_URI, id);
			int affected = resolver.delete(uri, null, null);
			MainActivity.mFuels.remove(id);
			updatFuelList();
		}
 	}
    
	/**
	 * 管理车辆信息的Fragment
	 * @author wangkun
	 *
	 */
	public static class CarMgrFragment extends Fragment implements AdapterView.OnItemClickListener{
		private ListView mCarView;
		private Car[] mCars;
		private ArrayAdapter<Car> mCarAdapter;
		
		public CarMgrFragment(){			
			setHasOptionsMenu(true);			
		}
	 
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
			View view;
			view = inflater.inflate(R.layout.activity_manager_car, container, false);
			mCarView = (ListView)view.findViewById(R.id.mgr_car_lv);
			updateCarList();
			mCarView.setOnItemClickListener(this);
			registerForContextMenu(mCarView);
			return view;
		}
		
		/** 更新车辆列表 */
		public void updateCarList(){
			mCars = null;
			mCars = new Car[MainActivity.mCars.size()];
			MainActivity.mCars.values().toArray(mCars);
			mCarAdapter = null;
			mCarAdapter = new ArrayAdapter<Car>(getActivity(), android.R.layout.simple_list_item_1,mCars);
			mCarView.setAdapter(mCarAdapter);
		}
		
		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
			inflater.inflate(R.menu.manager_menu_option_car, menu);
		}
		
		@Override
		public boolean onOptionsItemSelected (MenuItem item){
			switch (item.getItemId()) {
			case R.id.mnu_mgr_addcar:
				showCarInfomation(0, false);
				break;

			default:
				break;
			}
			return true;
		}
		
		@Override
		public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
			super.onCreateContextMenu(menu, v, menuInfo);
		    MenuInflater inflater = getActivity().getMenuInflater();
		    inflater.inflate(R.menu.manager_menu_context_car, menu);
		}
		
		@Override
		public boolean onContextItemSelected (MenuItem item){
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			int id = mCars[info.position].ID();
			switch (item.getItemId()) {
			case R.id.mnu_mgr_editcar:				
				showCarInfomation(id, false);
				break;
			case R.id.mnu_mgr_removecar:
				removeCar(id);
				break;
			default:
				break;
			}
			return true;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if(parent != mCarView)
				return;
			int carId = mCars[position].ID();
			showCarInfomation(carId,true);
		}
		
		/**
		 * 显示或编辑车辆信息
		 * @param id 车辆id
		 * @param readOnly	是否只读
		 */
		public void showCarInfomation(int id, boolean readOnly){
			Intent intent = new Intent(getActivity(),CarInfoEditor.class);
			intent.putExtra(CarInfoEditor.CAR_INFO_TAG_CARID, id);
			intent.putExtra(CarInfoEditor.CAR_INFO_TAG_READONLY, readOnly);
			startActivityForResult(intent, REQUEST_CODE_CAR_INFO);
		}
		
		/**
		 * 删除车辆及其相关的加油记录
		 * @param id		车辆id
		 * @param isInclude	true：删除该车辆的加油记录
		 */
		private void removeCar(int id){
			final int carId = id;
			final CheckBox includedBtn = new CheckBox(getActivity());
			includedBtn.setText("同时删除该车辆的加油记录");
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
			.setTitle("删除车辆 " + MainActivity.mCars.get(id).toString())
			.setView(includedBtn)
			.setPositiveButton(R.string.btn_text_ok, new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ContentResolver resolver = getActivity().getContentResolver();
					Uri uri;
					int affected = 0;
					if(includedBtn.isChecked()){
						uri = ContentUris.withAppendedId(ToFuelRecords.CONTENT_URI_CARS, carId);						
						affected =  resolver.delete(uri, null, null);
					}
					MainActivity.mCars.remove(carId);
					uri = ContentUris.withAppendedId(VehicleInfos.CONTENT_URI, carId);
					affected = 0;
					affected = resolver.delete(uri, null, null);
					updateCarList();
				}
			});
			builder.create().show();
		}
		
		@Override 
		public void onActivityResult(int requestCode, int resultCode, Intent data){			
			if(requestCode != REQUEST_CODE_CAR_INFO)
				return;
			if(requestCode == RESULT_CODE_CANCEL)
				return;
			if(mCars.length != MainActivity.mCars.size()){
				updateCarList();
			}
			
		}
	}
	

}
