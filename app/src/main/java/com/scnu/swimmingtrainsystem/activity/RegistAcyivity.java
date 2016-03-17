package com.scnu.swimmingtrainsystem.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.http.JsonTools;
import com.scnu.swimmingtrainsystem.model2db.User;
import com.scnu.swimmingtrainsystem.utils.CommonUtils;
import com.scnu.swimmingtrainsystem.utils.Constants;
import com.scnu.swimmingtrainsystem.utils.NetworkUtil;
import com.scnu.swimmingtrainsystem.utils.VolleyUtil;
import com.scnu.swimmingtrainsystem.view.LoadingDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录
 * @author lixinkun
 *
 * 2015年12月3日
 */
public class RegistAcyivity extends Activity {
	private MyApplication app;
	private EditText username;
	private EditText password;
	private EditText password1;
	private EditText email;
	private EditText phone;
	private EditText invitationCode;
	private Toast toast;
	private LoadingDialog loadingDialog;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_regist);

		init();
	}

	private void init(){
		app = (MyApplication) getApplication();
		app.addActivity(this);
		username = (EditText) findViewById(R.id.et_userID);
		password = (EditText) findViewById(R.id.et_password);
		password1 = (EditText) findViewById(R.id.et_password1);
		email = (EditText) findViewById(R.id.et_email);
		phone = (EditText) findViewById(R.id.et_phone);
		invitationCode = (EditText) findViewById(R.id.et_inviteCode);
	}


	public void getback(View v) {
		finish();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}

	/**
	 * 快速注册
	 * @param v
	 */
	public void quickRegist(View v) {
		final String userName = username.getText().toString().trim();
		final String pass = password.getText().toString().trim();
		final String pass1 = password1.getText().toString().trim();
		final String Email = email.getText().toString().trim();
		final String cellphone = phone.getText().toString().trim();
		final String invitation = invitationCode.getText().toString().trim();
		if (TextUtils.isEmpty(userName)) {
			CommonUtils.showToast(this, toast, getString(R.string.username_not_null));
		} else if (TextUtils.isEmpty(pass)) {
			CommonUtils.showToast(this, toast, getString(R.string.pwd_not_null));
		} else if (TextUtils.isEmpty(pass1) || !pass.equals(pass1)) {
			CommonUtils.showToast(this, toast, getString(R.string.con_pwd_not_equal_pwd));
		} else if (!TextUtils.isEmpty(Email)
				&& !CommonUtils.isEmail(email.getText().toString().trim())) {
			CommonUtils.showToast(this, toast, getString(R.string.email_not_right));
		} else if (!TextUtils.isEmpty(cellphone)
				&& !CommonUtils.isMobileNO(phone.getText().toString().trim())) {
			CommonUtils.showToast(this, toast, getString(R.string.phone_num_not_right));
		} else {
			//发送注册请求
			boolean isConnect = NetworkUtil.isConnected(this);
			if (isConnect) {
				if (loadingDialog == null) {
					loadingDialog = LoadingDialog.createDialog(this);
					loadingDialog.setMessage(getString(R.string.register_loading));
					loadingDialog.setCanceledOnTouchOutside(false);
				}
				loadingDialog.show();
				
				registRequest(userName,pass,Email,cellphone,invitation);
			} else {
				CommonUtils.showToast(RegistAcyivity.this, toast,
						getString(R.string.network_error));
			}
		}

	}

	/**
	 * 封装注册网络请求
	 * 
	 * @param
	 */
	private void registRequest(final String userName,final String password,final String email,
							   final String phone,final String invitationCode) {

		final User newUser = new User();
		newUser.setUsername(userName);
		newUser.setPassword(password);
		newUser.setEmail(email);
		newUser.setPhone(phone);
		final String jsonInfo = JsonTools.creatJsonString(newUser);

		Map<String,String> map = getDataMap(jsonInfo, invitationCode);

		VolleyUtil.ResponseListener listener = new VolleyUtil.ResponseListener() {
			@Override
			public void onSuccess(String string) {
				loadingDialog.dismiss();
						try {
							JSONObject obj = new JSONObject(string);
							int resCode = (Integer) obj.get("resCode");
							if (resCode == 1) {
								CommonUtils.showToast(RegistAcyivity.this,
										toast, getString(R.string.register_succeed));
								String uid = obj.get("uid").toString();
								newUser.setUid(Integer.parseInt(uid));
								newUser.save();
								overridePendingTransition(R.anim.slide_up_in,
										R.anim.slide_down_out);
								finish();
							} else if (resCode == 2) {
								CommonUtils.showToast(RegistAcyivity.this,
										toast, getString(R.string.user_donot_exists));
							} else {
								CommonUtils.showToast(RegistAcyivity.this,
										toast, getString(R.string.unkonwn_error));
							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

			}

			@Override
			public void onError(String string) {
				loadingDialog.dismiss();
				CommonUtils.showToast(RegistAcyivity.this,toast,getString(R.string.server_or_network_error));
			}
		};

		VolleyUtil.httpJson(Constants.REGIST, Request.Method.POST,map,listener,app);

	}

	/**
	 * 获取注册数据集
	 * @param jsonInfo
	 * @param invitationCode
	 * @return
	 */
	private Map<String,String> getDataMap(String jsonInfo,String invitationCode){
		Map<String, String> map = new HashMap<String, String>();
		map.put("registJson", jsonInfo);
		map.put("invitation_code",invitationCode);
		return map;
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			return false;
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		app.removeActivity(this);
	}
}
