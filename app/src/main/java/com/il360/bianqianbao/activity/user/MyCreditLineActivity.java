package com.il360.bianqianbao.activity.user;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import com.il360.bianqianbao.R;
import com.il360.bianqianbao.activity.BaseWidgetActivity;
import com.il360.bianqianbao.common.ExecuteTask;
import com.il360.bianqianbao.common.LogUmeng;
import com.il360.bianqianbao.connection.HttpRequestUtil;
import com.il360.bianqianbao.connection.TResult;
import com.il360.bianqianbao.connection.UrlEnum;
import com.il360.bianqianbao.model.user.UserAmount;
import com.il360.bianqianbao.util.FastJsonUtils;
import com.il360.bianqianbao.util.UserUtil;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

@EActivity(R.layout.act_my_credit_line)
public class MyCreditLineActivity extends BaseWidgetActivity {

	@ViewById
	TextView tvCreditLine;
	@ViewById
	TextView tvRepay;
	@ViewById
	TextView tvAvailable;
	
	private UserAmount userAmount;
	
	DecimalFormat df = new DecimalFormat("0.00");

	@AfterViews
	void init() {
		initData();
	}

	private void initData() {
		ExecuteTask.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Map<String, String> params = new HashMap<String, String>();
					params.put("token", UserUtil.getToken());
					TResult<Boolean, String> result = HttpRequestUtil.sendGetRequest(UrlEnum.BIZ_URL,
							"user/queryUserAmount", params);
					if (result.getSuccess()) {
						JSONObject obj = new JSONObject(result.getResult());
						if (obj.getInt("code") == 1) {
							JSONObject objRes = obj.getJSONObject("result");
							userAmount = FastJsonUtils.getSingleBean(objRes.toString(), UserAmount.class);
						} else {
							showInfo(obj.getString("desc"));
						}
					}
				} catch (Exception e) {
					Log.e("MyCreditLineActivity", "initData", e);
					LogUmeng.reportError(MyCreditLineActivity.this, e);
				}finally {
					runOnUiThread(new Runnable() {
							public void run() {
								if(userAmount != null){
									tvCreditLine.setText(df.format(userAmount.getAllAmount()));
									tvRepay.setText(df.format(userAmount.getUseAmount()));
									tvAvailable.setText(df.format(userAmount.getAllAmount().subtract(userAmount.getUseAmount())));
								}
							}
						});
					}
				}
		});
	}
	
	private void showInfo(final String info) {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(MyCreditLineActivity.this, info, Toast.LENGTH_SHORT).show();
			}
		});
	}
}
