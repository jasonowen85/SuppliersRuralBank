package com.grgbanking.ruralsupplier;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import com.grgbanking.ruralsupplier.api.ApiHttpClient;
import com.grgbanking.ruralsupplier.common.AppConfig;
import com.grgbanking.ruralsupplier.common.util.crash.AppCrashHandler;
import com.grgbanking.ruralsupplier.common.util.sys.SystemUtil;
import com.grgbanking.ruralsupplier.config.preference.Preferences;
import com.grgbanking.ruralsupplier.config.preference.UserPreferences;
import com.grgbanking.ruralsupplier.contact.ContactHelper;
import com.grgbanking.ruralsupplier.main.activity.MainActivity;
import com.grgbanking.ruralsupplier.session.SessionHelper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;
import com.netease.nim.uikit.ImageLoaderKit;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.cache.FriendDataCache;
import com.netease.nim.uikit.cache.NimUserInfoCache;
import com.netease.nim.uikit.cache.TeamDataCache;
import com.netease.nim.uikit.contact.ContactProvider;
import com.netease.nim.uikit.contact.core.query.PinYin;
import com.netease.nim.uikit.session.viewholder.MsgViewHolderThumbBase;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimStrings;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.msg.MessageNotifierCustomization;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.model.IMMessageFilter;
import com.netease.nimlib.sdk.team.model.UpdateTeamAttachment;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NimApplication extends Application {
    private static NimApplication instance;
    public static String lastBranchId;
    public static String lastEndtime;
    public static String lastStarttime;
    public static String preMessageSessionId;
    //云信  账号 前缀 标识符 与 银行客户 通信；
    public static String sendApkName = "rcb";


    /**
     * 获得当前app运行的AppContext
     *
     * @return
     */
    public static NimApplication getInstance() {
        return instance;
    }

    public void onCreate() {
        super.onCreate();
        DemoCache.setContext(this);

        NIMClient.init(this, getLoginInfo(), getOptions());//getOptions()
        SDKInitializer.initialize(getApplicationContext());
        // crash handler
        AppCrashHandler.getInstance(this);
        CrashReport.initCrashReport(getApplicationContext(), "432eef5ff7", false);

        if (inMainProcess()) {
            // init pinyin
            PinYin.init(this);
            PinYin.validate();

            // 初始化UIKit模块
            initUIKit();
            // 注册通知消息过滤器
            registerIMMessageFilter();

            // 初始化消息提醒
            NIMClient.toggleNotification(UserPreferences.getNotificationToggle());//UserPreferences.getNotificationToggle()

            // 注册语言变化监听 默认关闭；
            registerLocaleReceiver(true);
//            NIMClient.getService(MsgServiceObserve.class).observeReceiveMessage(incomingMessageObserver, true);

            AppConfig.getAppConfig(this).initProperties(this);
            init();
        }
    }

    private void init() {
        // 初始化网络请求
        AsyncHttpClient client = new AsyncHttpClient();
        PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        client.setCookieStore(myCookieStore);
        ApiHttpClient.setHttpClient(client);
    }

    private LoginInfo getLoginInfo() {
        String account = Preferences.getUserAccount();
        String token = Preferences.getUserToken();
        String userid = Preferences.getUserid();
        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)) {
            DemoCache.setAccount(account.toLowerCase());
            DemoCache.setUserid(userid);
            return new LoginInfo(account, token);
        } else {
            return null;
        }
    }


    private SDKOptions getOptions() {
        SDKOptions options = new SDKOptions();

        // 如果将新消息通知提醒托管给SDK完成，需要添加以下配置。
        StatusBarNotificationConfig config = UserPreferences.getStatusConfig();
        if (config == null) {
            config = new StatusBarNotificationConfig();
        }
        // 点击通知需要跳转到的界面
        //config.notificationEntrance = WelcomeActivity.class;
        // config.notificationSmallIconId = R.drawable.ic_stat_notify_msg;

        // 通知铃声的uri字符串
        config.notificationSound = "android.resource://com.grgbanking.ruralsupplier/raw/msg";

        // 呼吸灯配置
        config.ledARGB = Color.GREEN;
        config.ledOnMs = 1000;
        config.ledOffMs = 1500;

        options.statusBarNotificationConfig = config;
        DemoCache.setNotificationConfig(config);
        UserPreferences.setStatusConfig(config);

        // 配置保存图片，文件，log等数据的目录
        String sdkPath = Environment.getExternalStorageDirectory() + "/" + getPackageName() + "/nim";
        options.sdkStorageRootPath = sdkPath;

        // 配置数据库加密秘钥
        options.databaseEncryptKey = "NETEASE";

        // 配置是否需要预下载附件缩略图
        options.preloadAttach = true;

        // 配置附件缩略图的尺寸大小，
        options.thumbnailSize = MsgViewHolderThumbBase.getImageMaxEdge();

        // 用户信息提供者
        options.userInfoProvider = infoProvider;
//
//        // 定制通知栏提醒文案（可选，如果不定制将采用SDK默认文案）
        options.messageNotifierCustomization = messageNotifierCustomization;

        return options;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    public boolean inMainProcess() {
        String packageName = getPackageName();
        String processName = SystemUtil.getProcessName(this);
        return packageName.equals(processName);
    }

    /**
     * 通知消息过滤器（如果过滤则该消息不存储不上报）
     */
    private void registerIMMessageFilter() {
        NIMClient.getService(MsgService.class).registerIMMessageFilter(new IMMessageFilter() {
            @Override
            public boolean shouldIgnore(IMMessage message) {
                if (UserPreferences.getMsgIgnore() && message.getAttachment() != null) {
                    if (message.getAttachment() instanceof UpdateTeamAttachment) {
                        UpdateTeamAttachment attachment = (UpdateTeamAttachment) message.getAttachment();
                        for (Map.Entry<TeamFieldEnum, Object> field : attachment.getUpdatedFields().entrySet()) {
                            if (field.getKey() == TeamFieldEnum.ICON) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        });
    }

    public void registerLocaleReceiver(boolean register) {
        if (register) {
            updateLocale();
            IntentFilter filter = new IntentFilter(Intent.ACTION_LOCALE_CHANGED);
            registerReceiver(localeReceiver, filter);
        } else {
            unregisterReceiver(localeReceiver);
        }
    }

    private BroadcastReceiver localeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
                updateLocale();
            }
        }
    };


    private void updateLocale() {
        NimStrings strings = new NimStrings();
        strings.status_bar_multi_messages_incoming = getString(R.string.nim_status_bar_multi_messages_incoming);
        strings.status_bar_image_message = getString(R.string.nim_status_bar_image_message);
        strings.status_bar_audio_message = getString(R.string.nim_status_bar_audio_message);
        strings.status_bar_custom_message = getString(R.string.nim_status_bar_custom_message);
        strings.status_bar_file_message = getString(R.string.nim_status_bar_file_message);
        strings.status_bar_location_message = getString(R.string.nim_status_bar_location_message);
        strings.status_bar_notification_message = getString(R.string.nim_status_bar_notification_message);
        strings.status_bar_ticker_text = getString(R.string.nim_status_bar_ticker_text);
        strings.status_bar_unsupported_message = getString(R.string.nim_status_bar_unsupported_message);
        strings.status_bar_video_message = getString(R.string.nim_status_bar_video_message);
        strings.status_bar_hidden_message_content = getString(R.string.nim_status_bar_hidden_msg_content);
        NIMClient.updateStrings(strings);
    }

    private void initUIKit() {
        // 初始化，需要传入用户信息提供者
        NimUIKit.init(this, infoProvider, contactProvider);

        // 会话窗口的定制初始化。
        SessionHelper.init();

        // 通讯录列表定制初始化
        ContactHelper.init();
    }

    private UserInfoProvider infoProvider = new UserInfoProvider() {
        @Override
        public UserInfo getUserInfo(String account) {
            UserInfo user = NimUserInfoCache.getInstance().getUserInfo(account);
            if (user == null) {
                NimUserInfoCache.getInstance().getUserInfoFromRemote(account, null);

            }

            return user;
        }

        @Override
        public int getDefaultIconResId() {
            return R.drawable.avatar_def;
        }

        @Override
        public Bitmap getTeamIcon(String teamId) {
            Drawable drawable = getResources().getDrawable(R.drawable.nim_avatar_group);
            if (drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            }
            return null;
        }

        @Override
        public Bitmap getAvatarForMessageNotifier(String account) {
            /**
             * 注意：这里最好从缓存里拿，如果读取本地头像可能导致UI进程阻塞，导致通知栏提醒延时弹出。
             */
            UserInfo user = getUserInfo(account);
            return (user != null) ? ImageLoaderKit.getNotificationBitmapFromCache(user) : null;
        }

        @Override
        public String getDisplayNameForMessageNotifier(String account, String sessionId, SessionTypeEnum sessionType) {
            String nick = null;
            if (sessionType == SessionTypeEnum.P2P) {
                nick = NimUserInfoCache.getInstance().getAlias(account);
            } else if (sessionType == SessionTypeEnum.Team) {
                nick = TeamDataCache.getInstance().getTeamNick(sessionId, account);
                if (TextUtils.isEmpty(nick)) {
                    nick = NimUserInfoCache.getInstance().getAlias(account);
                }
            }
            // 返回null，交给sdk处理。如果对方有设置nick，sdk会显示nick
            if (TextUtils.isEmpty(nick)) {
                return null;
            }

            return nick;
        }
    };

    private ContactProvider contactProvider = new ContactProvider() {
        @Override
        public List<UserInfoProvider.UserInfo> getUserInfoOfMyFriends() {
            List<NimUserInfo> nimUsers = NimUserInfoCache.getInstance().getAllUsersOfMyFriend();
            List<UserInfoProvider.UserInfo> users = new ArrayList<>(nimUsers.size());
            if (!nimUsers.isEmpty()) {
                users.addAll(nimUsers);
            }

            return users;
        }

        @Override
        public int getMyFriendsCount() {
            return FriendDataCache.getInstance().getMyFriendCounts();
        }

        @Override
        public String getUserDisplayName(String account) {
            return NimUserInfoCache.getInstance().getUserDisplayName(account);
        }
    };

    private MessageNotifierCustomization messageNotifierCustomization = new MessageNotifierCustomization() {
        @Override
        public String makeNotifyContent(String nick, IMMessage message) {
            return null; // 采用SDK默认文案
        }

        @Override
        public String makeTicker(String nick, IMMessage message) {
            return null; // 采用SDK默认文案
        }
    };

    /**
     * 消息接收观察者
     */
    Observer<List<IMMessage>> incomingMessageObserver = new Observer<List<IMMessage>>() {
        @Override
        public void onEvent(List<IMMessage> messages) {
            if (messages == null || messages.isEmpty()) {
                return;
            }
            //这里先判断 是否该用户的消息 消息 需要被过滤
            List<IMMessage> addedListItems = new ArrayList<>(messages.size());
            Log.e("messagerFragment", "消息来了");
            NotificationManager nm = (NotificationManager) getApplicationContext()
                    .getSystemService(getApplicationContext().NOTIFICATION_SERVICE);

            for (IMMessage message : messages) {
                Map<String, Object> data = message.getPushPayload();
                if(null == data){
                    //说明需要过滤通知栏；并且保存用户数据；
                    filterNotificationReceiver(message);
                    Log.e("messagerFragment", "被过滤  ");
                } else{
                    String packName = (String) data.get("key1");
                    if(!packName.equals(getPackageName())){
                        //说明需要过滤通知栏；并且保存用户数据；
                        filterNotificationReceiver(message);
                        Log.e("messagerFragment", "被过滤   ");

                    } else {
//                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
////                        Notification mNotification = new Notification(android.R.drawable.ic_menu_share, null, System.currentTimeMillis());
//                        int count = 1;
//                        String countNum = null;
//                        if(preMessageSessionId != null && message.getSessionId().equals(preMessageSessionId)){
//                            count++;
//                        }
//                        if(count > 1){
//                            countNum = "(" + count + "" + ")";
//                        } else {
//                            countNum = "";
//                        }
//                        mBuilder.setContentTitle(message.getSessionId() + countNum)//设置通知栏标题
//                                .setSmallIcon(R.drawable.actionbar_dark_logo_icon)
//                                .setLargeIcon(getBitmap(message.getSessionId()))
//                                .setContentText(message.getContent()) //设置通知栏显示内容</span>
//                                .setContentIntent(getDefalutIntent()) //设置通知栏点击意图
////                              .setNumber(number) //设置通知集合的数量
//                                .setTicker("") //通知首次出现在通知栏，带上升动画效果的
//                                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
//                                .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
//                                .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
//                                .setOngoing(false)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
//                                .setDefaults(Notification.DEFAULT_VIBRATE);//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
//                                        //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
////                                .setSmallIcon(R.drawable.ic_launcher);//设置通知小ICON
//                        Log.e("messagerFragment", "通知栏提醒 = " + "message.内容" + message.getContent() + "未读数目 =   " + count);
//                        Notification mNotification  = mBuilder.build();
//                        nm.notify(0001, mNotification);
//                        preMessageSessionId = message.getSessionId();
                    }
                }
            }
        }
    };

    private Bitmap getBitmap(String account){

        UserInfoProvider.UserInfo user = NimUserInfoCache.getInstance().getUserInfo(account);
        if (user == null) {
            NimUserInfoCache.getInstance().getUserInfoFromRemote(account, null);
        }
        Bitmap result =(user != null) ? ImageLoaderKit.getNotificationBitmapFromCache(user) : null;
        if(result == null) {
            result = ((BitmapDrawable) getApplicationContext().getResources().getDrawable(R.drawable.avatar_def)).getBitmap();
        }
        return result;
    }

    private PendingIntent getDefalutIntent(){
        PendingIntent pendingIntent= PendingIntent.getActivity(this, 1, new Intent(this, MainActivity.class), PendingIntent.FLAG_ONE_SHOT);
        return pendingIntent;
    }


    private void filterNotificationReceiver(IMMessage message){
        String account = message.getSessionId();
        String blackAccount = Preferences.getBlackUserAccount();
        if(TextUtils.isEmpty(blackAccount)){
            Preferences.saveBlackUserAccount(account);
        }else {
            if(!blackAccount.contains(account)){
                Preferences.saveBlackUserAccount(blackAccount + "," + account);
            }
        }
    }

    /**
     * 获取App安装包信息
     *
     * @return
     */
    public PackageInfo getPackageInfo() {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace(System.err);
        }
        if (info == null)
            info = new PackageInfo();
        return info;
    }

    /**
     * 判断当前版本是否兼容目标版本的方法
     *
     * @param VersionCode
     * @return
     */
    public static boolean isMethodsCompat(int VersionCode) {
        int currentVersion = android.os.Build.VERSION.SDK_INT;
        return currentVersion >= VersionCode;
    }

    /**
     * 获取cookie时传AppConfig.CONF_COOKIE
     *
     * @param key
     * @return
     */
    public String getProperty(String key) {
        String res = AppConfig.getAppConfig(this).get(key);
        return res;
    }
}
