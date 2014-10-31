package cn.wangjianlog.apkupdate.dialog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * 更新版本管理类
 * 
 */
public class EIMUpdateManagerSplash {

	private static EIMUpdateManagerSplash instance;

	private String downloadUrl;

	private AsyncTask<Object, Object, Object> _downloadTask;

	private EIMUpdateManagerSplash() {
	}

	public static EIMUpdateManagerSplash getInstance() {
		if (instance == null) {
			instance = new EIMUpdateManagerSplash();
		}
		return instance;
	}

	public void showToast(Activity activity, String msg) {
		Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
	}

	public void downloadAndroidApk(File file, final EIMCallBack callback,
			EIMProgress progress) {
		_downloadTask = downloadAndroidApk(file, downloadUrl, callback,
				progress);
	}
	
	public void cancelDownloadAndroidApk() {
		if (_downloadTask != null) {
			_downloadTask.cancel(true);
			_downloadTask = null;
		}
	}

	public AsyncTask<Object, Object, Object> downloadAndroidApk(File apkFile,
			final String downloadUrl, final EIMCallBack callback,
			final EIMProgress progress) {
		if(_downloadTask != null){
			return _downloadTask;
		}
		return new AsyncTask<Object, Object, Object>() {
			@Override
			protected Object doInBackground(Object... params) {
				FileOutputStream fos = null;
				BufferedInputStream bis = null;
				File apkFile = null;
				try{
				String _url = (String) params[0];
				// File _apkFile = (File) params[1];
				URL url = new URL(_url);

				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setDoInput(true);
				conn.setRequestMethod("GET");
				conn.connect();
				int code = conn.getResponseCode();

				if (code == 200) {
					int total = conn.getContentLength();
					InputStream is = conn.getInputStream();

					apkFile = new File(Environment
							.getExternalStorageDirectory().getAbsolutePath(),
							"renrenweidian.apk");

					if (!apkFile.exists()) {
						apkFile.createNewFile();
					}

					fos = new FileOutputStream(apkFile);
					bis = new BufferedInputStream(is);
					byte[] buffer = new byte[1024];
					int len;
					int total_while = 0;
					while ((len = bis.read(buffer)) != -1) {
						fos.write(buffer, 0, len);
						total_while += len;
						int pro = total_while * 100 / total;
						publishProgress(Integer.valueOf(pro));// 通知进度更新
					}
				}
					
				} catch (Exception e) {
					Log.e("error!", e.getMessage(), e);
					return e;
				} finally {
					try {
						if(fos!=null)fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						if(bis!=null)bis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
				return apkFile;
			}

			@Override
			protected void onPostExecute(Object obj) {
				if (obj instanceof Exception) {
					callback.fail(obj);
				} else {
					callback.success(obj);
				}
			}

			@Override
			protected void onProgressUpdate(Object... values) {
				super.onProgressUpdate(values);
				progress.onProgressUpdate(values);
			}
		}.execute(downloadUrl, apkFile);
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public interface EIMCallBack {
		
		public void success(Object object);

		public void fail(Object object);
	}

	public interface EIMProgress {
		public void onProgressUpdate(Object... progress);
	}

}
