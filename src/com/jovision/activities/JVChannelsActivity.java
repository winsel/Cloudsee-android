package com.jovision.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.jovetech.CloudSee.temp.R;
import com.jovision.adapters.TabPagerAdapter;
import com.jovision.bean.Device;

public class JVChannelsActivity extends BaseActivity {
	private String TAG = "ChannelFragment";

	/** topBar */
	private Button leftBtn;
	private TextView currentMenu;
	private Button rightBtn;

	/** scroll layout */
	private HorizontalScrollView mHorizontalScrollView;
	private LinearLayout mLinearLayout;
	private ViewPager channelPager;
	private ImageView mImageView;
	private int mScreenWidth;
	private int item_width;

	private int endPosition;
	private int beginPosition;
	private int currentFragmentIndex;
	private boolean isEnd;

	private ArrayList<Fragment> fragments;

	/** intent传递过来的设备和通道下标 */
	private int deviceIndex;
	// private int channelIndex;
	private ArrayList<Device> deviceList = new ArrayList<Device>();

	private int widthPixels;

	private TextView top_name;

	private String top_string;

	@Override
	public void onHandler(int what, int arg1, int arg2, Object obj) {
		// switch (what) {
		// case Consts.CHANNEL_FRAGMENT_ONRESUME:
		// ((ChannelFragment) fragments.get(arg1)).setData(arg1, deviceList);
		// channelPager.setCurrentItem(deviceIndex);
		// break;
		// }
	}

	@Override
	public void onNotify(int what, int arg1, int arg2, Object obj) {
		handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));

	}

	@Override
	protected void initSettings() {
		Intent intent = getIntent();
		deviceIndex = intent.getIntExtra("DeviceIndex", 0);
		// channelIndex = intent.getIntExtra("ChannelIndex", 0);
		String devJsonString = intent.getStringExtra("DeviceList");
		deviceList = Device.fromJsonArray(devJsonString);
	}

	@Override
	protected void initUi() {
		setContentView(R.layout.channels_layout);

		/** top bar */
		top_name = (TextView) findViewById(R.id.currentmenu);
		top_string = getResources().getString(R.string.channal_list);
		leftBtn = (Button) findViewById(R.id.btn_left);
		currentMenu = (TextView) findViewById(R.id.currentmenu);
		rightBtn = (Button) findViewById(R.id.btn_right);
		rightBtn.setBackgroundResource(R.drawable.qr_icon);
		currentMenu.setText(R.string.channal_list);
		leftBtn.setOnClickListener(mOnClickListener);
		rightBtn.setOnClickListener(mOnClickListener);

		mScreenWidth = disMetrics.widthPixels;
		mHorizontalScrollView = (HorizontalScrollView) findViewById(R.id.hsv_view);
		mLinearLayout = (LinearLayout) findViewById(R.id.hsv_content);
		mImageView = (ImageView) findViewById(R.id.img);
		item_width = (int) ((mScreenWidth / 4.0 + 0.5f));
		mImageView.getLayoutParams().width = item_width;
		channelPager = (ViewPager) findViewById(R.id.channels_pager);

		widthPixels = disMetrics.widthPixels;

		// 初始化导航
		initNav();
		// 初始化viewPager
		initViewPager();

		channelPager.setCurrentItem(deviceIndex);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void initViewPager() {
		fragments = new ArrayList<Fragment>();
		int size = deviceList.size();

		for (int i = 0; i < size; i++) {
			// Bundle data = new Bundle();
			// data.putString("DeviceList", deviceList.toString());
			// data.putInt("DeviceIndex", deviceIndex);
			ChannelFragment fragment = new ChannelFragment(i, deviceList,
					widthPixels);
			// fragment.setArguments(data);
			fragments.add(fragment);
		}
		TabPagerAdapter fragmentPagerAdapter = new TabPagerAdapter(
				getSupportFragmentManager(), fragments);
		channelPager.setAdapter(fragmentPagerAdapter);
		fragmentPagerAdapter.setFragments(fragments);
		channelPager.setOnPageChangeListener(new ChannelsPageChangeListener());

	}

	private void initNav() {
		int size = deviceList.size();
		for (int i = 0; i < size; i++) {
			RelativeLayout layout = new RelativeLayout(this);
			TextView view = new TextView(this);
			view.setText(deviceList.get(i).getFullNo());
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.CENTER_IN_PARENT);
			layout.addView(view, params);
			mLinearLayout.addView(layout, (int) (mScreenWidth / 4 + 0.5f), 50);
			layout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					int index = (Integer) view.getTag();
					deviceIndex = index;
					channelPager.setCurrentItem(index);
					// ((ChannelFragment) fragments.get(index)).setData(index,
					// deviceList, widthPixels);
				}
			});
			layout.setTag(i);
		}
	}

	OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.btn_left: {
				JVChannelsActivity.this.finish();
				break;
			}
			case R.id.btn_right: {
				break;
			}
			}

		}
	};

	public class ChannelsPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageSelected(final int position) {
			// MyLog.v(TAG, "onPageSelected---position="+position);
			Animation animation = new TranslateAnimation(endPosition, position
					* item_width, 0, 0);

			beginPosition = position * item_width;

			currentFragmentIndex = position;
			if (animation != null) {
				animation.setFillAfter(true);
				animation.setDuration(0);
				mImageView.startAnimation(animation);
				mHorizontalScrollView.smoothScrollTo((currentFragmentIndex - 1)
						* item_width, 0);
			}
			deviceIndex = position;
			// ((ChannelFragment) fragments.get(position)).setData(position,
			// deviceList, widthPixels);
			channelPager.setCurrentItem(position);
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			// MyLog.v(TAG, "onPageScrolled---position="+position);
			if (!isEnd) {
				if (currentFragmentIndex == position) {
					endPosition = item_width * currentFragmentIndex
							+ (int) (item_width * positionOffset);
				}
				if (currentFragmentIndex == position + 1) {
					endPosition = item_width * currentFragmentIndex
							- (int) (item_width * (1 - positionOffset));
				}

				Animation mAnimation = new TranslateAnimation(beginPosition,
						endPosition, 0, 0);
				mAnimation.setFillAfter(true);
				mAnimation.setDuration(0);
				mImageView.startAnimation(mAnimation);
				mHorizontalScrollView.invalidate();
				beginPosition = endPosition;
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			if (state == ViewPager.SCROLL_STATE_DRAGGING) {
				isEnd = false;
			} else if (state == ViewPager.SCROLL_STATE_SETTLING) {
				isEnd = true;
				beginPosition = currentFragmentIndex * item_width;
				if (channelPager.getCurrentItem() == currentFragmentIndex) {
					// 未跳入下一个页面
					mImageView.clearAnimation();
					Animation animation = null;
					// 恢复位置
					animation = new TranslateAnimation(endPosition,
							currentFragmentIndex * item_width, 0, 0);
					animation.setFillAfter(true);
					animation.setDuration(1);
					mImageView.startAnimation(animation);
					mHorizontalScrollView.invalidate();
					endPosition = currentFragmentIndex * item_width;
				}
				// MyLog.v(TAG,
				// "onPageScrollStateChanged---currentFragmentIndex="+currentFragmentIndex+"---deviceIndex="+deviceIndex);
			}
		}

	}

	@Override
	protected void saveSettings() {

	}

	@Override
	protected void freeMe() {

	}
}