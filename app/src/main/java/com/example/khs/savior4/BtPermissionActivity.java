package com.example.khs.savior4;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;

public class BtPermissionActivity extends Activity {
    private BluetoothAdapter mBluetoothAdapter = null;
    private static final int REQUEST_ENABLE_BT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.btpermission_activity);
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart(){
        super.onStart();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else{
            finish();
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    Intent intent = new Intent(
                            getApplicationContext(), // 현재 제어권자
                            BackService.class); // 이동할 컴포넌트
                    intent.putExtra("Service","ServiceCreate");
                    startService(intent); // 서비스 시작.

                    finish();
                }
                break;
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
}
