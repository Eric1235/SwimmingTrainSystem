package com.scnu.swimmingtrainsystem.activity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.adapter.ViewPargerAdpter;
import com.scnu.swimmingtrainsystem.db.DBManager;
import com.scnu.swimmingtrainsystem.entity.SmallPlan;
import com.scnu.swimmingtrainsystem.entity.SmallScore;
import com.scnu.swimmingtrainsystem.fragment.EachTimeScoreFragment;
import com.scnu.swimmingtrainsystem.http.JsonTools;
import com.scnu.swimmingtrainsystem.model2db.Athlete;
import com.scnu.swimmingtrainsystem.model2db.Plan;
import com.scnu.swimmingtrainsystem.model2db.Score;
import com.scnu.swimmingtrainsystem.model2db.User;
import com.scnu.swimmingtrainsystem.utils.AppController;
import com.scnu.swimmingtrainsystem.utils.CommonUtils;
import com.scnu.swimmingtrainsystem.utils.Constants;
import com.scnu.swimmingtrainsystem.utils.NetworkUtil;
import com.scnu.swimmingtrainsystem.utils.ScreenUtils;
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

public class EachTimeScoreActivity extends FragmentActivity implements View.OnClickListener{
	private MyApplication myApplication;
	private DBManager mDbManager;

	private ArrayList<Fragment> fragmentsList;
	private int idex = 0;
	private ArrayList<String> list;
	private Map<String,String> athleteStrokeMap;
	private Toast mToast;

	private String date;
	private Plan plan;
	private int userID;
	private boolean isReset;
	private boolean isSavedScore;

	private HorizontalScrollView scrollView;
	private LinearLayout layout;
	private ViewPager viewPager;
	private ImageButton btnBack;

	/**
	 * 运动员列表
	 */
	private List<Athlete> athletes;

