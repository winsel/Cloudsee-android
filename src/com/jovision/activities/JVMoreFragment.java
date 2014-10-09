package com.jovision.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jovetech.CloudSee.temp.R;
import com.jovision.Consts;
import com.jovision.adapters.FragmentAdapter;
import com.jovision.bean.MoreFragmentBean;
import com.jovision.commons.CheckUpdateTask;
import com.jovision.commons.MyActivityManager;
import com.jovision.commons.MySharedPreference;
import com.jovision.utils.AccountUtil;
import com.jovision.utils.ConfigUtil;
import com.jovision.utils.ListViewUtil;
import com.jovision.utils.UserUtil;

/**
 * 更多
 */
public class JVMoreFragment extends BaseFragment {
	// Adapter 存储模块文字和图标
	private ArrayList<MoreFragmentBean> dataList;
	// 模块listView
	private ListView more_listView;
	// listView 适配器
	private FragmentAdapter adapter;
	// Fragment依附的activity
	private Activity activity;
	// 修改资料
	private TextView more_modify;
	// 找回密码
	private TextView more_findpassword;
	// 头像
	private ImageView more_head;
	// 注销按钮
	private RelativeLayout more_cancle;
	// 用户名称
	private TextView more_username;
	// 用户名
	private String more_name;
	// 最后一次登录时间
	private TextView more_lasttime;
	// 图片数组
	private int[] Image = { R.drawable.more_help_img,
			R.drawable.more_usermessage_img, R.drawable.more_feature_img,
			R.drawable.more_vibrator_img, R.drawable.more_about_img,
			R.drawable.more_suggesr_img, R.drawable.more_remark_img };
	// 功能名称数组
	private String[] fragment_name;

	public static boolean localFlag = false;// 本地登陆标志位

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_more, container, false);
		intiUi(view);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mParent = getView();
		mActivity = (BaseActivity) getActivity();
		listViewClick();
		localFlag = Boolean.valueOf(mActivity.statusHashMap
				.get(Consts.LOCAL_LOGIN));
		currentMenu.setText(R.string.more_featrue);
		rightBtn.setVisibility(View.GONE);
	}

	@Override
	public void onHandler(int what, int arg1, int arg2, Object obj) {
		// TODO Auto-generated method stub

	}

	private void intiUi(View view) {
		activity = getActivity();
		fragment_name = activity.getResources().getStringArray(
				R.array.array_more);
		if (Boolean.valueOf(((BaseActivity) activity).statusHashMap
				.get(Consts.LOCAL_LOGIN))) {
			more_name = activity.getResources().getString(
					R.string.location_login);
		} else {
			more_name = ((BaseActivity) activity).statusHashMap
					.get(Consts.KEY_USERNAME);
		}
		initDatalist();
		more_cancle = (RelativeLayout) view.findViewById(R.id.more_cancle);
		more_modify = (TextView) view.findViewById(R.id.more_modify);
		more_findpassword = (TextView) view
				.findViewById(R.id.more_findpassword);
		more_username = (TextView) view.findViewById(R.id.more_uesrname);
		more_lasttime = (TextView) view.findViewById(R.id.more_lasttime);
		more_head = (ImageView) view.findViewById(R.id.more_head_img);

		more_listView = (ListView) view.findViewById(R.id.more_listView);
		adapter = new FragmentAdapter(activity, dataList);
		more_listView.setAdapter(adapter);
		ListViewUtil.setListViewHeightBasedOnChildren(more_listView);

		more_username.setText(more_name);
		more_cancle.setOnClickListener(myOnClickListener);
		more_modify.setOnClickListener(myOnClickListener);
		more_findpassword.setOnClickListener(myOnClickListener);

	}

	private void initDatalist() {
		dataList = new ArrayList<MoreFragmentBean>();
		for (int i = 0; i < Image.length; i++) {
			MoreFragmentBean bean = new MoreFragmentBean();
			bean.setItem_img(Image[i]);
			bean.setName(fragment_name[i]);
			if (i == 0 || i == 2 || i == 6) {
				bean.setIsnew(true);
			} else {
				bean.setIsnew(false);
			}
			dataList.add(bean);
		}
	}

	OnClickListener myOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.more_cancle:// 注销
				LogOutTask task = new LogOutTask();
				String[] strParams = new String[3];
				task.execute(strParams);
				break;
			case R.id.more_modify:

				break;
			case R.id.more_findpassword:

				break;
			default:
				break;

			}
		}
	};

	private void listViewClick() {
		more_listView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// TODO Auto-generated method stub
						switch (position) {
						case 0:
							Toast.makeText(activity, "点击成功0",
									Toast.LENGTH_SHORT).show();
							break;
						case 1:
							Toast.makeText(activity, "点击成功1",
									Toast.LENGTH_SHORT).show();
							break;
						case 2:
							Toast.makeText(activity, "点击成功2",
									Toast.LENGTH_SHORT).show();
							break;
						case 3:
							Toast.makeText(activity, "点击成功3",
									Toast.LENGTH_SHORT).show();
							break;
						case 4:
							Toast.makeText(activity, "点击成功4",
									Toast.LENGTH_SHORT).show();
							break;
						case 5:
							if (("firsted").equals(MySharedPreference
									.getString(Consts.MORE_FREGMENT_FEEDBACK))) {
								Intent intent = new Intent(mActivity,
										JVFeedbackActivity.class);
								startActivity(intent);
							} else {
								Intent intent = new Intent(mActivity,
										JVIntroduceAty.class);
								MySharedPreference.putString(
										Consts.MORE_FREGMENT_FEEDBACK,
										"firsted");
								intent.putExtra("viewnum", 3);
								startActivity(intent);
							}
							break;
						case 6:
							Toast.makeText(activity, "点击成功6",
									Toast.LENGTH_SHORT).show();
							CheckUpdateTask task = new CheckUpdateTask(
									mActivity);
							String[] strParams = new String[3];
							strParams[0] = "1";// 0,手动检查更新
							task.execute(strParams);
							break;
						default:
							break;
						}
					}
				});
	}

	@Override
	public void onNotify(int what, int arg1, int arg2, Object obj) {
		// TODO Auto-generated method stub
	}

	// 注销线程
	class LogOutTask extends AsyncTask<String, Integer, Integer> {// A,361,2000
		// 可变长的输入参数，与AsyncTask.exucute()对应
		@Override
		protected Integer doInBackground(String... params) {
			int logRes = -1;
			try {
				if (!localFlag) {
					AccountUtil.userLogout();
				}
				ConfigUtil.logOut();
				UserUtil.resetAllUser();
				MySharedPreference.putString(Consts.DEVICE_LIST, "");
				mActivity.statusHashMap.put(Consts.HAG_GOT_DEVICE, "false");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return logRes;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Integer result) {
			// 返回HTML页面的内容此方法在主线程执行，任务执行的结果作为此方法的参数返回。
			((BaseActivity) mActivity).dismissDialog();
			MyActivityManager.getActivityManager().popAllActivityExceptOne(
					JVLoginActivity.class);
			Intent intent = new Intent();
			intent.setClass(mActivity, JVLoginActivity.class);
			mActivity.startActivity(intent);
			mActivity.finish();
		}

		@Override
		protected void onPreExecute() {
			// 任务启动，可以在这里显示一个对话框，这里简单处理,当任务执行之前开始调用此方法，可以在这里显示进度对话框。
			((BaseActivity) mActivity).createDialog("");
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// 更新进度,此方法在主线程执行，用于显示任务执行的进度。
		}
	}
}
