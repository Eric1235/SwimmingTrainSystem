package com.scnu.swimmingtrainsystem.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.db.DBManager;
import com.scnu.swimmingtrainsystem.http.JsonTools;
import com.scnu.swimmingtrainsystem.model2db.Athlete;
import com.scnu.swimmingtrainsystem.model2db.Plan;
import com.scnu.swimmingtrainsystem.entity.ResponseScore;
import com.scnu.swimmingtrainsystem.model2db.Score;
import com.scnu.swimmingtrainsystem.entity.ScoreSum;
import com.scnu.swimmingtrainsystem.model2db.User;
import com.scnu.swimmingtrainsystem.utils.CommonUtils;
import com.scnu.swimmingtrainsystem.utils.Constants;
import com.scnu.swimmingtrainsystem.utils.SpUtil;
import com.scnu.swimmingtrainsystem.utils.Statistics;
import com.scnu.swimmingtrainsystem.view.LoadingDialog;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询成绩展示界面
 * 
 * @author LittleByte
 * 
 */
public class QueryScoreActivity extends Activity {
	private final static String GENERATING_RESULT_STRING = "正在生成查询结果...";
	private final static int RequestCode = 0x01;
	// Volley请求队列
	private RequestQueue mQueue;
	private LinearLayout containLayout;
	private ExpandableListView mExpandableListView;
	private TextView details;
	private TextView dateTextView;
	private DBManager dbManager;

