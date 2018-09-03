package com.tttpush.jpushdemo.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tttpush.JPushDemo.R;
import com.tttpush.jpushdemo.Helper.TTTRtcEngineHelper;
import com.tttpush.jpushdemo.JMessageManager;
import com.tttpush.jpushdemo.LocalConfig;
import com.tttpush.jpushdemo.LocalConstans;
import com.tttpush.jpushdemo.MainApplication;
import com.tttpush.jpushdemo.bean.JniObjs;
import com.tttpush.jpushdemo.bean.PermissionBean;
import com.tttpush.jpushdemo.callback.MyTTTRtcEngineEventHandler;
import com.tttpush.jpushdemo.dialog.VideoInfoDialog;
import com.tttpush.jpushdemo.jpush.ExampleUtil;
import com.tttpush.jpushdemo.test.TestUtils;
import com.tttpush.jpushdemo.utils.MyLog;
import com.tttpush.jpushdemo.utils.PermissionUtils;
import com.tttpush.jpushdemo.utils.SharedPreferencesUtil;
import com.wushuangtech.jni.NativeInitializer;
import com.wushuangtech.library.Constants;
import com.wushuangtech.library.GlobalConfig;
import com.wushuangtech.utils.PviewLog;
import com.wushuangtech.wstechapi.TTTRtcEngine;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.event.ChatRoomMessageEvent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Message;

import static com.tttpush.jpushdemo.LocalConfig.mLoginRoomID;
import static com.tttpush.jpushdemo.LocalConfig.mPullUrlPrefix;
import static com.wushuangtech.library.Constants.CLIENT_ROLE_ANCHOR;
import static com.wushuangtech.library.Constants.CLIENT_ROLE_AUDIENCE;
import static com.wushuangtech.library.Constants.CLIENT_ROLE_BROADCASTER;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_BAD_VERSION;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_NOEXIST;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_TIMEOUT;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_UNKNOW;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_VERIFY_FAILED;

public class SplashActivity extends BaseActivity {

    public static final int VIDEO_MODE = 1;
    public static final int AUDIO_MODE = 2;
    public static boolean isForeground = false;

    private int mRole = -1;
    private ProgressDialog mDialog;
    public static boolean mIsLoging;
    private EditText mRoomIDET;
    private RadioButton mHostBT, mAuthorBT, mAudienceBT;
    private MyLocalBroadcastReceiver mLocalBroadcast;
    private TTTRtcEngineHelper mTTTRtcEngineHelper;
    private VideoInfoDialog videoInfoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        ArrayList<PermissionBean> mPermissionList = new ArrayList<>();
        mPermissionList.add(new PermissionBean(Manifest.permission.WRITE_EXTERNAL_STORAGE, getResources().getString(R.string.permission_write_external_storage)));
        mPermissionList.add(new PermissionBean(Manifest.permission.CAMERA, getResources().getString(R.string.permission_camera)));
        mPermissionList.add(new PermissionBean(Manifest.permission.RECORD_AUDIO, getResources().getString(R.string.permission_record_audio)));
        mPermissionList.add(new PermissionBean(Manifest.permission.READ_PHONE_STATE, getResources().getString(R.string.permission_read_phone_state)));
        boolean isOk = PermissionUtils.checkPermission(this, new PermissionUtils.PermissionUtilsInter() {
            @Override
            public List<PermissionBean> getApplyPermissions() {
                return mPermissionList;
            }

            @Override
            public AlertDialog.Builder getTipAlertDialog() {
                return null;
            }

            @Override
            public Dialog getTipDialog() {
                return null;
            }

            @Override
            public AlertDialog.Builder getTipAppSettingAlertDialog() {
                return null;
            }

            @Override
            public Dialog getTipAppSettingDialog() {
                return null;
            }
        });

        if (isOk) {
            init();
        }

        initTestCode();

