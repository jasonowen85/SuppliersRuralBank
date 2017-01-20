package com.grgbanking.ruralsupplier.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.grgbanking.ruralsupplier.R;
import com.grgbanking.ruralsupplier.api.ApiHttpClient;
import com.grgbanking.ruralsupplier.api.ServerApi;
import com.grgbanking.ruralsupplier.common.bean.workOrder;
import com.grgbanking.ruralsupplier.common.util.sys.ImageUtils;
import com.grgbanking.ruralsupplier.common.util.widget.IconCenterEditText;
import com.grgbanking.ruralsupplier.config.preference.Preferences;
import com.grgbanking.ruralsupplier.login.LoginActivity;
import com.grgbanking.ruralsupplier.main.fragment.ToolUtil;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.model.ToolBarOptions;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class input_order_list_activity extends UI implements View.OnClickListener {
    private ImageView iv_drop_down1, iv_drop_down2, iv_drop_down3, iv_drop_down4, iv_drop_down5;
    private LinearLayout ll_maintenance, ll_haveinhand, ll_confirmed, ll_evaluation, ll_workorder;
    private FrameLayout fl_maintenance, fl_haveinhand, fl_confirmed, fl_evaluation, fl_workorder;
    private IconCenterEditText icet_search;
    private List<workOrder> datas1;
    private List<workOrder> datas2;
    private List<workOrder> datas3;
    private List<workOrder> datas4;
    private List<workOrder> datas5;

    private ListView lv1, lv2, lv3, lv4, lv5;
    private ListAdapt_1 mListAdapt1; //待维修
    private ListAdapt_2 mListAdapt2;//进行中
    private ListAdapt_3 mListAdapt3;//待确认
    private ListAdapt_4 mListAdapt4;//待评价
    private ListAdapt_5 mListAdapt5;//历史工单


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_order_list);
        ToolBarOptions options = new ToolBarOptions();
        options.titleId = R.string.workorder;
        setToolBar(R.id.toolbar, options);
        datas1 = new ArrayList<>();
        datas2 = new ArrayList<>();
        datas3 = new ArrayList<>();
        datas4 = new ArrayList<>();
        datas5 = new ArrayList<>();
        initID();
        getDate("");
    }

    private void initID() {
        ll_maintenance = (LinearLayout) findViewById(R.id.ll_maintenance);
        ll_maintenance.setOnClickListener(this);
        ll_haveinhand = (LinearLayout) findViewById(R.id.ll_haveinhand);
        ll_haveinhand.setOnClickListener(this);
        ll_confirmed = (LinearLayout) findViewById(R.id.ll_confirmed);
        ll_confirmed.setOnClickListener(this);
        ll_evaluation = (LinearLayout) findViewById(R.id.ll_evaluation);
        ll_evaluation.setOnClickListener(this);
        ll_workorder = (LinearLayout) findViewById(R.id.ll_workorder);
        ll_workorder.setOnClickListener(this);

        iv_drop_down1 = (ImageView) findViewById(R.id.iv_drop_down1);
        iv_drop_down2 = (ImageView) findViewById(R.id.iv_drop_down2);
        iv_drop_down3 = (ImageView) findViewById(R.id.iv_drop_down3);
        iv_drop_down4 = (ImageView) findViewById(R.id.iv_drop_down4);
        iv_drop_down5 = (ImageView) findViewById(R.id.iv_drop_down5);

        fl_maintenance = (FrameLayout) findViewById(R.id.fl_maintenance);
        fl_maintenance.setOnClickListener(this);
        fl_haveinhand = (FrameLayout) findViewById(R.id.fl_haveinhand);
        fl_haveinhand.setOnClickListener(this);
        fl_confirmed = (FrameLayout) findViewById(R.id.fl_confirmed);
        fl_confirmed.setOnClickListener(this);
        fl_evaluation = (FrameLayout) findViewById(R.id.fl_evaluation);
        fl_evaluation.setOnClickListener(this);
        fl_workorder = (FrameLayout) findViewById(R.id.fl_workorder);
        fl_workorder.setOnClickListener(this);
        icet_search = (IconCenterEditText) findViewById(R.id.icet_search4);
        icet_search.setOnSearchClickListener(new IconCenterEditText.OnSearchClickListener() {

            @Override
            public void onSearchClick(View view) {
                getDate(icet_search.getText().toString());
            }
        });

        lv1 = (ListView) findViewById(R.id.lv1);
        mListAdapt1 = new ListAdapt_1(input_order_list_activity.this);
        lv1.setAdapter(mListAdapt1);
        ToolUtil.ReCalListViewHeightBasedOnChildren(lv1);

        lv2 = (ListView) findViewById(R.id.lv2);
        mListAdapt2 = new ListAdapt_2(input_order_list_activity.this);
        lv2.setAdapter(mListAdapt2);
        ToolUtil.ReCalListViewHeightBasedOnChildren(lv2);

        lv3 = (ListView) findViewById(R.id.lv3);
        mListAdapt3 = new ListAdapt_3(input_order_list_activity.this);
        lv3.setAdapter(mListAdapt3);
        ToolUtil.ReCalListViewHeightBasedOnChildren(lv3);

        lv4 = (ListView) findViewById(R.id.lv4);
        mListAdapt4 = new ListAdapt_4(input_order_list_activity.this);
        lv4.setAdapter(mListAdapt4);
        ToolUtil.ReCalListViewHeightBasedOnChildren(lv4);

        lv5 = (ListView) findViewById(R.id.lv5);
        mListAdapt5 = new ListAdapt_5(input_order_list_activity.this);
        lv5.setAdapter(mListAdapt5);
        ToolUtil.ReCalListViewHeightBasedOnChildren(lv5);
    }

    private void getDate(String keyWord) {
        ServerApi.getSearchJobOrder(1, 10, Preferences.getUserid(), keyWord, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String ret_code = response.optString("ret_code");
                if (ret_code.equals("0")) {
                    JSONObject jsonObject = response.optJSONObject("lists");
                    JSONArray jsonArr = jsonObject.optJSONArray("lists");
                    datas1.clear();
                    datas2.clear();
                    datas3.clear();
                    datas4.clear();
                    datas5.clear();
                    for (int i = 0; i < jsonArr.length(); i++) {
                        workOrder order = new workOrder();
                        JSONObject jsonOb = new JSONObject();
                        try {
                            jsonOb = jsonArr.getJSONObject(i);
                            order.setId(jsonOb.getString("id"));//ID
                            order.setDeviceName(jsonOb.getString("deviceName"));//设备名
                            order.setSchedule(jsonOb.getString("schedule"));//工单进度操作
                            order.setScheduleStr(jsonOb.getString("scheduleStr"));//工单进度名称
                            order.setSituation(jsonOb.getString("situation"));//故障情况
                            order.setCreateTime(jsonOb.getString("createTime"));//工单创建时间
                            order.setState(jsonOb.getString("state"));//工单类别
                            String picUrls = jsonOb.getString("imgSerialNum");//图片路径集合
                            String[] arrs = picUrls.split(",");
                            for (String url : arrs) {
                                order.getImageUrls().add(url);
                            }

                            if ("001".equals(jsonOb.getString("state"))) {
                                datas1.add(order);
                            } else if ("003".equals(jsonOb.getString("state"))) {
                                datas3.add(order);
                            } else if ("002".equals(jsonOb.getString("state"))) {
                                datas2.add(order);
                            } else if ("004".equals(jsonOb.getString("state"))) {
                                datas4.add(order);
                            } else if ("005".equals(jsonOb.getString("state"))) {
                                datas5.add(order);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    mListAdapt1.notifyDataSetChanged();
                    mListAdapt2.notifyDataSetChanged();
                    mListAdapt3.notifyDataSetChanged();
                    mListAdapt4.notifyDataSetChanged();
                    mListAdapt5.notifyDataSetChanged();
                } else {
                    String ret_msg = response.optString("ret_msg");
                    Toast.makeText(input_order_list_activity.this, ret_msg, Toast.LENGTH_SHORT).show();
                    if (ret_code.equals("0011")){
                        Intent intent=new Intent(input_order_list_activity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject obj) {
                Toast.makeText(input_order_list_activity.this, "获取数据异常", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String message, Throwable throwable) {
                Toast.makeText(input_order_list_activity.this, "获取数据异常", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*关闭工单*/
    protected void closedOrder(String orderid) {
        ServerApi.closeOrder(orderid, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String ret_code = response.optString("ret_code");
                if (ret_code.equals("0")) {
                    Toast.makeText(input_order_list_activity.this, "关闭成功！", Toast.LENGTH_SHORT).show();
                } else {
                    String ret_msg = response.optString("ret_msg");
                    Toast.makeText(input_order_list_activity.this, ret_msg, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject obj) {
                Toast.makeText(input_order_list_activity.this, "获取数据异常", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String message, Throwable throwable) {
                Toast.makeText(input_order_list_activity.this, "获取数据异常", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* 转回*/
    private void forwardBack(String orderid) {
        ServerApi.forwardBack(orderid, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String ret_code = response.optString("ret_code");
                if (ret_code.equals("0")) {
                    Toast.makeText(input_order_list_activity.this, "转回成功！", Toast.LENGTH_SHORT).show();
                } else {
                    String ret_msg = response.optString("ret_msg");
                    Toast.makeText(input_order_list_activity.this, ret_msg, Toast.LENGTH_SHORT).show();
                    if (ret_code.equals("0011")){
                        Intent intent=new Intent(input_order_list_activity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject obj) {
                Toast.makeText(input_order_list_activity.this, "获取数据异常", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String message, Throwable throwable) {
                Toast.makeText(input_order_list_activity.this, "获取数据异常", Toast.LENGTH_SHORT).show();
            }
        });
    }  /* 2.2.7.接单和确认*/

    protected void comfirmOrder(String orderid, String userid) {
        ServerApi.comfirmOrder(orderid, userid, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String ret_code = response.optString("ret_code");
                if (ret_code.equals("0")) {
                    Toast.makeText(input_order_list_activity.this, "接单或者确认成功！", Toast.LENGTH_SHORT).show();
                } else {
                    String ret_msg = response.optString("ret_msg");
                    Toast.makeText(input_order_list_activity.this, ret_msg, Toast.LENGTH_SHORT).show();
                    if (ret_code.equals("0011")){
                        Intent intent=new Intent(input_order_list_activity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject obj) {
                Toast.makeText(input_order_list_activity.this, "获取数据异常", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String message, Throwable throwable) {
                Toast.makeText(input_order_list_activity.this, "获取数据异常", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == ll_maintenance) {
            if (fl_maintenance.getVisibility() == View.GONE) {
                iv_drop_down1.setImageResource(R.drawable.open2);
                fl_maintenance.setVisibility(View.VISIBLE);
            } else {
                iv_drop_down1.setImageResource(R.drawable.open);
                fl_maintenance.setVisibility(View.GONE);
            }
        } else if (v == ll_haveinhand) {
            if (fl_haveinhand.getVisibility() == View.GONE) {
                iv_drop_down2.setImageResource(R.drawable.open2);
                fl_haveinhand.setVisibility(View.VISIBLE);
            } else {
                iv_drop_down4.setImageResource(R.drawable.open);
                fl_haveinhand.setVisibility(View.GONE);
            }
        } else if (v == ll_confirmed) {
            if (fl_confirmed.getVisibility() == View.GONE) {
                iv_drop_down3.setImageResource(R.drawable.open2);
                fl_confirmed.setVisibility(View.VISIBLE);
            } else {
                iv_drop_down3.setImageResource(R.drawable.open);
                fl_confirmed.setVisibility(View.GONE);
            }
        } else if (v == ll_evaluation) {
            if (fl_evaluation.getVisibility() == View.GONE) {
                iv_drop_down4.setImageResource(R.drawable.open2);
                fl_evaluation.setVisibility(View.VISIBLE);
            } else {
                iv_drop_down4.setImageResource(R.drawable.open);
                fl_evaluation.setVisibility(View.GONE);
            }
        } else if (v == ll_workorder) {
            if (fl_workorder.getVisibility() == View.GONE) {
                iv_drop_down5.setImageResource(R.drawable.open2);
                fl_workorder.setVisibility(View.VISIBLE);
            } else {
                iv_drop_down5.setImageResource(R.drawable.open);
                fl_workorder.setVisibility(View.GONE);
            }
        }
    }

    class ViewHolder {
        TextView tv_schedule, tv_number, tv_deviceName, tv_situation, tv_createTime;
        LinearLayout ll_item;
        ImageView img1, img2, img3;
        ImageView iv_action1, iv_action2;
    }

    class ListAdapt_1 extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mLayoutInflater;

        public ListAdapt_1(Context context) {
            mContext = context;
            mLayoutInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return datas1.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder vHolder = null;
            if (convertView == null) {
                vHolder = new ViewHolder();
                convertView = mLayoutInflater.inflate(
                        R.layout.job_order_details_listview, null);
                vHolder.img1 = (ImageView) convertView.findViewById(R.id.img1);
                vHolder.img2 = (ImageView) convertView.findViewById(R.id.img2);
                vHolder.img3 = (ImageView) convertView.findViewById(R.id.img3);

                vHolder.tv_createTime = (TextView) convertView.findViewById(R.id.tv_createTime);
                vHolder.tv_situation = (TextView) convertView.findViewById(R.id.tv_situation);
                vHolder.tv_schedule = (TextView) convertView.findViewById(R.id.tv_schedule);
                vHolder.tv_deviceName = (TextView) convertView.findViewById(R.id.tv_deviceName);
                vHolder.tv_number = (TextView) convertView.findViewById(R.id.tv_number);

                vHolder.ll_item = (LinearLayout) convertView.findViewById(R.id.ll_item);
                vHolder.iv_action1 = (ImageView) convertView.findViewById(R.id.iv_action1);
                vHolder.iv_action2 = (ImageView) convertView.findViewById(R.id.iv_action2);

                vHolder.iv_action1.setImageResource(R.drawable.confirm_complete);
                vHolder.iv_action2.setImageResource(R.drawable.confirmation_delivery);
                convertView.setTag(vHolder);
            } else {
                vHolder = (ViewHolder) convertView.getTag();
            }
            vHolder.tv_deviceName.setText(datas1.get(position).getDeviceName());
            vHolder.tv_situation.setText(datas1.get(position).getSituation());
            vHolder.tv_schedule.setText(datas1.get(position).getScheduleStr());
            vHolder.tv_createTime.setText(datas1.get(position).getCreateTime());

            vHolder.ll_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent it = new Intent(input_order_list_activity.this, input_order_details_activity.class);
                    it.putExtra("orderId", datas1.get(position).getId().toString());
                    startActivity(it);
                }
            });

            if (datas1.get(position).getImageUrls().size() > 2) {
                Glide.with(input_order_list_activity.this)
                        .load(String.format(ApiHttpClient.API_URL_IMG, ImageUtils.getThumbnail(datas1.get(position).getImageUrls().get(2))))
                        .override(120, 120)
                        .into(vHolder.img3);
            }

            if (datas1.get(position).getImageUrls().size() > 1) {
                Glide.with(input_order_list_activity.this)
                        .load(String.format(ApiHttpClient.API_URL_IMG, ImageUtils.getThumbnail(datas1.get(position).getImageUrls().get(1))))
                        .override(120, 120)
                        .into(vHolder.img2);
            }

            if (datas1.get(position).getImageUrls().size() > 0) {
                Glide.with(input_order_list_activity.this)
                        .load(String.format(ApiHttpClient.API_URL_IMG, ImageUtils.getThumbnail(datas1.get(position).getImageUrls().get(0))))
                        .override(120, 120)
                        .into(vHolder.img1);
            }

            if (Preferences.getUserRole().equals("20001")) { //客服工程师   ---  action1 关闭， action2 转发
                vHolder.iv_action1.setImageResource(R.drawable.button6);
                vHolder.iv_action2.setImageResource(R.drawable.button5);
                vHolder.iv_action1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        closedOrder(datas1.get(position).getId());
                    }
                });
                vHolder.iv_action2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent it = new Intent(input_order_list_activity.this, forward_activity.class);
                        it.putExtra("orderId", datas1.get(position).getId());
                        startActivity(it);
                    }
                });
            } else if (Preferences.getUserRole().equals("20002")) { //服务主管   action1 转回客服  action2 转发工程师
                vHolder.iv_action1.setImageResource(R.drawable.button2);
                vHolder.iv_action2.setImageResource(R.drawable.button5);
                vHolder.iv_action1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        forwardBack(datas1.get(position).getId());
                    }
                });
                vHolder.iv_action2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent it = new Intent(input_order_list_activity.this, forward_activity.class);
                        it.putExtra("orderId", datas1.get(position).getId());
                        startActivity(it);
                    }
                });
            } else if (Preferences.getUserRole().equals("20003")) { //服务工程师   action1 转回主管 ，action2 接单
                vHolder.iv_action1.setImageResource(R.drawable.button7);
                vHolder.iv_action2.setImageResource(R.drawable.button8);
                vHolder.iv_action1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        forwardBack(datas1.get(position).getId());
                    }
                });
                vHolder.iv_action2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        comfirmOrder(datas1.get(position).getId(), Preferences.getUserid());
                    }
                });
            } else if (Preferences.getUserRole().equals("20004")) { //维修接口人   action2 接单
                vHolder.iv_action1.setVisibility(View.GONE);
                vHolder.iv_action2.setImageResource(R.drawable.button8);

                vHolder.iv_action2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        comfirmOrder(datas1.get(position).getId(), Preferences.getUserid());
                    }
                });
            } else {
                vHolder.iv_action1.setVisibility(View.GONE);
                vHolder.iv_action2.setVisibility(View.GONE);
            }
            return convertView;
        }
    }

    class ListAdapt_2 extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mLayoutInflater;

        public ListAdapt_2(Context context) {
            mContext = context;
            mLayoutInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return datas2.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder vHolder = null;
            if (convertView == null) {
                vHolder = new ViewHolder();
                convertView = mLayoutInflater.inflate(
                        R.layout.job_order_details_listview, null);
                vHolder.img1 = (ImageView) convertView.findViewById(R.id.img1);
                vHolder.img2 = (ImageView) convertView.findViewById(R.id.img2);
                vHolder.img3 = (ImageView) convertView.findViewById(R.id.img3);

                vHolder.tv_createTime = (TextView) convertView.findViewById(R.id.tv_createTime);
                vHolder.tv_situation = (TextView) convertView.findViewById(R.id.tv_situation);
                vHolder.tv_schedule = (TextView) convertView.findViewById(R.id.tv_schedule);
                vHolder.tv_deviceName = (TextView) convertView.findViewById(R.id.tv_deviceName);
                vHolder.tv_number = (TextView) convertView.findViewById(R.id.tv_number);

                vHolder.ll_item = (LinearLayout) convertView.findViewById(R.id.ll_item);
                vHolder.iv_action1 = (ImageView) convertView.findViewById(R.id.iv_action1);
                vHolder.iv_action2 = (ImageView) convertView.findViewById(R.id.iv_action2);

                vHolder.iv_action1.setImageResource(R.drawable.confirm_complete);
                vHolder.iv_action2.setImageResource(R.drawable.confirmation_delivery);
                convertView.setTag(vHolder);
            } else {
                vHolder = (ViewHolder) convertView.getTag();
            }
            vHolder.tv_deviceName.setText(datas2.get(position).getDeviceName());
            vHolder.tv_situation.setText(datas2.get(position).getSituation());
            vHolder.tv_schedule.setText(datas2.get(position).getScheduleStr());
            vHolder.tv_createTime.setText(datas2.get(position).getCreateTime());

            vHolder.ll_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent it = new Intent(input_order_list_activity.this, input_order_details_activity.class);
                    it.putExtra("orderId", datas2.get(position).getId().toString());
                    startActivity(it);
                }
            });

            if (datas2.get(position).getImageUrls().size() > 2) {
                Glide.with(input_order_list_activity.this)
                        .load(String.format(ApiHttpClient.API_URL_IMG, ImageUtils.getThumbnail(datas2.get(position).getImageUrls().get(2))))
                        .override(120, 120)
                        .into(vHolder.img3);
            }

            if (datas2.get(position).getImageUrls().size() > 1) {
                Glide.with(input_order_list_activity.this)
                        .load(String.format(ApiHttpClient.API_URL_IMG, ImageUtils.getThumbnail(datas2.get(position).getImageUrls().get(1))))
                        .override(120, 120)
                        .into(vHolder.img2);
            }

            if (datas2.get(position).getImageUrls().size() > 0) {
                Glide.with(input_order_list_activity.this)
                        .load(String.format(ApiHttpClient.API_URL_IMG, ImageUtils.getThumbnail(datas2.get(position).getImageUrls().get(0))))
                        .override(120, 120)
                        .into(vHolder.img1);
            }
            if (Preferences.getUserRole().equals("20003") && datas2.get(position).getSchedule().equals("5")) { //服务工程师   ---  action2 签到
                vHolder.iv_action1.setVisibility(View.GONE);
                vHolder.iv_action2.setImageResource(R.drawable.button11);//签到
                vHolder.iv_action2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, SignInActivity.class);
                        intent.putExtra("id", datas2.get(position).getId());
                        startActivity(intent);
                    }
                });
            } else if (Preferences.getUserRole().equals("20003") && datas2.get(position).getSchedule().equals("6")) { //服务工程师   ---  action2 确认完成
                vHolder.iv_action1.setVisibility(View.GONE);
                vHolder.iv_action2.setImageResource(R.drawable.button12);//确认完成
                vHolder.iv_action2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(input_order_list_activity.this, input_confirm_complete_activity.class);
                        intent.putExtra("jobOrderId", datas2.get(position).getId());
                        startActivity(intent);
                        input_order_list_activity.this.finish();
                    }
                });
            } else if (Preferences.getUserRole().equals("20003") && datas2.get(position).getSchedule().equals("7")) { //服务工程师   ---  action2 确认发货
                vHolder.iv_action1.setVisibility(View.GONE);
                vHolder.iv_action2.setImageResource(R.drawable.confirmation_delivery);//确认发货
                vHolder.iv_action2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(input_order_list_activity.this, input_confirmation_delivery_activity.class);
                        startActivity(intent);
                    }
                });
            } else if (Preferences.getUserRole().equals("20004")) { //维修接口人  ---  action2 确认到货 确认维修完成 确认发货
                vHolder.iv_action1.setVisibility(View.GONE);
                if (datas2.get(position).getSchedule().equals("13")) { //维修接口人已接单
                    vHolder.iv_action2.setImageResource(R.drawable.recived);//确认到货
                    vHolder.iv_action2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            comfirmOrder(datas2.get(position).getId(), Preferences.getUserid());
                        }
                    });
                } else if (datas2.get(position).getSchedule().equals("14")) { //维修接口人已确认到货
                    vHolder.iv_action2.setImageResource(R.drawable.completeconfirm);//确认维修完成
                    vHolder.iv_action2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            comfirmOrder(datas2.get(position).getId(), Preferences.getUserid());
//                                    Intent intent = new Intent(input_order_list_activity.this, input_confirm_complete_activity.class);
//                                    intent.putExtra("jobOrderId", datas.get(position).getId());
//                                    startActivity(intent);
                        }
                    });
                } else if (datas2.get(position).getSchedule().equals("15")) { //维修接口人已确认维修完成
                    vHolder.iv_action2.setImageResource(R.drawable.confirmation_delivery);//确认发货
                    vHolder.iv_action2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(input_order_list_activity.this, input_confirmation_delivery_activity.class);
                            intent.putExtra("jobOrderId", datas2.get(position).getId());
                            startActivity(intent);
                        }
                    });
                }

            } else {
                vHolder.iv_action1.setVisibility(View.GONE);
                vHolder.iv_action2.setVisibility(View.GONE);
            }
            return convertView;
        }
    }

    class ListAdapt_3 extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mLayoutInflater;

        public ListAdapt_3(Context context) {
            mContext = context;
            mLayoutInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return datas3.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder vHolder = null;
            if (convertView == null) {
                vHolder = new ViewHolder();
                convertView = mLayoutInflater.inflate(
                        R.layout.job_order_details_listview, null);
                vHolder.img1 = (ImageView) convertView.findViewById(R.id.img1);
                vHolder.img2 = (ImageView) convertView.findViewById(R.id.img2);
                vHolder.img3 = (ImageView) convertView.findViewById(R.id.img3);

                vHolder.tv_createTime = (TextView) convertView.findViewById(R.id.tv_createTime);
                vHolder.tv_situation = (TextView) convertView.findViewById(R.id.tv_situation);
                vHolder.tv_schedule = (TextView) convertView.findViewById(R.id.tv_schedule);
                vHolder.tv_deviceName = (TextView) convertView.findViewById(R.id.tv_deviceName);
                vHolder.tv_number = (TextView) convertView.findViewById(R.id.tv_number);

                vHolder.ll_item = (LinearLayout) convertView.findViewById(R.id.ll_item);
                vHolder.iv_action1 = (ImageView) convertView.findViewById(R.id.iv_action1);
                vHolder.iv_action2 = (ImageView) convertView.findViewById(R.id.iv_action2);

                vHolder.iv_action1.setVisibility(View.GONE);
                vHolder.iv_action2.setVisibility(View.GONE);
                convertView.setTag(vHolder);
            } else {
                vHolder = (ViewHolder) convertView.getTag();
            }
            vHolder.tv_deviceName.setText(datas3.get(position).getDeviceName());
            vHolder.tv_situation.setText(datas3.get(position).getSituation());
            vHolder.tv_schedule.setText(datas3.get(position).getScheduleStr());
            vHolder.tv_createTime.setText(datas3.get(position).getCreateTime());

            vHolder.ll_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent it = new Intent(input_order_list_activity.this, input_order_details_activity.class);
                    it.putExtra("orderId", datas3.get(position).getId().toString());
                    startActivity(it);
                }
            });

            if (datas3.get(position).getImageUrls().size() > 2) {
                Glide.with(input_order_list_activity.this)
                        .load(String.format(ApiHttpClient.API_URL_IMG, datas3.get(position).getImageUrls().get(2)))
                        .override(120, 120)
                        .into(vHolder.img3);
            }

            if (datas3.get(position).getImageUrls().size() > 1) {
                Glide.with(input_order_list_activity.this)
                        .load(String.format(ApiHttpClient.API_URL_IMG, datas3.get(position).getImageUrls().get(1)))
                        .override(120, 120)
                        .into(vHolder.img2);
            }

            if (datas3.get(position).getImageUrls().size() > 0) {
                Glide.with(input_order_list_activity.this)
                        .load(String.format(ApiHttpClient.API_URL_IMG, datas3.get(position).getImageUrls().get(0)))
                        .override(120, 120)
                        .into(vHolder.img1);
            }

            return convertView;
        }
    }

    class ListAdapt_4 extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mLayoutInflater;

        public ListAdapt_4(Context context) {
            mContext = context;
            mLayoutInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return datas4.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder vHolder = null;
            if (convertView == null) {
                vHolder = new ViewHolder();
                convertView = mLayoutInflater.inflate(
                        R.layout.job_order_details_listview, null);
                vHolder.img1 = (ImageView) convertView.findViewById(R.id.img1);
                vHolder.img2 = (ImageView) convertView.findViewById(R.id.img2);
                vHolder.img3 = (ImageView) convertView.findViewById(R.id.img3);

                vHolder.tv_createTime = (TextView) convertView.findViewById(R.id.tv_createTime);
                vHolder.tv_situation = (TextView) convertView.findViewById(R.id.tv_situation);
                vHolder.tv_schedule = (TextView) convertView.findViewById(R.id.tv_schedule);
                vHolder.tv_deviceName = (TextView) convertView.findViewById(R.id.tv_deviceName);
                vHolder.tv_number = (TextView) convertView.findViewById(R.id.tv_number);

                vHolder.ll_item = (LinearLayout) convertView.findViewById(R.id.ll_item);
                vHolder.iv_action1 = (ImageView) convertView.findViewById(R.id.iv_action1);
                vHolder.iv_action2 = (ImageView) convertView.findViewById(R.id.iv_action2);
                vHolder.iv_action1.setVisibility(View.GONE);
                vHolder.iv_action2.setVisibility(View.GONE);
                convertView.setTag(vHolder);
            } else {
                vHolder = (ViewHolder) convertView.getTag();
            }
            vHolder.tv_deviceName.setText(datas4.get(position).getDeviceName());
            vHolder.tv_situation.setText(datas4.get(position).getSituation());
            vHolder.tv_schedule.setText(datas4.get(position).getScheduleStr());
            vHolder.tv_createTime.setText(datas4.get(position).getCreateTime());

            vHolder.ll_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent it = new Intent(input_order_list_activity.this, input_order_details_activity.class);
                    it.putExtra("orderId", datas4.get(position).getId().toString());
                    startActivity(it);
                }
            });

            if (datas4.get(position).getImageUrls().size() > 2) {
                Glide.with(input_order_list_activity.this)
                        .load(String.format(ApiHttpClient.API_URL_IMG, datas4.get(position).getImageUrls().get(2)))
                        .override(120, 120)
                        .into(vHolder.img3);
            }

            if (datas4.get(position).getImageUrls().size() > 1) {
                Glide.with(input_order_list_activity.this)
                        .load(String.format(ApiHttpClient.API_URL_IMG, datas4.get(position).getImageUrls().get(1)))
                        .override(120, 120)
                        .into(vHolder.img2);
            }

            if (datas4.get(position).getImageUrls().size() > 0) {
                Glide.with(input_order_list_activity.this)
                        .load(String.format(ApiHttpClient.API_URL_IMG, datas4.get(position).getImageUrls().get(0)))
                        .override(120, 120)
                        .into(vHolder.img1);
            }

            return convertView;
        }
    }

    class ListAdapt_5 extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mLayoutInflater;

        public ListAdapt_5(Context context) {
            mContext = context;
            mLayoutInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return datas5.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder vHolder = null;
            if (convertView == null) {
                vHolder = new ViewHolder();
                convertView = mLayoutInflater.inflate(
                        R.layout.job_order_details_listview, null);
                vHolder.img1 = (ImageView) convertView.findViewById(R.id.img1);
                vHolder.img2 = (ImageView) convertView.findViewById(R.id.img2);
                vHolder.img3 = (ImageView) convertView.findViewById(R.id.img3);

                vHolder.tv_createTime = (TextView) convertView.findViewById(R.id.tv_createTime);
                vHolder.tv_situation = (TextView) convertView.findViewById(R.id.tv_situation);
                vHolder.tv_schedule = (TextView) convertView.findViewById(R.id.tv_schedule);
                vHolder.tv_deviceName = (TextView) convertView.findViewById(R.id.tv_deviceName);
                vHolder.tv_number = (TextView) convertView.findViewById(R.id.tv_number);

                vHolder.ll_item = (LinearLayout) convertView.findViewById(R.id.ll_item);
                vHolder.iv_action1 = (ImageView) convertView.findViewById(R.id.iv_action1);
                vHolder.iv_action2 = (ImageView) convertView.findViewById(R.id.iv_action2);
                vHolder.iv_action1.setVisibility(View.GONE);
                vHolder.iv_action2.setVisibility(View.GONE);
                convertView.setTag(vHolder);
            } else {
                vHolder = (ViewHolder) convertView.getTag();
            }
            vHolder.tv_deviceName.setText(datas5.get(position).getDeviceName());
            vHolder.tv_situation.setText(datas5.get(position).getSituation());
            vHolder.tv_schedule.setText(datas5.get(position).getScheduleStr());
            vHolder.tv_createTime.setText(datas5.get(position).getCreateTime());

            vHolder.ll_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent it = new Intent(input_order_list_activity.this, input_order_details_activity.class);
                    it.putExtra("orderId", datas5.get(position).getId().toString());
                    startActivity(it);
                }
            });

            if (datas5.get(position).getImageUrls().size() > 2) {
                Glide.with(input_order_list_activity.this)
                        .load(String.format(ApiHttpClient.API_URL_IMG, datas5.get(position).getImageUrls().get(2)))
                        .override(120, 120)
                        .into(vHolder.img3);
            }

            if (datas5.get(position).getImageUrls().size() > 1) {
                Glide.with(input_order_list_activity.this)
                        .load(String.format(ApiHttpClient.API_URL_IMG, datas5.get(position).getImageUrls().get(1)))
                        .override(120, 120)
                        .into(vHolder.img2);
            }

            if (datas5.get(position).getImageUrls().size() > 0) {
                Glide.with(input_order_list_activity.this)
                        .load(String.format(ApiHttpClient.API_URL_IMG, datas5.get(position).getImageUrls().get(0)))
                        .override(120, 120)
                        .into(vHolder.img1);
            }

            return convertView;
        }
    }
}
