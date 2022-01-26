package com.example.bledemo;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.UUID;

public class ChooseModeActivity extends AppCompatActivity implements View.OnClickListener{

    //服务uuid
    public static UUID UUID_SERVER = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    //读的特征值¸
    public static UUID UUID_CHAR_READ = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    //写的特征值
    public static UUID UUID_CHAR_WRITE = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
    public static UUID UUID_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_mode);
        Button gotoClientModeBtn=findViewById(R.id.goto_client_mode);
        Button gotoServiceModeBtn =findViewById(R.id.goto_service_mode);
        gotoClientModeBtn.setOnClickListener(this);
        gotoServiceModeBtn.setOnClickListener(this);
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    @Override
    public void onClick(View v) {
        Intent intent=new Intent();
        switch (v.getId()){
            case R.id.goto_client_mode:
                intent.setClass(ChooseModeActivity.this,ClientModeActivity.class);
                startActivity(intent);
                break;
            case R.id.goto_service_mode:
                //判断设备是否支持低功耗蓝牙
                if (!applyBLE())
                    break;
                //判断蓝牙是否打开
                if(!BLEIsEnable())
                    break;
                intent.setClass(ChooseModeActivity.this, ServerModeActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
    public Boolean applyBLE(){
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(ChooseModeActivity.this, "您的设备不支持低功耗蓝牙", Toast.LENGTH_SHORT).show();
            return false;
        }else {
            return true;
        }
    }
    public Boolean BLEIsEnable(){
        if(bluetoothAdapter==null||!bluetoothAdapter.isEnabled()) {
            Toast.makeText(ChooseModeActivity.this, "请先打开蓝牙", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}