package com.scnu.swimmingtrainsystem.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.adapter.AthleteListAdapter;
import com.scnu.swimmingtrainsystem.db.DBManager;
import com.scnu.swimmingtrainsystem.effect.Effectstype;
import com.scnu.swimmingtrainsystem.effect.NiftyDialogBuilder;
import com.scnu.swimmingtrainsystem.http.JsonTools;
import com.scnu.swimmingtrainsystem.model2db.Athlete;
import com.scnu.swimmingtrainsystem.model2db.User;
import com.scnu.swimmingtrainsystem.utils.CommonUtils;
import com.scnu.swimmingtrainsystem.utils.Constants;
import com.scnu.swimmingtrainsystem.utils.NetworkUtil;
import com.scnu.swimmingtrainsystem.utils.ScreenUtils;
import com.scnu.swimmingtrainsystem.utils.SpUtil;
import com.scnu.swimmingtrainsystem.utils.VolleyUtil;
import com.scnu.swimmingtrainsystem.view.LoadingDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运动员管理Activity
 * 
 * @author LittleByte
 * 
 */
public class AthleteActivity extends Activity implements View.OnClickListener ,RadioGroup.OnCheckedChangeListener{
	private static final String UNKNOW_ERROR = "服务器错误";
	private static final String ADD_ATHLETE_TITLE_STRING = "添加运动员";
	private static final String NAME_CANNOT_BE_EMPTY_STRING = "运动员名字不能为空";
	private static final String NAME_CANNOT_BE_REPEATE_STRING = "存在运动员名字重复，请更改";

	// 数据库管理类
	private DBManager mDbManager;
	// 当前用户对象
	private User mUser;
	// 当前用户对象id
	private int mUserId;
	//运动员性别
	private String ath_gender = "男";
	// 是否能连接服务器
	private boolean isConnect;

	private boolean editable;

	// 该对象保存全局变量
	private MyApplication mApplication;

	// 运动员信息数据集
	private List<Athlete> mAthletes;

	// 展示所有运动员信息的列表控件
	private SwipeMenuListView mListView;
	//左滑listview创建器
	private SwipeMenuCreator creator;

	private Toast mToast;
	// 运动员信息列表的数据适配器
	private AthleteListAdapter mAthleteListAdapter;

	// 运动员名字编辑框
	private EditText mAthleteName;
	// 运动员年龄编辑框
	private EditText mAthleteAge;
	// 运动员联系电话编辑框
	private EditText mAthleteContact;
	// 运动员备注编辑框
	private EditText mOthers;
	//运动员编号编辑框
	private EditText mAthleteNumber;
	// 运动员性别按钮布局
	private RadioGroup rgGender;
	private RadioButton rbMale,rbFemale;
	//悬浮添加运动员
	private ImageButton btnAddAthlete;
	private ImageButton btnSyncAthlete;

	private Toast toast;

