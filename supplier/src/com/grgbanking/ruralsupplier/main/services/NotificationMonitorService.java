package com.grgbanking.ruralsupplier.main.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

//@SuppressLint("OverrideAbstract")
//public class NotificationMonitorService extends NotificationListenerService {
//    // 在收到消息时触发
//    @Override
//    public void onNotificationPosted(StatusBarNotification sbn) {
//
//        Bundle extras = sbn.getNotification().extras;
//        // 获取接收消息APP的包名
//        String notificationPkg = sbn.getPackageName();
//        // 获取接收消息的抬头
//        String notificationTitle = extras.getString(Notification.EXTRA_TITLE);
//        // 获取接收消息的内容
//        String notificationText = extras.getString(Notification.EXTRA_TEXT);
//
////        Log.i("XSL_Test", "Notification posted " + notificationTitle + " & " + notificationText);
//    }
//
//}
