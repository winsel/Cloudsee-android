package com.jovision.activities;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jovetech.CloudSee.temp.R;
import com.jovision.Consts;
import com.jovision.commons.JVAccountConst;
import com.jovision.commons.MyActivityManager;
import com.jovision.commons.MyLog;
import com.jovision.commons.Url;
import com.jovision.utils.AccountUtil;
import com.jovision.utils.mails.MailSenderInfo;
import com.jovision.utils.mails.SimpleMailSender;

public class JVOffLineDialogActivity extends BaseActivity {

	private static final int COUNTS = 15;// 15秒倒计时
	protected static final int COUNT_END = 0x50;// 倒计时结束
	protected static final int COUNTING = 0x51;// 倒计时

	protected static final int SEND_MAIL_SUCC = 0x52;// 邮件发送成功
	protected static final int SEND_MAIL_FAIL = 0x53;// 邮件发送失败
	protected static final int SEND_MAIL_SHOWMSG = 0x54;// 谈提示

	/** 账号踢退 */
	private LinearLayout otherLoginLayout;
	private TextView lastCount;// 15秒倒计时
	private int lastSeconds;// 剩余秒数
	private Button exit;
	private Button keepOnline;
	Timer offlineTimer;

	/** 账号离线 */
	private LinearLayout offlineLayout;
	private Button sure;

	/** 崩溃日志发送 */
	private LinearLayout exceptionLayout;
	private Button send;
	private Button cancel;

	private int errorCode = 0;
	private String errorMsg = "";// 崩溃日志
	private SimpleMailSender sms;
	MailSenderInfo mailInfo;

	@Override
	public void onHandler(int what, int arg1, int arg2, Object obj) {
		switch (what) {
		case COUNTING: {
			lastCount.setText(String.valueOf(arg1));
			break;
		}
		case COUNT_END: {
			reLogin();
			break;
		}
		case SEND_MAIL_SHOWMSG: {
			dismissDialog();
			showTextToast(R.string.str_send_success);
			break;
		}
		case SEND_MAIL_SUCC: {
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(0);

			break;
		}
		case SEND_MAIL_FAIL: {
			dismissDialog();
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(0);
			break;
		}

		}
	}

	@Override
	public void onNotify(int what, int arg1, int arg2, Object obj) {
		handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
	}

	@Override
	protected void initSettings() {
		errorCode = getIntent().getIntExtra("ErrorCode", 0);
		errorMsg = getIntent().getStringExtra("ErrorMsg");
	}

	@Override
	protected void initUi() {
		setContentView(R.layout.dialog_offline);
		/** 账号踢退 */
		otherLoginLayout = (LinearLayout) findViewById(R.id.other_login_layout);
		lastCount = (TextView) findViewById(R.id.lastcount);
		exit = (Button) findViewById(R.id.exit);
		keepOnline = (Button) findViewById(R.id.keeponline);
		exit.setOnClickListener(mOnClickListener);
		keepOnline.setOnClickListener(mOnClickListener);

		/** 账号离线 */
		offlineLayout = (LinearLayout) findViewById(R.id.offline_layout);
		sure = (Button) findViewById(R.id.sure);
		sure.setOnClickListener(mOnClickListener);

		/** 程序崩溃 */
		exceptionLayout = (LinearLayout) findViewById(R.id.exception_layout);
		send = (Button) findViewById(R.id.send);
		cancel = (Button) findViewById(R.id.cancel);
		send.setOnClickListener(mOnClickListener);
		cancel.setOnClickListener(mOnClickListener);

		if (errorCode == JVAccountConst.MESSAGE_OFFLINE) {// 提掉线
			otherLoginLayout.setVisibility(View.VISIBLE);
			offlineLayout.setVisibility(View.GONE);
			exceptionLayout.setVisibility(View.GONE);
			lastSeconds = COUNTS;
			offlineTimer = new Timer();
			TimerTask offlineTask = new TimerTask() {

				@Override
				public void run() {
					lastSeconds--;
					if (0 == lastSeconds) {
						handler.sendMessage(handler.obtainMessage(COUNT_END, 0,
								0, null));
					} else {
						handler.sendMessage(handler.obtainMessage(COUNTING,
								lastSeconds, 0, null));
					}
				}

			};
			offlineTimer.schedule(offlineTask, 1 * 1000, 1 * 1000);
		} else if (errorCode == Consts.APP_CRASH) {// 程序崩溃
			otherLoginLayout.setVisibility(View.GONE);
			offlineLayout.setVisibility(View.GONE);
			exceptionLayout.setVisibility(View.VISIBLE);
		} else {
			otherLoginLayout.setVisibility(View.GONE);
			offlineLayout.setVisibility(View.VISIBLE);
			exceptionLayout.setVisibility(View.GONE);
		}

	}

	OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.keeponline: {
				createDialog(R.string.login_str_loging);
				LoginTask task = new LoginTask();
				String[] strParams = new String[3];
				task.execute(strParams);
				break;
			}
			case R.id.exit:
			case R.id.sure: {
				reLogin();
				break;
			}
			case R.id.send: {

				Calendar rightNow = Calendar.getInstance();
				String str = rightNow.get(Calendar.YEAR)
						+ "_"
						+ (rightNow.get(Calendar.MONTH) + 1 + "_" + rightNow
								.get(Calendar.DATE));
				mailInfo = new MailSenderInfo();
				mailInfo.setMailServerHost("smtp.qq.com");
				mailInfo.setMailServerPort("25");
				mailInfo.setValidate(true);
				mailInfo.setUserName("741376209@qq.com"); // 你的邮箱地址
				mailInfo.setPassword("mfq_zsw");// 您的邮箱密码
				mailInfo.setFromAddress("741376209@qq.com");
				mailInfo.setToAddress("jovision1203@163.com");// jovetech1203**
				// mailInfo.setToAddress("jy0329@163.com");
				mailInfo.setSubject("[BUG]["
						+ JVOffLineDialogActivity.this.getResources()
								.getString(R.string.app_name)
						+ "]"
						+ JVOffLineDialogActivity.this.getResources()
								.getString(R.string.str_current_version));
				mailInfo.setContent("[" + str + "]" + errorMsg);

				// 这个类主要来发送邮件
				sms = new SimpleMailSender();

				createDialog("");
				SendMailThread thread = new SendMailThread();
				thread.start();
				break;
			}
			case R.id.cancel: {
				System.exit(0);

				break;
			}
			}

		}

	};

	@Override
	protected void saveSettings() {

	}

	@Override
	protected void freeMe() {

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// 登陆线程
	private class LoginTask extends AsyncTask<String, Integer, Integer> {// A,361,2000
		// 可变长的输入参数，与AsyncTask.exucute()对应
		@Override
		protected Integer doInBackground(String... params) {
			int loginRes = 0;
			String strRes = AccountUtil.onLoginProcess(
					JVOffLineDialogActivity.this,
					statusHashMap.get(Consts.KEY_USERNAME),
					statusHashMap.get(Consts.KEY_PASSWORD), Url.SHORTSERVERIP,
					Url.LONGSERVERIP);
			JSONObject respObj = null;
			try {
				respObj = new JSONObject(strRes);
				loginRes = respObj.optInt("arg1", 1);
			} catch (JSONException e) {
				loginRes = JVAccountConst.LOGIN_FAILED_2;
				e.printStackTrace();
			}
			return loginRes;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Integer result) {
			// 返回HTML页面的内容此方法在主线程执行，任务执行的结果作为此方法的参数返回。

			if (JVAccountConst.LOGIN_SUCCESS == result) {
				showTextToast(R.string.keeponline_success);
				JVOffLineDialogActivity.this.finish();
			} else {
				showTextToast(R.string.keeponline_failed);
				reLogin();
			}
			dismissDialog();
		}

		@Override
		protected void onPreExecute() {
			// 任务启动，可以在这里显示一个对话框，这里简单处理,当任务执行之前开始调用此方法，可以在这里显示进度对话框。
			createDialog("");
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// 更新进度,此方法在主线程执行，用于显示任务执行的进度。
		}
	}

	/**
	 * 重新进登陆
	 */
	public void reLogin() {
		MyActivityManager.getActivityManager().popAllActivityExceptOne(
				JVLoginActivity.class);
		Intent intent = new Intent();
		intent.setClass(JVOffLineDialogActivity.this, JVLoginActivity.class);
		JVOffLineDialogActivity.this.startActivity(intent);
		JVOffLineDialogActivity.this.finish();
	}

	class SendMailThread extends Thread {

		@Override
		public void run() {
			super.run();
			boolean flag = sms.sendTextMail(mailInfo);// 发送文体格式
			if (flag) {
				handler.sendEmptyMessage(SEND_MAIL_SHOWMSG);
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				handler.sendEmptyMessage(SEND_MAIL_SUCC);
			} else {
				handler.sendEmptyMessage(SEND_MAIL_FAIL);
			}
		}

	}

	// 设置三种类型参数分别为String,Integer,String
	class SendMailTask extends AsyncTask<String, Integer, Integer> {
		// 可变长的输入参数，与AsyncTask.exucute()对应
		@Override
		protected Integer doInBackground(String... params) {
			int sendRes = -1;// 0成功 1失败
			boolean flag = false;
			try {
				flag = sms.sendTextMail(mailInfo);// 发送文体格式
				// sms.sendHtmlMail(mailInfo);//发送html格式
			} catch (Exception e) {
				MyLog.e("SendMail", e.getMessage());
			}
			if (flag) {
				sendRes = 0;
			} else {
				sendRes = 1;
			}

			return sendRes;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Integer result) {
			// 返回HTML页面的内容此方法在主线程执行，任务执行的结果作为此方法的参数返回。

			dismissDialog();
			if (0 == result) {
				showTextToast(R.string.str_send_success);
				System.exit(0);
			} else {
				System.exit(0);
			}
		}

		@Override
		protected void onPreExecute() {
			// 任务启动，可以在这里显示一个对话框，这里简单处理,当任务执行之前开始调用此方法，可以在这里显示进度对话框。
			createDialog("");
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// 更新进度,此方法在主线程执行，用于显示任务执行的进度。
		}
	}

}