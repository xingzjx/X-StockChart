package com.example.sample.ui.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DrawView extends View {
    // 新增绘图模式枚举
    public enum DrawState {
        NONE, DRAWING, FINISHED
    }

    public enum DrawShape {
        NONE, LINE, SEGMENT, TRIANGLE, HORIZONTAL_LINE
    }

    private DrawShape currentDrawShape = DrawShape.NONE;
    private List<PointF> currentPoints = new ArrayList<>();
    private Paint shapePaint;
    private List<ShapeData> drawnShapes = new ArrayList<>();
    private PointF mTempPoint = new PointF(-1, -1);

    public DrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DrawView(Context context) {
        super(context);
        init();
    }

    private void init() {
        initPaint();
    }

    // 初始化时配置画笔
    private void initPaint() {
        shapePaint = new Paint();
        shapePaint.setColor(Color.RED);
        shapePaint.setStrokeWidth(4f);
        shapePaint.setStyle(Paint.Style.STROKE);
        // ... 原有画笔初始化代码 ...
    }

    // 新增方法：启动绘图模式
    public void startDrawShape(DrawShape shape) {
        currentDrawShape = shape;
        currentPoints.clear();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (currentDrawShape != DrawShape.NONE) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    float x = event.getX();
                    float y = event.getY();

                    // 水平线模式第二个点自动对齐Y轴
                    if (currentDrawShape == DrawShape.HORIZONTAL_LINE && currentPoints.size() == 1) {
                        y = currentPoints.get(0).y;
                    }
                    currentPoints.add(new PointF(x, y));

                    if (currentPoints.size() == getRequiredPoints()) {
                        completeDrawing();
                    }
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (currentPoints.size() == 1) {
                        float moveX = event.getX();
                        float moveY = event.getY();
                        // 水平线模式预览时固定Y轴
                        if (currentDrawShape == DrawShape.HORIZONTAL_LINE) {
                            moveY = currentPoints.get(0).y;
                        }
                        mTempPoint.set(moveX, moveY);
                        invalidate();
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    mTempPoint.set(-1, -1);
                    invalidate();
                    return true;
            }
        }
        return super.onTouchEvent(event);
    }

    // 添加完成绘制方法
    private void completeDrawing() {
        drawnShapes.add(new ShapeData(currentDrawShape, new ArrayList<>(currentPoints)));
        currentDrawShape = DrawShape.NONE;
        currentPoints.clear();
        mTempPoint.set(-1, -1);
        invalidate();
    }

    private int getRequiredPoints() {
        switch (currentDrawShape) {
            case HORIZONTAL_LINE: // 新增水平线判断
                return 2;
            case LINE:
                return 2;
            case SEGMENT:
                return 2;
            case TRIANGLE:
                return 3;
            default:
                return 0;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 修改临时线绘制逻辑
        if (currentDrawShape == DrawShape.HORIZONTAL_LINE && mTempPoint.y != -1) {
            // 水平线始终贯穿整个视图
            canvas.drawLine(0, mTempPoint.y, getWidth(), mTempPoint.y, shapePaint);
        } else if (currentPoints.size() == 1 && mTempPoint.x > 0) {
            // 其他模式的临时线保持原有逻辑
            canvas.drawLine(currentPoints.get(0).x, currentPoints.get(0).y,
                    mTempPoint.x, mTempPoint.y, shapePaint);
        }
        // 绘制已保存的图形
        for (ShapeData shape : drawnShapes) {
            drawShape(canvas, shape);
        }


    }

    private void drawShape(Canvas canvas, ShapeData shape) {
        switch (shape.type) {
            case HORIZONTAL_LINE: // 新增水平线绘制分支
                if (shape.points.size() >= 2) {
                    canvas.drawLine(shape.points.get(0).x, shape.points.get(0).y,
                            shape.points.get(1).x, shape.points.get(1).y, shapePaint);
                }
                break;
            case TRIANGLE:
                Path path = new Path();
                path.moveTo(shape.points.get(0).x, shape.points.get(0).y);
                path.lineTo(shape.points.get(1).x, shape.points.get(1).y);
                path.lineTo(shape.points.get(2).x, shape.points.get(2).y);
                path.close();
                canvas.drawPath(path, shapePaint);
                break;
        }
    }

    // 形状数据存储类
    private static class ShapeData {
        DrawShape type;
        List<PointF> points;
        int color;

        ShapeData(DrawShape type, List<PointF> points) {
            this.type = type;
            this.points = points;
            this.color = Color.RED;
        }
    }
}