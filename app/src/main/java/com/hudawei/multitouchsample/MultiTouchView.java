package com.hudawei.multitouchsample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

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
 * <p>
 * List<Path> 和 List<Point>的一个元素 对应 一个图层
 * 根据Point来确定Canvas平移的距离
 * <p>
 * 当移动Canvas后（multiTouchFlag为true,在ACTION_UP中），首先判断上一次的List<Path>里面有没有元素,
 * 如果有就创建一个List<Path>并且添加到List<List<Path>>里面去 和 List<Point>，List<Point>添加当前的平移距离
 * 如果没有就不需要创建，改变List<Point>中最后一个元素的平移距离
 * <p>
 * PathsBean代表需要平移相同距离的一组路径
 * List<PathsBean>代表所有需要绘制的路径
 * <p>
 * <p>
 * 移动画布
 * <p>
 * 时机：在onTouchEvent中，event为ACTION_UP，multiTouchFlag为true
 * <p>
 * 操作：取List<PathsBean>中最后一个PathsBean,检查PathsBean中的List<Path>是否有元素
 * 如果没有元素，改变该PathsBean的offsetX和offsetY,原来移动的距离加上现在移动的距离
 * 如果有元素，创建一个新的PathsBean->创建一个List<Path>,offsetX和offsetY赋值为当前移动的距离,
 * layerId为当前PathsBean在List<PathsBean>中的索引
 * <p>
 * 前进
 * <p>
 * 后退
 * <p>
 * 取List<PathsBean>中最后一个元素PathsBean
 * 如果PathsBean中的List<Path>的size大于0，那么移除List<Path>中的最后一个Path
 * <p>
 * 画草稿
 */

public class MultiTouchView extends View {
    private int mTouchSlop;
    private Paint mPaint;
    private Path mPath;
    private float mLastX;
    private float mLastY;
    private float mOffsetX;
    private float mOffsetY;
    private PointF startPoint;
    private PointF endPoint;
    private MultiPathBean multiPathBean;
    private boolean drawFlag;
    private boolean moveFlag;

    public MultiTouchView(Context context) {
        super(context);
    }

    public MultiTouchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(5);
        mPaint.setStyle(Paint.Style.STROKE);

        mPath = new Path();
        startPoint = new PointF();
        endPoint = new PointF();
        multiPathBean = new MultiPathBean();
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.GRAY);

        if (drawFlag)
            canvas.drawPath(mPath, mPaint);
        if (moveFlag) {
            canvas.save();
            canvas.translate(mOffsetX, mOffsetY);
        }

        for (int i = multiPathBean.mPathsBeanList.size() - 1; i >= 0; i--) {
            canvas.save();
            PathsBean pathsBeen = multiPathBean.mPathsBeanList.get(i);
            canvas.translate(pathsBeen.mOffsetX, pathsBeen.mOffsetY);
            for (int j = 0; j < pathsBeen.mPaths.size(); j++) {
                Path path = pathsBeen.mPaths.get(j);
                canvas.drawPath(path, mPaint);
            }
        }

    }

    int preAction = MotionEvent.ACTION_CANCEL;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastX = event.getX();
                mLastY = event.getY();
                mPath.moveTo(mLastX, mLastY);
                preAction = action;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    getMiddlePoint(startPoint, event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    if (preAction == MotionEvent.ACTION_DOWN)
                        preAction = action;
                }
                break;
            case MotionEvent.ACTION_MOVE:

                if (preAction != MotionEvent.ACTION_CANCEL) {
                    if (preAction != action) {
                        if (preAction == MotionEvent.ACTION_DOWN) {
                            drawFlag = true;
                            moveFlag = false;
                        } else if (preAction == MotionEvent.ACTION_POINTER_DOWN) {
                            drawFlag = false;
                            moveFlag = true;
                        }
                        preAction = action;
                    }
                    if (drawFlag) {
                        drawPath(event);
                    }

                    if (moveFlag) {
                        getMiddlePoint(endPoint, event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                        translateCanvas();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                preAction = MotionEvent.ACTION_CANCEL;
                if (drawFlag) {
                    multiPathBean.addPath(new Path(mPath));
                }

                if (moveFlag) {
                    multiPathBean.translateCanvas(mOffsetX, mOffsetY);
                }

                moveFlag = false;
                drawFlag = false;
                mOffsetX = 0;
                mOffsetY = 0;
                mPath.reset();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getActionIndex() <= 1) {
                    Log.e("ACTION_POINTER_UP", " 抬起手指 ");
                    preAction = MotionEvent.ACTION_CANCEL;
                }
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
        mOffsetX = endPoint.x - startPoint.x;
        mOffsetY = endPoint.y - startPoint.y;
        if (Math.abs(mOffsetX) < mTouchSlop && Math.abs(mOffsetY) < mTouchSlop)
            return;
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
        if (Math.abs(mLastX - endX) < mTouchSlop && Math.abs(mLastY - endY) < mTouchSlop)
            return;
        float cX = mLastX + (endX - mLastX) / 2;
        float cY = mLastY + (endY - mLastY) / 2;

        mPath.quadTo(cX, cY, endX, endY);
        mLastX = endX;
        mLastY = endY;
        invalidate();
    }

    public void pre() {
        multiPathBean.pre();
        postInvalidate();
    }

    public void next() {
        multiPathBean.next();
        postInvalidate();
    }


}
