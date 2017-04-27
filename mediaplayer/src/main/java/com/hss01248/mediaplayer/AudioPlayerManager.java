package com.hss01248.mediaplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Created by Administrator on 2017/4/27 0027.
 */

public class AudioPlayerManager implements IPlayer{

    private  MediaPlayer player;

    public int getState() {
        return state;
    }

    private int state;
    PlayerCallback callback;
    private  static AudioPlayerManager instance;
    private Object dataSource;//string,Uri,filedesciptor
    Context context;
    int seekto;
    private  Handler handler;
    private  Runnable runnable;

    private AudioPlayerManager(){


    }

    public static AudioPlayerManager get(){
        if(instance ==null){
            instance = new AudioPlayerManager();
        }
        return instance;
    }


    @Deprecated
    public AudioPlayerManager setContext(Context context){
        this.context = context;
        return this;
    }


    public AudioPlayerManager setCallback(PlayerCallback callback){
        this.callback = callback;
        return this;
    }

    public AudioPlayerManager setDataSource(Object dataSource){
        this.dataSource = dataSource;
        return this;
    }

    private void startSendingProgress(){
        if(state == State.playing){
            callback.onProgress(player.getCurrentPosition(),dataSource,instance);
            handler.postDelayed(runnable,1000);
        }else {
            handler.removeCallbacks(runnable);
        }
    }
    public AudioPlayerManager setStartPosition(int duration){
        seekto = duration;
        return this;
    }


    @Override
    public void start() {
        if(player ==null){
            player = new MediaPlayer();
        }
        if(handler ==null){
            handler = new Handler(Looper.getMainLooper());
        }
        if(runnable == null){
            runnable = new Runnable() {
                @Override
                public void run() {
                    startSendingProgress();
                }
            };
        }
        start(null);
    }

    @Override
    public void start(Object newDataSource) {
        //seekto =0;//对上一次的清零
        boolean isOld = newDataSource==null || newDataSource.equals(dataSource)  ;
        if(!isOld){
            dataSource = newDataSource;
        }else {
            //如果是旧的,那么就切换暂停和播放状态
            if(state == State.paused){
                resume();
                return;
            }else if(state == State.playing){
                pause();
                return;
            }
        }
        switch (state){
            case State.idle:
                break;
            case State.preparing:
                if(!isOld){
                    player.reset();
                }
                break;
            case State.prepared:
                if(!isOld){
                    player.reset();
                }
                break;
            case State.playing:
                if(!isOld){
                    player.reset();
                }
                break;
            case State.paused:
                if(!isOld){
                    player.reset();
                }
                break;
            case State.playCompleted:
                if(!isOld){
                    player.reset();
                }
                break;
            case State.stopped:
                if(!isOld){
                    player.reset();
                }
                break;
            case State.error:
                player.reset();
                break;
            case State.realeased:
                player = new MediaPlayer();
                break;
        }
        /* int preparing =2;
        int prepared =3;
        int playing = 4;
        int paused =5;

        int playCompleted =6;
        int stopped=7;
        int error =8;
        int realeased =9;*/





        state = State.idle;
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //Logger.object(dataSource);
        try {
            if(dataSource instanceof String ){
                String str = (String) dataSource;
                player.setDataSource(str);
            }else if(dataSource instanceof FileDescriptor){
                FileDescriptor source = (FileDescriptor) dataSource;
                player.setDataSource(source);

            }else if(dataSource instanceof Uri){
                Uri source = (Uri) dataSource;
                player.setDataSource(context,source);
            }

            state = State.preparing;
            player.prepareAsync();
            callback.onPreparing(dataSource,instance);
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    state = State.prepared;
                    callback.onGetMaxDuration(mp.getDuration());
                    mp.start();
                    state = State.playing;
                    callback.onPlaying(dataSource,instance);
                    startSendingProgress();
                    if(seekto> 0){
                        state = State.preparing;
                        mp.seekTo(seekto);
                        seekto = 0;
                    }
                }
            });
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    state = State.error;
                    callback.onError(what+"",dataSource,instance);
                    return false;
                }
            });
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    state = State.playCompleted;
                    callback.onCompletion(dataSource,instance);

                }
            });
            player.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {
                    state = State.playing;
                    callback.onPlaying(dataSource,instance);
                    startSendingProgress();

                }
            });
            player.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    callback.onBufferingUpdate(percent,instance);

                }
            });

        } catch (IOException e) {// player.setDataSource(str)抛出
            e.printStackTrace();
            state = State.error;
            callback.onError(e.getMessage(),dataSource,instance);
        }

    }

    @Override
    public void pause() {
        if(state == State.playing ){
            state = State.paused;
            player.pause();
            callback.onPause(dataSource,instance);
        }

    }

    @Override
    public void resume() {
        if(state == State.paused ){
            state = State.playing;
            player.start();
            callback.onPlaying(dataSource,instance);
            startSendingProgress();
        }
    }

    @Override
    public void stop() {
        if(state == State.playing || state == State.paused || state == State.prepared){
            state = State.stopped;
            player.stop();
            callback.onStop(dataSource,instance);
        }
    }

    @Override
    public void seekTo(int duration) {
        //seekto = duration;
        if(state == State.playing || state == State.paused || state == State.prepared){
            state = State.preparing;
            player.seekTo(duration);
            callback.onSeeking(dataSource,instance);
        }

    }

    @Override
    public void release() {
        if( state == State.stopped){
            player.release();
            player = null;
            state = State.realeased;
            callback.onRelease(dataSource,instance);
        }
    }

    public interface State{
        int idle = 0;
       // int initialized =1;
        int preparing =2;
        int prepared =3;
        int playing = 4;
        int paused =5;

        int playCompleted =6;
        int stopped=7;
        int error =8;
        int realeased =9;
    }




}
