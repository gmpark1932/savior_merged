package com.example.khs.savior4;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.media.SoundPool;
import android.media.AudioManager;

import android.telephony.SmsManager;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {
    /*응급 구조 음성 관련*/
    SoundPool sound;
    int soundId;

    /*메시지 전송 관련*/
    String phoneNo = "01048999250";
    String message ="응급 구조 요청 메시지 입니다.";


    Intent intent;
    static public ToggleButton Sb;
    static public ToggleButton BtCheck;
    static public Button Exit;

    private BluetoothAdapter mBluetoothAdapter = null;
    private static final int REQUEST_ENABLE_BT = 3;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button setting = (Button) findViewById(R.id.settingButton);
        setting.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(intent);
            }
        });

        sound = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundId = sound.load(this, R.raw.alert, 1);
        Button alert = (Button) findViewById(R.id.alertButton);
        alert.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sound.play(soundId, 1, 1, 0, 1, 1f);
            }
        });

        Button sendMessage = (Button) findViewById(R.id.sendButton);
        sendMessage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sendSMS(phoneNo, message);
                //sendSMSMessage();
                sound.play(soundId, 1, 1, 0, 1, 1f);
            }
        });



        /* 서비스~~ */
        Sb = (ToggleButton) findViewById(R.id.Service);
        BtCheck = (ToggleButton) findViewById(R.id.BtConnectCheck);
        Exit = (Button) findViewById(R.id.Exit);
        //Toast.makeText(getApplicationContext(), "액티비티 시작!!", Toast.LENGTH_LONG).show();

        Sb.setClickable(false);
        BtCheck.setClickable(false);

        //service 시작
        // BackService.java로 ㄱㄱ
        intent = new Intent(
                getApplicationContext(), // 현재 제어권자
                BackService.class); // 이동할 컴포넌트
        intent.putExtra("Service","ServiceCreate");
        startService(intent); // 서비스 시작.


        Exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Sb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked == true){
                    Log.d("test", "Savior 시작버튼 클릭");
                    intent = new Intent(getApplicationContext(), BackService.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("Service","ServiceOn");
                    startService(intent);
                }
                else{


                    Log.d("test", "Savior 종료버튼 클릭");
                    intent = new Intent(
                            getApplicationContext(), // 현재 제어권자
                            BackService.class); // 이동할 컴포넌트
                    intent.putExtra("Service","ServiceOff");
                    startService(intent); // 서비스 시작.
                }
            }
        });



    }

    private void sendSMS(String phoneNumber, String message){
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }


    @Override
    public void onBackPressed(){
        //super.onBackPressed();
        moveTaskToBack(true);
    }

    @Override
    public void onDestroy(){
        //Toast.makeText(getApplicationContext(),"MainActivity 종료",Toast.LENGTH_LONG).show();
        Log.d("test", "액티비티 종료");
        super.onDestroy();
        intent = new Intent(
                getApplicationContext(), // 현재 제어권자
                BackService.class); // 이동할 컴포넌트
        intent.putExtra("Service","ServiceEnd");
        startService(intent);

    }

}
