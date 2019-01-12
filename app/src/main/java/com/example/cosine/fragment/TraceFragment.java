package com.example.cosine.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import com.example.cosine.R;
import com.example.cosine.activity.MainActivity;
import com.example.cosine.fragment.GestureFragment;

/**
 * Created by mac on 2017/9/20.
 */
public class TraceFragment extends Fragment {

    private MyView trailView;
    private int count_left = 0, count_right=0;

    private boolean isRunning = true;

    private boolean okay = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trace, container, false);
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.fragment_trace);
        trailView = new MyView((MainActivity) getActivity());
        layout.addView(trailView);
        return view;
    }


    public boolean sendMessage(char m, int length) {
        try {
            switch (m) {
                case 'Q':
                    ((MainActivity) getActivity()).forward(length);
                    break;
                case 'Z':
                    ((MainActivity) getActivity()).turnLeft(length);
                    break;
                case 'Y':
                    ((MainActivity) getActivity()).turnRight(length);
                    break;
                case 'H':
                    ((MainActivity) getActivity()).backward(length);
                    break;
                default:
                    break;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }


//    public void sendMessage(char input, int length)
//    {
//        System.out.println(input+":"+length);
//    }

    public void sendMessage(float posX_pre_pre, float posY_pre_pre, float posX_pre, float posY_pre, float posX, float posY) {
        //正处于ACTION_DOWN状态
        int pathLength = (int) Math.round(Math.sqrt((posX_pre - posX) * (posX_pre - posX) + (posY_pre - posY) * (posY_pre - posY)));
        if (equal(posX_pre_pre, posX_pre) && equal(posY_pre_pre, posY_pre)) {
            sendMessage('Q', Math.min(90, pathLength));
            System.out.println("Two points equals");
        }
        if (equal(posX_pre_pre, posX_pre) && equal(posX_pre, posX) && equal(posY_pre_pre, posY_pre) && equal(posY_pre, posY))
            return;

        //第一次移动
        //if(count < 3)
        //正常移动
        float vector1_x = posX_pre - posX_pre_pre;
        float vector1_y = posY_pre - posY_pre_pre;
        double vector1_len = Math.sqrt(vector1_x * vector1_x + vector1_y * vector1_y);
        float vector2_x = posX - posX_pre;
        float vector2_y = posY - posY_pre;
        double vector2_len = Math.sqrt(vector2_x * vector2_x + vector2_y * vector2_y);
        double theta_cosined = Math.acos((vector1_x * vector2_x + vector1_y * vector2_y) / (vector1_len * vector2_len)) / Math.PI * 180;
        System.out.println("theta_cosined = " + theta_cosined);
        double theta_sined = Math.asin((vector1_x * vector2_y - vector2_x * vector1_y) / (vector1_len * vector2_len)) / Math.PI * 180;
        System.out.println("theta_sined = " + theta_sined);

        if (Math.abs((int) Math.round(theta_sined)) < 9) {
            pathLength = Math.min(90, pathLength);

            sendMessage('Q', pathLength);
            return;
        }
        //turn right
        else if (theta_sined > 20) {
            ++count_right;
            if(count_right >= 1 ) {
                count_left = 0;
                System.out.println("Turn right by " + theta_sined);
                ((MainActivity) getActivity()).stop();
                sendMessage('Y', Math.min(90, (int) Math.round(theta_sined)));
            }
        }
        //turn left
        else if (theta_sined < -20) {
            ++count_left;
            if(count_left >= 1) {
                count_right = 0;
                System.out.println("Turn left by " + -theta_sined);
                ((MainActivity) getActivity()).stop();

                sendMessage('Z', Math.min(90, (int) Math.round(-theta_sined)));
            }
        }
    }

    public boolean equal(float x, float y) {
        if (Math.abs(x - y) <= 0.01)
            return true;
        return false;
    }

    public class MyView extends SurfaceView implements Callback, Runnable {


        public static final int TIME_IN_FRAME = 50;
        Paint mPaint = null;
        Paint mTextPaint = null;
        Paint mRecPaint = null;
        SurfaceHolder mSurfaceHolder = null;
        public boolean mRunning = false;
        Canvas mCanvas = null;
        private Path mPath;
        private float mPosX, mPosY, mPosX_pre, mPosY_pre, mPosX_pre_pre, mPosY_pre_pre;
        private float PosX, PosY, PosX_pre, PosY_pre, PosX_pre_pre, PosY_pre_pre;
        private int touch_count = 0;

        public MyView(Context context) {
            super(context);
            this.setFocusable(true);
            this.setFocusableInTouchMode(true);

            mSurfaceHolder = this.getHolder();
            mSurfaceHolder.addCallback(this);
            mCanvas = new Canvas();

            mPaint = new Paint();
            mPaint.setColor(Color.BLACK);
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(6);
            mPath = new Path();
            mTextPaint = new Paint();
            mTextPaint.setColor(Color.BLACK);
            mTextPaint.setTextSize(15);
//            mRecPaint = new Paint();
//            mRecPaint.setColor(Color.RED);
//            mRecPaint.set


        }

        public boolean onTouchEvent(MotionEvent event) {
            int action = event.getAction();
            float x = event.getX();
            float y = event.getY();
            System.out.println("x = " + x + ", y = " + y);
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mPath.moveTo(x, y);
                    mPosX_pre_pre = x;
                    mPosY_pre_pre = y;
                    mPosX_pre = x;
                    mPosY_pre = y;
                    touch_count = 0;
                    break;
                case MotionEvent.ACTION_MOVE:
                    mPosX_pre_pre = mPosX_pre;
                    mPosY_pre_pre = mPosY_pre;
                    mPosX_pre = mPosX;
                    mPosY_pre = mPosY;
                    mPath.quadTo(mPosX, mPosY, x, y);
                    touch_count++;
                    break;
                case MotionEvent.ACTION_UP:
                    mPosY_pre_pre = mPosY;
                    mPosY_pre = mPosY;
                    mPosX_pre_pre = mPosX;
                    mPosX_pre = mPosX;
                    ((MainActivity) getActivity()).stop();
                    mPath.reset();
                    touch_count = 0;
                    break;
            }
            //记录当前触摸点得当前得坐标
            mPosX = x;
            mPosY = y;

            if (touch_count % 5 == 0) {
                if (touch_count == 0) {
                    PosX_pre_pre = mPosX;
                    PosY_pre_pre = mPosY;
                } else if (touch_count == 5) {
                    PosX_pre = mPosX;
                    PosY_pre = mPosY;
                } else if (touch_count == 10) {
                    PosX = mPosX;
                    PosY = mPosY;
                } else {
                    sendMessage(PosX_pre_pre, PosY_pre_pre, PosX_pre, PosY_pre, PosX, PosY);
                    PosX_pre_pre = PosX_pre;
                    PosY_pre_pre = PosY_pre;
                    PosX_pre = PosX;
                    PosY_pre = PosY;
                    PosX = mPosX;
                    PosY = mPosY;
                    //mCanvas.drawRect(PosX-200f, PosY - 200f, PosX + 200f, PosY +200f, mPaint);
                }
            }

            return true;
        }

        protected void Draw() {
            mCanvas.drawColor(Color.WHITE);
            //绘制曲线
            mCanvas.drawPath(mPath, mPaint);
            mCanvas.drawText("当前触笔X：" + mPosX, 0, 20, mTextPaint);
            mCanvas.drawText("当前触笔Y:" + mPosY, 0, 40, mTextPaint);
            mCanvas.drawText("touch_count = " + touch_count, 0, 60, mTextPaint);
        }

        public void run() {
// TODO Auto-generated method stub
            while (mRunning) {
                long startTime = System.currentTimeMillis();
                synchronized (mSurfaceHolder) {
                    mCanvas = mSurfaceHolder.lockCanvas();
                    Draw();
                    mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                }
                long endTime = System.currentTimeMillis();
                int diffTime = (int) (endTime - startTime);
                while (diffTime <= TIME_IN_FRAME) {
                    diffTime = (int) (System.currentTimeMillis() - startTime);
                    Thread.yield();
                }
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
// TODO Auto-generated method stub
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mRunning = true;
            new Thread(this).start();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
// TODO Auto-generated method stub
            mRunning = false;
        }

    }


}
