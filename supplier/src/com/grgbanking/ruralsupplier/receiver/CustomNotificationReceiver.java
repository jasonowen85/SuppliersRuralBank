package com.grgbanking.ruralsupplier.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.grgbanking.ruralsupplier.config.preference.Preferences;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.grgbanking.ruralsupplier.main.helper.CustomNotificationCache;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.msg.model.CustomNotification;

import java.util.Map;

/**
 * 自定义通知消息广播接收器
 */
public class CustomNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = context.getPackageName() + NimIntent.ACTION_RECEIVE_CUSTOM_NOTIFICATION;
        if (action.equals(intent.getAction())) {

            // 从intent中取出自定义通知
            CustomNotification notification = (CustomNotification) intent.getSerializableExtra(NimIntent.EXTRA_BROADCAST_MSG);
             Map<String, Object> map= notification.getPushPayload();
            if(null == map){
                savaBlackAccount(notification);
            } else {
                String contentPackName = (String) map.get("key1");
                if(!context.getPackageName().equals(contentPackName)){
                    //不是指定的用户 保存到本地数据
                    savaBlackAccount(notification);
                }
            }
            try {
                JSONObject obj = JSONObject.parseObject(notification.getContent());
                if (obj != null && obj.getIntValue("id") == 2) {
                    // 加入缓存中
                    CustomNotificationCache.getInstance().addCustomNotification(notification);

                    // Toast
                    String content = obj.getString("content");
                    String tip = String.format("自定义消息[%s]：%s", notification.getFromAccount(), content);
                    Toast.makeText(context, tip, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                LogUtil.e("demo", e.getMessage());
            }

            // 处理自定义通知消息
            LogUtil.i("demo", "receive custom notification: " + notification.getContent() + " from :" + notification.getSessionId() + "/" + notification.getSessionType());
        }
    }

    private void savaBlackAccount(CustomNotification notification) {
        //  保存黑名单  用户到本地数据
        String blackAccount = Preferences.getBlackUserAccount();
        if(TextUtils.isEmpty(blackAccount)){
            Preferences.saveBlackUserAccount(notification.getSessionId());
        }else {
            if(!blackAccount.contains(notification.getSessionId())){
                Preferences.saveBlackUserAccount(blackAccount + "," + notification.getSessionId());
            }
        }
    }
}
