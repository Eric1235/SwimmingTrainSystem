package com.scnu.swimmingtrainsystem.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.adapter.TimeLineListAdapter;
import com.scnu.swimmingtrainsystem.util.CommonUtils;
import com.scnu.swimmingtrainsystem.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 计时秒表界面
 * 
 * @author LittleByte
 * 修改：李新坤
 * 
 */

public class TimerActivity extends Activity implements OnClickListener {

	public static final String FINISHTIMER = "FINISHTIMER";

	private MyApplication app;

	private int athleteNumber = 0;
	// 点击表盘次数
	private int clickCount = 0;
	// 经过毫秒数
	private long mlCount = 0;

	/**
	 * 记录成绩的个数
	 */
	private int athletes = 1;

	// 转动角度
	float predegree = 0;
	float secondpredegree = 0;
	float hourpredegree = 0;

	// 是否可以重置
	boolean okclear = false;

	//计时是否停表,默认是应该停表的
	private boolean isReset;

	// 保存成绩的list
	private ArrayList<String> time = new ArrayList<String>();
	// 保存两次成绩差的list
	private ArrayList<String> timesub = new ArrayList<String>();
	private String strTime_count = "";
	private long time_cur;
	private long time_beg;

	// 秒表显示时间
	private TextView tvTime;
	// 成绩列表中提示
	private TextView tvTip;
	private TextView time_title;
	private ImageButton resetButton;
	private ImageButton btnBack;
	private Button btnMatchPeople;
	// 成绩列表
	private ListView scoreList;
	private TimeLineListAdapter listItemAdapter;

	private ImageView min_progress_hand, second_progress_hand,
			hour_progress_hand;
	// 分针、秒针、时针动画
	private Animation rotateAnimation, secondrotateAnimation,
			hourrotateAnimation;

	/**
	 * 广播接收器
	 */
	private BroadcastReceiver receiver;

	private Toast toast;
	private Handler handler;
	private Message msg;

