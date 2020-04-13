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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.sunny.www.compass.view.CompassView;
import com.trycatch.mysnackbar.Prompt;
import com.trycatch.mysnackbar.TSnackbar;

import java.util.List;

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
     * 海拔
     */
    private TextView mAltTv;
    /**
     * 顶部提示悬浮框
     */
    private TSnackbar accuracyWarnSnackBar;

    private boolean isFirstOpen = true;

    private boolean isVibrate = true;

    private long lastTime = 0;

    private SensorManager mSensorManager;

    String direction = "UNKNOWN";

    private float lastDirAngel = 0;

    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initView();
        initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initSensor();
        checkLocationPermission();
    }

    @SuppressWarnings("deprecation")
    private void initSensor() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            //注册监听
            mSensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
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
        mAltTv = findViewById(R.id.tv_altitude);
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
                    Toast.makeText(MainActivity.this, isVibrate ? getString(R.string.open_vibrate)
                            : getString(R.string.close_vibrate), Toast.LENGTH_SHORT).show();
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
                if (vibrator != null) {
                    vibrator.vibrate(20);
                }
            }
            lastDirAngel = dirAngel;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            if (accuracy != SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM
                    && accuracy != SensorManager.SENSOR_STATUS_ACCURACY_HIGH) {
                if (accuracyWarnSnackBar == null) {
                    accuracyWarnSnackBar = TSnackbar.make(mViewGroup,
                            getString(R.string.calibration_tips),
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
            String locationProvider = null;
            Location location = null;
            if (providers.contains(LocationManager.GPS_PROVIDER)) {
                locationProvider = LocationManager.GPS_PROVIDER;
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            if (locationProvider == null && providers.contains(LocationManager.NETWORK_PROVIDER)) {
                locationProvider = LocationManager.NETWORK_PROVIDER;
            }

            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (locationProvider == null) {
                Toast.makeText(this, getString(R.string.open_location_service), Toast.LENGTH_SHORT).show();
                return;
            }

            updateLocation(location);

            locationManager.requestLocationUpdates(locationProvider, 2000, 2,
                    locationListener);
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

    private void updateLocation(Location location) {
        if (location != null) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            double alt = location.getAltitude();
            mLatTv.setText(String.format(getString(R.string.format_float_5), lat));
            mLonTv.setText(String.format(getString(R.string.format_float_5), lon));
            mAltTv.setText(String.format(getString(R.string.format_float_5), alt));
        } else {
            mLatTv.setText("---");
            mLonTv.setText("---");
            mAltTv.setText("---");
        }
    }

    private void showSnackBar() {
        TSnackbar snackBar = TSnackbar.make(mViewGroup, getString(R.string.vibrate_setting_tips),
                TSnackbar.LENGTH_INDEFINITE, TSnackbar.APPEAR_FROM_TOP_TO_DOWN);
        snackBar.setAction(getString(R.string.confirm), new View.OnClickListener() {
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
        // 注销传感器
        mSensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
}