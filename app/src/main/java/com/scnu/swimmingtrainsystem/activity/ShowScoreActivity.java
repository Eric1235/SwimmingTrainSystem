package com.scnu.swimmingtrainsystem.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.db.DBManager;
import com.scnu.swimmingtrainsystem.entity.ScoreSum;
import com.scnu.swimmingtrainsystem.model2db.Plan;
import com.scnu.swimmingtrainsystem.model2db.Score;
import com.scnu.swimmingtrainsystem.util.Constants;
import com.scnu.swimmingtrainsystem.util.Statistics;
import com.scnu.swimmingtrainsystem.view.LoadingDialog;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 计时并且调整完毕后展示成绩的Activity
 * 
 * @author LittleByte
 * 
 */
public class ShowScoreActivity extends Activity {
	public static final String GENERATING_THE_SCORES = "正在统计...";

	private boolean isReset;
	private String date;
	private Integer times = 0;
	private List<ScoreSum> mScoreSum = new ArrayList<ScoreSum>();
	private List<List<Score>> scoreList = new ArrayList<List<Score>>();


	private MyApplication mApplication;
	private DBManager mDbManager;
	private ExpandableListView mExpandableListView;
	private TextView mPlanName;
	private LoadingDialog mLoadingDialog;
	private ShowScoreListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_showscore);
		initData();
		initView();
	}

	/**
	 * 初始化界面
	 */
	private void initView() {

		mExpandableListView = (ExpandableListView) findViewById(R.id.show_list);
		mPlanName = (TextView) findViewById(R.id.show_the_plan);

		Long planid = (Long) mApplication.getMap().get(Constants.PLAN_ID);
		Plan plan = DataSupport.find(Plan.class, planid);
		mPlanName.setText(plan.getPool() + "  总距离：" + plan.getDistance()
				+ "米    共" + (times - 2) + "趟");

		/**
		 * 传递更多的参数进去
		 */
		adapter = new ShowScoreListAdapter(this, scoreList, mScoreSum, 0,isReset);
		mExpandableListView.setAdapter(adapter);
		// 启动查询异步任务
		new QueryScoreTask().execute(date);
	}

	private void initData(){
		mApplication = (MyApplication) getApplication();
		mApplication.addActivity(this);
		mDbManager = DBManager.getInstance();
		times = (Integer) mApplication.getMap()
				.get(Constants.CURRENT_SWIM_TIME) + 2;
		date = (String) mApplication.getMap().get(Constants.TEST_DATE);

		Intent i = getIntent();
		HashMap<String,Object> map = (HashMap)i.getSerializableExtra("dataMap");
		isReset = (Boolean)map.get("isReset");
	}

	public void showBack(View v) {
		finish();
		overridePendingTransition(R.anim.slide_bottom_in, R.anim.slide_top_out);
	}

	/**
	 * 数据库查询异步任务，防止ANR
	 * 
	 * @author LittleByte
	 * 
	 */
	class QueryScoreTask extends AsyncTask<String, Void, TempClass> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			if (mLoadingDialog == null) {
				mLoadingDialog = LoadingDialog
						.createDialog(ShowScoreActivity.this);
				mLoadingDialog.setMessage(GENERATING_THE_SCORES);
				mLoadingDialog.setCanceledOnTouchOutside(false);
			}
			mLoadingDialog.show();
		}

		@Override
		protected TempClass doInBackground(String... params) {
			// TODO Auto-generated method stub
			TempClass tempScore = new TempClass();
			List<Integer> athIds = mDbManager
					.getAthleteNumberInScoreByDate(params[0]);
			List<ScoreSum> totalScores = mDbManager.getAthleteIdInScoreByDate(
					date, athIds,isReset);
			List<List<Score>> lists = new ArrayList<List<Score>>();
			for (int i = 1; i < times; i++) {
				List<Score> ls = mDbManager.getScoreByDateAndTimes(date, i);
				lists.add(ls);
			}
			tempScore.setScoresList(lists);
			tempScore.setTemps(totalScores);
			return tempScore;
		}

		@Override
		protected void onPostExecute(TempClass result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			adapter.setDatas(result.getScoresList(), result.getTemps(), times);
			adapter.notifyDataSetChanged();
			// 默认展开
			for (int i = 0; i < adapter.getGroupCount(); i++) {
				mExpandableListView.expandGroup(i);
			}
			mLoadingDialog.dismiss();

		}
	}

	class TempClass {
		private List<List<Score>> scoresList = new ArrayList<List<Score>>();
		List<ScoreSum> temps = new ArrayList<ScoreSum>();

		public List<List<Score>> getScoresList() {
			return scoresList;
		}

		public void setScoresList(List<List<Score>> scoresList) {
			this.scoresList = scoresList;
		}

		public List<ScoreSum> getTemps() {
			return temps;
		}

		public void setTemps(List<ScoreSum> temps) {
			this.temps = temps;
		}

	}

	class ShowScoreListAdapter extends BaseExpandableListAdapter {
		private Context mContext;
		private List<List<Score>> mLists = new ArrayList<List<Score>>();
		private List<ScoreSum> mTemps = new ArrayList<ScoreSum>();
		private int mSwimTime = 0;
		private List<ScoreSum> avgScores = new ArrayList<ScoreSum>();
		private boolean isReset;

		public ShowScoreListAdapter(Context mContext,
				List<List<Score>> scoresList, List<ScoreSum> mTemps,
				int mSwimTime,boolean isReset) {
			this.mContext = mContext;
			this.mLists = scoresList;
			this.mTemps = mTemps;
			this.mSwimTime = mSwimTime;
			this.isReset = isReset;

		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return null;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			convertView = View.inflate(mContext,
					R.layout.show_score_list_item_sub, null);
			TextView tv1 = (TextView) convertView.findViewById(R.id.show_rank);
			TextView tv2 = (TextView) convertView.findViewById(R.id.show_score);
			TextView tv3 = (TextView) convertView.findViewById(R.id.show_name);
			tv1.setText("第" + (childPosition + 1) + "名");
			if (groupPosition < getGroupCount() - 2) {
				Score s = mLists.get(groupPosition).get(childPosition);
				String nameString = mDbManager.getAthleteNameByScoreID(s
						.getId());
				tv2.setText(s.getScore());
				tv3.setText(nameString);
			} else if (groupPosition == (getGroupCount() - 2)) {
				tv2.setText(mTemps.get(childPosition).getScore());
				tv3.setText(mTemps.get(childPosition).getAthleteName());
			} else {
				tv2.setText(avgScores.get(childPosition).getScore());
				tv3.setText(avgScores.get(childPosition).getAthleteName());
			}

			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			if (mLists.size() == 0) {
				return 0;
			}
			return mLists.get(0).size();

		}

		@Override
		public Object getGroup(int groupPosition) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getGroupCount() {
			return mSwimTime;
		}

		@Override
		public long getGroupId(int groupPosition) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			convertView = View.inflate(mContext,
					R.layout.show_score_list_item_head, null);
			TextView tv1 = (TextView) convertView.findViewById(R.id.show_times);
			TextView tipView = (TextView) convertView.findViewById(R.id.tv_1);
			if (groupPosition < getGroupCount() - 2) {
				tv1.setText("第" + (groupPosition + 1) + "趟");
				tipView.setText(mLists.get(groupPosition).get(0).getDistance()
						+ "米处记录");
			}
			/**
			 * 考虑查询不是间歇的时候，不显示总成绩，因为总成绩就是最后一条成绩
			 */
			else if (groupPosition == (getGroupCount() - 2)) {
				tv1.setText("成绩总计");
				tipView.setVisibility(View.GONE);
			}
			/**
			 * 考虑不是间歇的时候，计算规则不同，所以显示的成绩也不同
			 */
			else {
				tv1.setText("平均成绩");
				tipView.setVisibility(View.GONE);
			}
			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return false;
		}

		public void setDatas(List<List<Score>> mLists, List<ScoreSum> mTemps,
				int mSwimTime) {
			this.mLists.clear();
			this.mTemps.clear();
			this.avgScores.clear();
			this.mLists = mLists;
			this.mTemps = mTemps;
			this.mSwimTime = mSwimTime;
			/**
			 * 在这里进行统计，得出总成绩
			 */
//				//没有间歇的成绩统计
//				for (ScoreSum ss : this.mTemps) {
//					ScoreSum scoreSum = new ScoreSum();
//					int msec = CommonUtils.timeString2TimeInt(ss.getScore());
//					int avgsec = msec / (mSwimTime - 2);
//					String avgScore = CommonUtils.timeInt2TimeString(avgsec);
//					scoreSum.setScore(avgScore);
//					scoreSum.setAthleteName(ss.getAthleteName());
//					avgScores.add(scoreSum);
//				}

			/**
			 * 获得平均成绩
			 */
			avgScores = Statistics.getAvgScore(mSwimTime, mTemps);

		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mApplication.getMap().put(Constants.CURRENT_SWIM_TIME, 0);
		mApplication.getMap().put(Constants.PLAN_ID, 0);
		mApplication.removeActivity(this);
	}

}
