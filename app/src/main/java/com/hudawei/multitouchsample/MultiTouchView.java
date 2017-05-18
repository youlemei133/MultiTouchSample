package com.hudawei.multitouchsample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hudawei on 2017/5/18.
 * 主触摸点：
 * ACTION_DOWN
 * ACTION_MOVE
 * ACTION_UP
 * <p>
 * 其他触摸点：
 * ACTION_POINTER_DOWN
 * ACTION_MOVE
 * ACTION_POINTER_UP
 * <p>
 * 目标：
 * a. 2指触摸时，移动画布
 * b. 1指触摸时，绘制路径
 * c. 提供撤销，恢复功能
 *
 * 画布移动之前
 */

public class MultiTouchView extends View {
    private Paint mPaint;
    private Path mPath;
    private List<List<Path>> mPathList;
    private List<Path> mTempPathList;
    private List<PointF> mOffestList;
    private List<Path> mRecyclePathList;
    private boolean multiTouchFlag;
    private boolean translateFlag;
    private float mLastX;
    private float mLastY;
    private float mOffsetX;
    private float mOffsetY;
    private float mLastOffsetX;
    private float mLastOffsetY;
    private PointF startPoint;
    private PointF endPoint;

    public MultiTouchView(Context context) {
        super(context);
    }

    public MultiTouchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);
        mPathList = new ArrayList<>();
        mTempPathList = new ArrayList<>();
        mPathList.add(mTempPathList);

        mRecyclePathList = new ArrayList<>();

        mOffestList = new ArrayList<>();
        mOffestList.add(new PointF(0,0));

        mPath = new Path();
        startPoint = new PointF();
        endPoint = new PointF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.GRAY);
        canvas.drawRect(0, 0, 100, 100, mPaint);

        if(translateFlag){
            canvas.translate(mOffsetX,mOffsetY);
        }

        canvas.drawPath(mPath, mPaint);

        for(int i=0;i<mPathList.size();i++){
            canvas.save();
            canvas.translate(mOffestList.get(i).x, mOffestList.get(i).y);
            if (checkPaths(mPathList.get(i))) {
                for (Path path : mPathList.get(i)) {
                    canvas.drawPath(path, mPaint);
                }
            }
            canvas.restore();
        }

        if(translateFlag){
            canvas.restore();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastX = event.getX();
                mLastY = event.getY();
                mPath.moveTo(mLastX, mLastY);
                translateFlag = false;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                multiTouchFlag = true;
                getMiddlePoint(startPoint, event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                break;
            case MotionEvent.ACTION_MOVE:
                if (!multiTouchFlag)
                    drawPath(event);
                else if (event.getPointerCount() > 1) {
                    getMiddlePoint(endPoint, event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    translateCanvas();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!multiTouchFlag)
                    mTempPathList.add(new Path(mPath));
                else {
                    multiTouchFlag = false;
                    mLastOffsetX = mOffsetX;
                    mLastOffsetY = mOffsetY;

                    mOffestList.add(new PointF(mOffsetX,mOffsetY));
                    mTempPathList = new ArrayList<>();
                    mPathList.add(mTempPathList);
                }
                mPath.reset();
                break;
        }
        return true;
    }

    /**
     * 如何判断是多点触碰
     * 进入ACTION_POINTER_DOWN时，为多点触碰
     * MOVE时，如果event.getPointerCount()>1,那么是多点触碰
     * <p>
     * 取2指之间的中点作为平移的距离
     */
    private void translateCanvas() {
        mOffsetX = endPoint.x - startPoint.x + mLastOffsetX;
        mOffsetY = endPoint.y - startPoint.y + mLastOffsetY;
        translateFlag = true;
        invalidate();
    }

    /**
     * 获取2点之间的中点坐标
     */
    private void getMiddlePoint(PointF srcPoint, float startX, float startY, float endX, float endY) {
        float mX = startX + (endX - startX) / 2;
        float mY = startY + (endY - startY) / 2;
        if (srcPoint == null)
            srcPoint = new PointF();
        srcPoint.set(mX, mY);
    }


    private void drawPath(MotionEvent event) {
        float endX = event.getX();
        float endY = event.getY();
        float cX = mLastX + (endX - mLastX) / 2;
        float cY = mLastY + (endY - mLastY) / 2;

        mPath.quadTo(cX, cY, endX, endY);
        mLastX = endX;
        mLastY = endY;
        invalidate();
    }

//    public void pre() {
//        if (checkPaths(mPathList)) {
//            mRecyclePathList.add(mPathList.get(mPathList.size() - 1));
//            mPathList.remove(mPathList.size() - 1);
//            postInvalidate();
//        }
//    }
//
//    public void next() {
//        if (checkPaths(mRecyclePathList)) {
//            mPathList.add(mRecyclePathList.get(mRecyclePathList.size() - 1));
//            mRecyclePathList.remove(mRecyclePathList.size() - 1);
//            postInvalidate();
//        }
//    }

    private boolean checkPaths(List pathList) {
        return pathList != null && pathList.size() != 0;
    }

}
