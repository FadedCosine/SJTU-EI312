package com.example.cosine.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.cosine.R;
import com.example.cosine.activity.MainActivity;


public class GestureFragment extends Fragment {

    private GestureDetector mDetector;
    private ImageView mImageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDetector=new GestureDetector(getActivity(),new MyGestureListener());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gesture, container, false);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mDetector.onTouchEvent(motionEvent);
                return true;
            }
        });
        mImageView=view.findViewById(R.id.imgArrowGesture);
        mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop));

        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity)getActivity()).stop();
    }
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener{
        public MyGestureListener(){}

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2){
            if(motionEvent.getX()-motionEvent2.getX()>100&& Math.abs(v)>50){
                mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_left));
                if (getActivity() != null) {
                    ((MainActivity)getActivity()).turnLeft();
                }
            }else if(motionEvent2.getX()-motionEvent.getX()>100&& Math.abs(v)>50){
                mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_right));
                if (getActivity() != null) {
                    ((MainActivity)getActivity()).turnRight();
                }
            }else if(motionEvent2.getY()-motionEvent.getY()>100&& Math.abs(v)>50){
                mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_down));
                if (getActivity() != null) {
                    ((MainActivity)getActivity()).backward();;
                }
            }else if(motionEvent.getY()-motionEvent2.getY()>100&& Math.abs(v)>50){
                mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_up));
                if (getActivity() != null) {
                    ((MainActivity)getActivity()).forward();
                }
            }
            return super.onFling(motionEvent,motionEvent2,v,v2);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e){
            mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop));
            if (getActivity() != null) {
                ((MainActivity)getActivity()).stop();
            }
            return super.onDoubleTap(e);
        }
    }
}
