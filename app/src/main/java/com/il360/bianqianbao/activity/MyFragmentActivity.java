package com.il360.bianqianbao.activity;

import java.util.ArrayList;

import com.il360.bianqianbao.common.MyPermissions;
import com.il360.bianqianbao.util.AppUtil.AppUtil;
import com.il360.bianqianbao.util.AppUtil.PermissionsResultListener;
import com.umeng.analytics.MobclickAgent;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.WindowManager;

public abstract class MyFragmentActivity extends FragmentActivity {

	
	public static String TAG;// 日志过滤TAG
    protected Context mContext;
	
    
 // For Android 6.0
    private PermissionsResultListener mListener;
    //申请标记值
    public static final int REQUEST_CODE_ASK_PERMISSIONS = 100;
    //手动开启权限requestCode
    public static final int SETTINGS_REQUEST_CODE = 200;
    //拒绝权限后是否关闭界面或APP
    private boolean mNeedFinish = false;
    //界面传递过来的权限列表,用于二次申请
    private ArrayList<String> mPermissionsList = new ArrayList<String>();

    
    @Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		//super.onSaveInstanceState(outState);
	}
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		 //取消横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //输入法弹出的时候不顶起布局
        //如果我们不设置"adjust..."的属性，对于没有滚动控件的布局来说，采用的是adjustPan方式，
        // 而对于有滚动控件的布局，则是采用的adjustResize方式。
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
//                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        mContext = this;
        
        requestPermission(MyPermissions.FORCE_REQUIRE_PERMISSIONS, true, new PermissionsResultListener() {
            @Override
            public void onPermissionGranted() {
//                ToastUtils.showShortToast("已申请权限");
            }

            @Override
            public void onPermissionDenied() {
            	MyFragmentActivity.this.finish();
            }
        });
    }
    
    
    /**
     * 权限允许或拒绝对话框
     *
     * @param permissions 需要申请的权限
     * @param needFinish  如果必须的权限没有允许的话，是否需要finish当前 Activity
     * @param callback    回调对象
     */
	protected void requestPermission(final ArrayList<String> permissions, final boolean needFinish,
			final PermissionsResultListener callback) {
		if (permissions == null || permissions.size() == 0) {
			return;
		}
		mNeedFinish = needFinish;
		mListener = callback;
		mPermissionsList = permissions;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			// 获取未通过的权限列表
			ArrayList<String> newPermissions = checkEachSelfPermission(permissions);
			if (newPermissions.size() > 0) {// 是否有未通过的权限
				requestEachPermissions(newPermissions.toArray(new String[newPermissions.size()]));
			} else {// 权限已经都申请通过了
				if (mListener != null) {
					mListener.onPermissionGranted();
				}
			}
		} else {
			if (mListener != null) {
				mListener.onPermissionGranted();
			}
		}
	}
 
    /**
     * 申请权限前判断是否需要声明
     *
     * @param permissions
     */
	private void requestEachPermissions(String[] permissions) {
		if (shouldShowRequestPermissionRationale(permissions)) {// 需要再次声明
			showRationaleDialog(permissions);
		} else {
			ActivityCompat.requestPermissions(MyFragmentActivity.this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
		}
	}
 
    /**
     * 弹出声明的 Dialog
     *
     * @param permissions
     */
	private void showRationaleDialog(final String[] permissions) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("提示").setMessage("为了应用可以正常使用，请您点击确认申请权限。")
				.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ActivityCompat.requestPermissions(MyFragmentActivity.this, permissions,
								REQUEST_CODE_ASK_PERMISSIONS);
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						if (mNeedFinish)
							finish();
					}
				}).setCancelable(false).show();
	}
 
    /**
     * 检查每个权限是否申请
     *
     * @param permissions
     * @return newPermissions.size > 0 表示有权限需要申请
     */
    private ArrayList<String> checkEachSelfPermission(ArrayList<String> permissions) {
        ArrayList<String> newPermissions = new ArrayList<String>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                newPermissions.add(permission);
            }
        }
        return newPermissions;
    }
 
    /**
     * 再次申请权限时，是否需要声明
     *
     * @param permissions
     * @return
     */
    private boolean shouldShowRequestPermissionRationale(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }
 
    /**
     * 申请权限结果的回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @TargetApi(23)
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
			@NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_CODE_ASK_PERMISSIONS && permissions != null) {
			// 获取被拒绝的权限列表
			ArrayList<String> deniedPermissions = new ArrayList<>();
			for (String permission : permissions) {
				if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
					deniedPermissions.add(permission);
				}
			}
			// 判断被拒绝的权限中是否有包含必须具备的权限
			ArrayList<String> forceRequirePermissionsDenied = checkForceRequirePermissionDenied(
					MyPermissions.FORCE_REQUIRE_PERMISSIONS, deniedPermissions);
			if (forceRequirePermissionsDenied != null && forceRequirePermissionsDenied.size() > 0) {
				// 必备的权限被拒绝，
				if (mNeedFinish) {
					showPermissionSettingDialog();
				} else {
					if (mListener != null) {
						mListener.onPermissionDenied();
					}
				}
			} else {
				// 不存在必备的权限被拒绝，可以进首页
				if (mListener != null) {
					mListener.onPermissionGranted();
				}
			}
		}
	}
 
    /**
     * 检查回调结果
     *
     * @param grantResults
     * @return
     */
    private boolean checkEachPermissionsGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
 
	private ArrayList<String> checkForceRequirePermissionDenied(ArrayList<String> forceRequirePermissions,
			ArrayList<String> deniedPermissions) {
		ArrayList<String> forceRequirePermissionsDenied = new ArrayList<>();
		if (forceRequirePermissions != null && forceRequirePermissions.size() > 0 && deniedPermissions != null
				&& deniedPermissions.size() > 0) {
			for (String forceRequire : forceRequirePermissions) {
				if (deniedPermissions.contains(forceRequire)) {
					forceRequirePermissionsDenied.add(forceRequire);
				}
			}
		}
		return forceRequirePermissionsDenied;
	}
 
    /**
     * 手动开启权限弹窗
     */
	private void showPermissionSettingDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("提示").setMessage("必要的权限被拒绝").setPositiveButton("去设置", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				AppUtil.getAppDetailsSettings(MyFragmentActivity.this, SETTINGS_REQUEST_CODE);
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.dismiss();
				if (mNeedFinish)
					AppUtil.restart(MyFragmentActivity.this);
			}
		}).setCancelable(false).show();
	}
 
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //如果需要跳转系统设置页后返回自动再次检查和执行业务 如果不需要则不需要重写onActivityResult
        if (requestCode == SETTINGS_REQUEST_CODE) {
            requestPermission(mPermissionsList, mNeedFinish, mListener);
        }
    }
    
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	

}
