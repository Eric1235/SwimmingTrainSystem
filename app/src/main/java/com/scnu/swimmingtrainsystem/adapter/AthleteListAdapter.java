package com.scnu.swimmingtrainsystem.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.model2db.Athlete;

import java.util.List;

/**
 * 运动员列表数据适配器
 * 
 * @author LittleByte
 * 修改：李新坤
 * 
 */
public class AthleteListAdapter extends ArrayAdapter<Athlete> {

	private Context context;
	private List<Athlete> athletes;
	private int mResourseId;

	public AthleteListAdapter(Context context, int textViewResourceId,List<Athlete> list) {
		super(context, textViewResourceId);
		this.athletes = list;
		this.context = context;
		this.mResourseId = textViewResourceId;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return athletes.size();
	}

	@Override
	public Athlete getItem(int arg0) {
		// TODO Auto-generated method stub
		return athletes.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		Athlete a = getItem(position);
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(context, mResourseId, null);
			holder.id = (TextView) convertView.findViewById(R.id.tb_AthID);
			holder.name = (TextView) convertView.findViewById(R.id.tb_AthName);
			holder.phone = (TextView) convertView.findViewById(R.id.tv_athlete_phone);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.id.setText(String.format("%1$03d", position + 1));
		holder.name.setText(a.getName());
		holder.phone.setText(a.getPhone());
		return convertView;
	}


	class ViewHolder {
		private TextView id;
		private TextView name;
		private TextView phone;
	}

	public void setDatas(List<Athlete> athletes) {
		this.athletes.clear();
		this.athletes.addAll(athletes);
	}


}
