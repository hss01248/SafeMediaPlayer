package com.hss01248.safemedia;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hss01248.mediaplayer.AudioPlayerManager;
import com.hss01248.mediaplayer.PlayerCallback;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/4/27 0027.
 */

public class PlayerActy extends Activity {
    @Bind(R.id.btn_start)
    Button btnStart;
    @Bind(R.id.btn_pause)
    Button btnPause;
    @Bind(R.id.btn_resume)
    Button btnResume;
    @Bind(R.id.btn_stop)
    Button btnStop;
    @Bind(R.id.btn_release)
    Button btnRelease;
    @Bind(R.id.tv_progress)
    TextView tvProgress;
    @Bind(R.id.seekbar)
    SeekBar seekbar;
    @Bind(R.id.tv_size)
    TextView tvSize;
    @Bind(R.id.btn_play_url1)
    Button btnPlayUrl1;
    @Bind(R.id.btn_play_url2)
    Button btnPlayUrl2;
    @Bind(R.id.btn_play_file)
    Button btnPlayFile;

    AudioPlayerManager manager;
    String url1 = "http://static.qxinli.com/srv/voice/201702281645261757.mp3";
    String url2 = "http://static.qxinli.com/srv/voice/201702051645337133.mp3";
    String file = "hahahah";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);
        ButterKnife.bind(this);

        initPlayer();



        initEvent();
    }

    private void initPlayer() {
        manager =  AudioPlayerManager.get(getApplicationContext());
        manager.setDataSource(url1)
                .setStartPosition(30000)
                .setCallback(ProxyTools.getShowMethodInfoProxy(new PlayerCallback() {
                    @Override
                    public void onPreparing(Object dataSource, AudioPlayerManager manager) {

                    }

                    @Override
                    public void onPlaying(Object dataSource, AudioPlayerManager manager) {

                    }

                    @Override
                    public void onPause(Object dataSource, AudioPlayerManager manager) {

                    }

                    @Override
                    public void onCompletion(Object dataSource, AudioPlayerManager manager) {

                    }

                    @Override
                    public void onStop(Object dataSource, AudioPlayerManager manager) {

                    }

                    @Override
                    public void onError(String msg,Object dataSource, AudioPlayerManager manager) {

                    }

                    @Override
                    public void onRelease(Object dataSource, AudioPlayerManager manager) {

                    }

                    @Override
                    public void onProgress(int progress, Object dataSource, AudioPlayerManager manager) {
                        seekbar.setProgress( progress);
                        tvProgress.setText(String.format("%d:%02d",progress/1000/60,progress/1000%60));

                    }

                    @Override
                    public void onSeeking(Object dataSource, AudioPlayerManager manager) {

                    }

                    @Override
                    public void onBufferingUpdate(int percent, AudioPlayerManager manager) {

                    }

                    @Override
                    public void onGetMaxDuration(int maxDuration) {
                        tvSize.setText(String.format("%d:%02d",maxDuration/1000/60,maxDuration/1000%60));
                        seekbar.setMax(maxDuration);
                    }
                })).start();
    }

    private void initEvent() {
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                manager.seekTo(seekBar.getProgress());
            }
        });

    }

    @OnClick({R.id.btn_start, R.id.btn_pause, R.id.btn_resume, R.id.btn_stop, R.id.btn_release,
            R.id.tv_progress, R.id.seekbar, R.id.tv_size, R.id.btn_play_url1, R.id.btn_play_url2, R.id.btn_play_file})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                manager.start();
                break;
            case R.id.btn_pause:
                manager.pause();
                break;
            case R.id.btn_resume:
                manager.resume();
                break;
            case R.id.btn_stop:
                manager.stop();
                break;
            case R.id.btn_release:
                manager.release();
                break;
            case R.id.btn_play_url1:
                manager.start(url1);
                break;
            case R.id.btn_play_url2:
                manager.start(url2);
                break;
            case R.id.btn_play_file:
                manager.start(file);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.releaseEveryThing();
    }
}
