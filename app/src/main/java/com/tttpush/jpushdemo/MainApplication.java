package com.tttpush.jpushdemo;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.tttpush.jpushdemo.callback.MyTTTRtcEngineEventHandler;
import com.wushuangtech.wstechapi.TTTRtcEngine;

import java.util.Random;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.im.android.api.JMessageClient;

public class MainApplication extends Application {

    public MyTTTRtcEngineEventHandler mMyTTTRtcEngineEventHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        Random mRandom = new Random();
        LocalConfig.mLoginUserID = mRandom.nextInt(999999);

        //1.设置SDK的回调接收类
        mMyTTTRtcEngineEventHandler = new MyTTTRtcEngineEventHandler(getApplicationContext());
        //2.创建SDK的实例对象
        // 音视频模式用a967ac491e3acf92eed5e1b5ba641ab7 纯音频模式用496e737d22ecccb8cfa780406b9964d0
        TTTRtcEngine mTTTEngine = TTTRtcEngine.create(getApplicationContext(), "a967ac491e3acf92eed5e1b5ba641ab7",
                mMyTTTRtcEngineEventHandler);
        if (mTTTEngine == null) {
            System.exit(0);
        }

        initJPush();
        initJMessage();
    }

    private void initJMessage() {
        JMessageClient.setDebugMode(true);
        JMessageClient.init(this);
        JMessageManager.register();
        getJpushAppkey();
    }

    /**
     * 获取JPUSH_APPKEY
     * @return
     */
    private void getJpushAppkey() {
        String jpush_appkey = null;
        try {
            ApplicationInfo applicationInfo =
                    this.getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            jpush_appkey = applicationInfo.metaData.getString("JPUSH_APPKEY");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        LocalConfig.JPUSH_APPKEY = jpush_appkey;
    }

    private void initJPush() {
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
    }

    public void setAppID(String mAppID) {
        TTTRtcEngine mTTTEngine = TTTRtcEngine.create(getApplicationContext(), mAppID,
                mMyTTTRtcEngineEventHandler);
        if (mTTTEngine == null) {
            System.exit(0);
        }
    }
}
