package com.tttpush.jpushdemo;

import android.util.ArrayMap;
import android.util.Log;

import java.util.Map;

import cn.jpush.im.android.api.ChatRoomManager;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.RequestCallback;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.api.BasicCallback;

public class JMessageManager {

    private static final String JMESSAGE_PWD = "123456";
    private static final String TAG = "JMessageManager";
    private static final long CHAT_ROOM_ID = 12619421L;

    /**
     * 注册极光
     */
    public static void register() {
        JMessageClient.register(String.valueOf(LocalConfig.mLoginUserID), JMESSAGE_PWD, new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                Log.e(TAG, "register " + i + " " + s);
                if (i == 0) {
                    login();
                }
            }
        });
    }

    /**
     * 登录极光
     */
    public static void login() {
        JMessageClient.login(String.valueOf(LocalConfig.mLoginUserID), JMESSAGE_PWD, new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                Log.e(TAG, "login " + i + " " + s);
                if (i == 0) {
//                    enterChatRoom();
                }
            }
        });
    }

    /**
     * 退出极光
     */
    public static void logout() {
        JMessageClient.logout();
    }

    /**
     * 加入聊天室
     */
    public static void enterChatRoom() {
        ChatRoomManager.enterChatRoom(CHAT_ROOM_ID, new RequestCallback<Conversation>() {
            @Override
            public void gotResult(int i, String s, Conversation conversation) {
                Log.e(TAG, "enterChatRoom " + i + " " + s);
            }
        } );
    }

    /**
     * 发送文字聊天
     */
    public static void sendMessage() {
        Conversation conv = JMessageClient.getChatRoomConversation(CHAT_ROOM_ID);
        if (null == conv) {
            conv = Conversation.createChatRoomConversation(CHAT_ROOM_ID);
        }
        final Message msg = conv.createSendTextMessage("这是安卓的测试消息 id=" + LocalConfig.mLoginUserID);
        msg.setOnSendCompleteCallback(new BasicCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage) {
                Log.e(TAG, "sendMessage " + responseCode + " " + responseMessage);
                if (0 == responseCode) {
                } else {
                }
            }
        });
        JMessageClient.sendMessage(msg);
    }

    /**
     * 发送自定义消息
     */
    public static void sendJoinRoomMessage() {
        Conversation conv = JMessageClient.getChatRoomConversation(CHAT_ROOM_ID);
        if (null == conv) {
            conv = Conversation.createChatRoomConversation(CHAT_ROOM_ID);
        }
        Map<String, String> map = new ArrayMap<>();
        map.put("roomId", String.valueOf(LocalConfig.mLoginRoomID));
        map.put("msg", "邀请进入房间");
        final Message msg = conv.createSendCustomMessage(map);
        msg.setOnSendCompleteCallback(new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                Log.e(TAG, "sendJoinRoomMessage " + i + " " + s);
            }
        });
        JMessageClient.sendMessage(msg);
    }

    /**
     * 发送单人文字消息
     * @param userName
     * @param text
     */
    public static void createSingleTextMessage(String userName, String text) {
        Message msg = JMessageClient.createSingleTextMessage(userName, LocalConfig.JPUSH_APPKEY, text);
        msg.setOnSendCompleteCallback(new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                Log.e(TAG, "createSingleTextMessage" + i + " " + s);
            }
        });
        JMessageClient.sendMessage(msg);
    }

    /**
     * 发送单人自定义消息
     * @param userName
     */
    public static void createSingleCustomMessage(String userName) {
        Map<String, String> map = new ArrayMap<>();
        String roomId = String.valueOf(LocalConfig.mLoginRoomID);
        String s = "邀请进入房间 " + roomId;
        map.put("roomId", String.valueOf(LocalConfig.mLoginRoomID));
        map.put("msg", s);
        map.put("text", s);
        Message msg = JMessageClient.createSingleCustomMessage(userName, LocalConfig.JPUSH_APPKEY, map);
        msg.setOnSendCompleteCallback(new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                Log.e(TAG, "createSingleCustomMessage" + i + " " + s);
            }
        });
        JMessageClient.sendMessage(msg);
    }
}
