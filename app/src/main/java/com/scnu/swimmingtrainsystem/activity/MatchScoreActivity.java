package com.scnu.swimmingtrainsystem.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.scnu.draglistview_library.dslv.DragSortListView;
import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.adapter.DragAdapter;
import com.scnu.swimmingtrainsystem.adapter.ScoreListAdapter;
import com.scnu.swimmingtrainsystem.db.DBManager;
import com.scnu.swimmingtrainsystem.entity.SmallPlan;
import com.scnu.swimmingtrainsystem.entity.SmallScore;
import com.scnu.swimmingtrainsystem.http.JsonTools;
import com.scnu.swimmingtrainsystem.model2db.Athlete;
import com.scnu.swimmingtrainsystem.model2db.Plan;
import com.scnu.swimmingtrainsystem.model2db.Score;
import com.scnu.swimmingtrainsystem.model2db.User;
import com.scnu.swimmingtrainsystem.utils.AppController;
import com.scnu.swimmingtrainsystem.utils.CommonUtils;
import com.scnu.swimmingtrainsystem.utils.Constants;
import com.scnu.swimmingtrainsystem.utils.NetworkUtil;
import com.scnu.swimmingtrainsystem.utils.SpUtil;
import com.scnu.swimmingtrainsystem.utils.VolleyUtil;
import com.scnu.swimmingtrainsystem.view.LoadingDialog;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchScoreActivity extends Activity implements
		OnItemLongClickListener ,OnClickListener{

	private MyApplication app;
	private int userId;
	private Plan plan;
	private ArrayList<String> originScores = new ArrayList<>();
	private ArrayList<Athlete> originNames = new ArrayList<>();
	private ArrayList<String> scores = new ArrayList<>();
	private List<String> dragDatas1;
	private List<Athlete> dragDatas;
	private boolean isReset;
	//计时趟数够了的标志
	private boolean isMatchDone;
	private boolean isConnected;
	private Map<String,String> athleteStrokeMap;
	private List<Integer> dragAthleteIDs;

	/**
	 * 拖拽运动员的适配器
	 */
	private DragAdapter dragAdapter;
	private ScoreListAdapter adapter;
	private List<ListView> viewList;
	private AutoCompleteTextView acTextView;
	private DBManager mDbManager;

	private Button btNextTiming, btStatistics;
	private ImageButton btnReload,btnBack;
	private DragSortListView scoreListView;
	private DragSortListView nameListView;

	private LoadingDialog loadingDialog;
	private Toast mToast;
	private LinearLayout mLayout;
	private RelativeLayout mLayout2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_matchscore);
		Intent i = getIntent();
		isReset = i.getBooleanExtra(Constants.WATCHISRESET,true);
		initData();
		initView();

	}

	@SuppressWarnings("unchecked")
	private void initView() {
		// TODO Auto-generated method stub

		mLayout = (LinearLayout) findViewById(R.id.ll_pop);
		mLayout2 = (RelativeLayout) findViewById(R.id.match_score_headbar);
		btNextTiming = (Button) findViewById(R.id.match_done);
		btStatistics = (Button) findViewById(R.id.match_statistic);
		btnReload = (ImageButton) findViewById(R.id.btn_match_reload);
		btnBack = (ImageButton) findViewById(R.id.btn_match_back);
		scoreListView = (DragSortListView) findViewById(R.id.matchscore_list);
		nameListView = (DragSortListView) findViewById(R.id.matchName_list);
		acTextView = (AutoCompleteTextView) findViewById(R.id.match_act_current_distance);
		nameListView.setDropListener(onDrop);

		btNextTiming.setOnClickListener(this);
		btnReload.setOnClickListener(this);
		btNextTiming.setOnClickListener(this);
		btStatistics.setOnClickListener(this);
		btnBack.setOnClickListener(this);

		/**
		 * 设置运动员名字不能删除
		 */
		nameListView.setDragScrollProfile(ssProfile);

		scoreListView.setRemoveListener(onRemove2);
		// 设置数据源
		String[] autoStrings = getResources().getStringArray(R.array.swim_length);

		ArrayAdapter<String> tipsAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, autoStrings);
		// 设置AutoCompleteTextView的Adapter
		acTextView.setAdapter(tipsAdapter);
		acTextView.setDropDownHeight(350);
		acTextView.setThreshold(1);

		int numberth = (Integer) app.getMap().get(Constants.CURRENT_SWIM_TIME);
		String intervalString = (String) app.getMap().get(Constants.INTERVAL);
		int intervalDistance = Integer
				.parseInt(intervalString.replace("米", ""));
		acTextView.setText(intervalDistance * numberth + "");
		int totalDistance = plan.getDistance();
		/**
		 * 当计时结束的时候，隐藏第一个按钮，第二个按钮改字
		 */
		if (totalDistance <= intervalDistance * numberth) {
			btNextTiming.setVisibility(View.GONE);
			btStatistics.setText(getString(R.string.adjust_finish_goto_statistics));
			isMatchDone = true;
		}