	private LoadingDialog loadingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		this.setTheme(R.style.AppThemeLight);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_athlete);

		init();

	}

	/**
	 * 界面初始化
	 */
	private void init() {
		mApplication = (MyApplication) getApplication();
		mApplication.addActivity(this);
		mDbManager = DBManager.getInstance();
		mUserId = SpUtil.getUID(AthleteActivity.this);
		mUser = mDbManager.getUserByUid(mUserId);
		mListView = (SwipeMenuListView) findViewById(R.id.lv);
		btnAddAthlete = (ImageButton) findViewById(R.id.btn_add_athlete);
		btnSyncAthlete = (ImageButton) findViewById(R.id.btn_sync_athlete);
		btnSyncAthlete.setOnClickListener(this);
		btnAddAthlete.setOnClickListener(this);

		/**
		 *设置左滑监听事件
		 */
		creator = new SwipeMenuCreator() {
			@Override
			public void create(SwipeMenu swipeMenu) {
				SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
				deleteItem.setBackground(R.color.red);
				deleteItem.setIcon(R.drawable.ic_delete);
				deleteItem.setWidth(ScreenUtils.dip2px(AthleteActivity.this, 80));

				SwipeMenuItem modifyItem = new SwipeMenuItem(getApplicationContext());
				modifyItem.setWidth(ScreenUtils.dip2px(AthleteActivity.this,80));
				modifyItem.setBackground(R.color.light_gray);
				modifyItem.setTitle(R.string.modify);
				modifyItem.setTitleSize(18);
				modifyItem.setTitleColor(R.color.white);
				swipeMenu.addMenuItem(modifyItem);
				swipeMenu.addMenuItem(deleteItem);
			}
		};
		
		mAthletes = mDbManager.getAthletes(mUserId);
		mAthleteListAdapter = new AthleteListAdapter(this, R.layout.athlete_list_item,mAthletes);
		mListView.setAdapter(mAthleteListAdapter);
		mListView.setMenuCreator(creator);
		mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(int position, SwipeMenu swipeMenu, int index) {
				switch (index) {
					case 0:
						//modify
						Athlete a = mAthleteListAdapter.getItem(position);
						createDialog(position);
						break;
					case 1:
						//delete
						Athlete a1 = mAthleteListAdapter.getItem(position);
						deleteAthlete(a1);
						break;
				}
				return false;
			}
		});
	

		SharedPreferences sp = getSharedPreferences(Constants.LOGININFO,
				Context.MODE_PRIVATE);
		boolean isFirst = sp.getBoolean(Constants.FISRTOPENATHLETE, true);
		boolean userFirstLogin = sp.getBoolean(
				Constants.IS_THIS_USER_FIRST_LOGIN, false);

		// 如果新用户第一次打开应用并且可以连接服务器，就会尝试从服务器获取运动员信息
		if (isFirst && userFirstLogin) {
			SpUtil.initAthletes(this, false);
			SpUtil.saveIsThisUserFirstLogin(this, false);
			if (loadingDialog == null) {
				loadingDialog = LoadingDialog.createDialog(this);
				loadingDialog.setMessage(getString(R.string.synchronizing));
				loadingDialog.setCanceledOnTouchOutside(false);
			}
			loadingDialog.show();
			getAthleteRequest();
		}
	}

	/**
	 * 弹出修改对话框
	 */
	private void createDialog(final int position) {
		final NiftyDialogBuilder viewDialog = NiftyDialogBuilder
				.getInstance(this);
		Effectstype effect = Effectstype.RotateLeft;
		viewDialog
				.withTitle(getString(R.string.check_athlete_msg))
				.withMessage(null)
				.withIcon(
						getResources().getDrawable(
								R.drawable.ic_launcher))
				.isCancelableOnTouchOutside(false).withDuration(500)
				.withEffect(effect).withButton1Text(getString(R.string.enable_modify))
				.withButton2Text(Constants.OK_STRING)
				.setCustomView(R.layout.add_athlete_dialog, AthleteActivity.this)
				.setButton1Click(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						enableModification();
					}

				}).setButton2Click(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isConnect = NetworkUtil.isConnected(AthleteActivity.this);
				if(isConnect){
					updateModification(position, viewDialog);
				}else{
					CommonUtils.showToast(AthleteActivity.this, mToast, getString(R.string.network_error));
				}

			}

		}).show();
		Window window = viewDialog.getWindow();
		mAthleteName = (EditText) window.findViewById(R.id.add_et_user);
		mAthleteAge = (EditText) window.findViewById(R.id.add_et_age);
		mAthleteNumber = (EditText)  window.findViewById(R.id.add_et_number);
		mAthleteContact = (EditText) window.findViewById(R.id.add_et_contact);
		mOthers = (EditText) window.findViewById(R.id.add_et_extra);
		rgGender = (RadioGroup) window.findViewById(R.id.rg_gender);
		rbMale = (RadioButton) window.findViewById(R.id.rb_male);
		rbFemale = (RadioButton) window.findViewById(R.id.rb_female);

		mAthleteName.setText(mAthletes.get(position).getName());
		mAthleteAge.setText(mAthletes.get(position).getAge() + "");
		mAthleteNumber.setText(mAthletes.get(position).getNumber());

		String gender = mAthletes.get(position).getGender();
