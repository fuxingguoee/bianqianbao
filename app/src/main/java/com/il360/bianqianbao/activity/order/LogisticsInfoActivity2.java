package com.il360.bianqianbao.activity.order;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.Extra;
import com.il360.bianqianbao.R;
import com.il360.bianqianbao.activity.BaseWidgetActivity;
import com.il360.bianqianbao.activity.recovery.ExpressListActivity_;
import com.il360.bianqianbao.activity.user.LoginActivity_;
import com.il360.bianqianbao.common.ExecuteTask;
import com.il360.bianqianbao.common.GlobalPara;
import com.il360.bianqianbao.common.LogUmeng;
import com.il360.bianqianbao.connection.HttpRequestUtil;
import com.il360.bianqianbao.connection.TResult;
import com.il360.bianqianbao.connection.UrlEnum;
import com.il360.bianqianbao.model.hua.ArrayOfCardConfig;
import com.il360.bianqianbao.model.hua.CardConfig;
import com.il360.bianqianbao.model.order.LeaseOrder;
import com.il360.bianqianbao.model.order.UserRecovery;
import com.il360.bianqianbao.util.FastJsonUtils;
import com.il360.bianqianbao.util.ResultUtil;
import com.il360.bianqianbao.util.StringUtil;
import com.il360.bianqianbao.util.UserUtil;
import com.il360.bianqianbao.util.ViewUtil;
import com.il360.bianqianbao.view.CustomDialog1;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@EActivity(R.layout.act_logistics_info2)
public class LogisticsInfoActivity2 extends BaseWidgetActivity {
//	@ViewById 
//	TextView tvTextClick;
	@ViewById
	TextView tvOurAddress;
	@ViewById
	TextView tvOurPhone;
	@ViewById
	RelativeLayout rlExpressCompany;
	@ViewById
	TextView tvName;
	@ViewById
	EditText etOrderNo;
	@ViewById
	TextView tvSubmit;
	
	private String expressCompany;

	@Extra
	LeaseOrder leaseOrder;

	private List<CardConfig> list = null;

	@AfterViews
	void init() {
		if (GlobalPara.getCardConfigList() != null && GlobalPara.getCardConfigList().size() > 0) {
			list = GlobalPara.getCardConfigList();
			initViews();
		} else {
			initQueryConfig();
		}
	}
	
	private void initViews(){
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getConfigGroup().equals("express")
					&& list.get(i).getConfigName().equals("companyAddress")) {
				tvOurAddress.setText(list.get(i).getConfigValue());
			} else if (list.get(i).getConfigGroup().equals("express")
					&& list.get(i).getConfigName().equals("companyTelephone")) {
				tvOurPhone.setText(list.get(i).getConfigValue());
			}
		}
	}

	private void initQueryConfig() {
		ExecuteTask.execute(new Runnable() {

			@Override
			public void run() {
				try {
					Map<String, String> params = new HashMap<String, String>();
					TResult<Boolean, String> result = HttpRequestUtil.sendGetRequest(UrlEnum.BIZ_URL,
							"card/queryConfig", params);
					if (result.getSuccess()) {
						JSONObject obj = new JSONObject(result.getResult());
						if (obj.getInt("code") == 1) {
							JSONObject objRes = obj.getJSONObject("result");
							JSONObject objRetRes = objRes.getJSONObject("returnResult");
							ArrayOfCardConfig arrayOfCardConfig = FastJsonUtils.getSingleBean(objRetRes.toString(),
									ArrayOfCardConfig.class);
							if (arrayOfCardConfig.getList() != null && arrayOfCardConfig.getList().size() > 0) {
								GlobalPara.cardConfigList = arrayOfCardConfig.getList();
								list = GlobalPara.getCardConfigList();
							}
						}
					}
				} catch (Exception e) {
				}finally {
					runOnUiThread(new Runnable() {
						public void run() {
							initViews();
						}
					});
				}
			}
		});
	}
	
