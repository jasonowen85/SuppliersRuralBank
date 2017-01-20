package com.grgbanking.ruralsupplier.receiver;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.grgbanking.ruralsupplier.config.preference.Preferences;
import com.grgbanking.ruralsupplier.main.helper.CustomNotificationCache;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SDK通知消息广播接收器
 */
public class NotificationReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = context.getPackageName() + NimIntent.ACTION_RECEIVE_MSG;
        LogUtil.e("ruralsupplier", " 是否拦截 111 notification: ");
        if (action.equals(intent.getAction())) {

            // 处理自定义通知消息
            LogUtil.e("ruralsupplier", " 是否拦截  notification: " );
            //这个时候 判断一下  是否需要这一类型的消息 需要被过滤掉；

            List<IMMessage> messageList  = (ArrayList) intent.getSerializableExtra(NimIntent.EXTRA_BROADCAST_MSG);
            for(int i= 0; i<messageList.size(); i++){
                Map<String, Object> data  =  messageList.get(i).getRemoteExtension();

                if(data == null){
                    LogUtil.e("ruralsupplier 客户端 广播被取消了", " 拦截成功");
                    //直接保存数据   保存到本地数据
                    String blackAccount = Preferences.getBlackUserAccount();
                    if(TextUtils.isEmpty(blackAccount)){
                        Preferences.saveBlackUserAccount(messageList.get(i).getSessionId());
                    }else {
                        if(!blackAccount.contains(messageList.get(i).getSessionId())){
                            Preferences.saveBlackUserAccount(blackAccount + "," + messageList.get(i).getSessionId());
                        }
                    }
//                    listener.closeRegisterLocal(false);
                    return;
                }

                String contentPackName = (String) data.get("key1");
                if(context.getPackageName().equals(contentPackName)){
//                    listener.closeRegisterLocal(true);
                    LogUtil.e("ruralsupplier 客户端 ", " 这个广播 不拦截  ");
                } else {
//                    listener.closeRegisterLocal(false);
                    LogUtil.e("ruralsupplier 客户端 广播被取消了", " 拦截成功");
                    //直接保存数据   保存到本地数据
                    String blackAccount = Preferences.getBlackUserAccount();
                    if(TextUtils.isEmpty(blackAccount)){
                        Preferences.saveBlackUserAccount(messageList.get(i).getSessionId());
                    }else {
                        if(!blackAccount.contains(messageList.get(i).getSessionId())){
                            Preferences.saveBlackUserAccount(blackAccount + "," + messageList.get(i).getSessionId());
                        }
                    }
                }
            }
        }
    }


}
