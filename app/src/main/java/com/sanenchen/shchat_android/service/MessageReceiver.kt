package com.sanenchen.shchat_android.service

import android.util.Log
import cn.jpush.android.service.JPushMessageReceiver

class MessageReceiver : JPushMessageReceiver() {

    override fun onNotifyMessageOpened(p0: android.content.Context?, p1: cn.jpush.android.api.NotificationMessage?) {
        super.onNotifyMessageOpened(p0, p1)
        p0!!.startActivity(p0.packageManager.getLaunchIntentForPackage(p0.packageName))
    }

}