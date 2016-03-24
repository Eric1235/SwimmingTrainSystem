package com.scnu.swimmingtrainsystem.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.db.DBManager;
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

import java.util.HashMap;
import java.util.Map;

public class ModifyPassActivity extends Activity {
	private MyApplication app;
	private DBManager dbManager;
	private EditText modify_oldpass;
	private EditText modify_newpass;
	private EditText modify_comfirmpass;
	private Toast toast;
	private int userId;
	private User user;
	private LoadingDialog loadingDialog;
	private String newPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modify);
		init();
	}

	private void init() {
		app = (MyApplication) getApplication();
		app.addActivity(this);
		userId = SpUtil.getUID(ModifyPassActivity.this);
		dbManager = DBManager.getInstance();
		user = dbManager.getUserByUid(userId);
		modify_oldpass = (EditText) findViewById(R.id.modify_oldpass);
		modify_newpass = (EditText) findViewById(R.id.modify_newpass);
		modify_comfirmpass = (EditText) findViewById(R.id.modify_comfirmpass);
//		mQueue = Volley.newRequestQueue(this);
	}

	/**
	 * 响应修改密码事件
	 * 
	 * @param v
	 */
	public void modify(View v) {
		String oldPassword = modify_oldpass.getText().toString().trim();
		newPassword = modify_newpass.getText().toString().trim();
		String comfPassword = modify_comfirmpass.getText().toString().trim();

		String name = user.getUsername();
		String userPass = user.getPassword();
		if (TextUtils.isEmpty(oldPassword)) {
			CommonUtils.showToast(this, toast, getString(R.string.old_pwd_not_null));
		} else if (TextUtils.isEmpty(newPassword)) {
			CommonUtils.showToast(this, toast, getString(R.string.new_pwd_not_null));
		} else if (TextUtils.isEmpty(comfPassword)) {
			CommonUtils.showToast(this, toast, getString(R.string.confirm_pwd_not_null));
		} else if (!userPass.equals(oldPassword)) {
			CommonUtils.showToast(this, toast, getString(R.string.old_pwd_wrong));
		} else if (userPass.equals(newPassword)) {
			CommonUtils.showToast(this, toast, getString(R.string.new_pwd_the_same_with_the_old));
		} else if (name.equals("defaultUser")) {
			CommonUtils.showToast(this, toast, getString(R.string.the_default_account_cannot_be_modified));
		} else {
			dbManager.modifyUserPassword(userId, comfPassword);
		
			// 如果处在联网状态，则发送至服务器
			boolean isConnect = NetworkUtil.isConnected(this);
			if (isConnect) {
				if (loadingDialog == null) {
					loadingDialog = LoadingDialog.createDialog(this);
					loadingDialog.setMessage(getString(R.string.onSubmitting));
					loadingDialog.setCanceledOnTouchOutside(false);
				}
				loadingDialog.show();
				// 发送至服务器
				modifyRequest(oldPassword, newPassword, comfPassword);
			}else {
				CommonUtils.showToast(this,toast,getString(R.string.network_error));
			}
			
		}
	}

	/**
	 * 创建修改密码请求
	 * 
	 * @param oldPassword
	 *            旧密码
	 * @param newPassword
	 *            新密码
	 * @param comfPassword
	 *            确认新密码
	 */
	public void modifyRequest(final String oldPassword,
			final String newPassword, final String comfPassword) {

		String uid = String.valueOf(user.getUid());
		Map<String,String> map = getDataMap(uid,newPassword);
		VolleyUtil.ResponseListener listener = new VolleyUtil.ResponseListener() {
			@Override
			public void onSuccess(String string) {
				loadingDialog.dismiss();
				JSONObject obj;
				try {
					obj = new JSONObject(string);
					int resCode = (Integer) obj.get("resCode");
					if (resCode == 1) {
						//在网络更新以后，更新本地数据库
						ContentValues values = new ContentValues();
						values.put("password", newPassword);
						DataSupport.updateAll(User.class, values, "password=?", oldPassword);
						CommonUtils.showToast(ModifyPassActivity.this,
								toast, getString(R.string.modify_succeed));
						AppController.gotoLogin(ModifyPassActivity.this);
						finish();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			@Override
			public void onError(String string) {
				loadingDialog.dismiss();
			}
		};

		VolleyUtil.httpJson(Constants.MODIFY_PASSWORD,Method.POST,map,listener,app);

//		StringRequest stringRequest = new StringRequest(Method.POST,
//				Constants.MODIFY_PASSWORD, new Listener<String>() {
//
//					@Override
//					public void onResponse(String response) {
//						// TODO Auto-generated method stub
//						Log.i("modifyPass", response);
//						loadingDialog.dismiss();
//						JSONObject obj;
//						try {
//							obj = new JSONObject(response);
//							int resCode = (Integer) obj.get("resCode");
//							if (resCode == 1) {
//								//在网络更新以后，更新本地数据库
//								ContentValues values = new ContentValues();
//								values.put("password", newPassword);
//								DataSupport.updateAll(User.class, values, "password=?",oldPassword);
//								CommonUtils.showToast(ModifyPassActivity.this,
//										toast, getString(R.string.modify_succeed));
//								Intent i = new Intent(ModifyPassActivity.this,LoginActivity.class);
//								startActivity(i);
//								finish();
//							}
//						} catch (JSONException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//
//					}
//				}, new ErrorListener() {
//
//					@Override
//					public void onErrorResponse(VolleyError error) {
//						// TODO Auto-generated method stub
//						Log.e("modifyPass", error.getMessage());
//						loadingDialog.dismiss();
//					}
//				}) {
//
//			@Override
//			protected Map<String, String> getParams() throws AuthFailureError {
//				// 设置请求参数
//				Map<String, String> map = new HashMap<String, String>();
//				map.put("uid", user.getUid() + "");
//				map.put("newPass", newPassword);
//				return map;
//			}
//
//		};
//		stringRequest.setRetryPolicy(new DefaultRetryPolicy(
//				Constants.SOCKET_TIMEOUT,
//				DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//		mQueue.add(stringRequest);
	}

	private Map<String,String> getDataMap(String uid,String newpwd){
		Map<String, String> map = new HashMap<String, String>();
		map.put("uid", uid);
		map.put("newPass", newpwd);
		return map;
	}

	/**
	 * 退出当前窗体
	 * 
	 * @param v
	 */
	public void getback(View v) {
		finish();
		overridePendingTransition(R.anim.slide_bottom_in,
				R.anim.slide_top_out);
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
		app.removeActivity(this);
	}
}
