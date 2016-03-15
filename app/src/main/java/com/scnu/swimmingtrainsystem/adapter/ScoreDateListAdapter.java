package com.scnu.swimmingtrainsystem.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.entity.ScoreDateEntity;
import com.scnu.swimmingtrainsystem.util.CommonUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by lixinkun on 16/3/2.
 */
public class ScoreDateListAdapter extends BaseAdapter {

    private List<ScoreDateEntity> mItems;
    private Context mContext;

    public ScoreDateListAdapter(List<ScoreDateEntity> mItems, Context mContext) {
        this.mItems = mItems;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public ScoreDateEntity getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holer = null;
        ScoreDateEntity item = getItem(position);
        if(convertView == null){
            holer = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.xlist_item, null);
            holer.tvDate = (TextView)convertView.findViewById(R.id.tv_date);
            convertView.setTag(holer);

        }else{
            holer = (ViewHolder)convertView.getTag();
        }

        String dateString = item.getPdate();
        Date date = new Date(dateString);
        dateString = CommonUtils.formatDate(date);
        holer.tvDate.setText(dateString);
        if(!item.isChecked()){
            holer.tvDate.setTextColor(mContext.getResources().getColor(R.color.black));
        }else{
            holer.tvDate.setTextColor(mContext.getResources().getColor(R.color.light_gray));
        }
        return convertView;
    }

    class ViewHolder{
        TextView tvDate;
    }
}
