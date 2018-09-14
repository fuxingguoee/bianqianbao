package com.il360.bianqianbao.activity.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import com.il360.bianqianbao.R;
import com.il360.bianqianbao.activity.BaseWidgetActivity;
import com.il360.bianqianbao.common.ExecuteTask;
import com.il360.bianqianbao.common.LogUmeng;
import com.il360.bianqianbao.connection.HttpRequestUtil;
import com.il360.bianqianbao.connection.TResult;
import com.il360.bianqianbao.connection.UrlEnum;
import com.il360.bianqianbao.util.AESEncryptor;
import com.il360.bianqianbao.util.ThreeDES;
import com.il360.bianqianbao.util.UserUtil;
import com.il360.bianqianbao.util.ViewUtil;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@EActivity(R.layout.act_deal_password)
public class DealPwdModifyActivity extends BaseWidgetActivity {
	/** 注册账号 **/
	@ViewById EditText etAccount;
	/** 注册密码 **/
	@ViewById EditText etPassword;
	/** 确认密码 **/
	@ViewById EditText etPassword2;
	/** 手机验证码 **/
	@ViewById EditText etCode;
	/** 获取验证码 **/
	@ViewById TextView btnGetCode;
	/** 注册 **/
	@ViewById Button btnConfirm;
	
	Pattern pt = Pattern.compile("^1[3|4|5|7|8][0-9]\\d{8}$");
	
	/** 倒计时器 */
	private Timer timer = new Timer();
	private static final int TIME_DOWN_COUNT = 60;//倒计时60秒
	private int time = 0;//倒计时计时数,初始为0
	
	@AfterViews
	void init(){
		etAccount.setKeyListener(null);
		etAccount.setText(UserUtil.getUserInfo().getLoginName());
		
		initTimeDown();
	}
	
	/** 验证倒计时 */
	private void initTimeDown() {
		timer.schedule(task, 100, 1000);
	}
	
	/** 倒计时的内容 */
	private TimerTask task = new TimerTask() {
		@Override
		public void run() {
			mHandler.sendEmptyMessage(time--);
		}
	};

	/** handler */
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.what>0){
				btnGetCode.setText(msg.what+"秒后重新获取");
				btnGetCode.setEnabled(false);
			}else{
				if (!ViewUtil.getText(etAccount).equals("")) {
					if (pt.matcher(ViewUtil.getText(etAccount)).matches()) {
						btnGetCode.setText("获取验证码");
						btnGetCode.setEnabled(true);
					}else{
						btnGetCode.setEnabled(false);
					}
				}else{
					btnGetCode.setEnabled(false);
				}
			}
		};
	};
	
	@AfterTextChange
	void etAccount() {
		validateConfirm();
		validateAccount();
	}
	
	@AfterTextChange
	void etCode(){
		validateConfirm();
	}
	
	@AfterTextChange
	void etPassword() {
		validateConfirm();
	}
	
	@AfterTextChange
	void etPassword2() {
		validateConfirm();
	}
	
	@Click
	void btnGetCode() {
		btnGetCode.setClickable(false);
		ExecuteTask.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Map<String, String> params = new HashMap<String, String>();
					params.put("phone", etAccount.getText().toString());
					params.put("verifyType", "4");// 4用户设置或修改支付密码
					TResult<Boolean, String> result = HttpRequestUtil.sendPostRequest(UrlEnum.BIZ_URL,
									"user/attachUserVerifyCode", params);
					if (result.getSuccess()) {
						try {
							JSONObject obj = new JSONObject(result.getResult());
							if (obj.getInt("code") == 1) {
								JSONObject objRes = obj.getJSONObject("result");
								String desc = (String)objRes.getString("returnMessage");
								showInfo(desc);
								time = TIME_DOWN_COUNT;
							} else {
								showInfo(obj.getString("result"));
							}
						} catch (JSONException e) {
							Log.e("PasswordModifyActivity", "btnGetCode",e);
							LogUmeng.reportError(DealPwdModifyActivity.this, e);
						}
					} else {
						showInfo(getString(R.string.A6));
					}
				} catch (Exception e) {
					showInfo(getString(R.string.A7));
				}finally{
					if(!btnGetCode.isClickable()){
						btnGetCode.setClickable(true);
					}
				}
			}
		});
	}
	
	@Click
	void btnConfirm(){
		if(ok()){
			ExecuteTask.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Map<String, String> params = new HashMap<String, String>();
					params.put("payPwd", AESEncryptor.encrypt(ThreeDES.encryptDESCBC(ViewUtil.getText(etPassword))));
					params.put("verifyCode", etCode.getText().toString());
					params.put("token", UserUtil.getToken());
					TResult<Boolean, String> result = HttpRequestUtil.sendPostRequest(UrlEnum.BIZ_URL,
									"user/savePayPwd", params);
					if (result.getSuccess()) {
						try {
							JSONObject obj = new JSONObject(result.getResult());
							if (obj.getInt("code") == 1) {
								JSONObject objRes = obj.getJSONObject("result");
								String desc = (String)objRes.getString("returnMessage");
								showInfo(desc);
								if(objRes.getInt("returnCode") == 1){
									DealPwdModifyActivity.this.finish();
								}
							} else {
								showInfo(obj.getString("result"));
							}
						} catch (JSONException e) {
							Log.e("DealPwdModifyActivity", "btnConfirm",e);
							LogUmeng.reportError(DealPwdModifyActivity.this, e);
						}
					}else{
						showInfo(getString(R.string.A6));
					}
				}catch(Exception e){
					showInfo(getString(R.string.A2));
				}
			}
		});
		}
	}
	
	private void showInfo(final String info) {

		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(DealPwdModifyActivity.this, info, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	
	private void validateAccount() {
		if (!ViewUtil.getText(etAccount).equals("")) {
			if (pt.matcher(ViewUtil.getText(etAccount)).matches()) {
				btnGetCode.setEnabled(true);
			} else {
				btnGetCode.setEnabled(false);
			}
		} else {
			btnGetCode.setEnabled(false);
		}
	}
	
	private void validateConfirm() {
		if (!ViewUtil.getText(etAccount).equals("") && !ViewUtil.getText(etPassword).equals("")
				&& !ViewUtil.getText(etPassword2).equals("")) {
			if (pt.matcher(ViewUtil.getText(etAccount)).matches() && ViewUtil.getText(etAccount).length() == 11
					&& ViewUtil.getText(etCode).length() > 3 && ViewUtil.getText(etPassword).length() == 6) {
				btnConfirm.setEnabled(true);
			} else {
				btnConfirm.setEnabled(false);
			}
		} else {
			btnConfirm.setEnabled(false);
		}
	}
	
	private boolean ok() {
		if (ViewUtil.getText(etPassword).length() != 6) {
			showInfo("交易密码为6位数字");
		} else if (!ViewUtil.getText(etPassword).equals(ViewUtil.getText(etPassword2))) {
			showInfo(getString(R.string.password_different));
		} else {
			return true;
		}
		return false;
	}

}
