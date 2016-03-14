package com.scnu.swimmingtrainsystem.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.scnu.draglistview_library.dslv.DragSortListView;
import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.adapter.ChooseAthleteAdapter;
import com.scnu.swimmingtrainsystem.adapter.DragAdapter;
import com.scnu.swimmingtrainsystem.adapter.ScoreListAdapter;
import com.scnu.swimmingtrainsystem.db.DBManager;
import com.scnu.swimmingtrainsystem.effect.Effectstype;
import com.scnu.swimmingtrainsystem.effect.NiftyDialogBuilder;
import com.scnu.swimmingtrainsystem.http.JsonTools;
import com.scnu.swimmingtrainsystem.entity.AdapterHolder;
import com.scnu.swimmingtrainsystem.model2db.Athlete;
import com.scnu.swimmingtrainsystem.model2db.OtherScore;
import com.scnu.swimmingtrainsystem.entity.SmallOtherScore;
import com.scnu.swimmingtrainsystem.model2db.User;
import com.scnu.swimmingtrainsystem.util.CommonUtils;
import com.scnu.swimmingtrainsystem.util.Constants;
import com.scnu.swimmingtrainsystem.util.NetworkUtil;
import com.scnu.swimmingtrainsystem.util.SpUtil;
import com.scnu.swimmingtrainsystem.util.VolleyUtil;
import com.scnu.swimmingtrainsystem.view.LoadingDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("SimpleDateFormat")
public class MatchSprintScoreActivity extends Activity {

	private MyApplication app;
	private DBManager mDbManager;
	private boolean isConnected;
	private int userId;
	private boolean isSave = false;
	private ArrayList<String> scores = new ArrayList<String>();
	private List<Athlete> athletes = new ArrayList<Athlete>();
	private List<Athlete> dragDatas = new ArrayList<Athlete>();
	private SparseBooleanArray map = new SparseBooleanArray();
	private ArrayList<String> originScores = new ArrayList<String>();
	private List<ListView> viewList;

	private Toast mToast;
	private View mLayout, mLayout2;
	private LoadingDialog loadingDialog;
	private DragSortListView scoreListView;
	private DragSortListView nameListView;
	private ImageButton chooseButton;
	private Spinner distanceSpinner;
	/**
	 * 展示全部运动员的ListView
	 */
	private ListView athleteListView;


	private ScoreListAdapter adapter;

