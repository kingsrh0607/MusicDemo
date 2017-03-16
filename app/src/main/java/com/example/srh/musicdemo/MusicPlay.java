package com.example.srh.musicdemo;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.srh.musicdemo.bean.Song;

import java.text.SimpleDateFormat;

/**
 * Created by SRH on 2017/3/14.
 */

public class MusicPlay extends AppCompatActivity {

    public static final int CODE = 1;
    private TextView musicStatus, musicTime, musicTotal;
    private SeekBar seekBar;

    private Button btnPlayOrPause, btnPre, btnNext;
    private SimpleDateFormat time = new SimpleDateFormat("mm:ss");

    private boolean tag1 = false;
    private boolean tag2 = false;
    private MusicService musicService;
    private Song song;
    private TextView musicTitle;
    private float currentValue;
    private ObjectAnimator animator;
    private ImageView imageView;
    private int play;
    private MyReceiver receiver;


    private void bindServiceConnection() {
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("song", song);
        startService(intent);
        bindService(intent, serviceConnection, this.BIND_AUTO_CREATE);
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            musicService = ((MusicService.MyBinder) service).getService();
            musicService.reSet();
            if (tag2 == false) {
                handler.post(runnable);
                tag2 = true;
            }


            // musicTotal.setText(time.format(musicService.mediaPlayer.getDuration()));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
        }
    };


    @Override
    protected void onNewIntent(Intent intent) {


        super.onNewIntent(intent);


    }

    public Handler handler = new Handler();
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            musicTime.setText(time.format(musicService.mediaPlayer.getCurrentPosition()));
            seekBar.setProgress(musicService.mediaPlayer.getCurrentPosition());
            seekBar.setMax(musicService.mediaPlayer.getDuration());
            musicTotal.setText(time.format(musicService.mediaPlayer.getDuration()));
            musicTitle.setText(musicService.getCurSong().getSong().split("\\.")[0]);


            handler.postDelayed(runnable, 200);

        }
    };

    private void findViewById() {
        musicTime = (TextView) findViewById(R.id.MusicTime);
        musicTotal = (TextView) findViewById(R.id.MusicTotal);
        seekBar = (SeekBar) findViewById(R.id.MusicSeekBar);
        btnPlayOrPause = (Button) findViewById(R.id.BtnPlayorPause);
        btnPre = (Button) findViewById(R.id.BtnPre);
        btnNext = (Button) findViewById(R.id.BtnNext);
        musicStatus = (TextView) findViewById(R.id.MusicStatus);
        musicTitle = (TextView) findViewById(R.id.MusicTitle);
        musicTitle.setText(song.getSong().split("\\.")[0]);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_play_layout);

        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.RED);
        }


        song = (Song) getIntent().getSerializableExtra("song");
        findViewById();
        bindServiceConnection();
        myListener();

        receiver=new MyReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction("button_down");
        this.registerReceiver(receiver,filter);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser == true) {
                    musicService.mediaPlayer.seekTo(progress);

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public void startAnimation() {

        animator = ObjectAnimator.ofFloat(imageView, "rotation", currentValue, currentValue + 360);
        animator.setDuration(10000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        //animator.setRepeatCount(-1);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // TODO Auto-generated method stub
                // 监听动画执行的位置，以便下次开始时，从当前位置开始

                // animation.getAnimatedValue()为float类型
                currentValue = (float) animation.getAnimatedValue();
            }
        });
        animator.start();
    }

    private void myListener() {
        imageView = (ImageView) findViewById(R.id.Image);


        btnPlayOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPlayOrPause();//ziji


            }
        });

        btnPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.prev();
                handler.post(runnable);
                if(animator!=null) {
                    animator.cancel();
                    imageView.clearAnimation();
                }
                doAnimator();

            }
        });


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                musicService.next();
                handler.post(runnable);
                if(animator!=null) {
                    animator.cancel();
                    imageView.clearAnimation();
                }
                doAnimator();


            }
        });

    }


    private void setPlayOrPause() {

        if (musicService.mediaPlayer != null) {
            seekBar.setProgress(musicService.mediaPlayer.getCurrentPosition());
            seekBar.setMax(musicService.mediaPlayer.getDuration());
        }

        if (!musicService.mediaPlayer.isPlaying()) {
            btnPlayOrPause.setText("PAUSE");
            musicStatus.setText("Playing");
            musicService.playOrPause();

            startAnimation();

        } else {
            btnPlayOrPause.setText("PLAY");
            musicStatus.setText("Paused");
            musicService.playOrPause();
           animator.cancel();
            imageView.clearAnimation();
            musicService.tag = false;
        }
        if (tag2 == false) {
            handler.post(runnable);
            tag2 = true;
        }


    }


    private void setStop() {

    }


//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            moveTaskToBack(false);
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    public  void doAnimator(){
        if(musicService.mediaPlayer.isPlaying()){


            btnPlayOrPause.setText("PAUSE");
            musicStatus.setText("Playing");
            startAnimation();
        }else{
            btnPlayOrPause.setText("PLAY");
            musicStatus.setText("Paused");
            if(animator!=null) {
                animator.cancel();
                imageView.clearAnimation();
            }
        }
    }

    public class MyReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int flag = intent.getIntExtra("flag",-1);
            if(TextUtils.equals(action,"button_down")){

                if(flag==2||flag==3){//PRE OR NEXT
                    if(animator!=null) {
                        animator.cancel();
                        imageView.clearAnimation();
                    }

                }
                    doAnimator();

            }

        }
    }


}

