package cn.wangjianlog.apkupdate.notification;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import cn.wangjianlog.apkupdate.R;

public class FileMgrActivity extends Activity {
	private DownloadService.DownloadBinder binder;
	private ProgressBar progressBar;
	private Button cancel;
	private boolean binded;
	
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			int progress = msg.arg1;
			progressBar.setProgress(progress);
			if (progress == 100) {
				cancel.setEnabled(false);
			}
		};
	};
	
	private ServiceConnection conn = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (DownloadService.DownloadBinder) service;
			//监听进度信息
			listenProgress();
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filemgr);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        cancel = (Button) findViewById(R.id.cancel);
        
        if ("yes".equals(getIntent().getStringExtra("completed"))) {
        	//如果已完成,则不需再绑定service
        	progressBar.setProgress(100);
        	
        	cancel.setEnabled(false);
        } else {
        	//绑定service
            Intent intent = new Intent(this, DownloadService.class);
        	bindService(intent, conn, Context.BIND_AUTO_CREATE);
        	binded = true;
        }
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	//如果是绑定状态,则取消绑定
    	if (binded) {
    		unbindService(conn);
    	}
    }
    
    public void cancel(View view) {
    	//取消下载
    	binder.cancel();
    }

    /**
     * 监听进度信息
     */
	private void listenProgress() {
		new Thread() {
			public void run() {
				while (!binder.isCancelled() && binder.getProgress() <= 100) {
					int progress = binder.getProgress();
					Message msg = handler.obtainMessage();
					msg.arg1 = progress;
					handler.sendMessage(msg);
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		}.start();
	}
}