        registerMessageReceiver();
        //这里只是为了展示注册事件接受者接口的用法，实际上开发者可以在任意类中注册事件接收者
        //，而不仅仅在Activity中。 下同
        JMessageClient.registerEventReceiver(this);
    }

    private void init() {
        initView();
        mTTTRtcEngineHelper = new TTTRtcEngineHelper();
        // 读取保存的数据
        long roomID = (long) SharedPreferencesUtil.getParam(this, "RoomID", 0L);
        // 设置保存的数据
        if (roomID != 0) {
            String s = String.valueOf(roomID);
            mRoomIDET.setText(s);
            mRoomIDET.setSelection(s.length());
        }

        // 注册回调函数接收的广播
        mLocalBroadcast = new MyLocalBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyTTTRtcEngineEventHandler.TAG);
        registerReceiver(mLocalBroadcast, filter);
        MyLog.d("SplashActivity onCreate.... model : " + Build.MODEL);
        mDialog = new ProgressDialog(this);
        mDialog.setTitle("");
        mDialog.setMessage("正在进入房间...");

        videoInfoDialog = new VideoInfoDialog(mContext, R.style.NoBackGroundDialog);
    }

    private void initView() {
        mAuthorBT = findViewById(R.id.vice);
        mAudienceBT = findViewById(R.id.audience);
        mRoomIDET = findViewById(R.id.room_id);
        mHostBT = findViewById(R.id.host);
        TextView mVersion = findViewById(R.id.version);
        String string = getResources().getString(R.string.version_info);
        String result = String.format(string, TTTRtcEngine.getInstance().getVersion());
        mVersion.setText(result);

        TextView mSdkVersion = findViewById(R.id.sdk_version);
        mSdkVersion.setText("sdk version : " + NativeInitializer.getIntance().getVersion());

    }

    @Override
    protected void onStart() {
        super.onStart();
        isForeground = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isForeground = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        JMessageClient.unRegisterEventReceiver(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        MyLog.d("SplashActivity onDestroy....");
        TTTRtcEngine.destroy();
        try {
            unregisterReceiver(mLocalBroadcast);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public void onClickRoleButton(View v) {
        mHostBT.setChecked(false);
        mAuthorBT.setChecked(false);
        mAudienceBT.setChecked(false);

        JMessageManager.sendMessage();

        ((RadioButton) v).setChecked(true);
        switch (v.getId()) {
            case R.id.host:
                mRole = CLIENT_ROLE_ANCHOR;
                findViewById(R.id.set).setVisibility(View.VISIBLE);
                break;
            case R.id.vice:
                mRole = CLIENT_ROLE_BROADCASTER;
                findViewById(R.id.set).setVisibility(View.INVISIBLE);
                break;
            case R.id.audience:
                mRole = CLIENT_ROLE_AUDIENCE;
                findViewById(R.id.set).setVisibility(View.INVISIBLE);
                break;
            default:
                break;
        }
    }

    public void enterRoom(String roomId) {
        mRoomIDET.setText(roomId);
        onClickEnterButton(null);
    }

    public void onClickEnterButton(View v) {
        boolean checkResult = mTTTRtcEngineHelper.splashCheckSetting(this, mRoomIDET.getText().toString());
        if (!checkResult) {
            return;
        }

        if (mIsLoging) {
            return;
        }
        mIsLoging = true;

        // 重置直播房间内的参数
        LocalConfig.mAudience = 0;
        LocalConfig.mAuthorSize = 0;

        // 设置频道属性
        mTTTEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        if (LocalConfig.mRoomMode == VIDEO_MODE) {
            // 启用视频模式
            mTTTEngine.enableVideo();
            mTTTEngine.muteLocalVideoStream(false);
        }
        // app里的录屏功能需开启高音质，仅支持5.0以上版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTTTEngine.setHighQualityAudioParameters(true);
        }
        // -----以下为SDK测试代码，无需关注-----
        if (TestUtils.mTestDialog.mIsHightVoice) {
            mTTTEngine.setHighQualityAudioParameters(true);
        } else {
            mTTTEngine.setHighQualityAudioParameters(false);
        }
        // -----以上为SDK测试代码，无需关注-----
        TestUtils.setAddressAndPushUrl();
        // 设置进入直播房间的角色
        if (mRole == -1) {
            if (mHostBT.isChecked()) {
                mRole = CLIENT_ROLE_ANCHOR;
            } else if (mAuthorBT.isChecked()) {
                mRole = CLIENT_ROLE_BROADCASTER;
            } else if (mAudienceBT.isChecked()) {
                mRole = CLIENT_ROLE_AUDIENCE;
            }
        }
        LocalConfig.mRole = mRole;
        mTTTEngine.setClientRole(LocalConfig.mRole, null);
        // 拉流地址
        LocalConfig.mCDNAddress = mPullUrlPrefix + mLoginRoomID;
        // 保存配置
        SharedPreferencesUtil.setParam(this, "RoomID", mLoginRoomID);

        // 设置推流格式H264/H265
        if (videoInfoDialog.mCodingFormat == 0) {
            GlobalConfig.mPushUrl = LocalConfig.mPushUrlPrefix + mLoginRoomID + "?trans=1";
        } else {
            GlobalConfig.mPushUrl = LocalConfig.mPushUrlPrefix + mLoginRoomID;
        }

        if (LocalConfig.mExtraPushUrl != null)
            GlobalConfig.mPushUrl = LocalConfig.mExtraPushUrl;

        mTTTEngine.joinChannel("", String.valueOf(mLoginRoomID), LocalConfig.mLoginUserID);
        mDialog.show();
        return;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isOk = PermissionUtils.onRequestPermissionsResults(this, requestCode, permissions, grantResults);
        if (isOk) {
            init();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean isOk = PermissionUtils.onActivityResults(this, requestCode);
        if (isOk) {
            init();
        }
    }

    private class MyLocalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MyTTTRtcEngineEventHandler.TAG.equals(action)) {
                JniObjs mJniObjs = intent.getParcelableExtra(
                        MyTTTRtcEngineEventHandler.MSG_TAG);
                switch (mJniObjs.mJniType) {
                    case LocalConstans.CALL_BACK_ON_ENTER_ROOM:
                        mDialog.dismiss();
                        //界面跳转
                        startActivity(new Intent(mContext, MainActivity.class));
                        PviewLog.testPrint("joinChannel", "end");
                        mIsLoging = false;
                        break;
                    case LocalConstans.CALL_BACK_ON_ERROR:
                        mIsLoging = false;
                        mDialog.dismiss();
                        final int errorType = mJniObjs.mErrorType;
                        runOnUiThread(() -> {
                            MyLog.d("onReceive CALL_BACK_ON_ERROR errorType : " + errorType);
                            if (errorType == ERROR_ENTER_ROOM_TIMEOUT) {
                                Toast.makeText(mContext, getResources().getString(R.string.error_timeout), Toast.LENGTH_SHORT).show();
                            } else if (errorType == ERROR_ENTER_ROOM_UNKNOW) {
                                Toast.makeText(mContext, getResources().getString(R.string.error_unconnect), Toast.LENGTH_SHORT).show();
                            } else if (errorType == ERROR_ENTER_ROOM_VERIFY_FAILED) {
                                Toast.makeText(mContext, getResources().getString(R.string.error_verification_code), Toast.LENGTH_SHORT).show();
                            } else if (errorType == ERROR_ENTER_ROOM_BAD_VERSION) {
                                Toast.makeText(mContext, getResources().getString(R.string.error_version), Toast.LENGTH_SHORT).show();
                            } else if (errorType == ERROR_ENTER_ROOM_NOEXIST) {
                                Toast.makeText(mContext, getResources().getString(R.string.error_noroom), Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void onSetButtonClick(View v) {
        videoInfoDialog.okButton.setOnClickListener(v1 -> {
            mTTTEngine.setVideoMixerParams(videoInfoDialog.mBitRate, videoInfoDialog.mFrameRate, videoInfoDialog.mWidth, videoInfoDialog.mHeight);
            mTTTEngine.setAudioMixerParams(videoInfoDialog.mAudioBitRate, videoInfoDialog.mSamplingRate, videoInfoDialog.mChannels);

            videoInfoDialog.dismiss();
        });
        videoInfoDialog.show();

    }

    public void onClickModeButton(View v) {
        ((RadioButton) findViewById(R.id.videolink)).setChecked(false);
        ((RadioButton) findViewById(R.id.audiolink)).setChecked(false);
        ((RadioButton) v).setChecked(true);
        final MainApplication application = (MainApplication) getApplication();
        switch (v.getId()) {
            case R.id.audiolink:
                LocalConfig.mRoomMode = AUDIO_MODE;
                application.setAppID("496e737d22ecccb8cfa780406b9964d0");
                break;
            case R.id.videolink:
                LocalConfig.mRoomMode = VIDEO_MODE;
                application.setAppID("a967ac491e3acf92eed5e1b5ba641ab7");
                break;
            default:
                break;
        }
    }

    // -----以下为SDK测试代码，无需关注-----
    public void initTestCode() {
        TestUtils.initTestDialog(this);
        TestUtils.initTestBroadcast(this);
    }

    public void onTestButtonClick(View v) {
        Editable text = mRoomIDET.getText();
        String roomID = "";
        if (!TextUtils.isEmpty(text)) {
            roomID = text.toString();
        }

        TestUtils.mTestDialog.setRoomID(roomID);
        TestUtils.mTestDialog.setServerParams();
        TestUtils.mTestDialog.show();
    }
    // -----以上为SDK测试代码，无需关注-----

    //for receive customer msg from jpush server
    private MessageReceiver mMessageReceiver;
    public static final String MESSAGE_RECEIVED_ACTION = "com.example.jpushdemo.MESSAGE_RECEIVED_ACTION";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_EXTRAS = "extras";

    public void registerMessageReceiver() {
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(MESSAGE_RECEIVED_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);
    }

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
                    String messge = intent.getStringExtra(KEY_MESSAGE);
                    String extras = intent.getStringExtra(KEY_EXTRAS);
                    StringBuilder showMsg = new StringBuilder();
                    showMsg.append(KEY_MESSAGE + " : " + messge + "\n");
                    if (!ExampleUtil.isEmpty(extras)) {
                        showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
                    }
//                    setCostomMsg(showMsg.toString());
                    enterRoom(messge);
                }
            } catch (Exception e) {
            }
        }
    }

    public void onEvent(MessageEvent event) {
        Message msg = event.getMessage();
        Log.e("JMessage", "msg " + msg.getContent().toJson());
        switch (msg.getContentType()) {
            case text:
                break;
            case image:
                break;
            case custom:
                break;
            default:
                break;
        }
    }

    public void onEventMainThread(ChatRoomMessageEvent event) {
        List<Message> msgs = event.getMessages();
        int size = msgs.size();
        if (size > 0) {
            StringBuffer sb = new StringBuffer("聊天室消息 : \n");
            for (Message message : msgs) {
                if (message.getContent().getContentType() == ContentType.custom) {
                    Log.e("JMessage chatRoom", "join room " + message.getContent().toJson());
                    mRole = CLIENT_ROLE_BROADCASTER;
                    CustomContent customContent = (CustomContent) message.getContent();
                    String id = customContent.getStringValue("roomId");
                    enterRoom(id);
                    break;
                }
                sb.append(message.getContent().toJson());
                sb.append("\n");
            }
            Log.e("JMessage chatRoom", sb.toString());
//            mRoomIDET.setText(sb.toString());
        }
    }
}
