package com.carpediem.randy.lib;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.OverScroller;

/**
 * Created by randy on 16-2-27.
 */
public class HorizontalScroller extends LinearLayout {
    private final static int INVALID_ID = -1;
    private final static int MIN_MOVE_VELOCITY = 600;
    private Context mContext;
    private BaseAdapter mAdapter;

    private VelocityTracker mVelocityTracker;

    private boolean mIsBeingDragged = false;
    private int mActivePointerId = INVALID_ID;

    private float mLastTouchX = 0;
    private int mMinVelocity = MIN_MOVE_VELOCITY;

    private int mMinimumVeloctiy;
    private int mMaximumVeloctiy;

    private int mOverscrollDistance;
    private int mOverflingDistance;
    private int mTouchSlop;

    private int mCurrentScreen = 0;
    private int mNestedXOffset;
//    private Span
    //TODO:OverScroller !!!!!
    private OverScroller mScroller;

    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            refreshChildView();
        }

        @Override
        public void onInvalidated() {
            refreshChildView();
        }
    };
    public HorizontalScroller(Context context) {
        super(context);
        init(context);
    }

    public HorizontalScroller(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    public HorizontalScroller(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        setOrientation(HORIZONTAL);
        mScroller = new OverScroller(mContext);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVeloctiy = configuration.getScaledMinimumFlingVelocity();
        mMaximumVeloctiy = configuration.getScaledMaximumFlingVelocity();
        mOverscrollDistance = configuration.getScaledOverscrollDistance();
        mOverflingDistance = configuration.getScaledOverflingDistance();
    }

    public void setAdapter(BaseAdapter adapter) {
        if (adapter == null) {

        }
        //注意这里一个可能的内存泄露问题，mDataSetObserver可是一个内部对象欧，给上一个
        //adapter注册完之后，有立刻注册给另外一个
        mAdapter = adapter;
        mAdapter.registerDataSetObserver(mDataSetObserver);
        mAdapter.notifyDataSetChanged();
    }

    private void initChildView() {
        addViewFromAdapter();
        invalidate();
    }

   private void refreshChildView() {
        removeAllViews();
        addViewFromAdapter();
        invalidate();
    }

    private void addViewFromAdapter() {
        if (mAdapter == null) {
            throw new IllegalStateException();
        }
        int count = mAdapter.getCount();
        for (int i=0;i<count;i++) {
            //TODO:这里不是很准确，可以考虑复用的问题，参评ListView
            addView(mAdapter.getView(i,null,this));
        }
    }

    // onMeasure和onLayout暂时不用动
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();

        Log.e("TEST",action +" "+ mActivePointerId+" ");

        if ((action == MotionEvent.ACTION_MOVE) && mIsBeingDragged) {
            return  true;
        }

        //TODO:这里需要在仔细了解一下啊，什么时候无法左右滑动啦，应该要自定义实现啦
        if (getScrollY() == 0 && !canScrollHorizontally(1)) {
            return false;
        }


        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastTouchX = ev.getX();
                mActivePointerId = ev.getPointerId(0);

                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);


                mIsBeingDragged = !mScroller.isFinished();
                if (mIsBeingDragged ) {

                }
                //TODO:搞懂这个的具体含义NestScroll
                break;
            case MotionEvent.ACTION_MOVE:
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_ID) {
                    Log.e("HorizontalScroller","activePointerId is invalid");
                    break;
                }
                final int pointerIndex = ev.findPointerIndex(activePointerId);
                if (pointerIndex == -1) {
                    Log.e("HorizontalScroller","Invalid id is "+activePointerId + " in intercept");
                    break;
                }
                final int x = (int) ev.getX(pointerIndex);
                final int xDiff = (int) Math.abs(mLastTouchX - x);
                if (xDiff > mTouchSlop) {
                    mIsBeingDragged = true;
                    mLastTouchX = x;
                    //使用速度测试
                    initVelocityTrackerIfNotExists();
                    mVelocityTracker.addMovement(ev);
                    mNestedXOffset = 0;

                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;
            //TODO:这里是有问题的啊，如果这样写，无法接收up和cancel事件啦
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_ID;
                recycleVelocityTracker();
                if (mScroller.springBack(getScrollX(),getScrollY(),0,getScrollRangeX(),0,0)) {
//                    postInvalidateOnAnimation();
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.e("TEST123","ACTION_POINTER_UP");
                onSecondaryPointerUp(ev);
        }
        return mIsBeingDragged;
    }
    private int getScrollRangeX() {
        int scrollRange = 0;
        if (getChildCount() >0) {
            View child = getChildAt(0);
            scrollRange = Math.max(0,child.getWidth() - (getWidth() - getPaddingLeft() - getPaddingRight()));
        }
        return scrollRange;
    }
    //TODO:两个手指的动作，这里还是没有处理好
    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1: 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
            initOrResetVelocityTracker();
        }
    }
    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }
    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
         }
    }
    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e("TEST onTouchEvent" , event.getAction()+" "+mActivePointerId);
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        float x = event.getX();
        float y = event.getY();
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastTouchX = x;
                if (mScroller != null) {
                    if (!mScroller.isFinished()) {
                        mScroller.abortAnimation();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mVelocityTracker.computeCurrentVelocity(1000);
                int velocityX = (int)mVelocityTracker.getXVelocity();
                if (velocityX > ViewConfiguration.get(mContext).getScaledEdgeSlop() && mCurrentScreen > 0) {
//                    scrollToView();
                } else {

                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int detaX = (int)(mLastTouchX -x);
                scrollBy(detaX,0);
                mLastTouchX = x;
                break;
        }
        return true;
    }
}
