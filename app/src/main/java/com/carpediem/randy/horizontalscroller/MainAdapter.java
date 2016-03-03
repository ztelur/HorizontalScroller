package com.carpediem.randy.horizontalscroller;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by randy on 16-3-3.
 */
public class MainAdapter extends BaseAdapter{
    private List<String> mData = new ArrayList<>();
    private Context mContext;

    public MainAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<String> data) {
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        assert mData != null;
        return mData.size();
    }

    @Override
    public String getItem(int position) {
        assert mData.size() > position;
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = parent.inflate(mContext,R.layout.adpater_main,null);
            viewHolder = new ViewHolder();
            viewHolder.mTvName = (TextView) convertView.findViewById(R.id.text);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        String name = getItem(position);
        viewHolder.mTvName.setText(name);
        return convertView;
    }

    class ViewHolder {
        TextView mTvName;
    }
}
