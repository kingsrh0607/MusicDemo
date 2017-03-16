package com.example.srh.musicdemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.srh.musicdemo.Utils.MusicUtils;
import com.example.srh.musicdemo.bean.Song;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;


/**
 * Created by SRH on 2017/3/14.
 */


public class MusicService extends Service {



    public MediaPlayer mediaPlayer;
    public boolean tag = false;
    private Notification notification;
    private int flag;
    private SimpleDateFormat time = new SimpleDateFormat("mm:ss");
    private boolean isPlaying = false;
    private RemoteViews views;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private List<Song> songs;
    private int i = 0;
    private Song curSong;
    private String ACTION_NOTIFICATION = "action_notification";
    private int  BUTTON_PLAY = 1;
    private int  BUTTON_PRE = 2;
    private int  BUTTON_NEXT = 3;
    public static final int CODE = 1;

    public final IBinder myBinder = new MyBinder();

    public Song getCurSong() {
        return curSong;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        flag = intent.getIntExtra("flag", -1);
        String action = intent.getAction();
        if (TextUtils.equals(action,ACTION_NOTIFICATION)) {
            if (flag == BUTTON_NEXT) {

                i = (i + 1) >= songs.size() ? 0 : i + 1;
                curSong = songs.get(i);
                mediaPlayer.stop();

               mediaPlayer = MediaPlayer.create(MusicService.this, Uri.parse(curSong.getPath()));
//                try {
//                    mediaPlayer.release();
//                    mediaPlayer.setDataSource(curSong.getPath());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                mediaPlayer.start();

                views.setTextViewText(R.id.textView, curSong.getSong());
                views.setTextViewText(R.id.textView2, curSong.getSinger() + "--" + time.format(curSong.getDuration()));
                views.setImageViewResource(R.id.btn_play, R.drawable.note_btn_pause);
                mNotificationManager.notify(233, mBuilder.build());
            } else if (flag == BUTTON_PLAY) {
                if (mediaPlayer.isPlaying()) {
                    isPlaying = false;
                    mediaPlayer.pause();
                    views.setImageViewResource(R.id.btn_play, R.drawable.note_btn_play);
                    mNotificationManager.notify(233, mBuilder.build());
                } else {
                    isPlaying = true;
                    //mediaPlayer = MediaPlayer.create(MusicService.this, Uri.parse(curSong.getPath()));
                    mediaPlayer.start();
                    views.setImageViewResource(R.id.btn_play, R.drawable.note_btn_pause);

                    mNotificationManager.notify(233, mBuilder.build());
                }
            } else {

                i = (i - 1) < 0 ? songs.size() - 1 : i - 1;
                curSong = songs.get(i);
                mediaPlayer.stop();
                mediaPlayer = MediaPlayer.create(MusicService.this, Uri.parse(curSong.getPath()));

//                try {
//                    mediaPlayer.release();
//                    mediaPlayer.setDataSource(curSong.getPath());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                    mediaPlayer.start();

                views.setTextViewText(R.id.textView, curSong.getSong());
                views.setTextViewText(R.id.textView2, curSong.getSinger() + "--" + time.format(curSong.getDuration()));
                views.setImageViewResource(R.id.btn_play, R.drawable.note_btn_pause);
                mNotificationManager.notify(233, mBuilder.build());

            }

            notifyActivity(flag);




        }


        return super.onStartCommand(intent, flags, startId);


    }

    private void notifyActivity(int flag) {

        Intent intent=new Intent();
        intent.putExtra("flag",flag);
        intent.setAction("button_down");
        sendBroadcast(intent);
    }


    @Override
    public void onCreate() {


        super.onCreate();


        init();




    }

    public void init() {

        songs = MusicUtils.getMusicData(getApplicationContext());
        i = MainActivity.songPosition;
        curSong = songs.get(i);


        try {
            mediaPlayer = MediaPlayer.create(MusicService.this, Uri.parse(curSong.getPath()));

            //mediaPlayer.setDataSource(curSong.getPath());
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);
        } catch (Exception e) {
            e.printStackTrace();
        }


        views = new RemoteViews(getPackageName(), R.layout.remote_view_layout);
        views.setImageViewResource(R.id.image1, R.drawable.default_pic);
        views.setTextViewText(R.id.textView, curSong.getSong());
        views.setTextViewText(R.id.textView2, curSong.getSinger() + "--" + time.format(curSong.getDuration()));

        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(ACTION_NOTIFICATION);
        intent.putExtra("flag", BUTTON_PLAY);
        PendingIntent pintent = PendingIntent.getService(this, BUTTON_PLAY, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.btn_play, pintent);

