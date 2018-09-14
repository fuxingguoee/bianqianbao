package com.il360.bianqianbao.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import com.il360.bianqianbao.R;
import com.il360.bianqianbao.activity.WelcomeActivity;
import com.il360.bianqianbao.common.ExecuteTask;
import com.il360.bianqianbao.connection.HttpRequestUtil;
import com.il360.bianqianbao.connection.TResult;
import com.il360.bianqianbao.connection.UrlEnum;
import com.il360.bianqianbao.model.goods.GoodsExt;
import com.il360.bianqianbao.util.DimensionUtil;
import com.tencent.cos.model.COSRequest;
import com.tencent.cos.model.COSResult;
import com.tencent.cos.model.GetObjectRequest;
import com.tencent.cos.task.listener.IDownloadTaskListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

public class SlideGoodsPicsAdapter {
	
	private Context context;
	private String picPath = "";//保存本地的广告图片地址
	
	public SlideGoodsPicsAdapter(Context context, LinearLayout llytNewsCircle,
			ViewPager viewPager, List<GoodsExt> goodsExtList) {
		this.llytNewsCircle = llytNewsCircle;
		this.viewPager = viewPager;
		this.goodsExtList = goodsExtList;
		this.context = context;
		initUIForUrl(context);
		if (isAutoPlay) {
			startPlay();
		}
	}

	// 自动轮播启用开关
	private final static boolean isAutoPlay = true;

	// 自定义轮播图的资源ID
	private List<GoodsExt> goodsExtList;
	// 放轮播图片的ImageView 的list
	private List<View> imageViewsList;
	// 放圆点的View的list
	private List<ImageView> dotViewsList;

	private ViewPager viewPager;
	private LinearLayout llytNewsCircle;

	// 当前轮播页
	private int currentItem = 0;
	// 定时任务
	private ScheduledExecutorService scheduledExecutorService;

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			viewPager.setCurrentItem(currentItem);
		}

	};

	/**
	 * 开始轮播图切换
	 */
	private void startPlay() {
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutorService.scheduleAtFixedRate(new SlideShowTask(), 2, 3,
				TimeUnit.SECONDS);
	}
	
	/**
	 * 执行轮播图切换任务
	 *
	 */
	private class SlideShowTask implements Runnable {

		@Override
		public void run() {
			synchronized (viewPager) {
				currentItem = (currentItem + 1) % imageViewsList.size();
				handler.obtainMessage().sendToTarget();
			}
		}
	}


	/**
	 * 停止轮播图切换
	 */
	private void stopPlay() {
		scheduledExecutorService.shutdown();
	}
	
	/**
	 * 初始化Views等UI
	 */
	private void initUIForUrl(final Context context) {
		imageViewsList = new ArrayList<View>();
		dotViewsList = new ArrayList<ImageView>();
		imageViewsList.clear();
		dotViewsList.clear();
		llytNewsCircle.removeAllViews();

		ExecuteTask.execute(new Runnable() {

			@Override
			public void run() {
				for (int i = 0; i < goodsExtList.size(); i++) {
					initSignForUrl(goodsExtList.get(i).getGoodsPic(), i);
				}

				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						viewPager.setFocusable(true);
						viewPager.setAdapter(new MyPagerAdapter());
						viewPager.setOnPageChangeListener(new MyPageChangeListener());
					}
				});

			}
		});
		
//		viewPager.setFocusable(true);
//		viewPager.setAdapter(new MyPagerAdapter());
//		viewPager.setOnPageChangeListener(new MyPageChangeListener());
	}

	/**
	 * 生成滑动图片区域布局
	 * 
	 * @param index
	 * @return
	 */
	public View getSlideImageLayout(Context context, int index) {
		LinearLayout imageLinerLayout = new LinearLayout(context);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.MATCH_PARENT, 1);

		ImageView iv = new ImageView(context);
		iv.setBackgroundResource(index);
		iv.setScaleType(ScaleType.FIT_XY);
		imageLinerLayout.addView(iv, params);

		return imageLinerLayout;
	}
	
	/**
	 * 生成滑动图片区域布局
	 * 
	 * @param index
	 * @return
	 */
	public View getSlideImageLayoutForUrl(final Context context, String pathUrl, final int index) {
		LinearLayout imageLinerLayout = new LinearLayout(context);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.MATCH_PARENT, 1);

		ImageView iv = new ImageView(context);
//		iv.setScaleType(ScaleType.FIT_XY);
//		Bitmap signBitmap = PicUtil.getSmallBitmap(pathUrl);
		Bitmap signBitmap = BitmapFactory.decodeFile(pathUrl);
		iv.setImageBitmap(signBitmap);
