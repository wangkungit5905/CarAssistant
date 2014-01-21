package com.ssc.widgets.chart;

import android.R.color;
import android.R.integer;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.DateSorter;

public class ChartView extends View {

	public static final int CYLINDER_WIDTH = 10;		//直方条宽度
	public static final int MAX_FUEL_CONSUMPTION = 20;	//最大耗油量（升/百公里）
	private float baseCoast,warningCoast;		//基准、警告油耗值
	private int color_baseColor = Color.BLUE;
	private int color_warningCaost = Color.RED;
	private int color_averageCurve = Color.YELLOW;
	
	private boolean flag ;			//是使用直方图还是用曲线形式
	private int margin ;			//直方条之间的距离
	private int leftMargin = 30;	//直方图四周的填充区域宽度
	private int rightMargin = 30;
	private int topMargin = 20;
	private int bottonMargin = 100;
	private int totalHeight;		//可用于绘制圆柱条的高度
	private int totalWidth;			//可用于绘制圆柱条的宽度
	private int yScaleLength = 6;	//y轴刻度线的长度
	private int xStep = 10;			//在使用曲线来展示统计图时，每个数据在x方向上的增量长度

	private Paint paint ;
	private int w,h;
	private float mTotalMoney;
	
	private int[][] mDates;  //加油日期表（第二维的第一个元素是月份，第二个元素是日期）
	private float[] mDatas;		//油耗数据
	private float maxValue;		//值的上界
	private float[] mAverageDatas; //平均油耗数值表

