package com.chant.notificationtest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class MediaPlayService extends Service {
    /**
     * 记录是否在播放
     */
    private boolean isPlaying = false;
    /**
     * 状态栏上的播放控制器
     */
    private RemoteViews mRemoteViews;
    /**
     * 状态栏上的播放控制器(展开状态)
     */
    private RemoteViews mBigRemoteViews;

    private Notification mNotification;

    private static final String REQUEST_CODE_ACTION = "MediaPlayerAction";
    private static final String KEY_BUTTON_ID = "ButtonId";
    private static final int BUTTON_PREV = 1;
    private static final int BUTTON_PLAY = 2;
    private static final int BUTTON_NEXT = 3;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i("chant", "[MediaPlayService onCreate]");
        showNotification();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("chant", "[MediaPlayService onStartCommand]");
        if (intent != null
                && intent.getAction() != null
                && intent.getAction().equals(REQUEST_CODE_ACTION)) {
            switch (intent.getIntExtra(KEY_BUTTON_ID, 0)) {
                case BUTTON_PREV:
                    Toast.makeText(getApplicationContext(), "prev", Toast.LENGTH_SHORT).show();
                    Log.i("chant", "prev");
                    break;
                case BUTTON_PLAY:
                    isPlaying = !isPlaying;
                    updateRemoteView();
                    Toast.makeText(getApplicationContext(), isPlaying ? "play" : "pause", Toast.LENGTH_SHORT).show();
                    Log.i("chant", "play");
                    break;
                case BUTTON_NEXT:
                    Toast.makeText(getApplicationContext(), "next", Toast.LENGTH_SHORT).show();
                    Log.i("chant", "next");
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i("chant", "[MediaPlayService onDestroy]");
        super.onDestroy();
    }

    /**
     * 带按钮的通知栏
     */
    public void showNotification() {
        NotificationCompat.Builder customBuilder = new NotificationCompat.Builder(this);
        mRemoteViews = getContentView(R.layout.custom_player);

        // Custom
        customBuilder.setContent(mRemoteViews)
                .setContentIntent(PendingIntent.getActivity(this, 1, new Intent(this, NotificationActivity.class), PendingIntent.FLAG_CANCEL_CURRENT))
                .setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
                .setTicker("正在播放")
//                .setPriority(Notification.PRIORITY_DEFAULT)// 设置该通知优先级
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher);

        mNotification = customBuilder.build();
        if (Build.VERSION.SDK_INT >= 16) {
            mBigRemoteViews = getContentView(R.layout.custom_player_big);
            mNotification.bigContentView = mBigRemoteViews;
        }
        mNotification.contentView = mRemoteViews;
        mNotification.flags = Notification.FLAG_ONGOING_EVENT;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, mNotification);
    }

    private RemoteViews getContentView(@LayoutRes int layoutResId) {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), layoutResId);
        remoteViews.setImageViewResource(R.id.book_cover, R.mipmap.ic_launcher);
        remoteViews.setTextViewText(R.id.book_title, "书名");
        remoteViews.setTextViewText(R.id.book_author, "作者");
        remoteViews.setTextViewText(R.id.button_play, isPlaying ? "暂停" : "播放");

        // 点击事件
        /* 上一首按钮 */
        Intent prevIntent = new Intent(getApplicationContext(), MediaPlayService.class);
        prevIntent.setAction(REQUEST_CODE_ACTION);
        prevIntent.putExtra(KEY_BUTTON_ID, BUTTON_PREV);
        PendingIntent prevPendingIntent = PendingIntent.getService(getApplicationContext(), BUTTON_PREV, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.button_prev, prevPendingIntent);
        /* 播放/暂停 按钮 */
        Intent playIntent = new Intent(getApplicationContext(), MediaPlayService.class);
        playIntent.setAction(REQUEST_CODE_ACTION);
        playIntent.putExtra(KEY_BUTTON_ID, BUTTON_PLAY);
        PendingIntent playPendingIntent = PendingIntent.getService(getApplicationContext(), BUTTON_PLAY, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.button_play, playPendingIntent);
        /* 下一首 按钮  */
        Intent nextIntent = new Intent(getApplicationContext(), MediaPlayService.class);
        nextIntent.setAction(REQUEST_CODE_ACTION);
        nextIntent.putExtra(KEY_BUTTON_ID, BUTTON_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getService(getApplicationContext(), BUTTON_NEXT, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.button_next, nextPendingIntent);
        return remoteViews;
    }

    private void updateRemoteView() {
        mRemoteViews.setTextViewText(R.id.button_play, isPlaying ? "暂停" : "播放");
        if (mBigRemoteViews != null) {
            mBigRemoteViews.setTextViewText(R.id.button_play, isPlaying ? "暂停" : "播放");
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, mNotification);
    }

}
