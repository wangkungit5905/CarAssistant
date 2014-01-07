package com.ssc.widgets;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;

/**
 * 多选一对话框类
 * @author wangkun
 *
 */
public class SingelChoiceDialog extends AlertDialog {
	private Context mContext;
	AlertDialog mInstance;
	private Object[] mItems;
	private ArrayAdapter<Object> mAdapter;
	private Object mChoiceItem;
	
	private OnCancelListener mCancelListener;
	private OnDismissListener mDismissListener;
	
	public SingelChoiceDialog(Context context, Object[] items) {
		super(context);
		mContext = context;
		mItems = items;
		mAdapter = new ArrayAdapter<Object>(mContext, android.R.layout.simple_list_item_1,mItems);		
	}
	
	@Override
	public void cancel(){
		if(mCancelListener != null)
			mCancelListener.onCancel(this);
		mInstance.cancel();
	}
	
	@Override
	public void dismiss(){
		if(mDismissListener != null)
			mDismissListener.onDismiss(this);
		mInstance.dismiss();
	}
	
	public AlertDialog getInstance(String title,OnDismissListener dismissListener,OnCancelListener cancelListener){		
		mDismissListener = dismissListener;
		mCancelListener = cancelListener;
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
		.setTitle(title)
		.setSingleChoiceItems(mAdapter, 1, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mChoiceItem = mItems[which];
				dismiss();				
			}
		});		
		mInstance = builder.create();
		return mInstance;
	}
	
	public void setOnDismissListener(OnDismissListener listener){
		mDismissListener = listener;
	}
	
	public void setOnCancelListener(OnCancelListener listener){
		mCancelListener = listener;
	}
	
	public Object getChoiceItem(){
		return mChoiceItem;
	}

}
