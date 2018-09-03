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

    public static void login() {
        JMessageClient.login(String.valueOf(LocalConfig.mLoginUserID), JMESSAGE_PWD, new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                Log.e(TAG, "login " + i + " " + s);
                if (i == 0) {
                    enterChatRoom();
                }
            }
        });
    }

    public static void logout() {
        JMessageClient.logout();
    }

    public static void enterChatRoom() {
        ChatRoomManager.enterChatRoom(CHAT_ROOM_ID, new RequestCallback<Conversation>() {
            @Override
            public void gotResult(int i, String s, Conversation conversation) {
                Log.e(TAG, "enterChatRoom " + i + " " + s);
            }
        } );
    }

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
}