	private boolean isReset;
	private int userid;
	private LoadingDialog mLoadingDialog;
	private Toast mToast;
	private User mUser;
	private boolean isConnect;
	private float y;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_queryscore);
		try {
			init();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			startActivity(new Intent(this, LoginActivity.class));
		}
	}

	/**
	 * 初始化Activity
	 */
	private void init() {
		MyApplication app = (MyApplication) getApplication();
		app.addActivity(this);
		dbManager = DBManager.getInstance();
		mQueue = Volley.newRequestQueue(this);
		isReset = true;
//		userid = (Integer) mApplication.getMap().get(Constants.CURRENT_USER_ID);
		userid = SpUtil.getUID(this);
		mUser = dbManager.getUserByUid(userid);
		containLayout = (LinearLayout) findViewById(R.id.ll_query_score);
		details = (TextView) findViewById(R.id.show_details);
		dateTextView = (TextView) findViewById(R.id.text_category);
		dateTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				y = dateTextView.getY();
				TranslateAnimation animation = new TranslateAnimation(0, 0, 0,
						-y);
				animation.setDuration(500);
				animation.setFillAfter(true);
				animation.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						Intent intent = new Intent(QueryScoreActivity.this,
								SearchboxActivity.class);
						startActivityForResult(intent, RequestCode);
						overridePendingTransition(R.anim.animationb,
								R.anim.animationa);
					}
				});
				containLayout.startAnimation(animation);
			}
		});
		mExpandableListView = (ExpandableListView) findViewById(R.id.query_score_list);
		isConnect = (Boolean) app.getMap().get(
				Constants.IS_CONNECT_SERVER);

	}

	/**
	 * 退出当前Activity
	 * 
	 * @param v
	 */
	public void back(View v) {
		finish();
		overridePendingTransition(R.anim.slide_bottom_in, R.anim.slide_top_out);
	}

	/**
	 * 查询成绩异步任务，防止阻塞主线程导致ANR,本地查询成绩时使用
	 * 
	 * @author LittleByte
	 * 
	 */
	class QueryScoreTask extends AsyncTask<String, Void, Map<String, Object>> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			if (mLoadingDialog == null) {
				mLoadingDialog = LoadingDialog
						.createDialog(QueryScoreActivity.this);
				mLoadingDialog.setMessage(GENERATING_RESULT_STRING);
				mLoadingDialog.setCanceledOnTouchOutside(false);
			}
			mLoadingDialog.show();
		}

		@Override
		protected Map<String, Object> doInBackground(String... params) {
			// TODO Auto-generated method stub
			Map<String, Object> map = new HashMap<String, Object>();
			// 获取本轮所有成绩
			List<Score> reScores = DataSupport.where("date=?", params[0]).find(
					Score.class, true);
			// 获取该轮成绩的计划
			int plan_id = reScores.get(0).getPlan_id();
//			Plan plan = reScores.get(0).getP();
			Plan plan = dbManager.getPlanByPid(plan_id);
			// 获取本轮成绩的总趟数
			int maxTime = reScores.get(reScores.size() - 1).getTimes();

			List<Integer> athIds = dbManager.getAthleteIdInScoreByDate(params[0]);
			List<ScoreSum> sumList = dbManager.getAthleteIdInScoreByDate(
					params[0], athIds,isReset);

			List<List<Score>> listss = new ArrayList<List<Score>>();
			// 根据时间查询成绩
			for (int t = 1; t <= maxTime; t++) {
				List<Score> sco = dbManager
						.getScoreByDateAndTimes(params[0], t);
				listss.add(sco);
			}

			map.put("time", maxTime);
			map.put("plan", plan);
			map.put("sumList", sumList);
			map.put("scores", listss);
			return map;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(Map<String, Object> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result != null) {
				int maxTime = (Integer) result.get("time");
				final Plan plan = (Plan) result.get("plan");
				List<ScoreSum> sumList = (List<ScoreSum>) result.get("sumList");
				List<List<Score>> scores = (List<List<Score>>) result
						.get("scores");
				List<ScoreSum> avgScores = new ArrayList<ScoreSum>();
				for (ScoreSum ss : sumList) {
					ScoreSum scoreSum = new ScoreSum();
					int msec = Statistics.timeString2TimeInt(ss.getScore());
					int avgsec = msec / maxTime;
					String avgScore = Statistics.timeInt2TimeString(avgsec);
					scoreSum.setScore(avgScore);
					scoreSum.setAthleteName(ss.getAthleteName());
					avgScores.add(scoreSum);
				}
				setDetailTextView(maxTime, plan);
				NameScoreListAdapter scoreListAdapter = new NameScoreListAdapter(
						QueryScoreActivity.this, scores, sumList, avgScores,
						maxTime + 2);
				mExpandableListView.setAdapter(scoreListAdapter);
				// 默认展开
				for (int i = 0; i < (maxTime + 2); i++) {
					mExpandableListView.expandGroup(i);
				}
			}
			mLoadingDialog.dismiss();
		}

	}

	private void setDetailTextView(int maxTime, final Plan plan) {
		details.setVisibility(View.VISIBLE);
		details.setText(plan.getPool() + "  共" + maxTime + "趟  " + "  目标总距离："
				+ plan.getDistance() + "米");
		details.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String extraString = "无备注";
				if (!TextUtils.isEmpty(plan.getExtra().trim())) {
					extraString = plan.getExtra();
				}
				showPlanExtra(extraString);
			}

		});
	}

	/**
	 * 弹出备注
	 * 
	 * @param s
	 *            备注内容
	 */
	private void showPlanExtra(String s) {
		// TODO Auto-generated method stub
		View view = getLayoutInflater()
				.inflate(R.layout.popupwindow_tips, null);
		TextView tipTextView = (TextView) view.findViewById(R.id.tv_pop_tips);
		tipTextView.setText(s);
		PopupWindow popupWindow = new PopupWindow(view,
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT, true);
		popupWindow.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.title_function_bg));
		popupWindow.showAsDropDown(details, containLayout.getWidth() - 50, 0);
	}

	protected void getScoresRequest(final String string) {
		// TODO Auto-generated method stub
		if (mLoadingDialog == null) {
			mLoadingDialog = LoadingDialog
					.createDialog(QueryScoreActivity.this);
			mLoadingDialog.setMessage(GENERATING_RESULT_STRING);
			mLoadingDialog.setCanceledOnTouchOutside(false);
		}
		mLoadingDialog.show();
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("up_time", string);
		jsonMap.put("uid", mUser.getUid());
		final String dateJson = JsonTools.creatJsonString(jsonMap);

		StringRequest getScoreDateList = new StringRequest(Method.POST,
				Constants.GET_SCORE, new Listener<String>() {

					@Override
					public void onResponse(String response) {
						mLoadingDialog.dismiss();
						JSONObject obj;
						try {
							obj = new JSONObject(response);
							int resCode = (Integer) obj.get("resCode");
							int distance = (Integer) obj.get("distance");
							String poolString = (String) obj.get("pool");
							if (resCode == 1) {
								int maxTime = 0;
								ResponseScore[] tempScores = JsonTools
										.getObject(obj.get("dataList")
												.toString(),
												ResponseScore[].class);
								for (ResponseScore responseScore : tempScores) {
									int curTime = responseScore.getTimes();
									maxTime = curTime > maxTime ? curTime
											: maxTime;
									Score score = new Score();
									score.setDate(responseScore.getUp_time());
									score.setDistance(responseScore
											.getDistance());
									score.setScore(responseScore.getScore());
									score.setTimes(responseScore.getTimes());
									score.setType(1);
//									score.setUser(mUser);
									Athlete athlete = dbManager
											.getAthleteByAid(responseScore
													.getAthlete_id());
//									score.setAthlete(athlete);
									score.setAthlete_id(athlete.getAid());
									score.save();
								}
								String resDate = tempScores[0].getUp_time();
								List<Integer> athIds = dbManager
										.getAthleteIdInScoreByDate(resDate);
								List<ScoreSum> sumList = dbManager
										.getAthleteIdInScoreByDate(resDate,
												athIds,isReset);
								List<List<Score>> listss = new ArrayList<List<Score>>();
								// 根据时间查询成绩
								for (int t = 1; t <= maxTime; t++) {
									List<Score> sco = dbManager
											.getScoreByDateAndTimes(resDate, t);
									listss.add(sco);
								}
								details.setVisibility(View.VISIBLE);
								details.setText(poolString +" 米 " + "  目标总距离:"
										+ distance + "米");

								List<ScoreSum> avgScores = new ArrayList<ScoreSum>();
								for (ScoreSum ss : sumList) {
									ScoreSum scoreSum = new ScoreSum();
									int msec = Statistics
											.timeString2TimeInt(ss.getScore());
									int avgsec = msec / maxTime;
									String avgScore = Statistics
											.timeInt2TimeString(avgsec);
									scoreSum.setScore(avgScore);
									scoreSum.setAthleteName(ss.getAthleteName());
									avgScores.add(scoreSum);
								}

								NameScoreListAdapter scoreListAdapter = new NameScoreListAdapter(
										QueryScoreActivity.this, listss,
										sumList, avgScores, maxTime + 2);
								mExpandableListView
										.setAdapter(scoreListAdapter);
								dbManager.deleteScores(resDate);
								for (int i = 0; i < (maxTime + 2); i++) {
									mExpandableListView.expandGroup(i);
								}
							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						mLoadingDialog.dismiss();
					}
				}) {

			@Override
			protected Response<String> parseNetworkResponse(
					NetworkResponse response) {
				String str = null;
				try {
					str = new String(response.data, "utf-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return Response.success(str,
						HttpHeaderParser.parseCacheHeaders(response));
			}

			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				// 设置请求参数
				Map<String, String> map = new HashMap<String, String>();
				map.put("date", dateJson);
				return map;
			}
		};
		getScoreDateList.setRetryPolicy(new DefaultRetryPolicy(
				Constants.SOCKET_TIMEOUT,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		mQueue.add(getScoreDateList);

	}

	class NameScoreListAdapter extends BaseExpandableListAdapter {
		private Context mContext;
		private List<List<Score>> mLists = new ArrayList<List<Score>>();
		private List<ScoreSum> mTemps = new ArrayList<ScoreSum>();
		private List<ScoreSum> mAvgScores = new ArrayList<ScoreSum>();
		private int mSwimTime = 0;

		public NameScoreListAdapter(Context mContext, List<List<Score>> mLists,
				List<ScoreSum> mTemps, List<ScoreSum> avgScores, int mSwimTime) {
			this.mContext = mContext;
			this.mLists = mLists;
			this.mTemps = mTemps;
			this.mAvgScores = avgScores;
			this.mSwimTime = mSwimTime;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return mLists.get(groupPosition).get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ChildHolder childHolder = null;
			if (convertView == null) {
				childHolder = new ChildHolder();
				convertView = View.inflate(mContext,
						R.layout.show_score_list_item_sub, null);
				childHolder.rank = (TextView) convertView
						.findViewById(R.id.show_rank);
				childHolder.score = (TextView) convertView
						.findViewById(R.id.show_score);
				childHolder.name = (TextView) convertView
						.findViewById(R.id.show_name);
				convertView.setTag(childHolder);
			} else {
				childHolder = (ChildHolder) convertView.getTag();
			}

			if (groupPosition < mSwimTime - 2) {
				childHolder.rank.setText("第" + (childPosition + 1) + "名");
				childHolder.score.setText(mLists.get(groupPosition)
						.get(childPosition).getScore());
				/**
				 * 要转换为名字啦
				 */
				childHolder.name.setText(mLists.get(groupPosition)
						.get(childPosition).getAthlete_id());
			} else if (groupPosition == (mSwimTime - 2)) {
				childHolder.rank.setText("第" + (childPosition + 1) + "名");
				childHolder.score.setText(mTemps.get(childPosition).getScore());
				childHolder.name.setText(mTemps.get(childPosition)
						.getAthleteName());
			} else {
				childHolder.rank.setText("第" + (childPosition + 1) + "名");
				childHolder.score.setText(mAvgScores.get(childPosition)
						.getScore());
				childHolder.name.setText(mAvgScores.get(childPosition)
						.getAthleteName());
			}

			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			// TODO Auto-generated method stub
			if (mLists.size() == 0) {
				return 0;
			}
			return mLists.get(0).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			// TODO Auto-generated method stub
			return groupPosition;
		}

		@Override
		public int getGroupCount() {
			// TODO Auto-generated method stub
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
			GroupHolder groupHolder = null;
			if (convertView == null) {
				groupHolder = new GroupHolder();
				convertView = View.inflate(mContext,
						R.layout.query_score_list_item_head, null);
				groupHolder.timeTextView = (TextView) convertView
						.findViewById(R.id.test_date);
				groupHolder.curDistance = (TextView) convertView
						.findViewById(R.id.test_plan);
				convertView.setTag(groupHolder);
			} else {
				groupHolder = (GroupHolder) convertView.getTag();
			}

			if (groupPosition < getGroupCount() - 2) {
				groupHolder.timeTextView.setText("第" + (groupPosition + 1)
						+ "趟");
				groupHolder.curDistance.setText("当前距离 "
						+ mLists.get(groupPosition).get(0).getDistance() + "米");
			} else if (groupPosition == (getGroupCount() - 2)) {
				groupHolder.timeTextView.setText("本轮总计");
			} else {
				groupHolder.timeTextView.setText("平均成绩");
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

		final class GroupHolder {
			private TextView timeTextView;
			private TextView curDistance;
		}

		final class ChildHolder {
			private TextView rank;
			private TextView score;
			private TextView name;
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			finish();
			overridePendingTransition(R.anim.slide_bottom_in,
					R.anim.slide_top_out);
			return false;
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		TranslateAnimation animation = new TranslateAnimation(0, 0, -y, 0);
		animation.setDuration(500);
		animation.setFillAfter(true);
		containLayout.startAnimation(animation);
		if (resultCode != 0x11) {
			String dateString = data.getStringExtra("date");
			dateTextView.setText(dateString);
			if (resultCode == 1) {
				if (isConnect) {
					getScoresRequest(dateString);
				} else {
					CommonUtils.showToast(this, mToast, getString(R.string.cannot_connect_to_server));
				}
			} else {
				new QueryScoreTask().execute(dateString);
			}
		}

	}

}
