package com.scnu.swimmingtrainsystem.adapter;/**
 * ${PROJET_NAME}
 * Created by lixinkun on 16/3/14 17:50.
 * Email EricLi1235@gmail.com.
 */

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.entity.SpinnerEntity;

import java.util.List;

/**
 * User: lixinkun
 * Date: 2016-03-14
 * Time: 17:50
 * FIXME
 */
public class NormalSpinnerAdapter extends BaseAdapter{

    private List<SpinnerEntity> mLists;
    private Context mContext;

    public NormalSpinnerAdapter(List<SpinnerEntity> mLists, Context mContext) {
        this.mLists = mLists;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mLists.size();
    }

    @Override
    public SpinnerEntity getItem(int i) {
        return mLists.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        SpinnerEntity s = getItem(i);
        ViewHolder holder = null;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_spinner, null);
            holder.tvName = (TextView)convertView;
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvName.setText(s.getDisplayString());
        return  convertView;
    }

    class ViewHolder {
        private TextView tvName;
    }
}
