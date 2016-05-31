package com.company.myapp.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.SeekBar;

import com.company.myapp.R;
import com.ghy.baseapp.base.AbsBaseActivity;
import com.ghy.baseapp.common.log.Log;
import com.ghy.baseapp.component.slidr.Slidr;
import com.ghy.baseapp.component.slidr.model.SlidrConfig;
import com.ghy.baseapp.helper.FileHelper;
import com.ghy.baseapp.helper.ToastHelper;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;

import butterknife.Bind;
import butterknife.OnClick;

public class FileDownloadTestActivity extends AbsBaseActivity {


    @Bind(R.id.down_seekBar)
    SeekBar downSeekBar;

    private String url = "http://c.cheyipai.com/app/cyp_c1_v2.3.1.apk";//文件下载地址
    private String filePath;//文件保存路径
    NotificationManager nm;
    Notification notification;
    int notificationId = 10000;

    private SlidrConfig mConfig;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_file_download_test;
    }

    @Override
    protected String getToolBarTitle() {
        return "文件下载测试";
    }

    @Override
    protected void init() {

        downSeekBar.setMax(100);

        final String path = FileHelper.getDownloadPath();
        filePath = path + "cheyipai.apk";
        Log.i("downloadPath-->>", filePath);

        Slidr.attach(this);
    }

    @OnClick(R.id.btn_down_start)
    public void startDownload() {
        startDownLoad(url, filePath);
        showNotification();
    }

    /**
     * 显示下载通知
     */
    private void showNotification() {
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification = new Notification(R.mipmap.ic_launcher, "开始下载", System.currentTimeMillis());
        notification.contentView = new RemoteViews(getPackageName(), R.layout.download_notify_layout);
        notification.contentView.setProgressBar(R.id.down_progressbar, 100, 0, false);
        Intent notificationIntent = new Intent(this, FileDownloadTestActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.contentIntent = contentIntent;
        showNotify();
    }

    private void showNotify() {
        nm.notify(notificationId, notification);
    }

    /**
     * 开始下载
     *
     * @param url
     * @param path
     */
    private void startDownLoad(String url, String path) {

        ToastHelper.getInstance().showToast("开始下载");
        FileDownloader.getImpl().create(url).setPath(path)
                .setListener(new MyFileDownLoadListener()).start();

    }

    /**
     * 下载监听
     */
    private class MyFileDownLoadListener extends FileDownloadListener {

        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            int progress = soFarBytes * 100 / totalBytes + 1;
            downSeekBar.setProgress(progress);
            notification.contentView.setProgressBar(R.id.down_progressbar, 100, progress, false);
            if (progress != 100) {
                notification.contentView.setTextViewText(R.id.down_app_number, "已完成" + progress + "%");
            } else {
                notification.contentView.setTextViewText(R.id.down_app_number, "下载完成");
            }
            showNotify();
        }

        @Override
        protected void blockComplete(BaseDownloadTask task) {

        }

        @Override
        protected void completed(BaseDownloadTask task) {
            ToastHelper.getInstance().showToast("下载完成");
            installApp();
        }

        @Override
        protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            ToastHelper.getInstance().showToast("下载失败");
            Log.e("download-->>", e.toString());
            nm.cancel(notificationId);
        }

        @Override
        protected void warn(BaseDownloadTask task) {

        }
    }

    /**
     * 下载完成调用安装
     */
    private void installApp() {
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(install);
        nm.cancel(notificationId);
    }

}
