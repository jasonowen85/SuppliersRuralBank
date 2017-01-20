package com.grgbanking.ruralsupplier.main.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by LiuPeng on 2016/9/20.
 */
public class SupplierLocationServices extends Service {

    //定位点信息
    public LatLng latlng;
    private LocationClient mLocationClient =null;//定位客户端
    public MyLocationListener mMyLocationListener = new MyLocationListener();;
    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private boolean isStop = false;
    List<LatLng> points = new ArrayList<>();
    private boolean isFirstLoc = true; // 是否首次定位
    LatLng p1;
    LatLng p2;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.setLocOption(setLocationClientOption());
        mLocationClient.registerLocationListener(mMyLocationListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 触发定时器
        if (!isStop) {
            Log.i("tag", "定时器启动");
            startTimer();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        if (mLocationClient!=null) {
            mLocationClient.stop();
        }
        super.onDestroy();
        // 停止定时器
        if (isStop) {
            Log.i("tag", "定时器服务停止");
            stopTimer();
        }
    }
    /**
     * 定时器 每隔一段时间执行一次
     */
    private void startTimer() {
        isStop = true;//定时器启动后，修改标识，关闭定时器的开关
        if (mTimer == null) {
            mTimer = new Timer();
        }
        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {

                @Override
                public void run() {
                    do {
                        try {
                            Log.d("tag", "isStop="+isStop);
                            Log.d("tag", "mMyLocationListener="+mMyLocationListener);
                            mLocationClient.start();
                            Log.d("tag", "mLocationClient.start()");
                            Log.d("tag", "mLocationClient=="+mLocationClient);
                            Thread.sleep(1000*60*30);//3秒后再次执行
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } while (isStop);

                }
            };
        }

        if (mTimer != null && mTimerTask != null) {
            Log.d("tag", "mTimer.schedule(mTimerTask, delay)");
            mTimer.schedule(mTimerTask, 0);//执行定时器中的任务
        }
    }
    /**
     * 停止定时器，初始化定时器开关
     */
    private void stopTimer() {

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        isStop = false;//重新打开定时器开关
        Log.d("tag", "isStop="+isStop);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 定位客户端参数设定，更多参数设置，查看百度官方文档
     * @return
     */
    private LocationClientOption setLocationClientOption() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setScanSpan(1000);//每隔1秒发起一次定位
        option.setCoorType("bd09ll");// 可选，默认gcj02，设置返回的定位结果坐标系
        option.setOpenGps(true);//是否打开gps
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到该描述，不设置则在4G情况下会默认定位到“天安门广场”
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要，不设置则拿不到定位点的省市区信息
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        /*可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        该参数若不设置，则在4G状态下，会出现定位失败，将直接定位到天安门广场
         */
        return option;
    }
    /**
     * 定位监听器
     * @author User
     *
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location==null) {
                return;
            }
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            latlng = new LatLng(lat, lng);
            //定位成功后对获取的数据依据需求自定义处理，这里只做log显示
            Log.d("tag", "latlng.lat="+lat);
            Log.d("tag", "latlng.lng="+lng);

            if (isFirstLoc) {
                isFirstLoc = false;
                p1 = p2 = latlng;
                points.add(p1);
            } else {
                LatLng p0 = p2;
                p2 = latlng;
                if(p0!=p2){
                    points.add(p2);
                }
            }
            //EventBus.getDefault().post(new EventLatLng(points));

            // 到此定位成功，没有必要反复定位
            // 应该停止客户端再发送定位请求
            if (mLocationClient.isStarted()) {
                mLocationClient.stop();
            }

        }

    }

}
