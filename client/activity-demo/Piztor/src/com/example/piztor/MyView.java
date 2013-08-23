package com.example.piztor;

import java.io.PrintStream;
import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class MyView extends View {
	static PrintStream cout = System.out;
	private Paint mPaint, oPaint;
	public Canvas c = null;
	public Bitmap b = null;
	public double scale = 1;
	public double centerX, centerY;
	
	
	Point myLocation;
	Vector<Point> Location;

	void setup(Canvas c, Bitmap b, double x, double y) {
		this.c = c;
		this.b = b;
		centerX = x;
		centerY = y;
		myLocation = new Point(10, 10);
		Location = new Vector<Point>();
		Location.add(myLocation);
	}

	public MyView(Context context) {
		super(context);
		// v = new Vector<PointF>();
		mPaint = new Paint();
		mPaint.setColor(Color.RED);
		mPaint.setStyle(Style.FILL);
		oPaint = new Paint();
		oPaint.setColor(Color.BLUE);
		oPaint.setStyle(Style.FILL);
	}

	public MyView(Context context, AttributeSet attr) {
		super(context, attr);
		// v = new Vector<PointF>();
		mPaint = new Paint();
		mPaint.setColor(Color.RED);
		mPaint.setStyle(Style.FILL);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		cout.println(myLocation.x + "   --   " + myLocation.y);
		canvas.drawCircle(myLocation.x, myLocation.y, 5, mPaint);
		//canvas.drawBitmap(b, 0, 0, mPaint);
		/*for (int i = 1; i < Location.size(); i++) {
			canvas.drawCircle(Location.get(i).x, Location.get(i).y, 2, oPaint);
		}*/
		// canvas.restore();
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		// v.add(new PointF(e.getX(), e.getY()));
		c.drawRect(e.getX() - 1, e.getY() + 1, e.getX() + 1, e.getY() - 1,
				mPaint);
		
		invalidate();
		return true;
	}

	void changMyLocation(double x, double y) {
		if (centerX < 0) {
			centerX = x;
			centerY = y;
		}
		int x1 = (int) (getWidth() / 2 + (x - centerX) * scale);
		int y1 = (int) (getHeight() / 2 + (y - centerY) * scale);
		myLocation.x = x1;
		myLocation.y = y1;
		cout.println(x + " " + y);
		invalidate();
	}

	void drawString(String s) {
		mPaint.setTextSize(15);
		c.drawText(s, 0, getHeight() / 2, mPaint);
		invalidate();
	}

}