	private LoadingDialog loadingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modify_each_score);
		Intent i = getIntent();
		isReset = i.getBooleanExtra("isReset",false);
		isSavedScore = false;
		init();
		InitViewPager();

	}

	private void init() {
		myApplication = (MyApplication) getApplication();
		myApplication.addActivity(this);
		mDbManager = DBManager.getInstance();
		date = (String) myApplication.getMap().get(Constants.TEST_DATE);
		athleteStrokeMap = (Map<String,String>)myApplication.getMap().get("stroke");
		userID = SpUtil.getUID(EachTimeScoreActivity.this);
		Long planId = (Long) myApplication.getMap().get(Constants.PLAN_ID);
		plan = DataSupport.find(Plan.class, planId);

		scrollView = (HorizontalScrollView) findViewById(R.id.horizontalScrollView_home);
		layout = (LinearLayout) findViewById(R.id.ll_shouyebiaoqian);
		viewPager = (ViewPager) findViewById(R.id.viewparger_home);
		scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);

		btnBack = (ImageButton) findViewById(R.id.modify_back);
		btnBack.setOnClickListener(this);

		list = new ArrayList<>();
		int swimTimes = (Integer) myApplication.getMap().get(
				Constants.CURRENT_SWIM_TIME);
		for (int i = 1; i <= swimTimes; i++) {
			String str = "第" + i + "趟计时";
			list.add(str);
		}

	}

	private void InitViewPager() {
		if (list.size() <= 1) {
			layout.setVisibility(View.GONE);
		}
		fragmentsList = new ArrayList<Fragment>();
		int width;
		// 这里设置标签最多显示四个
		if (list.size() > 4) {
			width = ScreenUtils.getScreenWidth(this) / 4;
		} else {
			width = ScreenUtils.getScreenWidth(this) / list.size();
		}

		for (int i = 0; i < list.size(); i++) {
			SharedPreferences sp = getSharedPreferences(Constants.LOGININFO,
					MODE_PRIVATE);
			int currentDistance = sp.getInt(Constants.CURRENT_DISTANCE
					+ (i + 1), 0);
			String scoresJsonString = sp.getString(Constants.SCORESJSON
					+ (i + 1), "");
			String namesJsonString = sp.getString(Constants.ATHLETEJSON
					+ (i + 1), "");
			/**
			 * 获取运动员id字符串，然后传入
			 */
			String athleteidJsonString = sp.getString(Constants.ATHLETEIDJSON + (i + 1),"");

			EachTimeScoreFragment homeItemfragment = new EachTimeScoreFragment(
					i, currentDistance, scoresJsonString, namesJsonString,athleteidJsonString);
			fragmentsList.add(homeItemfragment);
			LinearLayout linearLayout = new LinearLayout(this);
			linearLayout.setGravity(Gravity.CENTER);
			linearLayout.setLayoutParams(new LinearLayout.LayoutParams(width,
					ViewGroup.LayoutParams.WRAP_CONTENT));
			TextView tv = new TextView(this);
			tv.setText(list.get(i));
			tv.setTextSize(18);
			tv.setGravity(Gravity.CENTER);
			tv.setPadding(20, 10, 20, 10);
			tv.setTextColor(getResources().getColor(R.color.black));
			tv.setLayoutParams(new LinearLayout.LayoutParams(170,
					ViewGroup.LayoutParams.WRAP_CONTENT));
			tv.setSingleLine();
			linearLayout.setId(i);
			if (i == 0) {
				linearLayout.setBackgroundColor(getResources().getColor(R.color.white));
			}
			linearLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					viewPager.setCurrentItem(arg0.getId());
				}
			});
			linearLayout.addView(tv);
			layout.addView(linearLayout);
		}
		viewPager.setAdapter(new ViewPargerAdpter(getSupportFragmentManager(),
				fragmentsList));
		viewPager.setCurrentItem(0);
		layout.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.light_blue));
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}

	public class MyOnPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageSelected(int arg0) {
			int width = ScreenUtils.getScreenWidth(EachTimeScoreActivity.this) / 4;
			for (int i = 0; i < layout.getChildCount(); i++) {

				layout.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.white));
				if (arg0 == i) {
					layout.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.light_blue));
				}
			}
			if (idex < arg0 && arg0 > 2) {
				scrollView.smoothScrollBy(width, 0);

			} else {
				if (fragmentsList.size() - arg0 > 3) {

					scrollView.smoothScrollBy(-width, 0);
				}
			}
			idex = arg0;
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.modify_back:
				finish();
				break;
			default:
				break;
		}
	}

	private void createDialog() {
		AlertDialog.Builder build = new AlertDialog.Builder(this);
		build.setTitle(getString(R.string.system_hint)).setMessage(getString(R.string.quit_and_dont_save_score));
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
				myApplication.getMap().put(Constants.COMPLETE_NUMBER, 0);
				finish();
			}
		}).show();

	}

	/**
	 * 匹配完成以后，进行点击，上传成绩
	 * @param v
	 * @throws JSONException
	 */
	public void finishModify(View v) throws JSONException {
		if(isSavedScore == false){
			saveScore();
		}
		isSavedScore = true;
		upLoadScore();

	}

	private void saveScore(){
		int length = fragmentsList.size();
		List<Integer> aidList = new ArrayList<Integer>();
		/**
		 * 循环读取fragment的成绩
		 */
		for (int i = 0; i < length; i++) {
			Map<String, Object> result = ((EachTimeScoreFragment) fragmentsList
					.get(i)).check();
			int number = (Integer) myApplication.getMap().get(
					Constants.COMPLETE_NUMBER);
			int resCode = (Integer) result.get("resCode");
			int position = (Integer) result.get("position");
			List<Integer> aidsubList = (List<Integer>)result.get("athleteids");
			aidList.addAll(aidsubList);
			if (resCode == 1 && number != length) {
				viewPager.setCurrentItem(position);
				CommonUtils.showToast(this, mToast, getString(R.string.score_is_zero));
				break;
			} else if (resCode == 2 && number != length) {
				viewPager.setCurrentItem(position);
				CommonUtils.showToast(this, mToast, getString(R.string.score_num_not_match_athlete_num));
				break;
			} else if (resCode == 0 && number != length) {
				// 以下只是保存length-1次
				saveScore(i, result,aidsubList);
				viewPager.setCurrentItem(position + 1);
			} else if (resCode == 0 && number == length) {
				saveScore(i, result, aidsubList);
				viewPager.setCurrentItem(length - 1);
				CommonUtils.showToast(this, mToast, getString(R.string.match_done));

				break;
			}
		}
	}

	/**
	 * 这个函数是
	 */
	private void upLoadScore(){
		boolean isConnect = NetworkUtil.isConnected(this);
		if (isConnect) {
			// 如果可以联通服务器则发送添加成绩请求
			if (loadingDialog == null) {
				loadingDialog = LoadingDialog.createDialog(this);
				loadingDialog.setMessage(getString(R.string.synchronizing));
				loadingDialog.setCanceledOnTouchOutside(false);
			}
			loadingDialog.show();
			addScoreRequest();
		} else {
			CommonUtils.showToast(this,mToast,getString(R.string.network_error));
		}
	}

	/**
	 * 保存成绩到数据库
	 * 存储关联
	 * @param i
	 * @param result
	 */
	@SuppressWarnings("unchecked")
	private void saveScore(int i, Map<String, Object> result,List<Integer> athleteids) {
		String curDistance = (String) result.get("distance");
		int distance = Integer.parseInt(curDistance);
		List<String> scoresList = (List<String>) result.get("scores");
		/**
		 * 获取运动员id列表
		 */
		int scoresNumber = scoresList.size();
		for (int l = 0; l < scoresNumber; l++) {
			Score score = new Score();
			score.setDate(date);
			score.setType(Constants.NORMALSCORE);
			score.setTimes(i + 1);
			score.setDistance(distance);
			score.setPlan_id(plan.getPid());
			score.setScore(scoresList.get(l));
			/**
			 * 通过运动员id来获取运动员，然后就直接将运动员id存到score那里去
			 */
			Athlete athlete = mDbManager.getAthleteByAid(athleteids.get(l));
			int strokeNumber = CommonUtils.getAthleteStroke(athleteStrokeMap, String.valueOf(athlete.getAid()));
			score.setStroke(strokeNumber);
			score.setAthlete_id(athlete.getAid());
			/**
			 * 保存成绩到数据库
			 */
			score.save();
		}
	}

	/**
	 * 创建保存本轮计时成绩的请求,传入athleteids
	 * 
	 * @param
	 *
	 */
	private void addScoreRequest() {


		Map<String,String> map = getDataMap();

		VolleyUtil.ResponseListener listener = new VolleyUtil.ResponseListener() {
			@Override
			public void onSuccess(String string) {
				loadingDialog.dismiss();
				JSONObject obj;
				try {
					obj = new JSONObject(string);
					int resCode = (Integer) obj.get("resCode");
					int planId = (Integer) obj.get("plan_id");
					if (resCode == 1) {
						CommonUtils.showToast(
								EachTimeScoreActivity.this, mToast,
								getString(R.string.synchronized_success));
						ContentValues values = new ContentValues();
						values.put("pid", planId);
						Plan.updateAll(Plan.class, values,
								String.valueOf(plan.getId()));
						HashMap<String,Object> map = getDeliveryMap(isReset);
						AppController.gotoShowScoreActivity(EachTimeScoreActivity.this, map);
						finish();
					} else {
						CommonUtils.showToast(
								EachTimeScoreActivity.this, mToast,
								getString(R.string.synchronized_failed));
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onError(String string) {
				loadingDialog.dismiss();
				CommonUtils.showToast(EachTimeScoreActivity.this,mToast,getString(R.string.server_or_network_error));
			}
		};

		VolleyUtil.httpJson(Constants.ADD_SCORE,Method.POST,map,listener,myApplication);


	}

	private HashMap<String,Object> getDeliveryMap(boolean isReset){
		HashMap<String,Object> map = new HashMap<String, Object>();
		map.put("isReset",isReset);
		return map;
	}

	/**
	 * 获取上传成绩的数据集
	 * @param
	 * @return
	 */
	private Map<String,String> getDataMap(){

		plan.setPdate(date);
		SmallPlan sp = CommonUtils.convertPlan(plan);

		List<SmallScore> smallScores = new ArrayList<SmallScore>();
		List<Score> scoresResult = mDbManager.getScoreByDate(date);
		/**
		 * 组装要上传的成绩
		 */
		for (Score s : scoresResult) {
			SmallScore smScore = CommonUtils.convertScore(s);
			smallScores.add(smScore);
		}

		User user = mDbManager.getUserByUid(userID);
		Map<String, Object> scoreMap = new HashMap<String, Object>();
		scoreMap.put("score", smallScores);
		scoreMap.put("plan", sp);
		scoreMap.put("uid", user.getUid());
		scoreMap.put("type", 1);
		final String jsonString = JsonTools.creatJsonString(scoreMap);

		// 设置请求参数
		Map<String, String> map = new HashMap<String, String>();
		map.put("scoresJson", jsonString);
		return map;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			createDialog();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		myApplication.getMap().put(Constants.COMPLETE_NUMBER, 0);
		myApplication.removeActivity(this);
	}


}