//		if (gender.equals(getString(R.string.male))) {
//			mGenderSwitch.setChecked(true);
//		} else {
//			mGenderSwitch.setChecked(false);
//		}
		setRadioButtonChecked(gender);
		rgGender.setOnCheckedChangeListener(this);
		mAthleteContact.setText(mAthletes.get(position).getPhone());
		mOthers.setText(mAthletes.get(position).getExtras());
		// 禁用编辑框
		mAthleteName.setEnabled(false);
		mAthleteAge.setEnabled(false);
		mAthleteNumber.setEnabled(false);
		mAthleteContact.setEnabled(false);
		mOthers.setEnabled(false);
		rgGender.setFocusable(false);

	}

	private void setRadioButtonChecked(String gender){
		if(gender.equals(getString(R.string.male))){
			rbMale.setChecked(true);
			rbFemale.setChecked(false);
		}
		if(gender.equals(getString(R.string.female))){
			rbMale.setChecked(true);
			rbFemale.setChecked(false);
		}
	}

	/**
	 * 使得运动员信息可以修改
	 */
	private void enableModification() {
		editable = true;
		mAthleteName.setBackgroundResource(R.drawable.bg_edittext_selector);
		mAthleteAge.setBackgroundResource(R.drawable.bg_edittext_selector);
		mAthleteContact.setBackgroundResource(R.drawable.bg_edittext_selector);
		mOthers.setBackgroundResource(R.drawable.bg_edittext_selector);
		mAthleteNumber.setBackgroundResource(R.drawable.bg_edittext_selector);
		mAthleteNumber.setEnabled(true);
		mAthleteName.setEnabled(true);
		mAthleteAge.setEnabled(true);
		mAthleteContact.setEnabled(true);
		mOthers.setEnabled(true);
		rgGender.setFocusable(true);
	}

	/**
	 * 提交修改，更新数据库数据，如果处于联网状态则将更新请求发送至服务器
	 *
	 * @param position
	 * @param viewDialog
	 */
	private void updateModification(final int position,
									final NiftyDialogBuilder viewDialog) {
		if (editable) {
			editable = false;
			String ath_name = mAthleteName.getText().toString().trim();
			String ath_age = mAthleteAge.getText().toString().trim();
			String ath_phone = mAthleteContact.getText().toString().trim();
			String ath_extras = mOthers.getText().toString().trim();
//			String ath_gender = "男";
//			if (!mGenderSwitch.isChecked()) {
//				ath_gender = "女";
//			}
			setRadioButtonChecked(ath_gender);
			// 如果处在联网状态，则发送至服务器
			boolean isConnect = NetworkUtil.isConnected(this);
			if (isConnect) {
				if (loadingDialog == null) {
					loadingDialog = LoadingDialog.createDialog(this);
					loadingDialog.setMessage(getString(R.string.synchronizing));
					loadingDialog.setCanceledOnTouchOutside(false);
				}
				loadingDialog.show();
				// 同步服务器
				modifyAthRequest(mAthletes, position, ath_name, ath_age,
						ath_gender, ath_phone, ath_extras);
			}else {
				mAthleteListAdapter.setDatas(mDbManager.getAthletes(mUserId));
				mAthleteListAdapter.notifyDataSetChanged();
				CommonUtils.showToast(AthleteActivity.this, toast, getString(R.string.network_error));
			}
		}
		viewDialog.dismiss();
	}



	/**
	 * 删除运动员
	 * @param a
	 */
	private void deleteAthlete(final Athlete a){
		AlertDialog.Builder build = new AlertDialog.Builder(this);
		build.setTitle(getString(R.string.system_hint)).setMessage(
				"确定要删除[ " + a.getName() + " ]的信息吗？");
		build.setPositiveButton(Constants.OK_STRING,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						boolean isConnect = NetworkUtil.isConnected(AthleteActivity.this);
						if (isConnect) {
							if (loadingDialog == null) {
								loadingDialog = LoadingDialog.createDialog(AthleteActivity.this);
								loadingDialog.setMessage(getString(R.string.synchronizing));
								loadingDialog.setCanceledOnTouchOutside(false);
							}
							loadingDialog.show();
							// 同步服务器
							deleteAthRequest(a);
						} else {
							CommonUtils.showToast(AthleteActivity.this, toast, getString(R.string.network_error));
						}

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

	/**
	 * 删除运动员网络请求
	 * @param a
	 */
	private void deleteAthRequest(final Athlete a){

		Map<String,String> map = getDeleteAthleteDataMap(a.getAid());

		VolleyUtil.ResponseListener listener = new VolleyUtil.ResponseListener() {
			@Override
			public void onSuccess(String string) {
				loadingDialog.dismiss();
				JSONObject obj;
				try {
					obj = new JSONObject(string);
					int resCode = (Integer) obj.get("resCode");
					if (resCode == 1) {
						mDbManager.deleteAthlete(a);
						mAthleteListAdapter.setDatas(mDbManager.getAthletes(mUserId));
						mAthleteListAdapter.notifyDataSetChanged();
						CommonUtils.showToast(AthleteActivity.this, toast, getString(R.string.delete_succeed));
					} else {
						CommonUtils.showToast(AthleteActivity.this, toast, getString(R.string.delete_failed));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			@Override
			public void onError(String string) {
				loadingDialog.dismiss();
				CommonUtils.showToast(AthleteActivity.this, toast, getString(R.string.server_or_network_error));
			}
		};
		VolleyUtil.httpJson(Constants.DELETE_ATHLETE_URL, Method.POST, map, listener, mApplication);
	}

	/**
	 * 获取删除运动员的数据集
	 * @param aid
	 * @return
	 */
	private Map<String,String> getDeleteAthleteDataMap(int aid){
		Map<String, String> map = new HashMap<String, String>();
		map.put("athlete_id", String.valueOf(aid));
		return map;
	}

	/**
	 * 修改运动员信息请求
	 *
	 * @param
	 */
	public void modifyAthRequest(final List<Athlete> athletes, final int position,
								 final String ath_name, final String ath_age, final String ath_gender,
								 final String ath_phone, final String ath_extras) {

		User user = mDbManager.getUserByUid(mUserId);
		Athlete obj = athletes.get(position);
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("uid", user.getUid());
		jsonMap.put("athlete_id", obj.getAid());
		jsonMap.put("athlete_name", ath_name);
		jsonMap.put("athlete_age", ath_age);
		jsonMap.put("athlete_gender", ath_gender);
		jsonMap.put("athlete_phone", ath_phone);
		jsonMap.put("athlete_extra", ath_extras);
		final String athleteJson = JsonTools.creatJsonString(jsonMap);

		Map<String,String> map = getModifyAthleteDataMap(athleteJson);

		VolleyUtil.ResponseListener listener = new VolleyUtil.ResponseListener() {
			@Override
			public void onSuccess(String string) {
				loadingDialog.dismiss();
				JSONObject obj;
				try {
					obj = new JSONObject(string);
					int resCode = (Integer) obj.get("resCode");
					if (resCode == 1) {
						mDbManager.updateAthlete(athletes, position, ath_name, ath_age,
								ath_gender, ath_phone, ath_extras);
						mAthleteListAdapter.setDatas(mDbManager.getAthletes(mUserId));
						mAthleteListAdapter.notifyDataSetChanged();
						CommonUtils.showToast(AthleteActivity.this, toast, getString(R.string.modify_succeed));
					} else {
						CommonUtils.showToast(AthleteActivity.this, toast, getString(R.string.modify_failed));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			@Override
			public void onError(String string) {
				loadingDialog.dismiss();
				CommonUtils.showToast(AthleteActivity.this,toast,getString(R.string.server_or_network_error));
			}
		};

		VolleyUtil.httpJson(Constants.MODIFY_ATHLETE_URL,Method.POST,map,listener,mApplication);
	}

	/**
	 * 获取修改运动员数据集
	 * @param athleteJson
	 * @return
	 */
	private Map<String,String> getModifyAthleteDataMap(String athleteJson) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("modifyAthleteJson", athleteJson);
		return map;
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.btn_add_athlete:
				addAthlete();
				break;
			case R.id.btn_sync_athlete:
				isConnect = NetworkUtil.isConnected(AthleteActivity.this);
				if(isConnect){
					if (loadingDialog == null) {
						loadingDialog = LoadingDialog.createDialog(this);
						loadingDialog.setMessage(getString(R.string.synchronizing));
						loadingDialog.setCanceledOnTouchOutside(false);
					}
					loadingDialog.show();
					getAthleteRequest();
				}else{
					CommonUtils.showToast(AthleteActivity.this,mToast,getString(R.string.network_error));
				}

				break;
		}
	}

	/**
	 * 弹出对话框并添加一个运动员信息
	 * 看清楚了哦，是弹出对话框
	 * @param
	 */
	public void addAthlete() {
		final NiftyDialogBuilder addDialog = NiftyDialogBuilder
				.getInstance(this);
		Effectstype effect = Effectstype.RotateLeft;
		Window window = addDialog.getWindow();
		addDialog
				.withTitle(ADD_ATHLETE_TITLE_STRING)
				.withMessage(null)
				.withIcon(getResources().getDrawable(R.drawable.ic_launcher))
				.isCancelableOnTouchOutside(false)
				.withDuration(500)
				.withEffect(effect)
				.withButton1Text(Constants.CANCLE_STRING)
				.withButton2Text(Constants.OK_STRING)
				.setButton1Click(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						addDialog.dismiss();
					}
				})
				.setButton2Click(new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						String name = mAthleteName.getText().toString().trim();
						String ageString = mAthleteAge.getText().toString()
								.trim();
						String phone = mAthleteContact.getText().toString()
								.trim();
						String other = mOthers.getText().toString().trim();

						String number = mAthleteNumber.getText().toString().trim();

						setRadioButtonChecked(ath_gender);
						boolean isExit = mDbManager.isAthleteNameExsit(mUserId,
								name);
						if (TextUtils.isEmpty(name)) {
							CommonUtils.showToast(AthleteActivity.this, mToast,
									NAME_CANNOT_BE_EMPTY_STRING);
						} else if (isExit) {
							CommonUtils.showToast(AthleteActivity.this, mToast,
									NAME_CANNOT_BE_REPEATE_STRING);
						} else {
							int age = 0;
							if (!TextUtils.isEmpty(ageString)) {
								age = Integer.parseInt(ageString);
							}
							addAthlete(name, age, ath_gender, phone, other, number);
							addDialog.dismiss();
						}
					}
				}).setCustomView(R.layout.add_athlete_dialog, this)
				.show();
		mAthleteName = (EditText) window.findViewById(R.id.add_et_user);
		mAthleteAge = (EditText) window.findViewById(R.id.add_et_age);
		mAthleteContact = (EditText) window.findViewById(R.id.add_et_contact);
		mOthers = (EditText) window.findViewById(R.id.add_et_extra);
		rbMale = (RadioButton) window.findViewById(R.id.rb_male);
		rbFemale = (RadioButton) window.findViewById(R.id.rb_female);
		rgGender = (RadioGroup) window.findViewById(R.id.rg_gender);
		rgGender.setOnCheckedChangeListener(this);
		mAthleteNumber = (EditText) window.findViewById(R.id.add_et_number);

	}

	/**
	 * 保存一个运动员信息，如果无法联网则直接保存到数据库， 如果成功连接至服务器则将运动员信息发送至服务器
	 * 
	 * @param name
	 *            运动员姓名
	 * @param age
	 *            运动员年龄
	 * @param gender
	 *            运动员性别
	 * @param contact
	 *            运动员手机号码
	 * @param others
	 *            运动员其他信息
	 */
	public void addAthlete(String name, int age, String gender, String contact,
			String others,String number) {


		isConnect = NetworkUtil.isConnected(this);
		if (isConnect) {
			if (loadingDialog == null) {
				loadingDialog = LoadingDialog.createDialog(this);
				loadingDialog.setMessage(getString(R.string.synchronizing));
				loadingDialog.setCanceledOnTouchOutside(false);
			}
			loadingDialog.show();
			addAthleteRequest(name,age,gender,contact,others,number);
		} else {
			CommonUtils.showToast(this,mToast,getString(R.string.network_error));
		}

	}

	/**
	 * 将需要保存的对象转换成json字符串，提交到服务器
	 * 
	 * @param
	 */
	public void addAthleteRequest(String name, int age, String gender, String contact,
								  String others,String number) {
		final Athlete a = new Athlete();
		final TempAthlete a1 = new TempAthlete();
		a1.setName(name);
		a1.setAge(age);
		a1.setGender(gender);
		a1.setPhone(contact);
		a1.setExtras(others);
		a1.setNumber(number);

		a.setName(name);
		a.setAge(age);
		a.setUid(mUserId);
		a.setGender(gender);
		a.setPhone(contact);
		a.setExtras(others);
		a.setNumber(number);

		Map<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("athlete", a1);
		jsonMap.put("uid", mUser.getUid());
		final String athleteJson = JsonTools.creatJsonString(jsonMap);

		Map<String,String> map = getAddAthleteDataMap(athleteJson);
		VolleyUtil.ResponseListener listener = new VolleyUtil.ResponseListener() {
			@Override
			public void onSuccess(String string) {
				loadingDialog.dismiss();
				try {
					JSONObject obj = new JSONObject(string);
					int resCode = (Integer) obj.get("resCode");
					if (resCode == 1) {
						CommonUtils.showToast(AthleteActivity.this,
								mToast, Constants.ADD_SUCCESS_STRING);
						int aid = (Integer) obj.get("athlete_id");
						a.setAid(aid);
//						a.setUser(mUser);
						a.save();
						mAthletes = mDbManager.getAthletes(mUserId);
						mAthleteListAdapter.setDatas(mAthletes);
						mAthleteListAdapter.notifyDataSetChanged();
					}else{
						CommonUtils.showToast(AthleteActivity.this,mToast,getString(R.string.unkonwn_error));
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			@Override
			public void onError(String string) {
				loadingDialog.dismiss();
				CommonUtils.showToast(AthleteActivity.this,toast,getString(R.string.server_or_network_error));
			}
		};

		VolleyUtil.httpJson(Constants.ADD_ATHLETE_URL,Method.POST,map,listener,mApplication);

	}

	/**
	 * 获取增加运动员数据集
	 * @param athleteJson
	 * @return
	 */
	private Map<String,String> getAddAthleteDataMap(String athleteJson){
		Map<String, String> map = new HashMap<String, String>();
		map.put("athleteJson", athleteJson);
		return map;
	}


	/**
	 * 获取服务器上的运动员信息
	 */
	private void getAthleteRequest() {

		Map<String,String> map = getDataMap(mUser);
		VolleyUtil.ResponseListener listener = new VolleyUtil.ResponseListener() {
			@Override
			public void onSuccess(String string) {
				loadingDialog.dismiss();
				try {
					JSONObject jsonObject = new JSONObject(string);
					int resCode = (Integer) jsonObject.get("resCode");
					if (resCode == 1) {
						JSONArray athleteArray = jsonObject
								.getJSONArray("athleteList");
						int athletesNumber = athleteArray.length();
						/**
						 * 获取当前用户的运动员列表
						 */
						List<Athlete> athleteList = mDbManager.getAthletes(mUserId);
						for (int i = 0; i < athletesNumber; i++) {
							TempAthlete tempAthlete = JsonTools.getObject(athleteArray.get(i).toString(),
									TempAthlete.class);
							/**
							 * 如果当前athlete不在本地，就存储到服务器
							 */
							if(!CommonUtils.isAthleteInLocal(athleteList,tempAthlete.getAid())){
								Athlete athlete = new Athlete();
								athlete.setAid(tempAthlete.getAid());
								athlete.setName(tempAthlete.getName());
								athlete.setAge(tempAthlete.getAge());
								athlete.setGender(tempAthlete.getGender());
								athlete.setPhone(tempAthlete.getPhone());
								athlete.setExtras(tempAthlete.getExtras());
								athlete.setNumber(tempAthlete.getNumber());
								athlete.setUid(mUserId);
								athlete.save();
							}

						}
						mAthletes = mDbManager.getAthletes(mUserId);
						mAthleteListAdapter.setDatas(mAthletes);
						mAthleteListAdapter.notifyDataSetChanged();
						CommonUtils.showToast(AthleteActivity.this,
								mToast, getString(R.string.synchronized_success));
					} else {
						CommonUtils.showToast(AthleteActivity.this,
								mToast, UNKNOW_ERROR);
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onError(String string) {
				loadingDialog.dismiss();
				CommonUtils.showToast(AthleteActivity.this,
						mToast, UNKNOW_ERROR);
			}
		};
		VolleyUtil.httpJson(Constants.GET_ATHLETE_LIST, Method.POST, map, listener, mApplication);

	}

	/**
	 * 获取请求参数
	 * @param mUser
	 * @return
	 */
	private Map<String,String> getDataMap(User mUser){
		Map<String, String> map = new HashMap<String, String>();
		map.put("getAthleteFirst", String.valueOf(mUser.getUid()));
		return map;
	}

	/**
	 * 性别选择的监听器
	 * @param radioGroup
	 * @param checkedId
	 */
	@Override
	public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
		if(checkedId == rbFemale.getId()){
			ath_gender = getString(R.string.female);
		}
		if(checkedId == rbMale.getId()){
			ath_gender = getString(R.string.male);
		}
	}


	class TempAthlete {
		/**
		 * 运动员id
		 */
		private long id;

		private int aid;
		/**
		 * 运动员名字
		 */
		private String name;
		
		/**
		 * 运动员编号
		 */
		private String number;
		/**
		 * 运动员年龄
		 */
		private int age;
		/**
		 * 运动员性别
		 */
		private String gender;
		/**
		 * 运动员电话
		 */
		private String phone;
		/**
		 * 运动员备注
		 */
		private String extras;

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public int getAid() {
			return aid;
		}

		public void setAid(int aid) {
			this.aid = aid;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public String getGender() {
			return gender;
		}

		public void setGender(String gender) {
			this.gender = gender;
		}

		public String getPhone() {
			return phone;
		}

		public void setPhone(String phone) {
			this.phone = phone;
		}

		public String getExtras() {
			return extras;
		}

		public void setExtras(String extras) {
			this.extras = extras;
		}

		public String getNumber() {
			return number;
		}

		public void setNumber(String number) {
			this.number = number;
		}

		@Override
		public String toString() {
			return "TempAthlete [id=" + id + ", aid=" + aid + ", name=" + name
					+ ", number=" + number + ", age=" + age + ", gender="
					+ gender + ", phone=" + phone + ", extras=" + extras + "]";
		}

	}

	/**
	 * 退出当前活动窗体
	 * 
	 * @param v
	 */
	public void back(View v) {
		finish();
		overridePendingTransition(R.anim.slide_bottom_in, R.anim.slide_top_out);
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
	protected void onDestroy() {
		super.onDestroy();
		mApplication.removeActivity(this);
	}
}
