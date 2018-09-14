package com.il360.bianqianbao.activity.order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.Extra;
import com.il360.bianqianbao.R;
import com.il360.bianqianbao.activity.BaseWidgetActivity;
import com.il360.bianqianbao.adapter.RecordAdapter;
import com.il360.bianqianbao.common.ExecuteTask;
import com.il360.bianqianbao.common.LogUmeng;
import com.il360.bianqianbao.connection.HttpRequestUtil;
import com.il360.bianqianbao.connection.TResult;
import com.il360.bianqianbao.connection.UrlEnum;
import com.il360.bianqianbao.model.order.ArrayOfRecord;
import com.il360.bianqianbao.model.order.Record;
import com.il360.bianqianbao.util.FastJsonUtils;
import com.il360.bianqianbao.util.UserUtil;

import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@EActivity(R.layout.act_order_status)
public class OrderSatusActivity extends BaseWidgetActivity {

	@ViewById TextView tvOrderNo;
	
	@ViewById ListView myList;
	
	private List<Record> myRecord = new ArrayList<Record>();
	private RecordAdapter adapter;
	
	@Extra
	String type, orderNo;

	@AfterViews
	void init() {
		tvOrderNo.setText(orderNo);
		initData();
	}

	private void initData() {
		ExecuteTask.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Map<String, String> params = new HashMap<String, String>();
					params.put("type", type);
					params.put("token", UserUtil.getToken());
					params.put("orderNo", orderNo);
					TResult<Boolean, String> result = HttpRequestUtil.sendGetRequest(UrlEnum.BIZ_URL,
							"order/queryRecordList", params);
					if (result.getSuccess()) {
						ArrayOfRecord arrayOfRecord = FastJsonUtils.getSingleBean(result.getResult(), ArrayOfRecord.class);
						if (arrayOfRecord.getCode() == 1) {
							if(arrayOfRecord.getResult() != null && arrayOfRecord.getResult().size() > 0){
								myRecord.clear();
//								myRecord.addAll(arrayOfRecord.getResult());
								for (int i = 0; i < arrayOfRecord.getResult().size(); i++) {
									myRecord.add(arrayOfRecord.getResult().get(arrayOfRecord.getResult().size() - i - 1));
								}
							}
						} else {
							showInfo(arrayOfRecord.getDesc());
						}
					} else {
						showInfo(getString(R.string.A6));
					}
				} catch (Exception e) {
					Log.e("OrderSatusActivity", "initData", e);
					LogUmeng.reportError(OrderSatusActivity.this, e);
				} finally {

					runOnUiThread(new Runnable() {
						public void run() {
							if(myRecord != null && myRecord.size() > 0){
								adapter = new RecordAdapter(myRecord, OrderSatusActivity.this);
								myList.setAdapter(adapter);
								adapter.notifyDataSetChanged();
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
				
				Toast.makeText(OrderSatusActivity.this, info, Toast.LENGTH_SHORT).show();
			}
		});
	}
}
