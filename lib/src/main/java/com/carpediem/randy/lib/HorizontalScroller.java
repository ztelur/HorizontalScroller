package com.carpediem.randy.lib;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * Created by randy on 16-2-27.
 */
public class HorizontalScroller extends LinearLayout  {
    private final static int TOUCH_STATE_REST = 0;
    private final static int TOUCH_STATE_MOVE = 1;
    private final static int MIN_MOVE_VELOCITY = 600;
    private Context mContext;
    private BaseAdapter mAdapter;

    private VelocityTracker mVelocityTracker;

    private int mTouchState = TOUCH_STATE_REST;
    private float mLastTouchX = 0;
    private int mMinVelocity = MIN_MOVE_VELOCITY;
    private int mCurrentScreen = 0;
    private Scroller mScroller;

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
        mScroller = new Scroller(mContext);
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
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST:TOUCH_STATE_MOVE;
                break;
            case MotionEvent.ACTION_MOVE:
                final int xDiff = (int) Math.abs(mLastTouchX - ev.getX());
                if (xDiff > ViewConfiguration.get(mContext).getScaledEdgeSlop()) {
                    mTouchState = TOUCH_STATE_MOVE;
                }
                break;
            //TODO:这里是有问题的啊，如果这样写，无法接收up和cancel事件啦
            case MotionEvent.ACTION_CANCEL:
                mTouchState = TOUCH_STATE_REST;
                break;
            case MotionEvent.ACTION_UP:
                mTouchState = TOUCH_STATE_REST;
                break;
        }
        return mTouchState == TOUCH_STATE_MOVE;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
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
