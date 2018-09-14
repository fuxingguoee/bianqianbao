package com.il360.bianqianbao.activity.address;

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
import com.il360.bianqianbao.adapter.AddressAdapter;
import com.il360.bianqianbao.common.ExecuteTask;
import com.il360.bianqianbao.common.Variables;
import com.il360.bianqianbao.connection.HttpRequestUtil;
import com.il360.bianqianbao.connection.TResult;
import com.il360.bianqianbao.connection.UrlEnum;
import com.il360.bianqianbao.model.address.Address;
import com.il360.bianqianbao.model.address.ArrayOfAddress;
import com.il360.bianqianbao.util.FastJsonUtils;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

@EActivity(R.layout.act_address)
public class DistrictActivity extends BaseWidgetActivity {
	@ViewById
	ListView lvAddress;

	private List<Address> list = new ArrayList<Address>();
	private AddressAdapter adapter;
	
	protected ProgressDialog transDialog;

	private String district;
	@Extra
	String province, city;

	@AfterViews
	void init() {
		initList();
		initViews();
	}

	private void initList() {
		transDialog = ProgressDialog.show(DistrictActivity.this, null, "加载中...", true);
		ExecuteTask.execute(new Runnable() {

			@Override
			public void run() {
				try {
					Map<String, String> params = new HashMap<String, String>();
					params.put("province", province);
					params.put("city", city);
					TResult<Boolean, String> result = HttpRequestUtil.sendGetRequest(UrlEnum.BIZ_URL,
							"card/querypcaList", params);
					if (result.getSuccess()) {
						ArrayOfAddress arrayOfAddress = FastJsonUtils.getSingleBean(result.getResult(), ArrayOfAddress.class);
						if (arrayOfAddress.getCode() == 1) {
							list.clear();
							list.addAll(arrayOfAddress.getResult());
						} else {
							showInfo(arrayOfAddress.getDesc());
						}
					} else {
						showInfo(getString(R.string.A6));
					}
				} catch (Exception e) {
					showInfo(getString(R.string.A2));
					Log.e("DistrictActivity", "initList", e);
				} finally {
					runOnUiThread(new Runnable() {
						public void run() {
							
							initViews();
							
							if (transDialog != null && transDialog.isShowing()) {
								transDialog.dismiss();
							}
						}
					});
				}
			}
		});
	}

	private void initViews() {
		adapter = new AddressAdapter(list, DistrictActivity.this, 3);
		lvAddress.setAdapter(adapter);
		lvAddress.setOnItemClickListener(new OnItemClickListener());
	}

	class OnItemClickListener implements android.widget.AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			district = list.get(position).getArea();
			Intent intent = new Intent();
			Bundle b = new Bundle();
			b.putString("province", province);
			b.putString("city", city);
			b.putString("area", district);
			intent.putExtras(b);
			setResult(Variables.ADDRESS_STATUS_CODE_SECCESS, intent);
			DistrictActivity.this.finish();
		}
	}
	
	private void showInfo(final String info) {
		runOnUiThread(new Runnable() {
			public void run() {
				if(transDialog!=null && transDialog.isShowing()){
					transDialog.dismiss();
				}
				Toast.makeText(DistrictActivity.this, info, Toast.LENGTH_SHORT).show();
			}
		});
	}
}
