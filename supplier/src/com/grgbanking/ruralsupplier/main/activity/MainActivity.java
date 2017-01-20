package com.grgbanking.ruralsupplier.main.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.grgbanking.ruralsupplier.DemoCache;
import com.grgbanking.ruralsupplier.NimApplication;
import com.grgbanking.ruralsupplier.R;
import com.grgbanking.ruralsupplier.api.ServerApi;
import com.grgbanking.ruralsupplier.common.dialog.CommonDialog;
import com.grgbanking.ruralsupplier.common.dialog.DialogHelper;
import com.grgbanking.ruralsupplier.config.preference.Preferences;
import com.grgbanking.ruralsupplier.login.LoginActivity;
import com.grgbanking.ruralsupplier.login.LogoutHelper;
import com.grgbanking.ruralsupplier.main.fragment.SessionListFragment;
import com.grgbanking.ruralsupplier.main.services.LocationServices;
import com.grgbanking.ruralsupplier.session.SessionHelper;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.netease.nim.uikit.LoginSyncDataStatusObserver;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 主界面
 * <p>
 * Created by huangjun on 2015/3/25.
 */
public class MainActivity extends UI implements View.OnClickListener {

    private static final String EXTRA_APP_QUIT = "APP_QUIT";
    private static final String TAG = MainActivity.class.getSimpleName();
    private SessionListFragment mainFragment;
    protected ImageView iv_message, iv_workorder, iv_me;


    public static void start(Context context) {
        start(context, null);
    }

    public static void start(Context context, Intent extras) {
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    // 注销
    public static void logout(Context context, boolean quit) {
        Intent extra = new Intent();
        extra.putExtra(EXTRA_APP_QUIT, quit);
        start(context, extra);
    }

    @Override
    protected boolean displayHomeAsUpEnabled() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ToolBarOptions options = new ToolBarOptions();
        options.titleId = R.string.message;
        setToolBarCenter(R.id.toolbar, options);
        toolbar.setNavigationIcon(null);
        onParseIntent();

        DialogMaker.showProgressDialog(this, null, getString(R.string.logining), true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        }).setCanceledOnTouchOutside(false);

        // 等待同步数据完成
        boolean syncCompleted = LoginSyncDataStatusObserver.getInstance().observeSyncDataCompletedEvent(new Observer<Void>() {
            @Override
            public void onEvent(Void v) {
                DialogMaker.dismissProgressDialog();
            }
        });

