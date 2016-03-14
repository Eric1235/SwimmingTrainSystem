package com.scnu.swimmingtrainsystem.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.model2db.Athlete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 被选中的运动员数据适配器
 * 
 * @author LittleByte
 * 修改：lixinkun
 */
public class ShowChosenAthleteAdapter extends BaseAdapter {
	private Context context;
	private List<Athlete> list;
	private ArrayAdapter<String> adapter2;
	private int[] strokeNumbers;

	public ShowChosenAthleteAdapter(Context context, List<Athlete> list) {
		this.context = context;
		this.list = list;
		this.strokeNumbers = new int[list.size()];

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position1, View view, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(context).inflate(
					R.layout.chosen_athlete_list_item, null);
			viewHolder.tvTitle = (TextView) view
					.findViewById(R.id.tv_chosen_ath_name);
			viewHolder.strokeSpinner = (Spinner) view.findViewById(R.id.stroke);
			viewHolder.strokeSpinner.setAdapter(getStrokeArray());
			/**
			 * 获取spinner的数据
			 */
			viewHolder.strokeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					/**
					 * 这里position要加1
					 */
					strokeNumbers[position1] = position + 1;
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {

				}
			});
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		viewHolder.tvTitle.setText(this.list.get(position1).getName());
		return view;
	}

	private ArrayAdapter getStrokeArray(){
//		if(adapter2 == null){
			List<String> stroke = new ArrayList<String>();
			String[] strokes = context.getResources().getStringArray(R.array.strokestrarray);
			Collections.addAll(stroke, strokes);
			adapter2 = new ArrayAdapter<String>(context,
					android.R.layout.simple_spinner_item, stroke);
			adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//		}

		return adapter2;
	}

	/**
	 * 获取运动员-泳姿数据集
	 * @return
	 */
	public Map<String,String> getAthleteWithStroke(){
		notifyDataSetChanged();
		Map<String,String> map = new HashMap<String, String>();
		for(int i = 0 ; i < list.size() ; i ++){
			map.put(String.valueOf(list.get(i).getAid()), String.valueOf(strokeNumbers[i]));
		}
		return  map;
	}

	final static class ViewHolder {
		private TextView tvTitle;
		private Spinner strokeSpinner;
	}


}
