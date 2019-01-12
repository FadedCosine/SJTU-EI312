package com.example.cosine.fragment;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.cosine.R;
import com.example.cosine.activity.MainActivity;

public class GravityFragment extends Fragment {
    private static final String TAG = GravityFragment.class.getCanonicalName();

    private SensorManager mSensorManager;
    private MySensorEventListener mMySensorEventListener;

    private ImageView mImageView;
    private boolean ViewonFlag = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mMySensorEventListener = new MySensorEventListener();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
            if(ViewonFlag) {
                if (mSensorManager != null) {
                    Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                    mSensorManager.registerListener(mMySensorEventListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
                }
                else ViewonFlag = true;
            }
        }
        else {
            if (mSensorManager != null) {
                mSensorManager.unregisterListener(mMySensorEventListener);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gravity, container , false);
        mImageView = (ImageView) view.findViewById(R.id.imgArrowGravity);
        if(ViewonFlag){
            if(mSensorManager!=null){
                Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mSensorManager.registerListener(mMySensorEventListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }else ViewonFlag=true;
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        if(getUserVisibleHint() && mSensorManager!=null){
            Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(mMySensorEventListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mMySensorEventListener);
        Log.d(TAG, "onPause");

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ViewonFlag=false;
        ((MainActivity)getActivity()).stop();
    }

    private class MySensorEventListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = sensorEvent.values[SensorManager.DATA_X];
                float y = sensorEvent.values[SensorManager.DATA_Y];
                switch (getDirection(x, y)) {
                    case 1: {
//                        mTextView.setText("FORWARDING");
                        Log.d(TAG, "forward");
                        mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_up));
                        if (getActivity() != null && ((MainActivity)getActivity()).isBlueToothConnected()) {
                            ((MainActivity)getActivity()).forward();
                        }
                        break;
                    }
                    case 4: {
                        Log.d(TAG, "back");
//                        mTextView.setText("BACK");
                        mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_down));
                        if (getActivity() != null && ((MainActivity)getActivity()).isBlueToothConnected()) {
                            ((MainActivity)getActivity()).backward();
                        }

                        break;
                    }
                    case 2: {
                        Log.d(TAG, "left");
//                        mTextView.setText("LEFT");
                        mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_left));
                        if (getActivity() != null && ((MainActivity)getActivity()).isBlueToothConnected()) {
                            ((MainActivity)getActivity()).turnLeft();
                        }

                        break;
                    }
                    case 3: {
                        Log.d(TAG, "right");
//                        mTextView.setText("RIGHT");
                        mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_right));
                        if (getActivity() != null && ((MainActivity)getActivity()).isBlueToothConnected()) {
                            ((MainActivity)getActivity()).turnRight();
                        }

                        break;
                    }
                    default:
                        mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop));
                        if (getActivity() != null && ((MainActivity)getActivity()).isBlueToothConnected()) {
                            ((MainActivity) getActivity()).stop();
                        }
                        break;

                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }

    private int getDirection(float x, float y) {
        if (-1 < x && x < 1) {
            if (y < -2) { //forwarding
                return 1;
            } else if (y > 2) { //back
                return 4;
            }
        } else if (x > 4) { //left
            return 2;
        } else if (x < -4) { //right
            return 3;
        }
        return -1;
    }
}