        intent.putExtra("flag", BUTTON_NEXT);
        pintent = PendingIntent.getService(this, BUTTON_NEXT, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.btn_next, pintent);


        intent.putExtra("flag", BUTTON_PRE);
        pintent = PendingIntent.getService(this, BUTTON_PRE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.btn_prev, pintent);


        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.default_pic);
        mBuilder.setContent(views);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(233, mBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.stopForeground(true);

    }

    public MusicService() {

    }





    public void playOrPause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();

            views.setImageViewResource(R.id.btn_play, R.drawable.note_btn_play);
            mNotificationManager.notify(233, mBuilder.build());

        } else {
            mediaPlayer.start();
            views.setImageViewResource(R.id.btn_play, R.drawable.note_btn_pause);
            mNotificationManager.notify(233, mBuilder.build());
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            try {
                mediaPlayer.reset();
                mediaPlayer = MediaPlayer.create(MusicService.this, Uri.parse(curSong.getPath()));
                mediaPlayer.prepare();
                mediaPlayer.seekTo(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    class MyBinder extends Binder {

        MusicService getService(){
            return  MusicService.this;
        }


    }

    public void next() {
        i = (i + 1) >= songs.size() ? 0 : i + 1;
        curSong = songs.get(i);
        MainActivity.songPosition++;
        mediaPlayer.stop();

       mediaPlayer = MediaPlayer.create(MusicService.this, Uri.parse(curSong.getPath()));
//        try {
//            mediaPlayer.release();
//            mediaPlayer.setDataSource(curSong.getPath());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

            mediaPlayer.start();
        views.setImageViewResource(R.id.btn_play, R.drawable.note_btn_pause);

        views.setTextViewText(R.id.textView, curSong.getSong());
        views.setTextViewText(R.id.textView2, curSong.getSinger() + "--" + time.format(curSong.getDuration()));
        mNotificationManager.notify(233, mBuilder.build());
    }

    public void prev() {
        i = (i - 1) < 0 ? songs.size() - 1 : i - 1;
        curSong = songs.get(i);
        MainActivity.songPosition--;
        mediaPlayer.stop();
        mediaPlayer = MediaPlayer.create(MusicService.this, Uri.parse(curSong.getPath()));
//        try {
//            mediaPlayer.release();
//            mediaPlayer.setDataSource(curSong.getPath());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

            mediaPlayer.start();
        views.setImageViewResource(R.id.btn_play, R.drawable.note_btn_pause);

        views.setTextViewText(R.id.textView, curSong.getSong());
        views.setTextViewText(R.id.textView2, curSong.getSinger() + "--" + time.format(curSong.getDuration()));
        mNotificationManager.notify(233, mBuilder.build());

    }

    public void play() {
        if(mediaPlayer.isPlaying()){
            isPlaying =false;
            mediaPlayer.pause();
        } else {
            isPlaying = true;
            mediaPlayer.start();
        }
    }

    public void reSet(){
        curSong = songs.get(MainActivity.songPosition);
        mediaPlayer.stop();

        mediaPlayer = MediaPlayer.create(MusicService.this, Uri.parse(curSong.getPath()));
//        try {
//            mediaPlayer.release();
//            mediaPlayer.setDataSource(curSong.getPath());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        if (mediaPlayer.isPlaying()) {
            views.setImageViewResource(R.id.btn_play, R.drawable.note_btn_pause);
        } else {
            views.setImageViewResource(R.id.btn_play, R.drawable.note_btn_play);
        }

        if (isPlaying) {
            mediaPlayer.start();
        }
        views.setTextViewText(R.id.textView, curSong.getSong());
        views.setTextViewText(R.id.textView2, curSong.getSinger() + "--" + time.format(curSong.getDuration()));
        mNotificationManager.notify(233, mBuilder.build());
    }

    public void buttonPlay(){
        if (mediaPlayer.isPlaying()) {
            isPlaying = false;
            mediaPlayer.pause();
            views.setImageViewResource(R.id.btn_play, R.drawable.note_btn_play);
            mNotificationManager.notify(233, mBuilder.build());
        } else {
            isPlaying = true;
            mediaPlayer = MediaPlayer.create(MusicService.this, Uri.parse(curSong.getPath()));
//            try {
//                mediaPlayer.release();
//                mediaPlayer.setDataSource(curSong.getPath());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            mediaPlayer.start();
            views.setImageViewResource(R.id.btn_play, R.drawable.note_btn_pause);
            mNotificationManager.notify(233, mBuilder.build());
        }
    }


}

