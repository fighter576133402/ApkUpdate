package cn.wangjianlog.apkupdate.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.RemoteViews;
import cn.wangjianlog.apkupdate.R;

public class DownloadService extends Service {

	private static final int NOTIFY_ID = 0;
	private boolean cancelled;
	private int progress;

	private Context mContext = this;

	private NotificationManager mNotificationManager;
	private Notification mNotification;

	private DownloadBinder binder = new DownloadBinder();

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				int rate = msg.arg1;
				if (rate < 100) {
					// 更新进度
					RemoteViews contentView = mNotification.contentView;
					contentView.setTextViewText(R.id.rate, rate + "%");
					contentView.setProgressBar(R.id.progress, 100, rate, false);
				} else {
					// 下载完毕后变换通知形式
					mNotification.flags = Notification.FLAG_AUTO_CANCEL;
					mNotification.contentView = null;
					Intent intent = new Intent(mContext, FileMgrActivity.class);
					// 告知已完成
					intent.putExtra("completed", "yes");
					//更新参数,注意flags要使用FLAG_UPDATE_CURRENT
					PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
					mNotification.setLatestEventInfo(mContext, "下载完成", "文件已下载完毕", contentIntent);
					stopSelf();//停掉服务自身
				}

				// 最后别忘了通知一下,否则不会更新
				mNotificationManager.notify(NOTIFY_ID, mNotification);
				break;
			case 0:
				// 取消通知
				mNotificationManager.cancel(NOTIFY_ID);
				break;
			}
		};
	};

	@Override
	public void onCreate() {
		super.onCreate();
		mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// 返回自定义的DownloadBinder实例
		return binder;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		cancelled = true; // 取消下载线程
	}
	
	/**
	 * 创建通知
	 */
	private void setUpNotification() {
		int icon = R.drawable.ic_launcher;
		CharSequence tickerText = "开始下载";
		long when = System.currentTimeMillis();
		mNotification = new Notification(icon, tickerText, when);

		// 放置在"正在运行"栏目中
		mNotification.flags = Notification.FLAG_ONGOING_EVENT;

		RemoteViews contentView = new RemoteViews(mContext.getPackageName(), R.layout.download_notification_layout);
		contentView.setTextViewText(R.id.fileName, "AngryBird.apk");
		// 指定个性化视图
		mNotification.contentView = contentView;

		Intent intent = new Intent(this, FileMgrActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		// 指定内容意图
		mNotification.contentIntent = contentIntent;
		mNotificationManager.notify(NOTIFY_ID, mNotification);
	}

	/**
	 * 下载模块
	 */
	private void startDownload() {
		cancelled = false;
		int rate = 0;
		while (!cancelled && rate < 100) {
			try {
				// 模拟下载进度
				Thread.sleep(500);
				rate = rate + 5;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Message msg = handler.obtainMessage();
			msg.what = 1;
			msg.arg1 = rate;
			handler.sendMessage(msg);

			this.progress = rate;
		}
		if (cancelled) {
			Message msg = handler.obtainMessage();
			msg.what = 0;
			handler.sendMessage(msg);
		}
	}

	/**
	 * DownloadBinder中定义了一些实用的方法
	 * 
	 * @author user
	 * 
	 */
	public class DownloadBinder extends Binder {

		/**
		 * 开始下载
		 */
		public void start() {
			//将进度归零
			progress = 0;
			//创建通知
			setUpNotification();
			new Thread() {
				public void run() {
					//下载
					startDownload();
				};
			}.start();
		}

		/**
		 * 获取进度
		 * 
		 * @return
		 */
		public int getProgress() {
			return progress;
		}

		/**
		 * 取消下载
		 */
		public void cancel() {
			cancelled = true;
		}

		/**
		 * 是否已被取消
		 * 
		 * @return
		 */
		public boolean isCancelled() {
			return cancelled;
		}
	}
}
