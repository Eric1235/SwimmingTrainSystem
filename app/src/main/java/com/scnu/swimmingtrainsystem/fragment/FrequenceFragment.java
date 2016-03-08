package com.scnu.swimmingtrainsystem.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.activity.MyApplication;
import com.scnu.swimmingtrainsystem.db.DBManager;
import com.scnu.swimmingtrainsystem.http.JsonTools;
import com.scnu.swimmingtrainsystem.model.Athlete;
import com.scnu.swimmingtrainsystem.model.OtherScore;
import com.scnu.swimmingtrainsystem.model.SmallOtherScore;
import com.scnu.swimmingtrainsystem.util.CommonUtils;
import com.scnu.swimmingtrainsystem.util.Constants;
import com.scnu.swimmingtrainsystem.util.NetworkUtil;
import com.scnu.swimmingtrainsystem.util.SpUtil;
import com.scnu.swimmingtrainsystem.util.VolleyUtil;
import com.scnu.swimmingtrainsystem.view.LoadingDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 三次计频fragment
 * 
 * @author LittleByte
 * 修改:李新坤
 * 
 */
@SuppressLint("HandlerLeak")
public class FrequenceFragment extends Fragment implements OnClickListener {

	private List<String> autoAthleteNames;
	private List<Athlete> mAthletes;
	private int uid;
	private String athleteName;
	private int aid;
	private String score;
	private String pdate;

	private MyApplication app;

	// 点击表盘次数
	private int clickCount = 0;
	// 经过毫秒数
	private long mlCount = 0;

	// 转动角度
	float predegree = 0;
	float secondpredegree = 0;
	float hourpredegree = 0;

	private boolean isConnected;

	private String strTime_count = "";
	private long time_cur;
	private long time_beg;

	private ArrayAdapter<String> athleteTipsAdapter;

	private Toast mToast;
	private LoadingDialog loadingDialog;

	private Button btReset;
	private Button btnSubmit;
	private TextView tvResult;
	private AutoCompleteTextView tvAthleteName;
	private Activity activity;

	// 秒表显示时间
	private TextView tvTime;

	// 表盘
	private RelativeLayout clockView;

	private ImageView min_progress_hand, second_progress_hand,
			hour_progress_hand;
	// 分针、秒针、时针动画
	private Animation rotateAnimation, secondrotateAnimation,
			hourrotateAnimation;

	private Handler handler;
	private Message msg;

	private DBManager mDBManager;

