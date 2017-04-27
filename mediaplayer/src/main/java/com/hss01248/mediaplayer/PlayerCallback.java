package com.hss01248.mediaplayer;

/**
 * Created by Administrator on 2017/4/27 0027.
 */

public interface PlayerCallback {


    void onPreparing(Object dataSource, AudioPlayerManager manager);
    void onPlaying(Object dataSource, AudioPlayerManager manager);
    void onPause(Object dataSource, AudioPlayerManager manager);
    void onCompletion(Object dataSource, AudioPlayerManager manager);
    void onStop(Object dataSource, AudioPlayerManager manager);
    void onError(String msg,Object dataSource, AudioPlayerManager manager);
    void onRelease(Object dataSource, AudioPlayerManager manager);

    void onGetMaxDuration(int maxDuration);
    void onProgress(int progress, Object dataSource, AudioPlayerManager manager);
    void onSeeking(Object dataSource, AudioPlayerManager manager);
    void onBufferingUpdate(int percent, AudioPlayerManager manager);




}
