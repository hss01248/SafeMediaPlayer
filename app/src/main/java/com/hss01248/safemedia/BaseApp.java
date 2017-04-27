package com.hss01248.safemedia;

import android.app.Application;
import android.util.Log;

import com.orhanobut.logger.LogPrintStyle;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.Settings;

import es.dmoral.toasty.MyToast;

/**
 * Created by Administrator on 2017/4/27.
 */

public class BaseApp extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        MyToast.init(this,true,true);
        Logger.initialize(
                new Settings()
                        .setStyle(new LogPrintStyle())
                        .isShowMethodLink(true)
                        .isShowThreadInfo(false)
                        .setMethodOffset(0)
                        .setLogPriority(BuildConfig.DEBUG ? Log.VERBOSE : Log.ASSERT)
        );
    }
}
