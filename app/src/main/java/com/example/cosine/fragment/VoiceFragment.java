package com.example.cosine.fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.util.Log;
import android.widget.Toast;

import com.example.cosine.R;
import com.example.cosine.activity.MainActivity;
import com.google.gson.Gson;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult ;
import com.iflytek.cloud.SpeechConstant ;
import com.iflytek.cloud.SpeechError ;
import com.iflytek.cloud.SpeechUtility ;
import com.iflytek.cloud.ui.RecognizerDialog ;
import com.iflytek.cloud.ui.RecognizerDialogListener ;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;


public class VoiceFragment extends Fragment {
    private static final String TAG = VoiceFragment.class.getCanonicalName();
    private Button voice_btn;
    private boolean hasPermission=false;
    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private RecognizerDialog mDialog;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_voice, container, false);
        if (!validateMicAvailability()) {
            Toast.makeText((MainActivity)getActivity(), "当前麦克风不可用" ,Toast.LENGTH_SHORT).show();
        }
        SpeechUtility.createUtility(getActivity(), SpeechConstant.APPID + "=5c067094");
        mDialog = new RecognizerDialog(getActivity(), mInitListener);
        mDialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                String result = parseIatResult(recognizerResult.getResultString());
                if (result.isEmpty()) {
                    return;
                }

                if (result.contains("前")) {
                    Toast.makeText(getActivity(), "前进", Toast.LENGTH_SHORT).show();
                    ((MainActivity)getActivity()).forward();
                } else if (result.contains("后")) {
                    Toast.makeText(getActivity(), "后退", Toast.LENGTH_SHORT).show();
                    ((MainActivity)getActivity()).backward();
                } else if (result.contains("停")) {
                    Toast.makeText(getActivity(), "停止", Toast.LENGTH_SHORT).show();
                    ((MainActivity)getActivity()).stop();
                } else if (result.contains("左")) {
                    Toast.makeText(getActivity(), "左转", Toast.LENGTH_SHORT).show();
                    ((MainActivity)getActivity()).turnLeft();
                } else if (result.contains("右")) {
                    Toast.makeText(getActivity(), "右转", Toast.LENGTH_SHORT).show();
                    ((MainActivity)getActivity()).turnRight();
                }
            }

            @Override
            public void onError(SpeechError error) {
                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

//        voice_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Vibrator vibrator = (Vibrator)((MainActivity)getActivity()).getSystemService(((MainActivity)getActivity()).VIBRATOR_SERVICE);
//                vibrator.vibrate(200);
//                initSpeech(getActivity());
//            }
//        });

        setIatParam();
        voice_btn = (Button) view.findViewById(R.id.btn_recog);
        voice_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.show();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(hasPermission)getPermission();
        else hasPermission=true;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hasPermission=false;
        ((MainActivity)getActivity()).stop();
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()){
            if(hasPermission) getPermission();
            else hasPermission=true;
        }
    }
    private void setIatParam() {
        mDialog.setParameter(SpeechConstant.PARAMS, null); // 清空参数
        mDialog.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); // 设置听写引擎
        mDialog.setParameter(SpeechConstant.RESULT_TYPE, "json"); // 设置返回结果格式
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn"); // 设置语言
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin"); // 设置语言区域
        mDialog.setParameter(SpeechConstant.VAD_BOS, "4000"); // 设置语音前端点:初次语音输入前超时时间（毫秒）
        mDialog.setParameter(SpeechConstant.VAD_EOS, "2000"); // 设置语音后端点:已有语音输入后超时时间（毫秒）
        mDialog.setParameter(SpeechConstant.ASR_PTT, "0"); // 设置返回结果是否包含标点符号："0"无,"1"有
    }

    private static String parseIatResult(String json) {
        if (json == null) {
            return "";
        }
        StringBuilder ret = ret = new StringBuilder();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);
            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                // 转写结果词，默认使用第一个结果
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                JSONObject obj = items.getJSONObject(0);
                ret.append(obj.getString("w"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret.toString();
    }

    public void getPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),Manifest.permission.RECORD_AUDIO)) {
                showMessageOKCancel("需要打开录音权限才能使用语音控制",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO},
                                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
                            }
                        });
                return;
            }
            ActivityCompat.requestPermissions(getActivity(),new String[] {Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        }
        return;
    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Toast.makeText((MainActivity)getActivity(), "初始化失败，错误码：" + code,Toast.LENGTH_SHORT).show();
            }
        }
    };
    private void initSpeech(final Context context){
        //Log.d("Debug", "almost here 0");

        //语言中文， 方言：默认
        //Log.d("Debug", "almost here 1");
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");

        mDialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean isLast) {
                if(!isLast){
                    Log.d("Debug", "almost here 2");
                    String result = recognizerResult.getResultString();
                    Log.d("Debug", "almost here 3");
                    Gson gson=new Gson();
                    StringBuffer stringbuffer=new StringBuffer();
                    //解析语音json
                    ArrayList<Voice.dString> ts =  gson.fromJson(result, Voice.class).ts;
                    for (Voice.dString ds: ts){
                        String word = ds.ds.get(0).s;
                        stringbuffer.append(word);
                    }
                    result=stringbuffer.toString();
                    Toast.makeText(getActivity(),result,Toast.LENGTH_SHORT).show();
                    processReult(result);
                }
            }

            @Override
            public void onError(SpeechError speechError) {
                int error = speechError.getErrorCode();
                switch (error) {
                    case 20006:
                        Toast.makeText(getActivity(), "未打开录音权限", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        });
    }
    private void processReult(String result){
        if(getActivity() != null) {
            if (result.contains("前")) {
                Toast.makeText(getActivity(), "前进", Toast.LENGTH_SHORT).show();
                ((MainActivity)getActivity()).forward();
            } else if (result.contains("后")) {
                Toast.makeText(getActivity(), "后退", Toast.LENGTH_SHORT).show();
                ((MainActivity)getActivity()).backward();
            } else if (result.contains("停")) {
                Toast.makeText(getActivity(), "停止", Toast.LENGTH_SHORT).show();
                ((MainActivity)getActivity()).stop();
            } else if (result.contains("左")) {
                Toast.makeText(getActivity(), "左转", Toast.LENGTH_SHORT).show();
                ((MainActivity)getActivity()).turnLeft();
            } else if (result.contains("右")) {
                Toast.makeText(getActivity(), "右转", Toast.LENGTH_SHORT).show();
                ((MainActivity)getActivity()).turnRight();
            }
        }
    }
    public class Voice {

        public ArrayList<dString> ts;

        public class dString {
            public ArrayList<oneString> ds;
        }

        public class oneString {
            public String s;
        }
    }
    private boolean validateMicAvailability() {
        Boolean available = true;
        AudioRecord recorder = null;
        try {
            recorder =
                    new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
                            AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_DEFAULT, 44100);
            if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED) {
                available = false;
            }
            recorder.startRecording();
            if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                recorder.stop();
                available = false;
            }
            recorder.stop();
        } finally {
            if (recorder != null) {
                recorder.release();
            }
        }
        return available;
    }

}
