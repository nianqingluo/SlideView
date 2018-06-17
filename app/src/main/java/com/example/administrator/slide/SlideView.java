package com.example.administrator.slide;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2018/6/9 0009.
 */

public class SlideView extends RelativeLayout {

    private int mOriginWidth = 0;
    private float mScale = 0.8f;

    public float getScale() {
        return mScale;
    }

    /**
     * 设置比例,默认0.8
     *
     * @param scale 比例
     */
    public void setScale(float scale) {
        if (mScale != scale) {
            mScale = scale;
            for (int i = 0; i < mViewList.size(); i++) {
                setViewTranslationAndScale(mViewList.get(i), i, mMissmatch);
            }
        }
    }

    public int getOverlapDistance() {
        return mOverlapDistance;
    }

    /**
     * 设置重叠的距离
     *
     * @param overlapDistance 重叠距离
     */
    public void setOverlapDistance(int overlapDistance) {
        if (mOverlapDistance != overlapDistance) {
            mOverlapDistance = overlapDistance;
            for (int i = 0; i < mViewList.size(); i++) {
                setViewTranslationAndScale(mViewList.get(i), i, mMissmatch);
            }
        }
    }

    private int mOverlapDistance = 20;

    //    -0.5f - 0.5f
    private float mMissmatch = 0f;

    private Scroller mScroller;
    private Context mContext;
    private int mMaxFlintVelocity;
    private int mMinFlintVelocity;
    private VelocityTracker velocityTracker = VelocityTracker.obtain();
    private float mLastX, mLastY;
    private float mDownX;
    private List<View> mViewList = new LinkedList<>();
    private int mMotionEventAction = MotionEvent.ACTION_UP;

    public SlideView(Context context) {
        this(context, null);
    }