	/**
	 * 成绩拖动适配器
	 */
	private DragAdapter dragAdapter;
	/**
	 * 运动员拖动适配器
	 */
	private ChooseAthleteAdapter allAthleteAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_match_dash_score);
		init();
	}

	private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener() {
		@Override
		public void remove(int which) {
			if (dragDatas.size() > 1) {
				dragAdapter.remove(dragAdapter.getItem(which));
			} else {
				CommonUtils.showToast(MatchSprintScoreActivity.this, mToast,
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

	private DragSortListView.RemoveListener onRemove2 = new DragSortListView.RemoveListener() {
		@Override
		public void remove(int which) {
			if (scores.size() > 1) {
				scores.remove(which);
			} else {
				CommonUtils.showToast(MatchSprintScoreActivity.this, mToast,
						getString(R.string.leave_at_least_one_score));
			}
			adapter.notifyDataSetChanged();
		}
	};

	private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
		@Override
		public void drop(int from, int to) {
			Athlete item = dragAdapter.getItem(from);

			dragAdapter.notifyDataSetChanged();
			dragAdapter.remove(item);
			dragAdapter.insert(item, to);
		}
	};

	private void init() {
		// TODO Auto-generated method stub
		app = (MyApplication) getApplication();
		app.addActivity(this);
		mDbManager = DBManager.getInstance();
		chooseButton = (ImageButton) findViewById(R.id.add_match_athlete);
		distanceSpinner = (Spinner) findViewById(R.id.spinner_match_dash);
		String[] dashLength = getResources().getStringArray(R.array.dash_length);
		List<String> dashDistanceList = new ArrayList<String>();
		Collections.addAll(dashDistanceList, dashLength);
		ArrayAdapter<String> spinerAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, dashDistanceList);
		distanceSpinner.setAdapter(spinerAdapter);
		distanceSpinner.setSelection(2);

		scores = getIntent().getStringArrayListExtra("SCORES");
		userId = SpUtil.getUID(MatchSprintScoreActivity.this);
		originScores.addAll(scores);

		upDateAthleteList();

		for (int i = 0; i < athletes.size(); i++) {
			map.put(i, false);
		}

		mLayout = findViewById(R.id.match_dash_headbar);
		mLayout2 = findViewById(R.id.ll_match_dash2);
		scoreListView = (DragSortListView) findViewById(R.id.matchscore_list);
		nameListView = (DragSortListView) findViewById(R.id.matchName_list);
		nameListView.setDropListener(onDrop);
		nameListView.setRemoveListener(onRemove);
		nameListView.setDragScrollProfile(ssProfile);
		scoreListView.setRemoveListener(onRemove2);
		viewList = new ArrayList<ListView>();
		viewList.add(scoreListView);
		viewList.add(nameListView);
		MyScrollListener mListener = new MyScrollListener();
		scoreListView.setOnScrollListener(mListener);
		nameListView.setOnScrollListener(mListener);
		adapter = new ScoreListAdapter(scoreListView, this, scores);


		dragAdapter = new DragAdapter(this, R.layout.drag_list_item, dragDatas);
		scoreListView.setAdapter(adapter);
		nameListView.setAdapter(dragAdapter);
		scoreListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				showPopWindow(position);
				return true;
			}
		});

		chooseAthlete(chooseButton);

	}

	/**
	 * 更新运动员列表
	 */
	private void upDateAthleteList(){
		athletes = mDbManager.getAthletes(userId);
	}

	public void chooseAthlete(View v) {
		upDateAthleteList();
		final NiftyDialogBuilder selectDialog = NiftyDialogBuilder
				.getInstance(MatchSprintScoreActivity.this);
		Effectstype effect = Effectstype.Fall;
		selectDialog.setCustomView(R.layout.dialog_choose_athlete, MatchSprintScoreActivity.this);
		Window window = selectDialog.getWindow();
		athleteListView = (ListView) window.findViewById(R.id.choose_list);

		allAthleteAdapter = new ChooseAthleteAdapter(MatchSprintScoreActivity.this, athletes, map);
		athleteListView.setAdapter(allAthleteAdapter);
		athleteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
									long arg3) {
				AdapterHolder holder = (AdapterHolder) arg1.getTag();
				// 改变CheckBox的状态
				holder.cb.toggle();
				Athlete a = (Athlete) allAthleteAdapter.getItem(position);
				if (holder.cb.isChecked()) {
					if(!CommonUtils.ListContainsAthlete(dragDatas,a)){
						dragDatas.add(a);
					}

				} else {
					if(CommonUtils.ListContainsAthlete(dragDatas,a)){
						CommonUtils.removeAthleteFromList(dragDatas,a);
					}

				}
				// 将CheckBox的选中状况记录下来
				map.put(position, holder.cb.isChecked());
			}
		});
		selectDialog.withTitle(getString(R.string.choose_athlete)).withMessage(null)
				.withIcon(getResources().getDrawable(R.drawable.ic_launcher))
				.isCancelableOnTouchOutside(false).withDuration(500)
				.withEffect(effect).withButton1Text(getString(R.string.back))
				.withButton2Text(Constants.OK_STRING)
				.setButton1Click(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						selectDialog.dismiss();
					}
				}).setButton2Click(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dragAdapter = new DragAdapter(MatchSprintScoreActivity.this, R.layout.drag_list_item, dragDatas);
				nameListView.setAdapter(dragAdapter);
				selectDialog.dismiss();
			}

		}).show();
	}

	public void saveScores(View v) {
		if (!isSave) {
			isSave = true;
			int scoreNumber = scores.size();
			int athleteNumber = dragDatas.size();
			if (scoreNumber != athleteNumber) {
				CommonUtils.showToast(this, mToast, getString(R.string.score_num_not_equalwith_athlete_num));
			} else {
				String distance = distanceSpinner.getSelectedItem().toString()
						.replace("米", "");
				List<Integer> athIds = new ArrayList<Integer>();
				User user = mDbManager.getUserByUid(userId);
				String date = CommonUtils.formatDate(new Date());
				for (int i = 0; i < scoreNumber; i++) {
					Athlete athlete = dragDatas.get(i);
					athIds.add(athlete.getAid());
//					Score s = new Score();
//					s.setScore(scores.get(i));
//					s.setDate(date);
//					s.setDistance(Integer.parseInt(distance));
//					s.setTimes(1);
//					s.setType(Constants.SPRINTSCORE);
//					s.setAthlete(athlete);
//					s.setUser(user);
//					s.save();

					OtherScore oScore = new OtherScore();
					oScore.setAthlete_id(athlete.getAid());
					oScore.setScore(scores.get(i));
					oScore.setAthlete_name(athlete.getName());
					oScore.setType(1);
					oScore.setDistance(Integer.parseInt(distance));
					oScore.setUid(userId);
					oScore.setPdate(date);
					oScore.save();


				}
				isConnected = NetworkUtil.isConnected(this);
				if (isConnected) {
					if (loadingDialog == null) {
						loadingDialog = LoadingDialog.createDialog(this);
						loadingDialog.setMessage(getString(R.string.onSubmitting));
						loadingDialog.setCanceledOnTouchOutside(false);
					}
					loadingDialog.show();
					addScoreRequest(date, athIds,distance);
				} else {
					CommonUtils.showToast(this, mToast, getString(R.string.network_error));

				}
			}

		} else {
			CommonUtils.showToast(this, mToast, getString(R.string.dont_need_to_save_again));
			ShowTipDialog();
		}

	}

	private void addScoreRequest(String date, List<Integer> athIds,String distance) {

		Map<String,String> map = getDataMap(date,athIds,distance);

		VolleyUtil.ResponseListener listener = new VolleyUtil.ResponseListener() {
			@Override
			public void onSuccess(String string) {
				loadingDialog.dismiss();
						JSONObject obj;
						try {
							obj = new JSONObject(string);
							boolean resCode = (Boolean) obj.get("success");
							if (resCode) {
								CommonUtils.showToast(
										MatchSprintScoreActivity.this, mToast,
										getString(R.string.submit_succeed));
								ShowTipDialog();
							} else {
								CommonUtils.showToast(
										MatchSprintScoreActivity.this, mToast,
										getString(R.string.submit_failed));
							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			}

			@Override
			public void onError(String string) {
				loadingDialog.dismiss();
				CommonUtils.showToast(MatchSprintScoreActivity.this,mToast,getString(R.string.unkonwn_error));
			}
		};
		VolleyUtil.httpJson(Constants.ADD_OTHER_SCORE, Request.Method.POST,map,listener,app);

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
				finish();
				overridePendingTransition(R.anim.slide_bottom_in,
						R.anim.slide_top_out);
			}
		}).show();

	}

	/**
	 * 获取上传的数据集
	 * @param date
	 * @param athIds
	 * @return
	 */
	private Map<String,String> getDataMap(String date, List<Integer> athIds,String distance){
//		List<Score> scoresResult = new ArrayList<Score>();
		List<OtherScore> scoresResult = new ArrayList<OtherScore>();
		List<Integer> athList = new ArrayList<Integer>();
		scoresResult.addAll(mDbManager.getOtherScoreByDate(date));
		athList.addAll(athIds);
		List<SmallOtherScore> scores = new ArrayList<SmallOtherScore>();
		scores = getScore(scoresResult);
		User user = mDbManager.getUserByUid(userId);
		Map<String, Object> scoreMap = new HashMap<String, Object>();
		scoreMap.put("scoredata", scores);
//		scoreMap.put("plan", null);
		scoreMap.put("uid", user.getUid());
//		scoreMap.put("athlete_id", athList);
		scoreMap.put("type", 1);
		scoreMap.put("distance",distance);
		final String jsonString = JsonTools.creatJsonString(scoreMap);
		// 设置请求参数
		Map<String, String> map = new HashMap<String, String>();
		map.put("data", jsonString);
		return map;
	}

	private List<SmallOtherScore> getScore(List<OtherScore> originScores){
		List<SmallOtherScore> mScores = new ArrayList<SmallOtherScore>();
		SmallOtherScore s1;
		for(OtherScore s : originScores){
			s1 = new SmallOtherScore();
			s1.setAthlete_id(String.valueOf(s.getAthlete_id()));
			s1.setAthlete_name(s.getAthlete_name());
			s1.setPdate(s.getPdate());
			s1.setScore(s.getScore());
			mScores.add(s1);
		}
		return  mScores;
	}


	public void matchBack(View v) {
		if (!isSave) {
			createDialog();
		} else {
			finish();
			overridePendingTransition(R.anim.slide_bottom_in,
					R.anim.slide_top_out);
		}

	}

	public void reLoad(View v) {
		scores.clear();
		scores.addAll(originScores);
		adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		// 进入计时界面却不进行成绩匹配而直接返回,要将当前第几次计时置0
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!isSave) {
				createDialog();
			} else {
				finish();
				overridePendingTransition(R.anim.slide_bottom_in,
						R.anim.slide_top_out);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
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

	public void ShowTipDialog() {
		AlertDialog.Builder build = new AlertDialog.Builder(this);
		build.setTitle(getString(R.string.system_hint)).setMessage(getString(R.string.score_saved_goto_to_home));
		build.setPositiveButton(Constants.OK_STRING,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						MatchSprintScoreActivity.this.finish();
						overridePendingTransition(R.anim.slide_bottom_in,
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
		int yoff = mLayout.getHeight()
				* (position - scoreListView.getFirstVisiblePosition() + 1);
		pop.showAsDropDown(mLayout2, scoreListView.getRight() / 2, yoff);
		copyView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				scores.add(position, scores.get(position));
				adapter.notifyDataSetChanged();
				dragDatas.add(position, null);
//				dragAdapter.notifyDataSetChanged();
				pop.dismiss();
			}
		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		app.removeActivity(this);
	}
}