//		PicUtil.deleteTempFile(pathUrl);
		imageLinerLayout.addView(iv, params);
		
		return imageLinerLayout;
	}
	
	public void initSignForUrl(final String url ,final int i) {
		try {
			Map<String, String> params = new HashMap<String, String>();
			params.put("picurl", url);
			TResult<Boolean, String> result = HttpRequestUtil.sendGetRequest(UrlEnum.BIZ_URL, "card/querySign", params);
			if (result.getSuccess()) {
				JSONObject obj = new JSONObject(result.getResult());
				if (obj.getInt("code") == 1) {
					loadPic(url, obj.getString("result"), i);
				}
			}
		} catch (Exception e) {
		}
	}
	
	public void loadPic(final String loadUrl ,String sign,final int i) {
		final String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
	                File.separator + "load";

		GetObjectRequest getObjectRequest = new GetObjectRequest(loadUrl, savePath);
		getObjectRequest.setSign(sign);
		getObjectRequest.setListener(new IDownloadTaskListener() {
			@Override
			public void onProgress(COSRequest cosRequest, final long currentSize, final long totalSize) {}

			@Override
			public void onSuccess(COSRequest cosRequest, COSResult cosResult) {
				String fileName = getNameFromUrl(loadUrl);
				picPath = savePath + File.separator + fileName; 
				
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						View imageView = getSlideImageLayoutForUrl(context, picPath, i);
						imageViewsList.add(imageView);

						ImageView circleImg = getCircleImage(context, i);
						dotViewsList.add(circleImg);
						View view = getLinearLayout(context, circleImg, 10, 10);
						llytNewsCircle.addView(view);
					}
				});
			}

			@Override
			public void onFailed(COSRequest COSRequest, COSResult cosResult) {
				picPath = "";
			}

			@Override
			public void onCancel(COSRequest arg0, COSResult arg1) {}
		});

		WelcomeActivity.getCOSClient().getObject(getObjectRequest);
	}
	

	private String getNameFromUrl(String loadUrl) {
		String[] a = loadUrl.split("/");
		String s = a[a.length - 1];
		return s;
	}

	/**
	 * 生成圆点图片
	 * 
	 * @param index
	 * @return
	 */
	public ImageView getCircleImage(Context context, int index) {

		int width = DimensionUtil.dip2px(context, 10);
		int height = DimensionUtil.dip2px(context, 10);

		ImageView imageView = new ImageView(context);
		imageView.setLayoutParams(new LayoutParams(width, height));
		imageView.setScaleType(ScaleType.CENTER);

		if (index == 0) {
			imageView.setBackgroundResource(R.drawable.icon_round_on); // 默认选中第一张图片
		} else {
			imageView.setBackgroundResource(R.drawable.icon_round_off);
		}
		return imageView;
	}

	/**
	 * 生成圆点布局区域
	 * 
	 * @param view
	 * @param width
	 * @param height
	 * @return
	 */
	public View getLinearLayout(Context context, View view, int width,
			int height) {
		width = DimensionUtil.dip2px(context, width);
		height = DimensionUtil.dip2px(context, height);
		LinearLayout linerLayout = new LinearLayout(context);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,
				height, 1);
		linerLayout.setPadding(7, 0, 7, 0);// 设置圆点间距
		linerLayout.addView(view, params);
		return linerLayout;
	}

	/**
	 * 填充ViewPager的页面适配器
	 * 
	 */
	private class MyPagerAdapter extends PagerAdapter {

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(imageViewsList.get(position));
		}

		@Override
		public Object instantiateItem(View container, int position) {
			((ViewPager) container).addView(imageViewsList.get(position));
			return imageViewsList.get(position);
		}

		@Override
		public int getCount() {
			return imageViewsList.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public Parcelable saveState() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void finishUpdate(View arg0) {
			// TODO Auto-generated method stub

		}

	}

	/**
	 * ViewPager的监听器 当ViewPager中页面的状态发生改变时调用
	 */
	private class MyPageChangeListener implements OnPageChangeListener {

		boolean isAutoPlay = false;

		@Override
		public void onPageScrollStateChanged(int arg0) {
			switch (arg0) {
			case 0:// 滑动结束，即切换完毕或者加载完毕
					// 当前为最后一张，此时从右向左滑，则切换到第一张
				if (viewPager.getCurrentItem() == viewPager.getAdapter()
						.getCount() - 1 && !isAutoPlay) {
					viewPager.setCurrentItem(0);
				}
				// 当前为第一张，此时从左向右滑，则切换到最后一张
				else if (viewPager.getCurrentItem() == 0 && !isAutoPlay) {
					viewPager
							.setCurrentItem(viewPager.getAdapter().getCount() - 1);
				}
				if(scheduledExecutorService.isShutdown()) {
					startPlay();
				}
				break;
			case 1:// 手势滑动，空闲中
				isAutoPlay = false;
				stopPlay();
				break;
			case 2:// 界面切换中
				isAutoPlay = true;
				break;
			}
			
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int pos) {
			currentItem = pos;
			for (int i = 0; i < dotViewsList.size(); i++) {
				if (i == pos) {
					((View) dotViewsList.get(pos))
							.setBackgroundResource(R.drawable.icon_round_on);
				} else {
					((View) dotViewsList.get(i))
							.setBackgroundResource(R.drawable.icon_round_off);
				}
			}
		}
	}

}
