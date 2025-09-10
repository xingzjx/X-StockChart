package com.example.sample.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

// ... 原有import保持不变 ...
import android.graphics.PointF;

public class DrawView2 extends View {
    // 修改当前坐标记录方式为PointF
    private PointF mCurrentPoint = new PointF(-100, -100);
    // 新增圆形画笔
    private Paint mCirclePaint;
    private static final float CIRCLE_RADIUS = 20f; // 圆形半径
    private List<Float> mHorizontalLines = new ArrayList<>();
    private Paint mPaint;
    private DrawState mDrawState = DrawState.NONE;

    public void setDrawState(DrawState state) {
        mDrawState = state;
    }

    public enum DrawState {// NONE表示不绘制，DRAWING表示正在绘制，EDIT表示编辑完成等待下一步操作
        NONE, DRAWING, EDIT
    }

    public DrawView2(Context context) {
        super(context);
        init();
    }

    public DrawView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawView2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        // 原有线条画笔初始化
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(4f);
        mPaint.setStyle(Paint.Style.STROKE);

        // 新增圆形画笔初始化
        mCirclePaint = new Paint();
        mCirclePaint.setColor(Color.RED);
        mCirclePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("DrawView2", "onTouchEvent: " + event.getAction() + ", DrawState: " + mDrawState);
        if ((mDrawState == DrawState.NONE || mDrawState == DrawState.EDIT) && event.getAction() == MotionEvent.ACTION_DOWN) {// 记录点击的位置
            // 点击的位置在历史的水平线上，则进入编辑状态
            boolean isHit = false;
            for (Float y : mHorizontalLines) {
                if (Math.abs(event.getY() - y) < CIRCLE_RADIUS) {
                    isHit = true;
                    mDrawState = DrawState.DRAWING;
                    // mDrawShape = DrawShape.HORIZONTAL_LINE;
                    mCurrentPoint.set(event.getX(), y);
                    invalidate();
                    // 清理当前水平线
                    mHorizontalLines.remove(y);
                    // return true;
                }
            }
            if (!isHit) {
                mCurrentPoint.set(-100, -100);
                mDrawState = DrawState.NONE;
                invalidate();
            }
            return super.onTouchEvent(event);
        }
        if (mDrawState == DrawState.DRAWING) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    // 记录完整坐标
                    mCurrentPoint.set(event.getX(), event.getY());
                    invalidate();
                    return true;

                case MotionEvent.ACTION_UP:
                    mHorizontalLines.add(mCurrentPoint.y);
                    //mCurrentPoint.set(-1, -1);
                    mDrawState = DrawState.EDIT;
                    invalidate();
                    return true;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制历史水平线
        for (Float y : mHorizontalLines) {
            canvas.drawLine(0, y, getWidth(), y, mPaint);
            canvas.drawCircle(mCurrentPoint.x, mCurrentPoint.y, CIRCLE_RADIUS, mCirclePaint);
        }
        // 绘制当前预览元素
        if (mCurrentPoint.y != -1) {
            // 绘制水平线
            canvas.drawLine(0, mCurrentPoint.y, getWidth(), mCurrentPoint.y, mPaint);
            // 绘制实心圆
            canvas.drawCircle(mCurrentPoint.x, mCurrentPoint.y, CIRCLE_RADIUS, mCirclePaint);
        }
    }
}
