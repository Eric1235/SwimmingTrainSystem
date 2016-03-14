package com.scnu.swimmingtrainsystem.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.model2db.Athlete;

import java.util.List;

/**
 * Created by lixinkun on 16/1/25.
 */
public class AthleteSpinnerAdapter extends BaseAdapter {
    private Context context;
    private List<Athlete> athletes;

    public AthleteSpinnerAdapter(Context context, List<Athlete> list) {
        this.context = context;
        this.athletes = list;
    }

    @Override
    public int getCount() {
        return athletes.size();
    }

    @Override
    public Object getItem(int position) {
        return athletes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Athlete a = (Athlete)getItem(position);
        ViewHolder holder = null;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.item_spinner, null);
            holder.tvName = (TextView)convertView;
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvName.setText(a.getName());
        return  convertView;
    }
    class ViewHolder {
        private TextView tvName;
    }
}
