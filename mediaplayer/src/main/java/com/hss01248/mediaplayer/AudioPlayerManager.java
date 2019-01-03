package com.hss01248.mediaplayer;

import android.content.Context;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;

import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;

/**
 * Created by Administrator on 2017/4/27 0027.
 */

public class AudioPlayerManager implements IPlayer {

    private MediaPlayer player;

    public int getState() {
        return state;
    }

    private int state;
    PlayerCallback callback;
    private  static AudioPlayerManager instance;
    private Object dataSource;//string,Uri,filedesciptor
    Context context;
    int seekto;
    private Handler handler;
    private Runnable runnable;
    AudioManager am;
    AudioManager.OnAudioFocusChangeListener listener;
    int stateBeforeFocusChange;
    BecomingNoisyReceiver becomingNoisyReceiver;



    private boolean listeneAudioFocus;



    private AudioPlayerManager(){


    }

    public static AudioPlayerManager get(Context context){
        if(instance ==null){
            instance = new AudioPlayerManager();
            instance.context = context;
            instance.am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            instance.listener = new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    Log.d("AudioPlayerManager","onAudioFocusChange:"+focusChange);
                    if(instance == null){
                        Log.w("AudioPlayerManager","instance == null");
                        return;
                    }
                    if(!instance.listeneAudioFocus){
                        return;
                    }
                    if (focusChange == AUDIOFOCUS_LOSS_TRANSIENT){
                        //Log.e("dd"," AUDIOFOCUS_LOSS_TRANSIENT ---------------------");
                        if(instance.state == State.playing){
                            instance.stateBeforeFocusChange = State.playing;
                        }
                        instance.pause();
                        // Pause playback
                    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                        //Log.e("dd"," AUDIOFOCUS_GAIN ---------------------");
                        if(instance.stateBeforeFocusChange == State.playing){
                            instance.resume();
                        }

                        // Resume playback
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        //Log.e("dd"," AUDIOFOCUS_LOSS ---------------------");
                        //am.unregisterMediaButtonEventReceiver(RemoteControlReceiver);

                        instance.stop();
                        // Stop playback
                    }

                }
            };
            instance.registerHeadsetPlugReceiver();
        }
        return instance;
    }

    public AudioPlayerManager setListeneAudioFocus(boolean listeneAudioFocus) {
        this.listeneAudioFocus = listeneAudioFocus;
        return this;
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
        boolean isOld = newDataSource==null || newDataSource.equals(dataSource)  ;//这里有问题,如果是同一个音频,确实需要播两遍,第二遍会报错.
        //boolean isSameAsOldDataSource = newDataSource != null && newDataSource.equals(dataSource);
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
                //if(!isOld){
                //如果老的播完了,那么就重播一遍,一样的url,也需要reset
                player.reset();
                //}
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
            if(dataSource instanceof String){
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

                    int result = am.requestAudioFocus(listener,
                            // Use the music stream.
                            AudioManager.STREAM_MUSIC,
                            // Request permanent focus.
                            AudioManager.AUDIOFOCUS_GAIN);


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
            am.abandonAudioFocus(null);

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
        stop();
        if( state == State.stopped || state == State.idle || state == State.playCompleted){
            if(player != null){
                player.release();
                player = null;
            }
            state = State.realeased;
            callback.onRelease(dataSource,instance);
        }
    }

    /**
     * 释放所有资源,连instance都置为空
     */
    public void releaseEveryThing(){
        release();
        unregisterHeadsetPlugReceiver();
        instance=null;
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


    /**
     * 参考: http://blog.csdn.net/mu399/article/details/38516039
     * AudioManager.ACTION_AUDIO_BECOMING_NOISY: 只是针对有线耳机，或者无线耳机的手机断开连接的事件,无延迟.但监听不到有线耳机和蓝牙耳机的接入.
     */
    private   void registerHeadsetPlugReceiver() {
         instance.becomingNoisyReceiver = new BecomingNoisyReceiver();
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        instance.context.registerReceiver(instance.becomingNoisyReceiver, intentFilter);
    }

    private   void unregisterHeadsetPlugReceiver(){
        if(instance !=null && instance.becomingNoisyReceiver !=null){
            instance.context.unregisterReceiver(instance.becomingNoisyReceiver);
        }
    }








}
