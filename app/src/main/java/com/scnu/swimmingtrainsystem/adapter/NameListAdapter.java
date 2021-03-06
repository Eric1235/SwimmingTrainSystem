package com.scnu.swimmingtrainsystem.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.scnu.swimmingtrainsystem.R;

import java.util.List;

/**
 * 运动员名字拖拽列表数据适配器
 * 
 * @author LittleByte
 * 
 */
public class NameListAdapter extends BaseAdapter {

	private Context context;
	private List<String> lists;

	public NameListAdapter(Context context, List<String> lists) {
		this.context = context;
		this.lists = lists;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return lists.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return lists.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<String> getList() {
		return lists;
	}

	public void remove(Object dragItem) {
		lists.remove(dragItem);
	}

	public void insert(Object dragItem, int position) {
		lists.add(position, (String) dragItem);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;

		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(context, R.layout.drag_list_item,
					null);
			holder.athName = (TextView) convertView
					.findViewById(R.id.drag_list_item_text);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.athName.setText(lists.get(position));
		return convertView;
	}

	final class ViewHolder {
		private TextView athName;
	}
}