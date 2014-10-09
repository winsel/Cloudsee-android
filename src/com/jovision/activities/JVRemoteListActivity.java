package com.jovision.activities;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jovetech.CloudSee.temp.R;
import com.jovision.Consts;
import com.jovision.adapters.RemoteVideoAdapter;
import com.jovision.bean.RemoteVideo;
import com.jovision.commons.JVConst;
import com.jovision.commons.MyLog;
import com.jovision.utils.PlayUtil;

public class JVRemoteListActivity extends BaseActivity {

	private final String TAG = "JV_RemoteList";
	private Button back;
	private TextView currentMenu;
	private Button rightFunc;

	private EditText selectDate;// 选择的日期
	private ImageView moreData;// 下三角
	private Button search;// 检索
	private RemoteVideoAdapter remoteVideoAdapter;
	private ListView remoteListView;

	private String date = "";
	private Calendar rightNow = Calendar.getInstance();
	private int year;
	private int month;
	private int day;

	private ArrayList<RemoteVideo> videoList;
	private int deviceType;// 设备类型
	private int channelIndex;// 通道index
	private boolean is05;// 是否05版解码器

	@Override
	public void onHandler(int what, int arg1, int arg2, Object obj) {

		switch (what) {
		case Consts.CALL_CHECK_RESULT: {// 查询远程回放数据
			byte[] pBuffer = (byte[]) obj;
			videoList = PlayUtil.getRemoteList(pBuffer, deviceType,
					channelIndex);
			if (null != videoList && 0 != videoList.size()) {
				handler.sendMessage(handler
						.obtainMessage(JVConst.REMOTE_DATA_SUCCESS));
			} else {
				handler.sendMessage(handler
						.obtainMessage(JVConst.REMOTE_NO_DATA_FAILED));
			}
			break;
		}
		case JVConst.REMOTE_DATA_SUCCESS: {
			remoteVideoAdapter.setData(videoList);
			remoteListView.setAdapter(remoteVideoAdapter);
			remoteVideoAdapter.notifyDataSetChanged();
			dismissDialog();
			break;
		}
		case JVConst.REMOTE_DATA_FAILED: {
			remoteVideoAdapter.setData(videoList);
			remoteVideoAdapter.notifyDataSetChanged();
			showTextToast(R.string.str_video_load_failed);
			dismissDialog();
			break;
		}
		case JVConst.REMOTE_NO_DATA_FAILED: {
			remoteVideoAdapter.setData(videoList);
			remoteVideoAdapter.notifyDataSetChanged();
			showTextToast(R.string.str_video_nodata_failed);
			dismissDialog();
			break;
		}
		case JVConst.REMOTE_START_PLAY:
			// setResult(JVConst.REMTE_PLAYBACK_BEGIN);// 开始远程回放
			finish();
			break;
		case JVConst.REMOTE_PLAY_FAILED:// 回放失败
			showTextToast(R.string.str_video_play_failed);
			break;
		case JVConst.REMOTE_PLAY_NOT_SUPPORT:
			// Message msg1 = JVPlayActivity.getInstance().playHandler
			// .obtainMessage();
			// msg1.what = JVConst.NOT_SUPPORT_REMOTE_PLAY;
			// JVPlayActivity.getInstance().playHandler.sendMessage(msg1);
			finish();
			break;
		case JVConst.REMOTE_PLAY_EXCEPTION:
			// if (JVSUDT.PLAY_EXCEPTION_FLAG) {
			// showTextToast(R.string.str_video_disconnected);
			// JVSUDT.PLAY_EXCEPTION_FLAG = false;
			// }
			//
			// JVSUDT.PLAY_FLAG = 0;// 视频预览
			// JVSUDT.ChangePlayFalg(windowIndex + 1, 0);
			// setResult(JVConst.REMTE_CLOSE_FETURE_);// 关闭远程回放界面
			// finish();
			break;
		}

	}

