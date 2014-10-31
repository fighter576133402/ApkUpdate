package cn.wangjianlog.apkupdate.dialog;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import cn.wangjianlog.apkupdate.dialog.EIMUpdateManagerSplash.EIMCallBack;
import cn.wangjianlog.apkupdate.dialog.EIMUpdateManagerSplash.EIMProgress;

public class UpdateActivitySplash extends Activity {
	ProgressDialog progressDialog;
	WakeLock wakeLock;

	private String downloadUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 透明activity
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(new View(this));
		// 创建进度对话框
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMax(100);
		progressDialog.setTitle("版本更新");
		progressDialog.setMessage("下载中，请稍候...");
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setCancelable(false);
		progressDialog.setButton(Dialog.BUTTON_NEGATIVE, "取消",
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 取消更新
						EIMUpdateManagerSplash.getInstance()
								.cancelDownloadAndroidApk();
						if (progressDialog != null) {
							progressDialog.dismiss();
							progressDialog = null;
						}
						Toast.makeText(UpdateActivitySplash.this, "更新已取消",
								3 * 1000).show();
						UpdateActivitySplash.this.finish();
					}
				});
		progressDialog.show();
		progressDialog.setProgress(0);

		downloadUrl = getIntent().getStringExtra("downloadUrl");
		if (downloadUrl != null || !"".equals(downloadUrl)) {
			EIMUpdateManagerSplash.getInstance().setDownloadUrl(downloadUrl);
			EIMUpdateManagerSplash.getInstance().downloadAndroidApk(null,
					new EIMCallBack() {
						@Override
						public void success(Object object) {
							if (progressDialog != null) {
								progressDialog.dismiss();
								progressDialog = null;
							}
							installApk((File) object);
							// 关闭自身，否则取消安装后，跳回此activity，会重新下载
							UpdateActivitySplash.this.finish();
							EIMUpdateManagerSplash.getInstance()
									.cancelDownloadAndroidApk();
						}

						@Override
						public void fail(Object object) {

							if (progressDialog != null) {
								progressDialog.dismiss();
								progressDialog = null;
							}
							showDownloadError("版本下载失败");
						}
					}, new EIMProgress() {

						@Override
						public void onProgressUpdate(Object... progress) {
							Integer _progress = (Integer) progress[0];
							if (progressDialog != null) {
								progressDialog.setProgress(_progress.intValue());
							}
						}
					});
		}

	}

	// 安装apk
	protected void installApk(File file) {
		Intent intent = new Intent();
		// 执行动作
		intent.setAction(Intent.ACTION_VIEW);
		// 执行的数据类型
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");// 编者按：此处Android应为android，否则造成安装不了
		startActivity(intent);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// 下载期间，暗屏，不睡眠
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this
				.getClass().getName());
		wakeLock.acquire();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
			wakeLock = null;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void showDownloadError(String error) {
		Log.e("VersionUpdate", "显示更新下载错误");
		// 关闭进度框
		if(progressDialog!=null&&progressDialog.isShowing())
			
		progressDialog.dismiss();
		// 提示更新失败
		AlertDialog dialog = new AlertDialog.Builder(this).setTitle("版本更新出错")
				.setMessage(error)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						UpdateActivitySplash.this.finish();
					}
				}).create();
		dialog.show();
	}
}


