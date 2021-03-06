package com.scnu.swimmingtrainsystem.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.utils.Constants;
import com.scnu.swimmingtrainsystem.utils.ScreenUtils;


/**
 * 使用说明Activity
 * 
 * @author LittleByte
 * 修改:李新坤
 * 实在太丑了，我受不了了，要改界面
 */
public class QuestionHelpActivity extends Activity {
	private MyApplication application;
	private ExpandableListView expandableListView;
	private ImageButton btnBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tips);
		init();
	}

	private void init() {
		application=(MyApplication) getApplication();
		application.addActivity(this);
		btnBack = (ImageButton) findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				exitActivity();
			}
		});
		expandableListView = (ExpandableListView) findViewById(R.id.tips_list);
		final ExpandableListAdapter adapter = new BaseExpandableListAdapter() {

			// 自己定义一个获得文字信息的方法
			TextView getTextView() {
				AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, 64);
				TextView textView = new TextView(QuestionHelpActivity.this);
				textView.setLayoutParams(lp);
				textView.setGravity(Gravity.CENTER_VERTICAL);
				textView.setPadding(16, 0, 0, 0);
				textView.setTextSize(ScreenUtils.dip2px(QuestionHelpActivity.this,5));
				textView.setTextColor(Color.WHITE);
				return textView;
			}

			@Override
			public int getGroupCount() {
				// TODO Auto-generated method stub
				return Constants.TITLES.length;
			}

			@Override
			public int getChildrenCount(int groupPosition) {
				// TODO Auto-generated method stub
				return 1;
			}

			@Override
			public Object getGroup(int groupPosition) {
				// TODO Auto-generated method stub
				return Constants.TITLES[groupPosition];
			}

			@Override
			public Object getChild(int groupPosition, int childPosition) {
				// TODO Auto-generated method stub
				return Constants.CONTENTS[groupPosition][childPosition];
			}

			@Override
			public long getGroupId(int groupPosition) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public long getChildId(int groupPosition, int childPosition) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public boolean hasStableIds() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public View getGroupView(int groupPosition, boolean isExpanded,
					View convertView, ViewGroup parent) {
				LinearLayout ll = new LinearLayout(QuestionHelpActivity.this);
				ll.setOrientation(LinearLayout.HORIZONTAL);
				ll.setMinimumHeight(ScreenUtils.dip2px(QuestionHelpActivity.this,16));
                ll.setBackgroundColor(getResources().getColor(R.color.light_blue));
				TextView textView = getTextView();
				textView.setTextColor(Color.WHITE);
				textView.setTextSize(18);
				textView.setText(getGroup(groupPosition).toString());
				ll.addView(textView);
				return ll;
			}

			@Override
			public View getChildView(int groupPosition, int childPosition,
					boolean isLastChild, View convertView, ViewGroup parent) {
				LinearLayout ll = new LinearLayout(QuestionHelpActivity.this);
				TextView textView = new TextView(QuestionHelpActivity.this);
				textView.setTextSize(16);
				textView.setPadding(10, 5, 10, 5);
//				textView.setText(getChild(groupPosition, childPosition)
//						.toString());
				textView.setText(Html.fromHtml(getChild(groupPosition, childPosition)
						.toString()));
				ll.addView(textView);
				return ll;
			}

			@Override
			public boolean isChildSelectable(int groupPosition,
					int childPosition) {
				// TODO Auto-generated method stub
				return false;
			}

		};
		expandableListView.setAdapter(adapter);
	}

	private void exitActivity(){
		finish();
		overridePendingTransition(R.anim.slide_bottom_in,
				R.anim.slide_top_out);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			exitActivity();
			return false;
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		application.removeActivity(this);
	}
}