    public SlideView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mScroller = new Scroller(mContext);
        initData(context);
    }

    private void initData(Context context) {
        mScroller = new Scroller(context, null, true);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mMaxFlintVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        mMinFlintVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        mMinFlintVelocity = mMinFlintVelocity > 1000 ? mMinFlintVelocity : 1000;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (params.height <= 0 || params.width <= 0) {
            throw new IllegalArgumentException("view height or width must be explicit size");
        }
        if (mOriginWidth == 0) {
            mOriginWidth = params.width;
        } else if (mOriginWidth != params.width) {
            throw new IllegalArgumentException("the view width is not equal with last view");
        }
        int childCount = getChildCount();
        RelativeLayout.LayoutParams para = (LayoutParams) params;
        setViewTranslationAndScale(child, childCount, mMissmatch);
        if (!mViewList.contains(child)) {
            mViewList.add(child);
        }
        super.addView(child, 0, para);
    }

    private void setViewTranslationAndScale(View child, int childCount, float missmatch) {
        float distance = getMoveDistance(childCount);

        float scale = getScale(childCount);

        float zoominTotalOffsetDistance = mOriginWidth * scale - scale * mOriginWidth * (1 - mScale) / 2f - mOverlapDistance;

        float zoominOffsetDistance = Math.abs(missmatch * zoominTotalOffsetDistance);

        float zoomoutTotalOffsetDistance = mOriginWidth * scale / mScale - scale / mScale * mOriginWidth * (1 - mScale) / 2f - mOverlapDistance;

        float zoomoutOffsetDistance = Math.abs(missmatch * zoomoutTotalOffsetDistance);

        float minifyoffsetScale = 1 + Math.abs(missmatch) * (mScale - 1);

        float magnifyoffsetScale = ((mScale + 1) / mScale - 2) * Math.abs(missmatch) + 1;

        if (missmatch > 0) {//右偏了

            //右边的缩小
            if (childCount % 2 == 1) {
                distance += zoominOffsetDistance;
                scale *= minifyoffsetScale;
            } else if (childCount == 0) {//0的位置右偏,缩小
                distance += zoominOffsetDistance;
                scale *= minifyoffsetScale;
            } else {//左边的放大
                distance += zoomoutOffsetDistance;
                scale *= magnifyoffsetScale;
            }

        } else if (missmatch < 0) {//左偏了
            //右边的放大
            if (childCount % 2 == 1) {
                distance -= zoomoutOffsetDistance;
                scale *= magnifyoffsetScale;
            } else if (childCount == 0) {//0的位置左偏,缩小
                distance -= zoominOffsetDistance;
                scale *= minifyoffsetScale;
            } else {//左边的缩小
                distance -= zoominOffsetDistance;
                scale *= minifyoffsetScale;
            }
        }
        child.setTranslationX(distance);
        child.setScaleX(scale);
        child.setScaleY(scale);
    }

    private void removeAllViewsForReadd() {
        super.removeAllViews();
    }

    @Override
    public void removeAllViews() {
        super.removeAllViews();
        mViewList.clear();
        mOriginWidth = 0;
    }

    @Override
    public void removeViewAt(int index) {
        super.removeViewAt(index);
        mViewList.remove(index);
    }

    @Override
    public void removeView(View view) {
        super.removeView(view);
        mViewList.remove(view);
    }

    private float getScale(int childCount) {
        float tempScale = 1;
        if (childCount % 2 == 0) { //往左边添加

            int multiple = childCount / 2;
            for (int j = 0; j < multiple; j++) {
                tempScale *= mScale;
            }
        } else {//往右边添加
            int multiple = childCount / 2 + 1;
            for (int j = 0; j < multiple; j++) {
                tempScale *= mScale;
            }
        }
        return tempScale;
    }

    private float getMoveDistance(int childCount) {
        if (childCount % 2 == 0) { //往左边添加
            int moveCount = (childCount / 2);
            float moveDistance = 0;
            for (int i = 1; i <= moveCount; i++) {
                float lastScale = getCurrentScale(i);
                moveDistance += mOriginWidth * lastScale - lastScale * mOriginWidth * (1 - mScale) / 2f - mOverlapDistance;
            }

            return -moveDistance;
        } else {//往右边添加
            int moveCount = childCount / 2 + 1;
            float moveDistance = 0;
            for (int i = 1; i <= moveCount; i++) {
                float lastScale = getCurrentScale(i);
                moveDistance += mOriginWidth * lastScale - lastScale * mOriginWidth * (1 - mScale) / 2f - mOverlapDistance;
            }
            return moveDistance;
        }
    }

    private float getCurrentScale(int i) {
        float totalScale = 1;
        for (int j = 0; j < i - 1; j++) {
            totalScale *= mScale;
        }
        return totalScale;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mMotionEventAction = MotionEvent.ACTION_DOWN;
        if (mViewList.size() <= 1) {
            return super.onInterceptTouchEvent(ev);
        }
        if (!mScroller.isFinished()) {
            mScroller.forceFinished(true);
            returnToNormal();
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = ev.getX();
                mLastY = ev.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                float x = ev.getX();
                float y = ev.getY();
                //需要拦截
                if (Math.abs(x - mLastX) > Math.abs(y - mLastY)) {
                    mDownX = x;
                    return true;
                }
                mLastX = x;
                mLastY = y;
                break;
            default:
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mViewList.size() <= 1) {
            return super.onTouchEvent(event);
        }

        velocityTracker.addMovement(event);
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //                System.out.println("event---->down");
                mDownX = event.getX();
                mMotionEventAction = MotionEvent.ACTION_DOWN;
                break;
            case MotionEvent.ACTION_MOVE:
                //                System.out.println("event---->move");
                mMotionEventAction = MotionEvent.ACTION_MOVE;
                float pointX = event.getX();
                setScrollEvent(pointX);
                break;
            case MotionEvent.ACTION_UP:
                //                System.out.println("event---->up");
                //手指抬起，计算当前速率
                float up_x = event.getX();
                velocityTracker.computeCurrentVelocity(1000, mMaxFlintVelocity);
                int xVelocity = (int) velocityTracker.getXVelocity();
                if (Math.abs(xVelocity) > mMinFlintVelocity) {
                    mScroller.fling((int) up_x, 0, xVelocity, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
                    invalidate();
                } else {
                    returnToNormal();
                }
                //                break;
            default:
                mMotionEventAction = MotionEvent.ACTION_UP;
                invalidate();
                break;
        }
        return true;
    }

    private void returnToNormal() {
        mMissmatch = 0f;
        for (int i = 0; i < mViewList.size(); i++) {
            setViewTranslationAndScale(mViewList.get(i), i, mMissmatch);
        }
    }

    private void setScrollEvent(float pointX) {
        float deltaX = pointX - mDownX;
        float moveRatio = deltaX / mOriginWidth;

        mMissmatch = moveRatio;
        boolean needReaddAllView = needReaddAllView();
        if (needReaddAllView) {
            if (mMissmatch > 0) {//往右边移动
                mDownX += mOriginWidth * (int) (mMissmatch + 0.5);
                mMissmatch = mMissmatch - (int) (mMissmatch + 0.5);

                List<View> right = new ArrayList<>();//奇数
                List<View> left = new ArrayList<>();//偶数
                right.add(mViewList.get(0));
                int size = mViewList.size();
                for (int i = 1; i < size; i++) {
                    if (i % 2 == 0) {
                        left.add(mViewList.get(i));
                    } else {
                        right.add(mViewList.get(i));
                    }
                }
                View view = right.get(right.size() - 1);
                left.add(view);
                right.remove(view);
                mViewList.clear();
                for (int i = 0; i < size; i++) {
                    if (i % 2 == 0) {
                        mViewList.add(left.get(i / 2));
                    } else {
                        mViewList.add(right.get(i / 2));
                    }
                }

            } else {//往左边移动
                mDownX += mOriginWidth * (int) (mMissmatch - 0.5);
                mMissmatch = mMissmatch - (int) (mMissmatch - 0.5);

                List<View> right = new ArrayList<>();//奇数
                List<View> left = new ArrayList<>();//偶数
                left.add(mViewList.get(0));
                int size = mViewList.size();
                for (int i = 1; i < size; i++) {
                    if (i % 2 == 0) {
                        left.add(mViewList.get(i));
                    } else {
                        right.add(mViewList.get(i));
                    }
                }
                View view = left.get(left.size() - 1);
                right.add(view);
                left.remove(view);
                mViewList.clear();
                mViewList.add(right.get(0));
                for (int i = 1; i < size; i++) {
                    if (i % 2 == 0) {
                        mViewList.add(left.get(i / 2 - 1));
                    } else {
                        mViewList.add(right.get(i / 2 + 1));
                    }
                }
            }

            removeAllViewsForReadd();

            for (int i = 0; i < mViewList.size(); i++) {
                addView(mViewList.get(i));
            }

        } else {
            for (int i = 0; i < mViewList.size(); i++) {
                setViewTranslationAndScale(mViewList.get(i), i, mMissmatch);
            }
        }
    }

    private boolean needReaddAllView() {
        if (mMissmatch > 0) { //往右移动
            return mMissmatch + 0.5f > 1;
        } else {
            return mMissmatch - 0.5f < -1;
        }
    }

    @Override
    public void computeScroll() {
        boolean x = mScroller.computeScrollOffset();
        if (x) {
            float pointX = mScroller.getCurrX();
            setScrollEvent(pointX);
            postInvalidate();
        } else if (mMotionEventAction == MotionEvent.ACTION_UP) {
            returnToNormal();
        }
    }
}
