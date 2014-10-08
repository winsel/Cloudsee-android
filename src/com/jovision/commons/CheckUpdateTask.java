package com.jovision.commons;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

import com.jovetech.CloudSee.temp.R;
import com.jovision.activities.BaseActivity;
import com.jovision.utils.ConfigUtil;
import com.jovision.utils.JSONUtil;

public class CheckUpdateTask extends AsyncTask<String, Integer, Integer> {
	private Context mContext;
	private String versionCode = "";// 远端版本号
	private String fileSize = "";// //远端文件大小
	private String updateLog = "";// 远端升级日志

	public CheckUpdateTask(Context con) {
		mContext = con;
	}

	// 可变长的输入参数，与AsyncTask.exucute()对应
	@Override
	protected Integer doInBackground(String... params) {
		int checkRes = -1;
		String lan = String.valueOf(ConfigUtil.getLanguage());
		try {
			JSONArray array = JSONUtil.getJSON(Url.CHECK_UPDATE_URL
					+ "?Language=" + String.valueOf(lan)
					+ "&"
					+ "RequestType=3&"
					+ "MobileType=1&"// 1 代表android
					+ "Version="
					+ mContext.getResources().getString(
							R.string.str_update_app_version));
			int curVersion = mContext.getPackageManager().getPackageInfo(
					mContext.getResources()
							.getString(R.string.str_package_name), 0).versionCode;

			if (null != array && 0 != array.length()) {
				for (int i = 0; i < array.length(); i++) {
					JSONObject temp = (JSONObject) array.get(i);
					if (-1 == Integer.parseInt(temp.getString("Num"))) {
						versionCode = temp.getString("Content");
					} else if (0 == Integer.parseInt(temp.getString("Num"))) {
						fileSize = temp.getString("Content");
					} else if (1 == Integer.parseInt(temp.getString("Num"))) {
						updateLog = temp.getString("Content");
					}
				}
			}

			updateLog = updateLog.replaceAll("&", "\n");
			if (null != versionCode && !"".equals(versionCode)) {
				if (curVersion < Integer.parseInt(versionCode)) {
					checkRes = 1;// 有更新
				} else {
					int tag = Integer.parseInt(params[0]);
					if (1 == tag) {// 手动更新才提示没有更新，自动更新没有更新时不提示
						checkRes = 0;// 没更新
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return checkRes;
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(Integer result) {
		// 返回HTML页面的内容此方法在主线程执行，任务执行的结果作为此方法的参数返回。
		((BaseActivity) mContext).dismissDialog();
		if (1 == result) {
			JVUpdate jvUpdate = new JVUpdate(mContext);
			jvUpdate.checkUpdateInfo(updateLog);
		} else if (0 == result) {
			((BaseActivity) mContext)
					.showTextToast(R.string.str_already_newest);
		}

	}

	@Override
	protected void onPreExecute() {
		// 任务启动，可以在这里显示一个对话框，这里简单处理,当任务执行之前开始调用此方法，可以在这里显示进度对话框。
		((BaseActivity) mContext).createDialog("");
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		// 更新进度,此方法在主线程执行，用于显示任务执行的进度。
	}
}