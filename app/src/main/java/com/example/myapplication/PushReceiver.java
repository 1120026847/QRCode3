package com.example.myapplication;

import android.content.Context;
import android.content.Intent;

import cn.jpush.android.api.NotificationMessage;
import cn.jpush.android.service.JPushMessageReceiver;

public class PushReceiver extends JPushMessageReceiver {

    @Override
    public void onNotifyMessageOpened(Context context, NotificationMessage notificationMessage) {

        Intent intent = new Intent(context, TestActivity.class);
        intent.putExtra("extras",notificationMessage.notificationExtras);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
        context.startActivity(intent);
    }
}
