package com.ssc.mycarassistant;

import com.ssc.mycarassistant.db.CarAssistant.ToFuelStationColumns;
import com.ssc.mycarassistant.db.CarAssistant.ToFuelStations;
import com.ssc.mycarassistant.model.FuelStation;

import android.R.integer;
import android.R.raw;
import android.R.string;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link StationChoiceFragment.OnFragmentInteractionListener} interface to
 * handle interaction events.
 * 
 */
public class StationChoiceFragment extends Fragment implements OnClickListener, OnItemClickListener{

	private int mStationId = -1;
	private OnSelectStationListener mListener;
	//private SimpleCursorAdapter adapter;
	private ArrayAdapter<FuelStation> adapter;
	private ListView mStationView;
	private Button mBtnLocation,mBtnOk,mBtnCancel;
	private TextView mNameEdit,mAddrEdit;
	private TextView mLatEdit,mLonEdit;
	private FuelStation[] mStations;
	private ViewGroup mSubView_list,mSubView_new;
	private ViewGroup container;

	public StationChoiceFragment() {
		//Bundle bundle = getArguments();
		
		//mStations = stations;
	}
	public static StationChoiceFragment getInstance(){
		StationChoiceFragment fragment = new StationChoiceFragment();
		
		return fragment;
	}
	
	public void setStationId(int id){mStationId=id;}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化内部加油站对象数组
        mStations = new FuelStation[ToFuelMgr.mStations.size()];
        ToFuelMgr.mStations.values().toArray(mStations);
        //mNum = getArguments() != null ? getArguments().getInt("num") : 1;
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.select_station_view, container,false);
		this.container = (ViewGroup)view.findViewById(R.id.select_station_container_view);
		mStationView = (ListView)view.findViewById(R.id.select_station_listview);
		mSubView_list = (ViewGroup)view.findViewById(R.id.select_station_subview_list);
		mSubView_new = null;
		
		adapter = new ArrayAdapter<FuelStation>(getActivity(), 
				android.R.layout.simple_list_item_single_choice,mStations);
		
		//这个使用光标适配器来直接从数据库读取所有加油站
//		ContentResolver resolver = getActivity().getContentResolver();
//		Cursor cursor = resolver.query(ToFuelStations.CONTENT_URI, null, null, null, null);
//		String[] names = new String[]{ToFuelStations.NAME};
//		int[] tos = new int[]{android.R.id.text1};
//		adapter = new SimpleCursorAdapter(getActivity(),
//				android.R.layout.select_dialog_singlechoice, cursor, names, tos, 0);
		
		mStationView.setAdapter(adapter);
		mStationView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		setChoicedItem();
		mStationView.setOnItemClickListener(this);
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnSelectStationListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnSelectStationListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated to
	 * the activity and potentially other fragments contained in that activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnSelectStationListener {
		// TODO: Update argument type and name
		public void onSelectChanged(int id);
	}

	@Override
	public void onClick(View v) {
		if(v == mBtnOk){
			String name = mNameEdit.getText().toString();
			String addr = mAddrEdit.getText().toString();
			double lat=0,lon=0;
			if(!mLatEdit.getText().toString().isEmpty())
				lat = Double.parseDouble(mLatEdit.getText().toString());
			if(!mLonEdit.getText().toString().isEmpty())
				lon = Double.parseDouble(mLonEdit.getText().toString());
			ContentResolver resolver = getActivity().getContentResolver();
			ContentValues values = new ContentValues();
			values.put(ToFuelStations.NAME, name);
			values.put(ToFuelStations.ADDRESS, addr);
			values.put(ToFuelStations.LAT, lat);
			values.put(ToFuelStations.LON, lon);
			values.put(ToFuelStations.COORDINATE_CLASS, COODINATIONTYPE.CT_GPS.toName());
			Uri uri = resolver.insert(ToFuelStations.CONTENT_URI, values);
			long id = ContentUris.parseId(uri);
			if(id == 0){
				Toast.makeText(getActivity(), "保存到数据库失败！", Toast.LENGTH_SHORT);
				return;
			}
			
			FuelStation station = new FuelStation((int)id, name, addr);
			station.setCoodination(lat, lon);
			station.setCoodinationType(COODINATIONTYPE.CT_GPS);
			ToFuelMgr.mStations.put((int)id, station);
			
			//好像视图被移除过后，连适配器也被销毁了？，因此导致空指针异常
			//adapter.clear();
			//adapter.add(station);
			//adapter.notifyDataSetChanged();  //这样调用但加油站列表视图并未更新(使用的是光标适配器)？	
			mStations = null;
			mStations = new FuelStation[ToFuelMgr.mStations.size()];
			ToFuelMgr.mStations.values().toArray(mStations);
			adapter = new ArrayAdapter<FuelStation>(getActivity(), 
					android.R.layout.simple_list_item_single_choice,mStations);
			mStationView.setAdapter(adapter);			
			container.removeView(mSubView_new);
			container.addView(mSubView_list);
			mStationId = (int)id;
			setChoicedItem();
			mListener.onSelectChanged(mStationId);
			//打开加油站编辑时，要定位到当前选择的加油站，并视觉上显示出。
			//调整加油站列表框的尺寸、还有单项尺寸等
		}
		else if(v == mBtnCancel){
			container.removeView(mSubView_new);
			container.addView(mSubView_list);
		}
		else { //定位按钮
			//输入一个模拟位置，实际应该从地图视图中获取
			mLatEdit.setText("29.90714");
			mLonEdit.setText("121.53096");
		}
	}
	
	/**
	 * 设置当前选定的加油站
	 * @param id 加油站的id
	 */
	private void setChoicedItem(){
		if(mStations.length == 0 || mStationId == -1)
			return;
		int pos = -1;
		for(int i = 0; i < mStations.length; ++i){
			if(mStations[i].ID() == mStationId){
				pos = i;
				break;
			}
		}
		if(pos == -1)
			return;
		mStationView.setItemChecked(pos, true);		//好像必须用此句来完成想要的改变默认选中项的功能
		mStationView.smoothScrollToPosition(pos);	//使选中项可见
	}
	
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// 当用户选择一个项目后，关闭此Framment
		mStationId = mStations[position].ID();
		mListener.onSelectChanged(mStationId);
	}
	
	public void swithNewStationView(){
		//切换视图到新建加油站
		if(mSubView_new == null){
			LayoutInflater inflater = getActivity().getLayoutInflater();
			mSubView_new = (ViewGroup)inflater.inflate(R.layout.select_station_subview_create, container, false);
			mBtnLocation = (Button)mSubView_new.findViewById(R.id.select_station_location);
			mNameEdit = (EditText)mSubView_new.findViewById(R.id.select_station_view_name);
			mAddrEdit = (EditText)mSubView_new.findViewById(R.id.select_station_view_addr);
			mLatEdit = (TextView)mSubView_new.findViewById(R.id.select_station_view_lat);
			mLonEdit = (TextView)mSubView_new.findViewById(R.id.select_station_view_lon);
			mBtnLocation = (Button)mSubView_new.findViewById(R.id.select_station_location);
			mBtnOk = (Button)mSubView_new.findViewById(R.id.select_station_btnOk);
			mBtnCancel = (Button)mSubView_new.findViewById(R.id.select_station_btnCancel);
			mBtnLocation.setOnClickListener(this);
			mBtnOk.setOnClickListener(this);
			mBtnCancel.setOnClickListener(this);
		}
		container.removeView(mSubView_list);
		container.addView(mSubView_new);
	}
}