	@Override
	public void onNotify(int what, int arg1, int arg2, Object obj) {
		handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));

	}

	@Override
	protected void initSettings() {

	}

	@Override
	protected void initUi() {
		setContentView(R.layout.remoteplayback_layout);

		back = (Button) findViewById(R.id.btn_left);
		rightFunc = (Button) findViewById(R.id.btn_right);
		back.setVisibility(View.VISIBLE);
		rightFunc.setVisibility(View.GONE);

		currentMenu = (TextView) findViewById(R.id.currentmenu);
		currentMenu.setText(R.string.str_remote_playback);
		selectDate = (EditText) findViewById(R.id.datetext);
		moreData = (ImageView) findViewById(R.id.dateselectbtn);
		search = (Button) findViewById(R.id.search);
		remoteVideoAdapter = new RemoteVideoAdapter(JVRemoteListActivity.this);
		remoteListView = (ListView) findViewById(R.id.videolist);
		remoteListView.setOnItemClickListener(onItemClickListener);
		back.setOnClickListener(onClickListener);
		selectDate.setInputType(InputType.TYPE_NULL);
		selectDate.setOnTouchListener(onTouchListener);
		moreData.setOnTouchListener(onTouchListener);
		search.setOnClickListener(onClickListener);

		year = rightNow.get(Calendar.YEAR);
		month = rightNow.get(Calendar.MONTH) + 1;
		day = rightNow.get(Calendar.DAY_OF_MONTH);
		selectDate.setText(year + "-" + month + "-" + day);

		date = String.format("%04d%02d%02d000000%04d%02d%02d000000", year,
				month, day, year, month, day);
		Intent intent = getIntent();
		if (null != intent) {
			deviceType = intent.getIntExtra("DeviceType", 0);
			channelIndex = intent.getIntExtra("ChannelIndex", 0);
			is05 = intent.getBooleanExtra("is05", false);
		}
		searchRemoteData(2 * 1000);
	}

	/**
	 * 远程回放列点击播放事件
	 */
	OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			createDialog(R.string.connecting);
			RemoteVideo videoBean = videoList.get(arg2);
			String acBuffStr = PlayUtil.getPlayFileString(videoBean, is05,
					deviceType, year, month, day, arg2);
			if (null != acBuffStr && !"".equalsIgnoreCase(acBuffStr)) {
				Intent intent = new Intent();
				intent.setClass(JVRemoteListActivity.this,
						JVRemotePlayBackActivity.class);
				intent.putExtra("ChannelIndex", channelIndex);
				intent.putExtra("acBuffStr", acBuffStr);
				JVRemoteListActivity.this.startActivity(intent);
			}
			dismissDialog();
		}
	};

	/**
	 * 单击事件
	 */
	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.back:
				JVRemoteListActivity.this.finish();
				break;
			case R.id.search:
				searchRemoteData(100);
				break;
			}
		}
	};

	/**
	 * 检索远程回放数据
	 */
	public void searchRemoteData(final int sleepCount) {

		createDialog(R.string.str_loading_data);

		String temStr = selectDate.getText().toString();
		String[] temArray = temStr.split("-");
		year = Integer.parseInt(temArray[0]);
		month = Integer.parseInt(temArray[1]);
		day = Integer.parseInt(temArray[2]);
		date = String.format("%04d%02d%02d000000%04d%02d%02d000000", year,
				month, day, year, month, day);

		MyLog.e("tas", "searchCheck  windowIndex: " + (channelIndex) + "date: "
				+ date.toString());

		Thread searchThread = new Thread() {

			@Override
			public void run() {
				super.run();
				try {
					Thread.sleep(sleepCount);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				PlayUtil.checkRemoteData(channelIndex, date);
			}

		};
		searchThread.start();
	}

	/**
	 * 日历轻触事件
	 */
	OnTouchListener onTouchListener = new TextView.OnTouchListener() {
		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			if (MotionEvent.ACTION_DOWN == arg1.getAction()) {
				new DatePickerDialog(JVRemoteListActivity.this,
						new DatePickerDialog.OnDateSetListener() {

							public void onDateSet(DatePicker arg0, int year,
									int month, int day) {
								selectDate.setText(year + "-" + (++month) + "-"
										+ day);
								date = String.format(
										"%04d%02d%02d000000%04d%02d%02d000000",
										year, month, day, year, month, day);
								MyLog.e("选中日期", date);
								searchRemoteData(100);
							}
						}, rightNow.get(Calendar.YEAR),
						rightNow.get(Calendar.MONTH),
						rightNow.get(Calendar.DAY_OF_MONTH)).show();
			}
			return true;
		}

	};

	@Override
	protected void saveSettings() {

	}

	@Override
	protected void freeMe() {

	}

	@Override
	protected void onResume() {
		super.onResume();
	}
}
