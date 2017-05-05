package com.hss01248.mediaplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

/**
 * Created by Administrator on 2017/5/5 0005.
 */

public class BecomingNoisyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
            Log.e("dd","onReceive ---------------------");
            AudioPlayerManager.get(context).pause();
        }
    }
}
