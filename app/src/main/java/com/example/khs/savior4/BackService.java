package com.example.khs.savior4;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class BackService extends Service {
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    //public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_CONNECT = 4;
    public static final int MESSAGE_DISCONNECT = 5;



    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread

    private ArrayAdapter<String> mConversationArrayAdapter;

    // Member object for the chat services

    private BluetoothAdapter mBluetoothAdapter = null;
    private BtService btService = null;

    private boolean ServiceOn = false;
    private boolean btServiceEnd = false;

    // 메시지 받았을 때 처리하는 handler
    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            //BtService class 쪽에서 handler로 msg 받아옴

            //Toast.makeText(getApplicationContext(), "msg = " + msg, Toast.LENGTH_LONG).show();
            if(msg.what == MESSAGE_READ){
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                Toast.makeText(getApplicationContext(), "readMessage = " + readMessage, Toast.LENGTH_LONG).show();
                if(ServiceOn) {
                    if (readMessage.equals("T")) {
                        // 강제로 어플을 시작화면에 띄운다.
                        // newActivity 실행
                        Intent intent = new Intent(
                                getApplicationContext(), // 현재 제어권자
                                NewActivity.class); // 이동할 컴포넌트
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                        startActivity(intent);
                    }
                }
            }
            if(msg.what == MESSAGE_CONNECT){ // 연결 성공
                Toast.makeText(getApplicationContext(), "bluetooth connect!", Toast.LENGTH_LONG).show();
                MainActivity.BtCheck.setBackgroundDrawable(getResources().getDrawable(R.drawable.bluetooth_active_icon));
                MainActivity.Sb.setClickable(true);
                MainActivity.Sb.setChecked(true);
            }
            if(msg.what == MESSAGE_DISCONNECT){

                Toast.makeText(getApplicationContext(), "bluetooth disconnect!", Toast.LENGTH_LONG).show();
                MainActivity.BtCheck.setBackgroundDrawable(getResources().getDrawable(R.drawable.bluetooth_disabled_icon));

                if(btServiceEnd == true){
                    //Toast.makeText(getApplicationContext(),"btService Null",Toast.LENGTH_LONG).show();
                }
                else{
                    //Toast.makeText(getApplicationContext(),"btService Not null", Toast.LENGTH_LONG).show();
                    if(mBluetoothAdapter.isEnabled()){
                        btService.stop();
                        if (btService == null) setupChat();
                        if (btService != null) {
                            // Only if the state is STATE_NONE, do we know that we haven't started already
                            if (btService.getState() == BtService.STATE_NONE) {
                                // Start the Bluetooth chat services
                                btService.start();
                                btServiceEnd = false;
                            }
                        }
                    }
                    MainActivity.Sb.setClickable(false);
                    MainActivity.Sb.setChecked(false);
                }

            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (bluetoothState) {
                    // 블루투스 꺼졌을 때 걍 어플 종료
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        if(!mBluetoothAdapter.isEnabled()){
                            Toast.makeText(getApplicationContext(),"블루투스가 꺼졌습니다. Savior를 종료합니다.",Toast.LENGTH_LONG).show();
                            MainActivity.Exit.callOnClick();
                        }
                }
            }
        }
    };
    @Override
    public IBinder onBind(Intent intent){

        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();

        Log.d("test", "서비스의 onCreate");

        // 블루투스 클래스 생성
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 블루투스가 켜져있지 않을 때, BtPermissionAcitivity를 시작한다.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(getApplicationContext(),BtPermissionActivity.class);
            enableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(enableIntent);
            // Otherwise, setup the chat session
        }

        /*
        블루투스가 켜져있는 경우
        블루투스 연결 대기상태로 만들어준다.
        객체 생성이 안되어 있을 경우 setupChat으로 객체생성
        BtService가 다른 device와 연결이 안되어 있는 경우 btService start
            -> BtService.java class로 ㄱㄱ

        */


    }


    private void setupChat() {

        // Initialize the BluetoothChatService to perform bluetooth connections
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        //mConversationView = (ListView) findViewById(R.id.in);
        btService = new BtService(mHandler);

        // Initialize the buffer for outgoing messages
        //mOutStringBuffer = new StringBuffer("");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d("test", "서비스의 onStartCommand");

        if(mBluetoothAdapter.isEnabled()) {
            if (btService == null) setupChat();

            if (btService != null) {
                // Only if the state is STATE_NONE, do we know that we haven't started already
                if (btService.getState() == BtService.STATE_NONE) {
                    // Start the Bluetooth chat services
                    btService.start();
                    btServiceEnd = false;
                }
            }
        }
        String ServiceOnOff = intent.getStringExtra("Service");
        if (ServiceOnOff.equals("ServiceOn")) {
            setNotibar("Savior 실행중", "실행중", true);
        } else if (ServiceOnOff.equals("ServiceOff") || ServiceOnOff.equals("ServiceCreate")) {
            setNotibar("Savior 실행 대기중", "실행 대기중", false);
        }
        else if (ServiceOnOff.equals("ServiceEnd")){
            stopSelf();
        }

        //final Handler handler = new Handler();
        return START_NOT_STICKY;
    }

    private void setNotibar(String Title,String Text, boolean serviceon){
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notificationService = new NotificationCompat.Builder(BackService.this)
                .setContentTitle(Title)
                .setContentText(Text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notificationService);
        ServiceOn = serviceon;
    }

    @Override
    public void onDestroy(){

        Log.d("test", "서비스의 onDestroy");
        super.onDestroy();


        unregisterReceiver(mReceiver);
        if(btService != null) {
            Log.d("test", "btService 종료");
            btService.stop();
            btServiceEnd = true;
        }
        stopForeground(true);
        //Toast.makeText(getApplicationContext(),"Service 종료",Toast.LENGTH_LONG).show();
    }

}
