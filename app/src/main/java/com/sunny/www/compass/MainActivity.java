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
import java.util.List;
import java.util.Locale;

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

    private TextView mDirectionTv;
    /**
     * 指南针罗盘自定义 View
     */
    private CompassView mCompassView;
    /**
     * 纬度
     */
    private TextView mLatTv;
    /**
     * 经度
     */
    private TextView mLonTv;
    /**
     * 速度
     */
    private TextView mSpeedTv;
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

    private LocationManager locationManager;

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
        checkLocationPermission();
    }

    @SuppressWarnings("deprecation")
    private void initSensor() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        //注册监听
        mSensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void initData() {
        sharedPreferences = getSharedPreferences(SP_CONFIG, Context.MODE_PRIVATE);
        isFirstOpen = sharedPreferences.getBoolean(IS_FIRST_OPEN, true);
        isVibrate = sharedPreferences.getBoolean(IS_VIBRATE, true);
    }

    private void initView() {
        mViewGroup = (ViewGroup) findViewById(android.R.id.content).getRootView();
        mDirectionTv = findViewById(R.id.tv_dir);
        mCompassView = findViewById(R.id.compass);
        mLatTv = findViewById(R.id.tv_lat);
        mLonTv = findViewById(R.id.tv_lon);
        mSpeedTv = findViewById(R.id.tv_speed);
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
                    Toast.makeText(MainActivity.this, isVibrate ? "打开震动" : "关闭震动",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                    }, PERMISSION_REQUEST_CODE);
        } else {
            initLocation();
        }
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float dirAngel = event.values[0];

            mCompassView.setDirectionAngle(dirAngel);
            direction = mDirectionText[((int) (dirAngel + 22.5f) % 360) / 45];
            mDirectionTv.setText(direction);
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
                    accuracyWarnSnackBar = TSnackbar.make(mViewGroup,
                            "附近可能有电磁干扰，请参照蔡徐坤打篮球的姿势晃动手机校准指南针",
                            TSnackbar.LENGTH_INDEFINITE, TSnackbar.APPEAR_FROM_TOP_TO_DOWN);
                    accuracyWarnSnackBar.setPromptThemBackground(Prompt.WARNING);
                }
                accuracyWarnSnackBar.show();
            } else if (accuracyWarnSnackBar != null) {
                accuracyWarnSnackBar.dismiss();
            }
        }
    };

    @SuppressLint("MissingPermission")
    private void initLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            List<String> providers = locationManager.getProviders(true);
            String locationProvider;
            Location location;
            if (providers.contains(LocationManager.GPS_PROVIDER)) {
                locationProvider = LocationManager.GPS_PROVIDER;
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
                locationProvider = LocationManager.NETWORK_PROVIDER;
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            } else {
                Toast.makeText(this, "请打开位置服务", Toast.LENGTH_SHORT).show();
                return;
            }

            updateLocation(location);

            locationManager.requestLocationUpdates(locationProvider, 2000, 2,
                    locationListener);
            // todo deprecated function
            locationManager.addGpsStatusListener(gpsStatusListener);
        }
    }

    public final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    private GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {

        @Override
        public void onGpsStatusChanged(int event) {
            switch (event) {
                case GpsStatus.GPS_EVENT_FIRST_FIX:  //第一次定位
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    // 卫星状态改变
                    // 获取当前状态
                    @SuppressLint("MissingPermission")
                    GpsStatus gpsStatus = locationManager.getGpsStatus(null);
                    // 获取卫星颗数的默认最大值
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    // 创建一个迭代器保存所有卫星
                    Iterator<GpsSatellite> iterator = gpsStatus.getSatellites().iterator();
                    int count = 0;
                    while (iterator.hasNext() && count <= maxSatellites) {
                        GpsSatellite s = iterator.next();
                        // 只有信噪比不为0时才算合格的卫星
                        if (s.getSnr() != 0) {
                            count++;
                        }
                    }

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            Locale.CHINA);
                    String curTime = dateFormat.format(new Date());
                    Log.e("gaozy", curTime + "-----卫星数量----" + count);
                    break;
                case GpsStatus.GPS_EVENT_STARTED:   //定位启动
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:   //定位结束
                    break;
            }
        }
    };

    private void updateLocation(Location location) {
        double lat;
        double lon;
        double speed;

        if (location != null) {
            lat = location.getLatitude();
            lon = location.getLongitude();
            if (location.hasSpeed()) {
                mLatTv.setText(String.format(getString(R.string.format_float_5), lat));
                mLonTv.setText(String.format(getString(R.string.format_float_5), lon));
                speed = location.getSpeed() * 3.6;
                mSpeedTv.setText(String.format(getString(R.string.format_float_2), speed));
            }
        } else {
            mLatTv.setText("---");
            mLonTv.setText("---");
        }
    }

    private void showSnackBar() {
        TSnackbar snackBar = TSnackbar.make(mViewGroup, "点击两次次表盘，可关闭(打开)震动哦",
                TSnackbar.LENGTH_INDEFINITE, TSnackbar.APPEAR_FROM_TOP_TO_DOWN);
        snackBar.setAction("确认", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences.edit().putBoolean(IS_FIRST_OPEN, false).apply();
            }
        });
        snackBar.setPromptThemBackground(Prompt.SUCCESS);
        snackBar.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //注销传感器
        mSensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
            locationManager.removeGpsStatusListener(gpsStatusListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    initLocation();
                } else {
                    Toast.makeText(MainActivity.this, "权限被拒绝",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}