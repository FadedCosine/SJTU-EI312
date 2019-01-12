package com.example.cosine.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.cosine.R;
import com.example.cosine.activity.MainActivity;


public class ButtonFragment extends Fragment {
    private static final String TAG = ButtonFragment.class.getCanonicalName();
    private ImageView btn_forward, btn_left, btn_right, btn_back, btn_stop;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_button, container, false);
        btn_forward = (ImageView) view.findViewById(R.id.forwarding);
        btn_forward.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(TAG, "forward");
                ((MainActivity)getActivity()).forward();
                return false;
            }
        });
        btn_left = (ImageView) view.findViewById(R.id.left);
        btn_left.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(TAG, "turnLeft");
                ((MainActivity)getActivity()).turnLeft();
                return false;
            }
        });
        btn_right = (ImageView) view.findViewById(R.id.right);
        btn_right.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(TAG, "turnRight");
                ((MainActivity)getActivity()).turnRight();
                return false;
            }
        });
        btn_back = (ImageView) view.findViewById(R.id.back);
        btn_back.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(TAG, "backward");
                ((MainActivity)getActivity()).backward();
                return false;
            }
        });
        btn_stop = (ImageView) view.findViewById(R.id.stop);
        btn_stop.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(TAG, "stop");
                ((MainActivity)getActivity()).stop();
                return false;
            }
        });
        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity)getActivity()).stop();
    }
}
