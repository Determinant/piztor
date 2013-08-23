package com.example.piztor;

import java.io.*;
import android.content.*;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.util.*;
import android.view.*;

public class MyView extends View {
	static PrintStream cout = System.out;
	private Paint mPaint;
	public Canvas c = null;
	public Bitmap b = null;
	public double scale = 100;
	public double centerX, centerY;
//	Vector<PointF> v;

	void setup(Canvas c, Bitmap b, double x, double y) {
		this.c = c;
		this.b = b;
		centerX = x;
		centerY = y;
	}

	public MyView(Context context) {
		super(context);
//		v = new Vector<PointF>();
		mPaint = new Paint();
		mPaint.setColor(Color.RED);
		mPaint.setStyle(Style.FILL);
	}

	public MyView(Context context, AttributeSet attr) {
		super(context, attr);
//		v = new Vector<PointF>();
		mPaint = new Paint();
		mPaint.setColor(Color.RED);
		mPaint.setStyle(Style.FILL);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawBitmap(b, 0, 0, mPaint);
		// canvas.restore();
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
//		v.add(new PointF(e.getX(), e.getY()));
		c.drawRect(e.getX() - 1, e.getY() + 1, e.getX() + 1, e.getY() - 1,
				mPaint);
		invalidate();
		return true;
	}

	void drawLocation(double x, double y) {
		if (centerX < 0) {
			centerX = x;
			centerY = y;
		}
		int x1 = (int)(getWidth() / 2 + (x - centerX) * scale);
		int y1 = (int)(getHeight() / 2 + (y - centerY) * scale);
		c.drawRect(x1 - 1, y1 + 1, x1 + 1, y1 - 1, mPaint);
		invalidate();
	}
	
	void drawString(String s) {
		mPaint.setTextSize(15);
		c.drawText(s, 0, getHeight() / 2, mPaint);
		invalidate();
	}
	
}