	// 表盘
	private RelativeLayout clockView;
	// 毫秒计数定时器
	private Timer timer;
	// 毫秒计数定时任务
	private TimerTask task = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_timer);
		Intent i = getIntent();
		isReset = i.getBooleanExtra(Constants.WATCHISRESET,true);
		setupView();
		setupData();
		/**
		 * 当不停表的时候，就要注册广播，在计时结束的时候接收广播来结束activity
		 */
		if(!isReset){
			RegistBrocast();
		}


	}

	/**
	 * 接收广播，结束计时
	 * 在不间歇的时候去注册
	 */
	private void RegistBrocast(){
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				/**
				 * 接收到广播的时候，结束Activity
				 */
				finish();
			}
		};

		IntentFilter filter = new IntentFilter();
		filter.addAction(FINISHTIMER);
		/**
		 * 动态注册广播
		 */
		registerReceiver(receiver,filter);
	}

	@SuppressWarnings("unchecked")
	private void setupView() {
		// TODO Auto-generated method stub
		app = (MyApplication) getApplication();
		app.addActivity(this);

		// 如果app中的全局变量被系统强制回收，通过以下改行代码会触发异常，直接将应用界面重启至登陆页面

		time_title = (TextView) findViewById(R.id.time_title);
		tvTime = (TextView) findViewById(R.id.duocitvTime);
		tvTip = (TextView) findViewById(R.id.textwujici);

		resetButton = (ImageButton) findViewById(R.id.resetbutton);
		btnBack = (ImageButton) findViewById(R.id.timer_back);
		btnMatchPeople = (Button) findViewById(R.id.match_people);

		scoreList = (ListView) findViewById(R.id.duocijishilist);
		min_progress_hand = (ImageView) findViewById(R.id.duocimin_progress_hand);
		second_progress_hand = (ImageView) findViewById(R.id.duocisecond_progress_hand);
		hour_progress_hand = (ImageView) findViewById(R.id.duocihour_progress_hand);
		clockView = (RelativeLayout) findViewById(R.id.clcokview);

		clockView.setOnClickListener(this);

		resetButton.setOnClickListener(this);
		btnBack.setOnClickListener(this);
		btnMatchPeople.setOnClickListener(this);

		/**
		 * 获取运动员的个数
		 */
		athleteNumber = ((List<String>) app.getMap().get(
				Constants.DRAG_NAME_LIST)).size();

	}

	/**
	 * 点击表盘，触发开始计时和记录时间事件
	 */
	private void clickTimer(){
		// 计时数量自增
		clickCount++;
		/**
		 * 判断记录的成绩个数是否过多，比运动员多两个的时候就应该是不再生成。
		 */
		if (athletes <= athleteNumber + 2) {
			// 开始计时，对计时数量进行判断
			if (clickCount == 1) {
				okclear = false;
				/**
				 * 开启线程，进行计时
				 */
				timer = new Timer(true);
				task = new TimerTask() {
					@Override
					public void run() {
						msg = handler.obtainMessage();
						msg.what = 1;
						time_cur = System.currentTimeMillis();
						/**
						 * 获取经过的时间段
						 */
						mlCount = time_cur - time_beg;
						/**
						 * 格式化时间字符串
						 */
						strTime_count = CommonUtils.getStrTime(mlCount);
						msg.sendToTarget();
					}
				};
				time_beg = System.currentTimeMillis();
				/**
				 * 每隔10毫秒刷新,延迟为1毫秒
				 */
				timer.schedule(task, 1, 10);
			} else {
				tvTip.setVisibility(View.GONE);
				/**
				 * 开始在listview上设置时间
				 */
				setlistview();
				if (athletes == (athleteNumber + 1)) {
					/**
					 * 成绩记录完成，给出提示
					 */
					CommonUtils.showToast(TimerActivity.this, toast,
							getString(R.string.score_record_done));
				}
			}
		} else {
			/**
			 * 当计时数量超出上限的时候，就停止计时器计时
			 */
			stopTimer();
			CommonUtils.showToast(TimerActivity.this, toast,
					getString(R.string.dont_save_more_score));
		}

	}

	@SuppressLint("HandlerLeak")
	private void setupData() {
		scoreList.setAdapter(null);
		tvTime.setText(getString(R.string.time_reset));
		/**
		 * 在这里刷新界面
		 */
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				/**
				 * 处理msg
				 */
				switch (msg.what) {
				case 1:
					try {
						tvTime.setText(strTime_count);
						// 设置指针转动动画
						setAnimation();
						predegree = (float) (0.006 * mlCount);
						secondpredegree = (float) (0.36 * mlCount);
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
	 * 生成listview
	 */
	private void setlistview() {
		// TODO Auto-generated method stub
		okclear = true;
		/**
		 * 把成绩放入数据list
		 */
		time.add(athletes - 1, tvTime.getText().toString());
		if (athletes > 1) {
			// 两个成绩之差
			String substracion = CommonUtils.getScoreSubtraction(
					time.get(athletes - 1), time.get(athletes - 2));
			timesub.add(substracion);
		}

		ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();
		for (int i = 1; i <= athletes; i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("athlete_score", time.get(i - 1));
			if (i == 1) {
				// 第一名
				map.put("score_between", "");
				map.put("athlete_ranking", getString(R.string.No_1));
			} else {
				map.put("score_between", timesub.get(i - 2));
				map.put("athlete_ranking", String.valueOf(i));
			}
			listItem.add(map);
		}
		listItemAdapter = new TimeLineListAdapter(this,
				listItem);
		scoreList.setAdapter(listItemAdapter);
		scoreList.setSelection(athletes - 1);
		athletes++;

	}


	/**
	 * 弹出是否退出计时的对话框
	 */
	private void createExitDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(TimerActivity.this);
		builder.setTitle(getString(R.string.system_hint));
		builder.setMessage(getString(R.string.quit_timing));
		builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				app.getMap().put(Constants.CURRENT_SWIM_TIME, 0);
				dialog.dismiss();
				finish();
			}
		});
		builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();
			}
		});
		builder.show();
	}

	/**
	 * 设置动画
	 */
	private void setAnimation() {
		//分别旋转不同的角度
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
	 * 重置计时器
	 * 
	 * @param
	 */
	private void reset() {
		if (okclear) {

			resetData();

			predegree = 0;
			secondpredegree = 0;
			hourpredegree = 0;
			mlCount = 0;
			tvTip.setVisibility(View.VISIBLE);

			stopTimer();
			setupData();
			setAnimation();
		}
	}

	/**
	 * 清空数据
	 */
	private void resetData(){
		okclear = false;

		time.clear();
		timesub.clear();
		athletes = 1;
	}

	/**
	 * 暂停计时器
	 */
	public void stopTimer() {
		if (null != task && null != timer) {
			task.cancel();
			task = null;
			timer.cancel();
			timer.purge();
			timer = null;
			handler.removeMessages(msg.what);
		}
	}

	/**
	 * 将成绩与运动员匹配
	 * 
	 * @param
	 */
	private void matchAthlete() {
		if (scoreList.getAdapter() != null
				&& scoreList.getAdapter().getCount() != 0) {
			Intent intent = new Intent(this, MatchScoreActivity.class);
			/**
			 * 传成绩到成绩匹配界面
			 */
			intent.putStringArrayListExtra("SCORES", time);
			intent.putExtra(Constants.WATCHISRESET,isReset);
			startActivity(intent);
			/**
			 * 退出与否
			 */
			if(isReset == true){
				finish();
			}
		} else {
			CommonUtils.showToast(this, toast, getString(R.string.leave_at_least_one_score));
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.clcokview:
				clickTimer();
				break;
			case R.id.resetbutton:
				reset();
				break;
			case R.id.timer_back:
				createExitDialog();
				break;
			case R.id.match_people:
				matchAthlete();
				break;
			default:
				break;
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

	}

	/**
	 * 在这里初始化数据
	 */
	@Override
	protected void onResume() {
		super.onResume();

		setTitle();
		resetData();
	}

	/**
	 * 设置标题
	 */
	private void setTitle(){
		/**
		 * 这里对计时次数进行加1
		 */
		int swimTime = ((Integer) app.getMap().get(Constants.CURRENT_SWIM_TIME)) + 1;
		time_title.setText(String.format(getString(R.string.No_timer), swimTime));
		app.getMap().put(Constants.CURRENT_SWIM_TIME, swimTime);
	}

	@Override
	protected void onPause() {
		super.onPause();
		clickCount = 1;
		scoreList.setAdapter(null);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		// 进入计时界面却不进行成绩匹配而直接返回,要将当前第几次计时置0
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			createExitDialog();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		reset();
		if(!isReset){
			unregisterReceiver(receiver);
		}

		app.removeActivity(this);
	}



}