//		dragDatas = (List<String>) app.getMap().get(Constants.DRAG_NAME_LIST);
		/**
		 * 通过数据库查询得到运动员
		 */
		dragDatas = DBManager.getInstance().getAthleteByIDs(dragAthleteIDs);
		/**
		 * 备份数据
		 */
		originNames.addAll(dragDatas);
		viewList = new ArrayList<>();
		viewList.add(scoreListView);
		viewList.add(nameListView);
		MyScrollListener mListener = new MyScrollListener();
		/**
		 * 设置成绩不能上下移动
		 */
//		scoreListView.setOnScrollListener(mListener);
		nameListView.setOnScrollListener(mListener);
		adapter = new ScoreListAdapter(scoreListView, this, scores);

		/**
		 * 运动员名单适配器
		 */
		dragAdapter = new DragAdapter(this, R.layout.drag_list_item, dragDatas);
		scoreListView.setAdapter(adapter);
		nameListView.setAdapter(dragAdapter);
		scoreListView.setOnItemLongClickListener(this);
	}

	private void initData(){

		/**
		 * 接收传过来的成绩，数组
		 */
		Intent result = getIntent();
		scores = result.getStringArrayListExtra("SCORES");
		originScores.addAll(scores);

		app = (MyApplication) getApplication();
		app.addActivity(this);
		mDbManager = DBManager.getInstance();
		athleteStrokeMap = (Map<String,String>)app.getMap().get("stroke");

		userId = SpUtil.getUID(MatchScoreActivity.this);
		Long planId = (Long) app.getMap().get(Constants.PLAN_ID);
		plan = DataSupport.find(Plan.class, planId);
		isMatchDone = false;

		/**
		 * 获取选择的运动员id
		 */
		dragAthleteIDs = (List<Integer>) app.getMap().get(Constants.DRAG_NAME_LIST_IDS);

	}

	private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
		@Override
		public void drop(int from, int to) {
			if(from != to){
				Athlete item = (Athlete)dragAdapter.getItem(from);

				dragAdapter.notifyDataSetChanged();
				dragAdapter.remove(item);
				dragAdapter.insert(item, to);
			}

		}
	};

	/**
	 * 运动员名字删除监听器
	 */
	private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener() {
		@Override
		public void remove(int which) {
			if (dragDatas.size() > 1) {
				dragAdapter.remove(dragAdapter.getItem(which));
			} else {
				CommonUtils.showToast(MatchScoreActivity.this, mToast,
						getString(R.string.leave_at_least_one_athlete));
			}
			dragAdapter.notifyDataSetChanged();
		}
	};

	private DragSortListView.DragScrollProfile ssProfile = new DragSortListView.DragScrollProfile() {
		@Override
		public float getSpeed(float w, long t) {
			if (w > 0.8f) {
				// Traverse all views in a millisecond
				return ((float) dragAdapter.getCount()) / 0.001f;
			} else {
				return 10.0f * w;
			}
		}
	};

	/**
	 * 成绩删除监听器
	 */
	private DragSortListView.RemoveListener onRemove2 = new DragSortListView.RemoveListener() {
		@Override
		public void remove(int which) {
			if (scores.size() > 1) {
				scores.remove(which);
			} else {
				CommonUtils.showToast(MatchScoreActivity.this, mToast,
						getString(R.string.leave_at_least_one_score));
			}
			adapter.notifyDataSetChanged();
		}
	};

	/**
	 * 匹配完毕，可以进入下一趟计时或者进入本轮总计
	 * 进一步设计，应该要把排序好的运动员存储到全局
	 * @param
	 */
	public void matchDone() {
		int nowCurrent = (Integer) app.getMap()
				.get(Constants.CURRENT_SWIM_TIME);
		String actv = acTextView.getText().toString().trim();
		int crrentDistance = 0;
		if (!TextUtils.isEmpty(actv)) {
			crrentDistance = Integer.parseInt(acTextView.getText().toString()
					.trim().replace("米", ""));
		}
		// 暂时保存到SharePreferences
		String scoresString = JsonTools.creatJsonString(scores);
		String athleteJson = JsonTools.creatJsonString(dragDatas);
		/**
		 * 获得排序过后的运动员列表
		 */
		List<Athlete> athletes = dragAdapter.getAthletes();
		List<Integer> athleteIds = CommonUtils.getAthleteIdsByAthletes(athletes);
		app.getMap().put(Constants.DRAG_NAME_LIST_IDS,athleteIds);
		String athleteidJson = JsonTools.creatJsonString(athleteIds);
		createDialog(this, nowCurrent, crrentDistance, scoresString,
				athleteJson,athleteidJson);
	}

	/**
	 * 如果当前成绩数目与运动员人数相等,并且是该轮游泳只有一趟则直接保存到数据库
	 * 
	 * @param date
	 * @param nowCurrent
	 * @param distance
	 */
	private void matchSuccess(String date, int nowCurrent, int distance) {
		List<Athlete> athletes = dragDatas;
		for (int i = 0; i < scores.size(); i++) {
			Athlete a = athletes.get(i);
			Score s = new Score();
			s.setDate(date);
			s.setTimes(nowCurrent);
			s.setScore(scores.get(i));
			s.setType(Constants.NORMALSCORE);
			s.setAthlete_id(a.getAid());
			//通过aid和传过来的map得到相应的泳姿
			int strokeNumber = CommonUtils.getAthleteStroke(athleteStrokeMap, String.valueOf(a.getAid()));
			s.setStroke(strokeNumber);
			s.setDistance(distance);
			s.setPlan_id(plan.getPid());
			/**
			 * 把成绩存入到数据库中
			 */
			s.save();
		}
		isConnected = NetworkUtil.isConnected(this);
		if (isConnected) {
			if (loadingDialog == null) {
				loadingDialog = LoadingDialog.createDialog(this);
				loadingDialog.setMessage(getString(R.string.onSubmitting));
				loadingDialog.setCanceledOnTouchOutside(false);
			}
			loadingDialog.show();
			addScoreRequest(date);
		} else {
			CommonUtils.showToast(this,mToast,getString(R.string.network_error));
		}

	}


	/**
	 * 結束本轮计时，进入本轮计时详情情况
	 * 需要清空之前的数据，或者要做一下操作，防止误触
	 * @param
	 */
	public void finishTiming() {
		String date = (String) app.getMap().get(Constants.TEST_DATE);
		String actv = acTextView.getText().toString().trim();
		int crrentDistance = 0;
		if (!TextUtils.isEmpty(actv)) {
			crrentDistance = Integer.parseInt(actv.replace("米", ""));
		}
		int nowCurrent = (Integer) app.getMap()
				.get(Constants.CURRENT_SWIM_TIME);
		app.getMap().put(Constants.DRAG_NAME_LIST, null);
		int scoresNumber = adapter.getCount();
		int athleteNumber = dragAdapter.getCount();

		if (nowCurrent == 1) {
			if (crrentDistance == 0 && TextUtils.isEmpty(actv)) {
				CommonUtils.showToast(this, mToast, getString(R.string.fill_in_scores_distance));
				return;
			} else if (scoresNumber != athleteNumber) {
				CommonUtils.showToast(this, mToast, getString(R.string.score_num_not_equalwith_athlete_num));
				return;
			} else {
				// 如果这是第一趟并且成绩数目与运动员数目相等，则直接保存到数据库
				matchSuccess(date, nowCurrent, crrentDistance);
			}
		} else {
			/**
			 * 通过适配器获取到string
			 */
			String scoresString = JsonTools.creatJsonString(scores);
			dragDatas1 = CommonUtils.getAthleteNamesByAthletes(dragAdapter.getAthletes());
			String athleteJson = JsonTools.creatJsonString(dragDatas1);
			String athleteidJson = JsonTools.creatJsonString(CommonUtils.getAthleteIdsByAthletes(dragAdapter.getAthletes()));
			SpUtil.saveCurrentScoreAndAthlete(this, nowCurrent,
					crrentDistance, scoresString, athleteJson, athleteidJson);
			AppController.gotoEachTimeScoreActivity(MatchScoreActivity.this, isReset);
			if(isReset == false){
				sendFinishTimerMsg();
			}
			finish();

		}


	}

	/**
	 * 发送结束计时的广播
	 */
	private void sendFinishTimerMsg(){
		Intent i = new Intent();
		i.setAction(TimerActivity.FINISHTIMER);
		sendBroadcast(i);
	}

	/**
	 * 退出当前窗体事件
	 */
	private void matchBack() {
		app.getMap().put(Constants.CURRENT_SWIM_TIME, 0);
		finish();
		overridePendingTransition(R.anim.slide_bottom_in, R.anim.slide_top_out);
	}

	private void reLoad() {
		scores.clear();
		scores.addAll(originScores);
		adapter.notifyDataSetChanged();
		dragDatas.clear();
		dragDatas.addAll(originNames);
		dragAdapter.notifyDataSetChanged();
	}

	/**
	 * 创建成绩数目与运动员数目不同提示对话框
	 * 
	 * @param context
	 * @param i
	 * @param crrentDistance
	 * @param scoreString
	 * @param athleteString
	 */
	private void createDialog(final Context context, final int i,
			final int crrentDistance, final String scoreString,
			final String athleteString,final String athleteidString) {
		AlertDialog.Builder build = new AlertDialog.Builder(this);
		build.setTitle(getString(R.string.system_hint)).setMessage(
				getString(R.string.goto_next_timer_or_adjust_score));
		build.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		build.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				// 暂时保存到SharePreferences
				SpUtil.saveCurrentScoreAndAthlete(context, i,
						crrentDistance, scoreString, athleteString,athleteidString);
				/**
				 * 跳转到计时activity
				 */
				if(isReset == true){
					AppController.gotoTimerActivity(MatchScoreActivity.this,isReset);
				}

				finish();
			}
		}).show();

	}

	private void createAlertDialog(){
		AlertDialog.Builder build = new AlertDialog.Builder(this);
		build.setTitle(getString(R.string.system_hint)).setMessage(
				getString(R.string.goto_adjust_score));
		build.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		build.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finishTiming();
				dialog.dismiss();
			}
		}).show();
	}

	private class MyScrollListener implements OnScrollListener {

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			// TODO Auto-generated method stub
			// 关键代码
			View subView = view.getChildAt(0);
			if (subView != null) {
				final int top = subView.getTop();
				for (ListView item : viewList) {
					item.setSelectionFromTop(firstVisibleItem, top);
				}
			}
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

			// 关键代码
			if (scrollState == SCROLL_STATE_IDLE
					|| scrollState == SCROLL_STATE_TOUCH_SCROLL) {
				View subView = view.getChildAt(0);
				if (subView != null) {
					final int top = subView.getTop();
					final int position = view.getFirstVisiblePosition();
					for (ListView item : viewList) {
						item.setSelectionFromTop(position, top);
					}
				}
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		// 进入计时界面却不进行成绩匹配而直接返回,要将当前第几次计时置0
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			app.getMap().put(Constants.CURRENT_SWIM_TIME, 0);
			finish();
			overridePendingTransition(R.anim.slide_bottom_in,
					R.anim.slide_top_out);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 发送成绩，找出需要的数据，哪个是空的
	 * @param date
	 */
	private void addScoreRequest(String date) {
		/**
		 * 获取上传的map
		 */
		Map<String,String> map = getDataMap(date);
		VolleyUtil.ResponseListener responseListener = new VolleyUtil.ResponseListener() {
			@Override
			public void onSuccess(String string) {
				loadingDialog.dismiss();
						JSONObject obj;
						try {
							obj = new JSONObject(string);
							int resCode = (Integer) obj.get("resCode");
							int planId = (Integer) obj.get("plan_id");
							if (resCode == 1) {
								CommonUtils.showToast(MatchScoreActivity.this,
										mToast, getString(R.string.synchronized_success));
								ContentValues values = new ContentValues();
								values.put("pid", planId);
								Plan.updateAll(Plan.class, values,
										String.valueOf(plan.getId()));
								HashMap<String,Object> map = getDeliveryMap(isReset);
								AppController.gotoShowScoreActivity(MatchScoreActivity.this,map);
								finish();
							} else {
								CommonUtils.showToast(MatchScoreActivity.this,
										mToast, getString(R.string.synchronized_failed));
							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

			}

			@Override
			public void onError(String string) {
				loadingDialog.dismiss();
				CommonUtils.showToast(MatchScoreActivity.this,mToast,getString(R.string.server_or_network_error));
			}
		};
		VolleyUtil.httpJson(Constants.ADD_SCORE, Request.Method.POST,map,responseListener,app);
	}


	private Map<String,String> getDataMap(String date){
		/**
		 * 获取到训练计划
		 */
		plan.setPdate(date);
		SmallPlan sp = CommonUtils.convertPlan(plan);
		List<SmallScore> smallScores = new ArrayList<SmallScore>();
		List<Score> scoresResult = mDbManager.getScoreByDate(date);
		for (Score s : scoresResult) {
			SmallScore smScore = CommonUtils.convertScore(s);
			smallScores.add(smScore);
		}
		/**
		 * 获取运动员id
		 */
		List<Integer> aidList = CommonUtils.getAthleteIdsByAthletes(dragAdapter.getAthletes());
		User user = mDbManager.getUserByUid(userId);
		Map<String, Object> scoreMap = new HashMap<String, Object>();
		scoreMap.put("score", smallScores);
		scoreMap.put("plan", sp);
		scoreMap.put("uid", user.getUid());
		scoreMap.put("type", 1);
		scoreMap.put("isreset",isReset);
		final String jsonString = JsonTools.creatJsonString(scoreMap);
		Map<String, String> map = new HashMap<String, String>();
		map.put("scoresJson", jsonString);

		return map;
	}

	private HashMap<String,Object> getDeliveryMap(boolean isReset){
		HashMap<String,Object> map = new HashMap<String, Object>();
		map.put("isReset",isReset);
		return map;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub
		showPopWindow(position);
		return true;
	}

	private void showPopWindow(final int position) {
		// TODO Auto-generated method stub
		TextView copyView = (TextView) getLayoutInflater().inflate(
				android.R.layout.simple_list_item_1, null);
		copyView.setText(getString(R.string.copy_add));
		copyView.setTextColor(getResources().getColor(R.color.white));
		final PopupWindow pop = new PopupWindow(copyView,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		pop.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.title_function_bg));
		pop.setOutsideTouchable(true);
		int yoff = mLayout2.getHeight()
				* (position - scoreListView.getFirstVisiblePosition() + 1);
		pop.showAsDropDown(mLayout, scoreListView.getRight() / 2, yoff);
		copyView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				scores.add(position, scores.get(position));
				adapter.notifyDataSetChanged();
//				dragDatas1.add(position, null);
//				dragAdapter.notifyDataSetChanged();
				pop.dismiss();
			}
		});

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()){
			case R.id.match_done:
				matchDone();
				break;
			case R.id.match_statistic:
				/**
				 * 如果趟数没有完成，就要给出提示，完成了，就直接进入统计界面
				 */
				if(isMatchDone){
					finishTiming();
				}else{
					createAlertDialog();
				}
				break;
			case R.id.btn_match_reload:
				reLoad();
				break;
			case R.id.btn_match_back:
				matchBack();
				break;
			default:
				break;
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	/**
	 * 进行数据的销毁
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		app.removeActivity(this);
	}
}