	// 毫秒计数定时器
	private Timer timer;
	// 毫秒计数定时任务
	private TimerTask task = null;



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_frequence, null);
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		init();
		resetData();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDBManager = DBManager.getInstance();
		uid = SpUtil.getUID(getActivity());
		app = (MyApplication) getActivity().getApplication();
	}

	private void init() {
		activity = getActivity();
		btReset = (Button) activity.findViewById(R.id.bt_frequen_reset);
		btnSubmit = (Button) activity.findViewById(R.id.bt_submit);
		tvResult = (TextView) activity.findViewById(R.id.tv_frequen_result);
		tvTime = (TextView) activity.findViewById(R.id.duocitvTime);
		tvAthleteName = (AutoCompleteTextView) activity.findViewById(R.id.tv_athlete_name);

		btnSubmit.setOnClickListener(this);

		initAutoTextView();

		btReset.setOnClickListener(this);
		// ----------------计时器动画相关--------------
		min_progress_hand = (ImageView) activity
				.findViewById(R.id.duocimin_progress_hand);
		second_progress_hand = (ImageView) activity
				.findViewById(R.id.duocisecond_progress_hand);
		hour_progress_hand = (ImageView) activity
				.findViewById(R.id.duocihour_progress_hand);
		clockView = (RelativeLayout) activity.findViewById(R.id.frequence_area);

		clockView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				clickTimer();
			}
		});
	}

	private void initAutoTextView(){
		mAthletes = mDBManager.getAthletes(uid);
		autoAthleteNames = CommonUtils.getAthleteNamesByAthletes(mAthletes);
		athleteTipsAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_dropdown_item_1line,autoAthleteNames);
		tvAthleteName.setAdapter(athleteTipsAdapter);
		tvAthleteName.setDropDownHeight(300);
		tvAthleteName.setThreshold(1);
	}

	/**
	 * 点击表盘
	 */
	private void clickTimer(){
		clickCount++;
		// 开始计时
		if (clickCount == 1) {
			timer = new Timer(true);
			task = new TimerTask() {
				@Override
				public void run() {
					msg = handler.obtainMessage();
					msg.what = 1;
					time_cur = System.currentTimeMillis();
					mlCount = time_cur - time_beg;
					strTime_count = CommonUtils.getStrTime(mlCount);
					msg.sendToTarget();
				}
			};
			time_beg = System.currentTimeMillis();
			timer.schedule(task, 1, 10);
		} else {
			timerStop();
			float timePermsec = 3 * 60000 / (float) mlCount;
			DecimalFormat decimalFormat = new DecimalFormat(".00");// 构造方法的字符格式这里如果小数不足2位,会以0补足.
			score = decimalFormat.format(timePermsec);
			Date date = new Date();
			pdate = CommonUtils.formatDate(date);
			tvResult.setText(score + " 次/分钟");
		}
	}

	private void resetData() {
		tvTime.setText(getString(R.string.click_clock_and_record_score));
		tvTime.setTextSize(14);
		tvTime.setTextColor(getResources().getColor(R.color.gray));
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				tvTime.setTextSize(24);
				tvTime.setTextColor(getResources().getColor(R.color.black));
				switch (msg.what) {
				case 1:
					try {
						tvTime.setText(strTime_count);
						// 设置指针转动动画
						setAnimation();
						predegree = (float) (0.0058 * mlCount);
						secondpredegree = (float) (0.358 * mlCount);
						hourpredegree = (float) (mlCount / 10000);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				default:
					break;
				}

			}
		};

	}

	/**
	 * 设置动画
	 */
	private void setAnimation() {
		rotateAnimation = new RotateAnimation(predegree,
				(float) (0.006 * mlCount), Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		secondrotateAnimation = new RotateAnimation(secondpredegree,
				(float) (0.36 * mlCount), Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		hourrotateAnimation = new RotateAnimation(hourpredegree,
				(float) (mlCount / 10000), Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		rotateAnimation.setDuration(100);
		rotateAnimation.setFillAfter(true);
		hourrotateAnimation.setDuration(100);
		hourrotateAnimation.setFillAfter(true);
		secondrotateAnimation.setDuration(100);
		secondrotateAnimation.setFillAfter(true);
		min_progress_hand.startAnimation(rotateAnimation);
		second_progress_hand.startAnimation(secondrotateAnimation);
		hour_progress_hand.startAnimation(hourrotateAnimation);
	}



	/**
	 * 暂停计时器
	 */
	public void timerStop() {
		if (null != task && null != timer) {
			task.cancel();
			task = null;
			timer.cancel();
			timer.purge();
			timer = null;
			handler.removeMessages(msg.what);
		}
	}

	private void resetTimer() {
		tvResult.setText(getString(R.string.default_time_per_min));
		clickCount = 0;
		predegree = 0;
		secondpredegree = 0;
		hourpredegree = 0;
		mlCount = 0;
		timerStop();
		resetData();
		setAnimation();
	}

	private void submitScore(String score,String aid,String uid,String athleteName,String pdate){
		isConnected = NetworkUtil.isConnected(getActivity());
		if(isConnected){
			if(loadingDialog == null){
				loadingDialog = LoadingDialog.createDialog(getActivity());
				loadingDialog.setMessage(getString(R.string.onSubmitting));
				loadingDialog.setCanceledOnTouchOutside(false);
			}
			loadingDialog.show();
			addFrequenceScoreRequest(score, aid, uid, athleteName, pdate);
		}else{
			CommonUtils.showToast(getActivity(),mToast,getString(R.string.network_error));
		}
	}

	/**
	 * 保存三次计频成绩到数据库
	 * @param score
	 * @param uid
	 * @param aid
	 * @param athleteName
	 * @param type
	 * @param pdate
	 */
	private void saveFrequenceScore(String score,int uid,int aid,String athleteName,int type,String pdate){
		OtherScore s = new OtherScore();
		s.setAthlete_name(athleteName);
		s.setAthlete_id(aid);
		s.setType(type);
		s.setPdate(pdate);
		s.setScore(score);
		s.setUid(uid);
		s.save();
	}

	/**
	 * 上传运动员的三次计频成绩
	 * @param score
	 * @param athleteName
	 */
	private void addFrequenceScoreRequest(String score,String aid,String uid,String athleteName,String pdate){

		Map<String,String> map = getDataMap(score,uid,aid,athleteName,pdate);

		VolleyUtil.ResponseListener listener = new VolleyUtil.ResponseListener() {
			@Override
			public void onSuccess(String response) {
				loadingDialog.dismiss();
				JSONObject obj;
				try{
					obj = new JSONObject(response);
					boolean resCode = (Boolean) obj.get("success");

					if(resCode){
						CommonUtils.showToast(getActivity(), mToast, getString(R.string.submit_succeed));
						ShowTipDialog();
					}else {
						CommonUtils.showToast(getActivity(),mToast,getString(R.string.submit_failed));
					}
				}catch (JSONException e){
					e.printStackTrace();
				}

			}

			@Override
			public void onError(String response) {
				loadingDialog.dismiss();
				CommonUtils.showToast(getActivity(),mToast,getString(R.string.unkonwn_error));
			}
		};

		VolleyUtil.httpJson(Constants.ADD_OTHER_SCORE, Request.Method.POST, map, listener, app);
	}

	/**
	 * 进行二次封装
	 * @param athleteName
	 * @param uid
	 * @param aid
	 * @param score
	 * @param pdate
	 * @return
	 */
	private Map<String,String> getDataMap(String score,String uid,String aid,String athleteName,String pdate){
		Map<String,Object> scoreMap = new HashMap<String, Object>();
//		Map<String,Object> scoreData = new HashMap<String, Object>();
		SmallOtherScore tmpScore = new SmallOtherScore();
		tmpScore.setScore(score);
		tmpScore.setPdate(pdate);
		tmpScore.setAthlete_name(athleteName);
		tmpScore.setAthlete_id(aid);
		List<SmallOtherScore> tempScores = new ArrayList<SmallOtherScore>();
		tempScores.add(tmpScore);

		String scoreDataStr = JsonTools.creatJsonString(tempScores);
		scoreMap.put("scoredata",scoreDataStr);
		scoreMap.put("uid",uid);
		scoreMap.put("distance",null);
		scoreMap.put("type", String.valueOf(Constants.FrequenceSCORE));
		Map<String,String> map = new HashMap<String, String>();
		final String scoreStr = JsonTools.creatJsonString(scoreMap);
		map.put("data", scoreStr);
		return  map;
	}

	public void ShowTipDialog() {
		AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
		build.setTitle(getString(R.string.system_hint)).setMessage(getString(R.string.score_saved_goto_to_home));
		build.setPositiveButton(Constants.OK_STRING,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						getActivity().finish();
						getActivity().overridePendingTransition(R.anim.slide_bottom_in,
								R.anim.slide_top_out);
					}
				});
		build.setNegativeButton(Constants.CANCLE_STRING,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}


	private boolean beforeSubmit(String score,String athleteName,String pdate){
		boolean b = true;
		if(TextUtils.isEmpty(score)){
			CommonUtils.showToast(getActivity(),mToast,getString(R.string.score_is_zero));
			b = false;
		}else if(TextUtils.isEmpty(athleteName)){
			b = false;
			CommonUtils.showToast(getActivity(),mToast,getString(R.string.input_athlete));
		}else if(TextUtils.isEmpty(pdate)){
			b = false;
			CommonUtils.showToast(getActivity(),mToast,getString(R.string.date_is_null));
		}
		return b;
	}

	private int getAidByAthleteName(String athleteName,List<Athlete> list){
		int aid = 0;
		for(Athlete a : list){
			if(a.getName().endsWith(athleteName)){
				aid = a.getAid();
				break;
			}
		}
		return aid;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.bt_frequen_reset:
				resetTimer();
				break;
			case R.id.bt_submit:
				athleteName = tvAthleteName.getText().toString().trim();
				aid = getAidByAthleteName(athleteName, mAthletes);
				saveFrequenceScore(score,uid,aid,athleteName,0,pdate);
				if(beforeSubmit(score,athleteName,pdate)){
					submitScore(score, String.valueOf(aid), String.valueOf(uid),athleteName,pdate);
				}

				break;
		default:
			break;
		}
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		resetTimer();
	}


}
