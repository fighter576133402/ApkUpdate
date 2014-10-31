package cn.wangjianlog.apkupdate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import cn.wangjianlog.apkupdate.dialog.UpdateActivitySplash;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showUpadateDialog(MainActivity.this,"新版本更新","","");
    }
    /**
	 * 
	 * @param activity
	 * @param msg
	 * @param build
	 */
    Dialog dialog;
	public void showUpadateDialog(final Activity activity, String msg, final String build,final String url) {
		if (dialog != null) {
			dialog.dismiss();
		}
		dialog = new AlertDialog.Builder(activity).setTitle("应用更新").setMessage(msg).setCancelable(false).setPositiveButton("更新", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent();
				intent.putExtra("build", build);
				String myUrl = "http://bcs.apk.r1.91.com/data/upload/2014/10_20/18/com.tencent.mobileqq_180221.apk";
				intent.putExtra("downloadUrl", myUrl);
				intent.setClass(MainActivity.this, UpdateActivitySplash.class);
				startActivity(intent);
			}
		}).setNeutralButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).setCancelable(false).create();
		dialog.show();
	}

}
