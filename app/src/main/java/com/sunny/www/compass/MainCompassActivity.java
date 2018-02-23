//package com.sunny.www.compass;
//
//import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//import android.widget.TextView;
//
//public class MainCompassActivity extends AppCompatActivity {
//    private static final String TAG = MainCompassActivity.class.getSimpleName();
//    private CompassManager mCompassManager;
//    private String mOrientaionText[] = new String[]{"北","东北","东","东南","南","西南","西","西北"};
//
//    private TextView mAngleTextview;
//    private TextView mOrientaionTextview;
//
//
//    private CompassManager.CompassLister mCompassLister = new CompassManager.CompassLister() {
//        @Override
//        public void onOrientationChange(float orientation) {
//            Log.e(TAG, "onOrientationChange: orientation "+ orientation);
//            mOrientaionTextview.setText(mOrientaionText[((int) (orientation+22.5f)%360)/45]);
//            mAngleTextview.setText(orientation+"");
//
//        }
//    };
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main_compass);
//
//        mAngleTextview = (TextView) findViewById(R.id.angle_value_textview);
//        mOrientaionTextview = (TextView) findViewById(R.id.orientation_value_textview);
//
//        mCompassManager = CompassManager.getInstance();
//        mCompassManager.init(this);
////        mCompassManager.setRotation(270);
//        mCompassManager.addCompassLister(mCompassLister);
//
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mCompassManager.unbind();
//    }
//}
//
//
//
