package com.scnu.swimmingtrainsystem.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.util.CommonUtils;
import com.scnu.swimmingtrainsystem.util.Constants;
import com.scnu.swimmingtrainsystem.util.NetworkUtil;
import com.scnu.swimmingtrainsystem.util.VolleyUtil;
import com.scnu.swimmingtrainsystem.view.LoadingDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RetrievePasswordActivity extends Activity {
	private MyApplication app;
	private EditText emailEditText;
	private Toast mToast;
	private LoadingDialog loadingDialog;

	private boolean isConnected;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_retrieve_password);
		init();
	}

	private void init(){
		app = (MyApplication) getApplication();
		app.addActivity(this);
		emailEditText = (EditText) findViewById(R.id.edt_email);
	}

	public void back(View v) {
		finish();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}

	public void sendEmail(View v) {
		String emailAdress = emailEditText.getText().toString().trim();
		isConnected = NetworkUtil.isConnected(RetrievePasswordActivity.this);
		if(isConnected){
			if (loadingDialog == null) {
				loadingDialog = LoadingDialog.createDialog(this);
				loadingDialog.setMessage(getString(R.string.sending_request));
				loadingDialog.setCanceledOnTouchOutside(false);
			}
			loadingDialog.show();
			sendEmailRequest(emailAdress);
		}else{
			CommonUtils.showToast(this, mToast, getString(R.string.network_error));
		}

	}

	/**
	 * 发送找回密码请求
	 * 
	 * @param s1
	 *            邮箱地址
	 */
	public void sendEmailRequest(final String s1) {

		Map<String,String> map = getDataMap(s1);
		VolleyUtil.ResponseListener listener = new VolleyUtil.ResponseListener() {
			@Override
			public void onSuccess(String string) {
				loadingDialog.dismiss();
				try {
					JSONObject obj = new JSONObject(string);
					int resCode = (Integer) obj.get("resCode");
					if (resCode == 1) {
						CommonUtils.showToast(
								RetrievePasswordActivity.this, mToast,
								getString(R.string.send_succ_check_email));
					} else if (resCode == 0) {
						CommonUtils.showToast(
								RetrievePasswordActivity.this, mToast,
								getString(R.string.user_not_exist));
					} else {
						CommonUtils.showToast(
								RetrievePasswordActivity.this, mToast,
								getString(R.string.server_error));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Handler handler = new Handler();
				Runnable updateThread = new Runnable() {
					public void run() {
						finish();
						overridePendingTransition(R.anim.in_from_left,
								R.anim.out_to_right);
					}
				};
				handler.postDelayed(updateThread, 800);
			}

			@Override
			public void onError(String string) {
				loadingDialog.dismiss();
				CommonUtils.showToast(RetrievePasswordActivity.this,
						mToast, getString(R.string.server_or_network_error));
			}
		};

		VolleyUtil.httpJson(Constants.GET_PASSWORD, Request.Method.POST,map,listener,app);

	}

	private Map<String,String> getDataMap(String emailString){
		Map<String, String> map = new HashMap<String, String>();
		map.put("email", emailString);
		return map;
	}

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
