package com.macaroon.piztor;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.ImageView;

public class MultiTouchListener implements OnTouchListener {

	private Matrix matrix = new Matrix();
	private Matrix preMatrix = new Matrix();

	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;
	private int mode = NONE;

	private PointF start = new PointF();
	private PointF mid = new PointF();
	private float preDis = 1f;
	private float d = 0f;
	private float newRot = 0f;
	private float[] values;
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		float[] values = new float[9];
        matrix.getValues(values);
        //System.out.println("1111111"+values[1]);
		//Log.d("Touch", "onTouch.......");
		ImageView view = (ImageView) v;
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				//Log.d("Touch", "ACTION_DOWN");
				preMatrix.set(matrix);
				start.set(event.getX(), event.getY());
				mode = DRAG;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				//Log.d("Touch", "ACTION_POINTER_DOWN");
				preDis = spacing(event);
				if(preDis > 10f) {
					preMatrix.set(matrix);
					midPoint(mid, event);
					mode = ZOOM;
				}
				break;
			case MotionEvent.ACTION_UP:
				//Log.d("Touch", "ACTION_UP");
			case MotionEvent.ACTION_POINTER_UP:
				//Log.d("Touch", "ACTION_POINTER_UP");
				mode = NONE;
				break;
			case MotionEvent.ACTION_MOVE:
				//Log.d("Touch", "ACTION_MOVE");
				if(mode == DRAG) {
					//Log.d("Touch", "Draging");
					matrix.set(preMatrix);
					float dx = event.getX() - start.x;
					float dy = event.getY() - start.y;
					matrix.postTranslate(dx, dy);
				} else if (mode == ZOOM) {
					//Log.d("Touch","Zooming");
					float newDis = spacing(event);
					if (newDis > 10f) {
						matrix.set(preMatrix);
						float scale = (newDis / preDis);
						matrix.postScale(scale,scale,mid.x,mid.y);
						//System.out.println("ssssssssssssssssss" + scale);
					}
				}
				break;
		}
		matrix.getValues(values);
        //System.out.println("222222"+values[1]);
		view.setImageMatrix(matrix);
		matrix.getValues(values);
        //System.out.println("333333"+values[1]);
		return true;
	}

	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getY(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}
}
