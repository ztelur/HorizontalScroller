package com.carpediem.randy.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

/**
 * Created by randy on 16-2-27.
 */
public class HorizontalScroller extends LinearLayout{
    private Context mContext;
    private BaseAdapter mAdapter;
    public HorizontalScroller(Context context) {
        super(context);
    }

    public HorizontalScroller(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalScroller(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(Context context) {
        mContext = context;

    }

    public void setAdapter(BaseAdapter adapter) {
        if (adapter == null) {

        }
        mAdapter = adapter;
        mAdapter.notifyDataSetChanged();
    }

}
