package com.example.drawingboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;

import java.util.ArrayList;


public class PaintView extends View {

    private static final float STROKE_WIDTH = 10f;
    private Canvas mCanvas;
    private Paint paint = new Paint();
    private Path mPath = new Path();
    private Paint mBitmapPaint;
    private Bitmap mBitmap;
    ArrayList<Path> mPaths = new ArrayList<Path>();
    ArrayList<Float> mStrokes = new ArrayList<Float>();

    private float lastTouchX;
    private float lastTouchY;
    private final RectF dirtyRect = new RectF();
    private int lastStroke = -1;
    int variableWidthDelta = 0;

    private static final float STROKE_DELTA = 0.001f; // for float comparison
    private static final float STROKE_INCREMENT = 0.2f; // amount to interpolate
    private float currentStroke = STROKE_WIDTH;
    private float targetStroke = STROKE_WIDTH;

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private int bitmapWidth;
    private int bitmapHeight;

    private int m_force;



    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);

        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);

        bitmapWidth = dm.widthPixels;
        bitmapHeight = dm.heightPixels - 2 * 45;

        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        mBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight,
                Bitmap.Config.RGB_565);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(STROKE_WIDTH);
    }

    public void clear() {
        mPath.reset();
        // Repaints the entire view.
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas)  {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

        for(int i=0; i<mPaths.size();i++) {

//            System.out.println("Stroke width: "+mPaths.size());

            paint.setStrokeWidth(mStrokes.get(i));
            canvas.drawPath(mPaths.get(i), paint);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        int historySize = event.getHistorySize();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                resetDirtyRect(eventX, eventY);
//                mPath.reset();
                mPath.moveTo(eventX, eventY);
                mX = eventX;
                mY = eventY;

                break;
            }
            case MotionEvent.ACTION_MOVE: {

                float normalization = (float) 20*(m_force-670)/(780-670)-3;

                variableWidthDelta = (int) normalization;
                targetStroke = variableWidthDelta;

                if (currentStroke > 0) {
                    // if current not roughly equal to target
                    if( Math.abs(targetStroke - currentStroke) > STROKE_DELTA ) {
                        // move towards target by the increment
                        if( targetStroke > currentStroke) {
                            currentStroke = Math.min(targetStroke, currentStroke + STROKE_INCREMENT);
                        }
                        else {
                            currentStroke = Math.max(targetStroke, currentStroke - STROKE_INCREMENT);
                        }

                    }
                } else {
                    currentStroke = (float) 0.8 * variableWidthDelta;
                }


                mStrokes.add(currentStroke);

                mPath.lineTo(mX, mY);

                mPath = new Path();
                mPath.moveTo(mX,mY);
                mPaths.add(mPath);

                mPath.quadTo(mX, mY, (eventX + mX)/2, (eventY + mY)/2);
                mX = eventX;
                mY = eventY;


//                float dx = Math.abs(eventX - mX);
//                float dy = Math.abs(eventY - mY);
////
//                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
//                    if(lastStroke != variableWidthDelta) {
//                        mPath.lineTo(mX, mY);
//
//                        mPath = new Path();
//                        mPath.moveTo(mX,mY);
////                        mPaths.add(mPath);
//                    }
//
//                    mPath.quadTo(mX, mY, (eventX + mX)/2, (eventY + mY)/2);
//                    mX = eventX;
//                    mY = eventY;
//                }
//
//                mPaths.add(mPath);

//
//                System.out.println(mPaths.size()+","+mStrokes.size());

                for (int i = 0; i < historySize; i++) {
                    float historicalX = event.getHistoricalX(i);
                    float historicalY = event.getHistoricalY(i);
                    expandDirtyRect(historicalX, historicalY);
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                for (int i = 0; i < historySize; i++) {
                    float historicalX = event.getHistoricalX(i);
                    float historicalY = event.getHistoricalY(i);
                    expandDirtyRect(historicalX, historicalY);
                }
                mPath.lineTo(mX, mY);

                break;
            }
        }

        // Include half the stroke width to avoid clipping.
        invalidate();

        lastTouchX = eventX;
        lastTouchY = eventY;
        lastStroke = variableWidthDelta;

        return true;
    }

    private void expandDirtyRect(float historicalX, float historicalY) {
        if (historicalX < dirtyRect.left) {
            dirtyRect.left = historicalX;
        }  else if (historicalX > dirtyRect.right) {
            dirtyRect.right = historicalX;
        }
        if (historicalY < dirtyRect.top) {
            dirtyRect.top = historicalY;
        } else if (historicalY > dirtyRect.bottom) {
            dirtyRect.bottom = historicalY;
        }
    }

    /**
     * Resets the dirty region when the motion event occurs.
     */
    private void resetDirtyRect(float eventX, float eventY) {
        // The lastTouchX and lastTouchY were set when the ACTION_DOWN
        // motion event occurred.
        dirtyRect.left = Math.min(lastTouchX, eventX);
        dirtyRect.right = Math.max(lastTouchX, eventX);
        dirtyRect.top = Math.min(lastTouchY, eventY);
        dirtyRect.bottom = Math.max(lastTouchY, eventY);
    }

    public void setStroke(int force) {
        m_force = force;
    }

    public void setColour(int colour) {
        paint.setColor(colour);
    }
}

