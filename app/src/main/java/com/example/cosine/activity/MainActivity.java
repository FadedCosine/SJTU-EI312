package com.example.cosine.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.cosine.R;
import com.example.cosine.fragment.ButtonFragment;
import com.example.cosine.fragment.GestureFragment;
import com.example.cosine.fragment.GravityFragment;
import com.example.cosine.fragment.JoyStickFragment;
import com.example.cosine.fragment.VoiceFragment;
import com.example.cosine.fragment.TraceFragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private ViewGroup mContainer;
    private ViewGroup mDirectionContainer;
    private View mDirection;
    private View mFragmentContainer;
    private BluetoothAdapter myBluetoothAdapter;
    public BluetoothSocket myBluetoothSocket;
    private static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private OutputStream os;
    private EditText input_BlueTooth;

    //两个接口，一个消息显示
    private EditText clientIpEditText;
    private ImageView receivedImageView;
    //private TextView textResponse;
    //
    private boolean enable_video = false;
    public String serverIP;
    public static Handler handler;

    private ServerSocket cameraSocket;
    private Bitmap bitmap;
    public static final int CAMERA_PORT = 8686;
    public static final int SERVER_PORT = 8080;
    Matrix matrix = new Matrix();
    @Override
    protected void onDestroy(){
        super.onDestroy();
        stop();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        View appBarMain = findViewById(R.id.app_bar_main);
        //mDirectionContainer = (FrameLayout)appBarMain.findViewById(R.id.myDirectionContainer);
        mContainer = (FrameLayout)appBarMain.findViewById(R.id.myContainer);
        //因为加入视频回传的ui，和普通模式的ui是不一样的，之后还需要加入视频回传的ui，所以要新加一个direction_main.xml布局
        //里边要放视频回传的显示组件，和控制组件，控制的组件用碎片实现，mFragmentContainer就用来放控制组件的碎片
//        mDirection = (View) LayoutInflater.from(this).inflate(R.layout.direction_main, mContainer, false);
//        mDirectionContainer = (ViewGroup)mDirection.findViewById(R.id.myDirectionContainer);
//        mFragmentContainer = mDirectionContainer.findViewById(R.id.fragment_container);

        // to do, 加入视频回传的组件
        receivedImageView = (ImageView)mContainer.findViewById(R.id.video_receive);// imageView for video
        //textResponse = (TextView) findViewById(R.id.responce);
        //textResponse.setVisibility(View.GONE);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerLayout = navigationView.getHeaderView(0);
        FloatingActionButton link_IP = (FloatingActionButton)headerLayout.findViewById(R.id.link_IP);
        FloatingActionButton link_BlueTooth = (FloatingActionButton)headerLayout.findViewById(R.id.link_bluetooth);
        input_BlueTooth = (EditText)headerLayout.findViewById(R.id.input_device);
        clientIpEditText = (EditText)headerLayout.findViewById(R.id.input_ip);// ip address

        link_BlueTooth.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String input_device_name = input_BlueTooth.getText().toString();
                //连接小车蓝牙
                myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (myBluetoothAdapter == null) {
                    Toast.makeText(MainActivity.this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
                    finish();
                }
                if (!myBluetoothAdapter.isEnabled()){
                    myBluetoothAdapter.enable();
                }
                Toast.makeText(MainActivity.this, "discover...", Toast.LENGTH_LONG).show();
                myBluetoothAdapter.startDiscovery();
                Object[] objs = myBluetoothAdapter.getBondedDevices().toArray();
                for (int i = 0; i < objs.length; ++i) {
                    BluetoothDevice device = (BluetoothDevice)objs[i];
                    if (device.getName().equals(input_device_name)) {
                        try {
                            myBluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                        } catch (IOException e) {
                        }
                        break;
                    }
                }
                if (myBluetoothSocket != null){
                    try{
                        myBluetoothSocket.connect();
                        myBluetoothAdapter.cancelDiscovery();
                    }catch(IOException e){
                    }
                    Toast.makeText(MainActivity.this, "Bluetooth connected", Toast.LENGTH_LONG).show();
                    input_BlueTooth.setVisibility(View.GONE);
                }
                else{
                    Toast.makeText(MainActivity.this, "Bluetooth not found", Toast.LENGTH_LONG).show();
                }
            }
        });

        //每次接受到图像以后在这里显示
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(bitmap!=null && msg.arg1 == 123) {
                    if (bitmap.getWidth() > bitmap.getHeight())
                        bitmap = bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    if(enable_video)  {
                        receivedImageView.setVisibility(View.VISIBLE);
                    }
                    else{
                        receivedImageView.setVisibility(View.INVISIBLE);
                    }

                    receivedImageView.setImageBitmap(bitmap);
                }
                super.handleMessage(msg);
            }
        };

        matrix.postRotate(90);

        //开启线程，监听8086端口
        Thread videoThread = new ReceiveVideo();
        videoThread.start();

        //redo.. 点击连接函数，需要重新实现，作用：读入ip，存入serverIP，并且连接录像端
        link_IP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //得到ip地址
                serverIP = clientIpEditText.getText().toString();

                //连接
                ConnectClientTask connectClientTask = new ConnectClientTask(
                        serverIP,
                        SERVER_PORT);
                connectClientTask.execute();
                clientIpEditText.setVisibility(View.GONE);
            }
        });

    }

    public void forward(){
        if (!isBlueToothConnected()) {
            Toast.makeText(MainActivity.this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            Log.e("Controller", "No connection!");
            return;
        }
        try {
            myBluetoothSocket.getOutputStream().write("A".getBytes());
            Log.e("Controller", "Forward");
        }
        catch (IOException e) {
            Log.e("Bluetooth", e.getMessage());
        }
    }
    public void forward(int duration){
        if (!isBlueToothConnected()) {
            Toast.makeText(MainActivity.this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            Log.e("Controller", "No connection!");
            return;
        }
        try {
            char[] msg_arr = Integer.toString(duration).toCharArray();
            myBluetoothSocket.getOutputStream().write("Q".getBytes());
            for(int i=0;i<msg_arr.length;i++)
                myBluetoothSocket.getOutputStream().write(msg_arr[i]);
            myBluetoothSocket.getOutputStream().write('O');
            myBluetoothSocket.getOutputStream().write('O');
            myBluetoothSocket.getOutputStream().write('O');
            Log.e("Controller", "Forward");
        }
        catch (IOException e) {
            Log.e("Bluetooth", e.getMessage());
        }
    }
    public void backward(){
        if (!isBlueToothConnected()) {
            Toast.makeText(MainActivity.this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            Log.e("Controller", "No connection!");
            return;
        }
        try {
            myBluetoothSocket.getOutputStream().write("B".getBytes());
            Log.e("Controller", "Backword");
        }
        catch (IOException e) {
            Log.e("Bluetooth", e.getMessage());
        }
    }
    public void backward(int duration){
        if (!isBlueToothConnected()) {
            Toast.makeText(MainActivity.this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            Log.e("Controller", "No connection!");
            return;
        }
        try {
            char[] msg_arr = Integer.toString(duration).toCharArray();
            myBluetoothSocket.getOutputStream().write("H".getBytes());
            for(int i=0;i<msg_arr.length;i++)
                myBluetoothSocket.getOutputStream().write(msg_arr[i]);
            myBluetoothSocket.getOutputStream().write('O');
            myBluetoothSocket.getOutputStream().write('O');
            myBluetoothSocket.getOutputStream().write('O');
            Log.e("Controller", "Backward");
        }
        catch (IOException e) {
            Log.e("Bluetooth", e.getMessage());
        }
    }
    public void turnLeft(){
        if (!isBlueToothConnected()) {
            Toast.makeText(MainActivity.this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            Log.e("Controller", "No connection!");
            return;
        }
        try {
            myBluetoothSocket.getOutputStream().write("L".getBytes());
            Log.e("Controller", "Turn Left");
        }
        catch (IOException e) {
            Log.e("Bluetooth", e.getMessage());
        }
    }
    public void turnLeft(int duration){
        if (!isBlueToothConnected()) {
            Toast.makeText(MainActivity.this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            Log.e("Controller", "No connection!");
            return;
        }
        try {
            char[] msg_arr = Integer.toString(duration).toCharArray();
            myBluetoothSocket.getOutputStream().write("Z".getBytes());
            System.out.println("Z getBytes = " + "Z".getBytes());
            for(int i=0;i<msg_arr.length;i++)
                myBluetoothSocket.getOutputStream().write(msg_arr[i]);
            myBluetoothSocket.getOutputStream().write('O');
            myBluetoothSocket.getOutputStream().write('O');
            myBluetoothSocket.getOutputStream().write('O');
            Log.e("Controller", "Turn left");
        }
        catch (IOException e) {
            Log.e("Bluetooth", e.getMessage());
        }
    }
    public void turnRight(){
        if (!isBlueToothConnected()) {
            Toast.makeText(MainActivity.this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            Log.e("Controller", "No connection!");
            return;
        }
        try {
            myBluetoothSocket.getOutputStream().write("R".getBytes());
            Log.e("Controller", "Turn right");
        }
        catch (IOException e) {
            Log.e("Bluetooth", e.getMessage());
        }
    }
    public void turnRight(int duration){
        if (!isBlueToothConnected()) {
            Toast.makeText(MainActivity.this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            Log.e("Controller", "No connection!");
            return;
        }
        try {
            char[] msg_arr = Integer.toString(duration).toCharArray();
            myBluetoothSocket.getOutputStream().write("Y".getBytes());
            for(int i=0;i<msg_arr.length;i++)
                myBluetoothSocket.getOutputStream().write(msg_arr[i]);
            myBluetoothSocket.getOutputStream().write('O');
            myBluetoothSocket.getOutputStream().write('O');
            myBluetoothSocket.getOutputStream().write('O');
            Log.e("Controller", "Turn right");
        }
        catch (IOException e) {
            Log.e("Bluetooth", e.getMessage());
        }
    }
    public void stop(){
        if (!isBlueToothConnected()) {
            Toast.makeText(MainActivity.this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            Log.e("Controller", "No connection!");
            return;
        }
        try {
            myBluetoothSocket.getOutputStream().write("P".getBytes());
        }
        catch (IOException e) {
            Log.e("Bluetooth", e.getMessage());
        }
    }
    public boolean isBlueToothConnected(){
        return myBluetoothSocket != null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.Video:
                enable_video = !enable_video;
                if(enable_video){
//                    item.setIcon(R.drawable.video);
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.video));
                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    Toast.makeText(MainActivity.this, "Enable Video", Toast.LENGTH_SHORT).show();
                }else{
//                    item.setIcon(R.drawable.close_video);
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.close_video));
                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    Toast.makeText(MainActivity.this, "Disable Video", Toast.LENGTH_SHORT).show();
                }
                break;
            default:

        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_button) {
            Toast.makeText(MainActivity.this, "Button Mode",Toast.LENGTH_SHORT).show();
            replaceFragment(new ButtonFragment());
        } else if (id == R.id.nav_gesture){
            Toast.makeText(MainActivity.this, "Gesture Mode",Toast.LENGTH_SHORT).show();
            replaceFragment(new GestureFragment());
        }else if (id == R.id.nav_joystick){
            Toast.makeText(MainActivity.this, "Joystick Mode",Toast.LENGTH_SHORT).show();
            replaceFragment(new JoyStickFragment());
        }else if (id == R.id.nav_voice){
            Toast.makeText(MainActivity.this, "Voice Mode",Toast.LENGTH_SHORT).show();
            replaceFragment(new VoiceFragment());
        }
        else if (id == R.id.nav_gravity){
            Toast.makeText(MainActivity.this, "Gravity Mode",Toast.LENGTH_SHORT).show();
            replaceFragment(new GravityFragment());
        }
        else if (id == R.id.nav_trace){
            Toast.makeText(MainActivity.this, "Trace Mode",Toast.LENGTH_SHORT).show();
            replaceFragment(new TraceFragment());
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    class ReceiveVideo extends Thread{
        private int length = 0;
        private int num = 0;
        private byte[] buffer = new byte[2048];
        private byte[] data = new byte[204800];

        @Override
        public void run(){
            try{
                cameraSocket = new ServerSocket(CAMERA_PORT);
                while(true){
                    Socket socket = cameraSocket.accept();
                    try{
                        //开始接受图像数据
                        InputStream input = socket.getInputStream();
                        Log.d("Image","GetImage");
                        num = 0;
                        do{
                            length = input.read(buffer);
                            if(length >= 0){
                                System.arraycopy(buffer,0,data,num,length);
                                num += length;
                            }
                        }while(length >= 0);

                        //调用线程后台处理图像
                        new setImageThread(data,num).start();
                        input.close();
                    }catch(Exception e){
                        e.printStackTrace();
                    }finally{
                        socket.close();
                    }
                }

            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    //设置图片，调用信号函数回显
    class setImageThread extends Thread{
        private byte[]data;
        private int num;
        public setImageThread(byte[] data, int num){
            this.data = data;
            this.num = num;
        }
        @Override
        public void run(){
            bitmap = BitmapFactory.decodeByteArray(data, 0, num);
            Message msg=new Message();

            //还记得前面的信号处理函数显示图像吗，在这里实现的信号传递
            msg.arg1 = 123;
            handler.sendMessage(msg);
        }
    }

    //连接录像端
    public class ConnectClientTask extends AsyncTask<Void, Void, Void> {

        //private TextView textResponse;
        String dstAddress;
        int dstPort = 8080;
        //String response = "Connected";

        ConnectClientTask(String addr, int port) {
            dstAddress = addr;
            dstPort = port;
            //textResponse = tResponse;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket = null;

            try {
                socket = new Socket(dstAddress, dstPort);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                //response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                e.printStackTrace();
                //response = "IOException: " + e.toString();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //textResponse.setText(response);
            Toast.makeText(MainActivity.this, "IP Connected!", Toast.LENGTH_LONG).show();
            super.onPostExecute(result);
        }
    }

}
