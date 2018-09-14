package com.il360.bianqianbao.activity.user;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import com.il360.bianqianbao.R;
import com.il360.bianqianbao.activity.BaseWidgetActivity;
import com.il360.bianqianbao.common.GlobalPara;
import com.il360.bianqianbao.util.NumReplaceUtil;
import com.il360.bianqianbao.util.UserUtil;

import android.widget.TextView;

@EActivity(R.layout.act_my_info)
public class MyInfoActivity extends BaseWidgetActivity {
	
	@ViewById TextView tvPhoneNum;
	@ViewById TextView tvRealName;
	@ViewById TextView tvInviteCode;
//	@ViewById TextView tvAddress;
//	
//	@ViewById TextView tvMyEducation;
//	@ViewById TextView tvMyMarriage;
	
	@AfterViews
	void init(){
		initViews();
	}

	private void initViews() {
		tvPhoneNum.setText(NumReplaceUtil.newPhoneNum(UserUtil.getUserInfo().getLoginName()));
		tvRealName.setText(NumReplaceUtil.newName(GlobalPara.getOutUserRz().getName()));
		tvInviteCode.setText(NumReplaceUtil.newIDNum(GlobalPara.getOutUserRz().getIdNo()));
//		tvAddress.setText(GlobalPara.getUserReg().getIdAddress());
//		tvMyEducation.setText(GlobalPara.getUserReg().getEducation());
//		tvMyMarriage.setText(GlobalPara.getUserReg().getMarriage());
	}

}
