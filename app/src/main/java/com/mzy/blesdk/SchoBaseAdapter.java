package com.mzy.blesdk;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vincent on 2016/1/22.
 */
public abstract class SchoBaseAdapter<T> extends BaseAdapter {
    protected List<T> mItemList;
    protected Context mContext;
    protected LayoutInflater mInflater;
    protected T dataBean;

    public SchoBaseAdapter(Context context) {
        // TODO Auto-generated constructor stub
        this.mContext=context;
        this.mItemList=new ArrayList<T>();
        this.mInflater = LayoutInflater.from(context);
    }

    public SchoBaseAdapter(Context context, List<T> list){
        this.mContext=context;
        this.mItemList=list;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mItemList.size();
    }

    @Override
    public T getItem(int position) {
        // TODO Auto-generated method stub
        return mItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    abstract public void setData(List<T> list);
}