	public ChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		flag = true ;
		margin = 10 ;
		mDatas = new float[]{};
		paint = new Paint() ;
		paint.setAntiAlias(true) ;
	}
	
	public void setBaseCoast(Float coast){
		baseCoast = coast;		
	}
	
	public void setWarningCoast(float coast){
		warningCoast = coast;
	}
	
	public void setTopRange(float top){
		maxValue = top;
	}
	
	public void setTotalMoney(float money){
		mTotalMoney = money;
	}
	
	public void setData(float[] datas, int[][] dates, float[] averages){
		if(datas.length != dates.length)
			return;
		mDates = dates;
		mDatas = datas;
		mAverageDatas = averages;
		invalidate();
	}
	
	/** 绘制坐标轴及其刻度 */
	public void drawAxis(Canvas canvas) {		
		paint.setColor(Color.YELLOW) ;
		paint.setStrokeWidth(2) ;
		canvas.drawLine(leftMargin, h - bottonMargin, w - rightMargin, h - bottonMargin, paint) ; //x
		canvas.drawLine(leftMargin, topMargin, leftMargin, h - bottonMargin, paint) ;			  //y

		int xnums,ynums,topValue;
		if(mDatas == null || mDatas.length == 0){
			return;
		}
		else{
			xnums = mDatas.length;
			topValue = (int)maxValue;
		}
		ynums = topValue/5;
		if(topValue%5 != 0)
			ynums++;
		//如果可以绘制整个时间范围的直方图，则x轴显示每次加油的日期
		int x = leftMargin + margin;
		int y1 = h-bottonMargin+15;		//绘制月份数的高度位置
		int y2 = h-bottonMargin+25;		//绘制日期数的高度位置		
		int i = 0;
		if(flag){	
			canvas.drawText("月", 0, y1, paint);
			canvas.drawText("日", 0, y2, paint);
			int dx = CYLINDER_WIDTH + margin;
			for (i = 0; i < xnums; i++) {	//x轴刻度数据
				canvas.drawText(Integer.toString(mDates[i][0]), x, y1, paint) ;
				canvas.drawText(Integer.toString(mDates[i][1]), x, y2, paint) ;
				x += dx ;
			}		
		}
		else{ //如果是绘制油耗曲线，则在x轴绘制加油月份		
			canvas.drawText("月", 0, y1, paint);
			canvas.drawText(Integer.toString(mDates[i][0]), x, y1, paint);
			boolean diff = false;
			int curMonth = mDates[0][0];
			for(i = 1; i < mDates.length; ++i){
				x+=xStep;
				if(curMonth != mDates[i][0]){
					curMonth = mDates[i][0];
					canvas.drawText(Integer.toString(curMonth), x, y1, paint);					
				}
			}
			
		}
		
		int dy = (h - topMargin - bottonMargin) / ynums;
		int y = h-bottonMargin-dy ;		
		for (i = 0; i < ynums; i++) {	//y轴刻度数据
			canvas.drawText(5 * (i + 1) + "", 10, y, paint) ;
			y -= dy ;
		}
		
		//y轴刻度线
		float[] pts = new float[ynums*4*5] ;
		float delY = (float)dy/5;
		float sx = leftMargin+1;
		float sy = h - bottonMargin - delY;
		for (i = 0; i < ynums*5; i++) {
			if((i+1)%5==0){
				pts[4*i] = sx;
				pts[4*i+1] = sy;
				pts[4*i+2] = sx + yScaleLength;
				pts[4*i+3] = sy;
			}
			else{
				pts[4*i] = sx;
				pts[4*i+1] = sy;
				pts[4*i+2] = sx + yScaleLength/2;
				pts[4*i+3] = sy;
			}			
			sy -= delY;
		}
		canvas.drawLines(pts, paint) ;
		canvas.drawText("升/百公里", leftMargin+1, topMargin-1, paint);
	}

	//绘制统计图（如果整个视图的宽度允许，则用直方图形式，否则用曲线形式）
	public void drawChart(Canvas canvas) {
		//绘制基准警告油耗线
		paint.setColor(color_baseColor);
		float y = h - bottonMargin - totalHeight*(baseCoast/maxValue);
		canvas.drawLine(leftMargin, y,w-rightMargin, y, paint);
		paint.setColor(color_warningCaost);
		y = h - bottonMargin - totalHeight*(warningCoast/maxValue);
		canvas.drawLine(leftMargin, y,w-rightMargin, y, paint);
				
		int startX =  leftMargin + margin;
		if (flag) {
			paint.setColor(Color.GREEN) ;			
			for (int i = 0; i < mDatas.length; i++) {
				int h1 = (int)((float)totalHeight * (mDatas[i]/maxValue));
				canvas.drawRect(startX, h-bottonMargin-h1, startX + CYLINDER_WIDTH, h-bottonMargin - 1, paint) ;
				startX = startX + CYLINDER_WIDTH + margin;
			}
		} 
		else{
			paint.setColor(Color.GREEN) ;
			int h1 = (int)((float)totalHeight * (mDatas[0]/maxValue));
			float startY = h-bottonMargin-h1;
			int endX = startX;
			int endY;
			for(int i = 1; i < mDatas.length; ++i){		
				endX += xStep;
				h1 = (int)((float)totalHeight * (mDatas[i]/maxValue));
				endY = h-bottonMargin-h1;
				canvas.drawLine(startX, startY, endX, endY, paint);
				startX = endX;
				startY = endY;
			}
		}
		
	}
	
	private void drawAverageCurve(Canvas canvas){
		if(mAverageDatas == null || mAverageDatas.length < 2)
			return;
		paint.setColor(color_averageCurve);
		float dx;
		if(flag)
			dx = margin + CYLINDER_WIDTH;
		else
			dx = xStep;
		float sx = leftMargin;
		float sy = h - bottonMargin - totalHeight*(mAverageDatas[0]/maxValue);
		float ex=sx,ey=0;
		for(int i = 1; i < mAverageDatas.length; ++i){
			ex += dx;
			ey = h - bottonMargin - totalHeight*(mAverageDatas[i]/maxValue);
			canvas.drawLine(sx, sy, ex, ey, paint);
			sx = ex;
			sy = ey;
		}
	}
	
	public void drawTotalMoney(Canvas canvas){
		int x = leftMargin + 4*margin;
		int y = h - bottonMargin + 40;
		canvas.drawText("合计金额：" + Float.toString(mTotalMoney), x, y, paint);
	}

	@Override
	public void onDraw(Canvas canvas) {
		w = canvas.getWidth();
		h = canvas.getHeight();
		totalHeight = h - topMargin - bottonMargin;
		totalWidth = w - leftMargin - rightMargin;
		if(mDatas.length < totalWidth/(margin+CYLINDER_WIDTH))
			flag = true;
		else 
			flag = false;		
		canvas.drawColor(Color.BLACK) ;
		drawAxis(canvas);
		if(mDatas.length > 0){					
			drawChart(canvas) ;
			drawTotalMoney(canvas);
			drawAverageCurve(canvas);
		}		
	}
}
