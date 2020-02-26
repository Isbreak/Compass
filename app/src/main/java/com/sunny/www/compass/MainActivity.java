package com.sunny.www.compass;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.sunny.www.compass.view.CompassView;
import com.trycatch.mysnackbar.Prompt;
import com.trycatch.mysnackbar.TSnackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

/**
 * 指南针主界面
 */
public class MainActivity extends AppCompatActivity {

    private final int PERMISSION_REQUEST_CODE = 100;

    /**
     * SharedPreferences name
     */
    private final String SP_CONFIG = "sp_config";
    /**
     * SharedPreferences key
     */
    private final String IS_VIBRATE = "IS_VIBRATE";
    private final String IS_FIRST_OPEN = "IS_FIRST_OPEN";

    private SharedPreferences sharedPreferences;

    private final String[] mDirectionText = new String[]{"北", "东北", "东", "东南", "南", "西南", "西", "西北"};

    private ViewGroup mViewGroup;

    private TextView mDirection;
    /**
     * 指南针罗盘自定义 View
     */
    private CompassView mCompassView;
    /**
     * 顶部提示悬浮框
     */
    private TSnackbar accuracyWarnSnackBar;

    private boolean isFirstOpen = true;

    private boolean isVibrate = true;

    private long lastTime = 0;

    private SensorManager mSensorManager;

    String direction = "未知";

    private float lastDirAngel = 0;

    @Override
    protected void onResume() {
        super.onResume();
        initSensor();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initView();
        initListener();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                    }, PERMISSION_REQUEST_CODE);
        } else {
            initLocation();
        }
    }

    private void initData() {
        sharedPreferences = getSharedPreferences(SP_CONFIG, Context.MODE_PRIVATE);
        isFirstOpen = sharedPreferences.getBoolean(IS_FIRST_OPEN, true);
        isVibrate = sharedPreferences.getBoolean(IS_VIBRATE, true);
    }

    private void initView() {
        mViewGroup = (ViewGroup) findViewById(android.R.id.content).getRootView();
        mDirection = findViewById(R.id.tv_dir);
        mCompassView = findViewById(R.id.compass);
        if (isFirstOpen) {
            showSnackBar();
        }
    }

    private void initListener() {
        mCompassView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((System.currentTimeMillis() - lastTime) > 2000) {
                    lastTime = System.currentTimeMillis();
                } else {
                    isVibrate = !isVibrate;
                    sharedPreferences.edit().putBoolean(IS_VIBRATE, isVibrate).apply();
                    Toast.makeText(MainActivity.this, isVibrate ? "打开震动" : "关闭震动", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private LocationManager locationManager;

    private void initLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        if (!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
            Toast.makeText(this, "请打开位置服务", Toast.LENGTH_SHORT).show();
            return;
        }

        long minSecond = 2 * 1000;
        long minDistance = 2;
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        updateLocation(location);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minSecond, minDistance, listener);

        GPSStatusImpl gpsStatusImpl = new GPSStatusImpl();
        locationManager.addGpsStatusListener(gpsStatusImpl);
    }

    public final LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                // GPS状态为可见时
                case LocationProvider.AVAILABLE:
                    Log.i("xml", "当前GPS状态为可见状态");
                    break;
                // GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    Log.i("xml", "当前GPS状态为服务区外状态");
                    break;
                // GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.i("xml", "当前GPS状态为暂停服务状态");
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            @SuppressLint("MissingPermission")
            Location location = locationManager.getLastKnownLocation(provider);
            updateLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    private class GPSStatusImpl implements GpsStatus.Listener {
        @Override
        public void onGpsStatusChanged(int event) {
            switch (event) {
                case GpsStatus.GPS_EVENT_FIRST_FIX:  //第一次定位
                    break;

                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:  //卫星状态改变
                {
                    //获取当前状态
                    @SuppressLint("MissingPermission")
                    GpsStatus gpsStatus = locationManager.getGpsStatus(null);
                    //获取卫星颗数的默认最大值
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    //创建一个迭代器保存所有卫星
                    Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                    int count = 0;
                    while (iters.hasNext() && count <= maxSatellites) {
                        GpsSatellite s = iters.next();
                        // 只有信噪比不为0时才算合格的卫星
                        if (s.getSnr() != 0) {
                            count++;
                        }
                    }


                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String curTime = dateFormat.format(new Date());
                    System.out.println(curTime + "-----卫星数量nun----" + count);
                    break;
                }

                case GpsStatus.GPS_EVENT_STARTED:   //定位启动
                    break;

                case GpsStatus.GPS_EVENT_STOPPED:   //定位结束
                    break;
            }
        }
    }

    Location lastLocation;

    private void updateLocation(Location location) {
        String result;
        double lat;
        double lon;

        if (location != null) {
            lat = location.getLatitude();
            lon = location.getLongitude();
            if (location.hasSpeed()) {
//                speed = location.getSpeed() * 3.6;
            } else {
                if (lastLocation != null) {
//                    speed = getMySpeed(lastLocation, location);
                }
            }

//            result = "纬度：" + lat + "\n经度：" + lon + "\n速度：" + String.format("%.2f", speed) + " km/h\n卫星数量：" + stars;
//            Log.i("xml", "显示结果 = " + result);
//            lastLocation = location;

            TextView tv = findViewById(R.id.tv_lon_lat);
            tv.setText(lat + " " + lon);
        }
    }

    private void showSnackBar() {
        TSnackbar snackBar = TSnackbar.make(mViewGroup, "点击两次次表盘，可关闭(打开)震动哦", TSnackbar.LENGTH_INDEFINITE, TSnackbar.APPEAR_FROM_TOP_TO_DOWN);
        snackBar.setAction("确认", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences.edit().putBoolean(IS_FIRST_OPEN, false).apply();
            }
        });
        snackBar.setPromptThemBackground(Prompt.SUCCESS);
        snackBar.show();
    }

    @SuppressWarnings("deprecation")
    private void initSensor() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        //注册监听
        mSensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float dirAngel = event.values[0];

            mCompassView.setDirectionAngle(dirAngel);
            direction = mDirectionText[((int) (dirAngel + 22.5f) % 360) / 45];
            mDirection.setText(direction);
            if (isVibrate && (int) dirAngel % 30 == 0 && (int) dirAngel != (int) lastDirAngel) {
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(20);
            }
            lastDirAngel = dirAngel;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            if (accuracy != SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM
                    && accuracy != SensorManager.SENSOR_STATUS_ACCURACY_HIGH) {
                if (accuracyWarnSnackBar == null) {
                    accuracyWarnSnackBar = TSnackbar.make(mViewGroup, "附近可能有电磁干扰，请参照蔡徐坤打篮球的姿势晃动手机校准指南针",
                            TSnackbar.LENGTH_INDEFINITE, TSnackbar.APPEAR_FROM_TOP_TO_DOWN);
                    accuracyWarnSnackBar.setPromptThemBackground(Prompt.WARNING);
                }
                accuracyWarnSnackBar.show();
            } else if (accuracyWarnSnackBar != null) {
                accuracyWarnSnackBar.dismiss();
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        //注销传感器
        mSensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    initLocation();
                } else {
                    Toast.makeText(MainActivity.this, "权限被拒绝", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}