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
import android.widget.EdgeEffect;
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

    private EdgeEffect mEdgeGlowRight;
    private EdgeEffect mEdgeGlowLeft;

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
                Log.e("TEST -----","test "+ev.getPointerId(0)+" "+ev.getPointerCount());
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
        Log.e("TEST onTouchEvent" , event.getAction()+" "+mActivePointerId+" "+event.getPointerCount());
        initVelocityTrackerIfNotExists();
        MotionEvent vtev = MotionEvent.obtain(event);

        float x = event.getX();
        float y = event.getY();
        int action = event.getAction();
        final int actionMasked = event.getActionMasked();

        if (actionMasked == MotionEvent.ACTION_DOWN) {
            mNestedXOffset = 0;
        }
        //TODO:这是什么目的
        vtev.offsetLocation(0,mNestedXOffset);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (getChildCount() == 0) {
                    return false;
                }
                if ((mIsBeingDragged = !mScroller.isFinished())) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
//                    if ()
                }

                mLastTouchX = x;
                mActivePointerId = event.getPointerId(0);
                break;

            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000,mMaximumVeloctiy);
                    int initialVelocity = (int) velocityTracker.getXVelocity(mActivePointerId);
                    Log.e("TEST","the up action "+initialVelocity +" "+mMinimumVeloctiy+" the tracker"+velocityTracker.getYVelocity(mActivePointerId)+" "+mActivePointerId);
                    if ((Math.abs(initialVelocity)> mMinimumVeloctiy)) { // indicate a fling
                        fling(-initialVelocity);
                    } else if (mScroller.springBack(getScrollX(),getScrollY(),0,0,0,getScrollRangeX())){
//                        postInvalidateOnAnimation();
                    }
                    mActivePointerId = INVALID_ID;
                    endDrag();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                final int activePointerIndex = event.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1) {
                    break;
                }
                final int x1 = (int)event.getX(activePointerIndex);
                int detaX = (int)(mLastTouchX -x1);
                if (!mIsBeingDragged && Math.abs(detaX) > mTouchSlop) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    mIsBeingDragged = true;
                    if (detaX >0) {
                        detaX -= mTouchSlop;
                    } else {
                        detaX +=mTouchSlop;
                    }
                }
                if (mIsBeingDragged) {

                }
                scrollBy(detaX,0);
                mLastTouchX = x;
                break;
        }
        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(vtev);
        }
        vtev.recycle();
        return true;
    }
    private void endDrag() {
        mIsBeingDragged =false;
        recycleVelocityTracker();
        //TODO:overscroll 动画效果
        if (mEdgeGlowRight != null) {
            mEdgeGlowRight.onRelease();
            mEdgeGlowLeft.onRelease();
        }
    }
    private void fling(int veloctiy) {
        //TODO:如何判断是否可以进行fling呢？
        Log.e("TEST","call fling");
        if (getChildCount() > 0) {
            int width = getWidth() - getPaddingRight() - getPaddingLeft();
            int bottom = getChildAt(0).getWidth();

            mScroller.fling(getScrollX(),getScrollY(),veloctiy,0,0,0,0,Math.max(0,bottom-width),0,width/2);

            invalidate();
        }
    }

    //TODO:!!!TODO:
    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        if (!mScroller.isFinished()) {
            final int oldX = getScrollX();
            final int oldY = getScrollY();
            onScrollChanged(scrollX,scrollY,oldX,oldY);

        } else {
            super.scrollTo(scrollX,scrollY);
        }
        awakenScrollBars();
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            Log.e("TEST","computeScrollOffset()");
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();
            if (oldX != x || oldY != y) {
                final int range = getScrollRangeX();
                final int overscrollMode = getOverScrollMode();
                //TODO:第二个判断条件是？
                final boolean canOverscroll = overscrollMode == OVER_SCROLL_ALWAYS ||
                        (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && range >0);
                Log.e("TEST",(x-oldX)+" "+(y-oldY));
                overScrollBy(x-oldX,y-oldY,oldX,oldY,0,range,0,mOverflingDistance,false);
                //TODO:?????
                onScrollChanged(getScrollX(),getScrollY(),oldX,oldY);
                if (canOverscroll) {
                    if (y<0 && oldY >=0) {
                        mEdgeGlowLeft.onAbsorb((int)mScroller.getCurrVelocity());
                    } else if (y > range && oldY <= range) {
                        mEdgeGlowRight.onAbsorb((int)mScroller.getCurrVelocity());
                    }
                }
                postInvalidate();
            }
        }
    }

}
