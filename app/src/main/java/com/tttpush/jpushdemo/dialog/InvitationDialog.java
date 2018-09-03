package com.tttpush.jpushdemo.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import com.tttpush.JPushDemo.R;
import com.tttpush.jpushdemo.JMessageManager;

/**
 * @author txw
 */
public class InvitationDialog extends Dialog {

    private EditText mEtUserName;
    private Button mBtnDone;

    public InvitationDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        setContentView(R.layout.layout_invitation);

        mEtUserName = findViewById(R.id.et_user_name);
        mBtnDone = findViewById(R.id.btn_done);

        mBtnDone.setOnClickListener(v -> {
            String userName = mEtUserName.getText().toString().trim();
            if (!TextUtils.isEmpty(userName)) {
                JMessageManager.createSingleCustomMessage(userName);
//                JMessageManager.createSingleTextMessage(userName, "单聊消息");
                dismiss();
            }
        });
    }


}