        Log.i(TAG, "sync completed = " + syncCompleted);
        if (!syncCompleted) {
            DialogMaker.showProgressDialog(MainActivity.this, getString(R.string.prepare_data)).setCanceledOnTouchOutside(false);
        } else {
            DialogMaker.dismissProgressDialog();
        }
        Intent service = new Intent(this, LocationServices.class);
        startService(service);
        onInit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent service = new Intent(this, LocationServices.class);
        stopService(service);
    }

    private void onInit() {
        iv_me = (ImageView) findViewById(R.id.iv_me);
        iv_message = (ImageView) findViewById(R.id.iv_message);
        iv_message.setImageDrawable(getResources().getDrawable(R.drawable.main_tab_item_home_focus));
        iv_workorder = (ImageView) findViewById(R.id.iv_workorder);
        iv_me.setOnClickListener(this);
        iv_workorder.setOnClickListener(this);
        // 加载主页面
        showMainFragment();

        LogUtil.ui("NIM SDK cache path=" + NIMClient.getSdkStorageDirPath());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        onParseIntent();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_btn:
                GlobalSearchActivity.start(MainActivity.this);
                break;
            case R.id.role_btn:
                String id = Preferences.getUserRoleids();
                String name = Preferences.getUserRolenames();

                if (id != null && !StringUtil.isEmpty(id)) {
                    final String[] ids = id.split(",");
                    String[] names = name.split(",");
                    final CommonDialog dialog = DialogHelper.getPinterestDialogCancelable(this);
                    dialog.setTitle("选择角色");
                    dialog.setNegativeButton(R.string.cancel, null);
                    dialog.setItemsWithoutChk(names, new AdapterView.OnItemClickListener() {

                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                    //loginRoleEdit.setText(parent.getAdapter().getItem(position).toString());
                                    String account = Preferences.getUserAccount();
                                    if (!TextUtils.isEmpty(account) && account.contains(NimApplication.sendApkName)) {
                                        account = account.substring(NimApplication.sendApkName.length());
                                    }
                                    ServerApi.changeRole(account, ids[position], new JsonHttpResponseHandler() {
                                        @Override
                                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                            String ret_code = null;
                                            try {
                                                ret_code = response.getString("ret_code");
                                                if (ret_code.equals("0")) {
                                                    Preferences.saveUserRole(ids[position]) ;
                                                    Intent intent = new Intent();
                                                    intent.setAction("role_changed");
                                                    sendBroadcast(intent);//发送广播
                                                } else if(ret_code.contains("0000")){  //重新登录
                                                    quitLoginUser();
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject obj) {
                                            LogUtil.e("", "change role fail:" + throwable.getMessage());
                                        }

                                        @Override
                                        public void onFailure(int statusCode, Header[] headers, String message, Throwable throwable) {
                                            LogUtil.e("", "change role :" + throwable.getMessage());
                                        }
                                    });
                                    dialog.dismiss();
                                }
                            }

                    );
                    dialog.show();
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 退出登录 注销用户信息 重新登录apk
     */
    private void quitLoginUser() {
        if (DemoCache.getUserid() == null) {
            DemoCache.clear();
            Preferences.clear();
            // 清理缓存&注销监听
            LogoutHelper.logout();
            NIMClient.getService(AuthService.class).logout();
            // 启动登录
            LoginActivity.start(MainActivity.this);
            finish();
            return;
        }
        ServerApi.logout(DemoCache.getUserid(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                DemoCache.clear();
                Preferences.clear();
                String ret_code = null;
                String ret_msg = null;
                try {
                    ret_code = response.getString("ret_code");
                    ret_msg = response.getString("ret_msg");
                    if (ret_code.equals("0")) {
                        DemoCache.clear();
                        Preferences.clear();
                        // 清理缓存&注销监听
                        LogoutHelper.logout();
                        NIMClient.getService(AuthService.class).logout();
                        // 启动登录
                        LoginActivity.start(MainActivity.this);
                        finish();
                    } else {
                        DemoCache.clear();
                        Preferences.clear();
                        Toast.makeText(MainActivity.this, ret_msg, Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(i);
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject obj) {
                LogUtil.e(TAG, "exit fail:" + throwable.getMessage());
                Toast.makeText(MainActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String message, Throwable throwable) {
                LogUtil.e(TAG, "exit fail:" + throwable.getMessage());
                Toast.makeText(MainActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onParseIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT)) {
            IMMessage message = (IMMessage) getIntent().getSerializableExtra(NimIntent.EXTRA_NOTIFY_CONTENT);
            switch (message.getSessionType()) {
                case P2P:
                    SessionHelper.startP2PSession(this, message.getSessionId());
                    break;
                case Team:
                    SessionHelper.startTeamSession(this, message.getSessionId());
                    break;
                default:
                    break;
            }
        } else if (intent.hasExtra(EXTRA_APP_QUIT)) {
            onLogout();
            return;
        } else if (intent.hasExtra(com.grgbanking.ruralsupplier.main.model.Extras.EXTRA_JUMP_P2P)) {
            Intent data = intent.getParcelableExtra(com.grgbanking.ruralsupplier.main.model.Extras.EXTRA_DATA);
            String account = data.getStringExtra(com.grgbanking.ruralsupplier.main.model.Extras.EXTRA_ACCOUNT);
            if (!TextUtils.isEmpty(account)) {
                SessionHelper.startP2PSession(this, account);
            }
        }
    }

    private void showMainFragment() {
        if (mainFragment == null && !isDestroyedCompatible()) {
            mainFragment = new SessionListFragment();
            switchFragmentContent(mainFragment);
        }
    }

    // 注销
    private void onLogout() {
        // 清理缓存&注销监听
        LogoutHelper.logout();

        // 启动登录
        LoginActivity.start(this);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_workorder:
                Intent i = new Intent(this, first_workorder_activity.class);
                i.putExtra("state", "001");
                startActivity(i);
                break;
            case R.id.iv_me:
                Intent i2 = new Intent(this, MeProfileActivity.class);
                startActivity(i2);
                break;

        }
    }
}