//	@Click
//	void tvTextClick(){
//		showDialog();
//	}
	
	@Click
	void rlExpressCompany(){
		Intent intent = new Intent();
		intent.setClass(this, ExpressListActivity_.class);
		startActivityForResult(intent, 0);
	}
	
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
//			showDialog();
//			return true;
//		}
//		return false;
//	}

	@Click
	void tvSubmit() {
		if (isOk()) {
			tvSubmit.setClickable(false);
			initUpdateUserRecovery();
		}
	}

	private void initUpdateUserRecovery() {
		ExecuteTask.execute(new Runnable() {

			@Override
			public void run() {
				try {
					Map<String, String> params = new HashMap<String, String>();
					params.put("userRecoveryJson", makeUserRecoveryJson());
					params.put("token", UserUtil.getToken());
					TResult<Boolean, String> result = HttpRequestUtil.sendPostRequest(UrlEnum.BIZ_URL,
							"recovery/applyRecovery", params);
					if (result.getSuccess()) {
						if (ResultUtil.isOutTime(result.getResult()) != null) {
							showInfo(ResultUtil.isOutTime(result.getResult()));
							Intent intent = new Intent(LogisticsInfoActivity2.this, LoginActivity_.class);
							startActivity(intent);
						} else {
							JSONObject obj = new JSONObject(result.getResult());
							if (obj.getInt("code") == 1) {
								showInfo("提交成功！");
								ViewUtil.backHomeActivity(LogisticsInfoActivity2.this);
								LogisticsInfoActivity2.this.finish();
							} else {
								showInfo(obj.getString("desc"));
							}
						}
					} else {
						showInfo(getString(R.string.A6));
					}
				} catch (Exception e) {
					Log.e("LogisticsInfoActivity", "initUpdateUserRecovery()", e);
					LogUmeng.reportError(LogisticsInfoActivity2.this, e);
					showInfo(getString(R.string.A2));
				}finally {
					runOnUiThread(new Runnable() {
						public void run() {
							tvSubmit.setClickable(true);
						}
					});
				}
			}
		});
	}
	
	private String makeUserRecoveryJson() {
		UserRecovery userRecovery = new UserRecovery();
		userRecovery.setOrderNo(leaseOrder.getLeaseOrderNo());
		userRecovery.setRecoveryType("快递");
		userRecovery.setExpressNo(StringUtil.getStringSub(ViewUtil.getText(etOrderNo)));
		userRecovery.setExpressCompany(StringUtil.getStringSub(ViewUtil.getText(tvName)));
		userRecovery.setExt1("");
		userRecovery.setExt2("");
		return FastJsonUtils.getJsonString(userRecovery);
	}

	private boolean isOk() {
		if (TextUtils.isEmpty(ViewUtil.getText(tvOurAddress))) {
			showInfo("邮寄地址获取失败");
		} else if (TextUtils.isEmpty(ViewUtil.getText(tvOurPhone))) {
			showInfo("邮寄电话获取失败");
		} else if (TextUtils.isEmpty(ViewUtil.getText(tvName))) {
			showInfo("请选择快递公司");
		} else if (TextUtils.isEmpty(ViewUtil.getText(etOrderNo))) {
			showInfo("请输入快递单号");
		} else {
			return true;
		}
		return false;
	}
	
	private void showDialog() {
		CustomDialog1.Builder builder = new CustomDialog1.Builder(LogisticsInfoActivity2.this);
		builder.setTitle("关闭订单");
		builder.setMessage("您还没补充物流信息，确认关闭？关闭后可以在订单中继续操作哦！");
		builder.setPositiveButton("确认", new android.content.DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ViewUtil.backHomeActivity(LogisticsInfoActivity2.this);
				LogisticsInfoActivity2.this.finish();
				dialog.dismiss();
			}
		});
		
		builder.setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == 101) {
			Bundle b = data.getExtras();
			if(b != null){
				expressCompany = b.getString("expressName");
				tvName.setText(expressCompany);
			}
		} 
	}

	private void showInfo(final String info) {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(LogisticsInfoActivity2.this, info, Toast.LENGTH_SHORT).show();
			}
		});
	}
}
