package com.example.bledemo;



import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ClientModeActivity extends AppCompatActivity implements View.OnClickListener{

    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG,"ScanCallback   onScanResult");
                    BluetoothDevice device=result.getDevice();
                    if(device!=null){
                        count++;
                        Log.d(TAG,"------- "+count+" -------");
                        Log.d(TAG,"name--->"+device.getName());
                        Log.d(TAG,"address--->"+device.getAddress());
                        String progress="正在扫描 "+count*10+"%";
                        progressTextView.setText(progress);
                        if(NewBLEDeviceList.size()!=0){
                            Boolean isExist=false;
                            for(BluetoothDevice bluetoothDevice: NewBLEDeviceList){
                                if(device.getAddress().equals(bluetoothDevice.getAddress())){
                                    isExist=true;
                                    break;
                                }
                            }
                            if(!isExist)
                                NewBLEDeviceList.add(device);
                            buildNewDeviceRecyclerView(NewBLEDeviceList);
                        }else{
                            NewBLEDeviceList.add(device);
                            buildNewDeviceRecyclerView(NewBLEDeviceList);
                        }
                        if(bluetoothLeScanner!=null&&count==10){
                            bluetoothLeScanner.stopScan(leScanCallback);
                            progressTextView.setText("扫描完成");
                            count=0;
                            Log.d(TAG,"stopScan...");
                        }
                    }
                }
            }).start();
        }
    };

    private TextView progressTextView;
    private static int count=0;
    private static String TAG="ClientModeActivity";
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private BluetoothLeScanner bluetoothLeScanner;
    private List<BluetoothDevice> NewBLEDeviceList =new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_mode);
        initView();
    }
    public void initBLE(){
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    }
    public void getOldDevice(){
        //得到本机已配对过的devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        List<BluetoothDevice> OldBLEDeviceList=new ArrayList<>();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device: pairedDevices) {
                String deviceName = device.getName();
                String deviceMACAddress = device.getAddress();
                Log.d(TAG,"pairedDevices Name --->"+deviceName);
                Log.d(TAG,"pairedDevices Address --->"+deviceMACAddress);
                OldBLEDeviceList.add(device);
            }
            buildOldDeviceRecyclerView(OldBLEDeviceList);
        }
    }
    public void getSomePermission(){
        if((ContextCompat.checkSelfPermission(ClientModeActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(ClientModeActivity.this,new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},1);
        }
    }

    public void buildOldDeviceRecyclerView(List<BluetoothDevice> list){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.old_device_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        BLEDeviceListRecyclerViewAdapter adapter = new BLEDeviceListRecyclerViewAdapter(list);
        recyclerView.setAdapter(adapter);
    }
    public void buildNewDeviceRecyclerView(List<BluetoothDevice> list){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recyclerView = (RecyclerView) findViewById(R.id.new_device_recyclerview);
                layoutManager = new LinearLayoutManager(ClientModeActivity.this);
                recyclerView.setLayoutManager(layoutManager);
                BLEDeviceListRecyclerViewAdapter adapter = new BLEDeviceListRecyclerViewAdapter(list);
                recyclerView.setAdapter(adapter);
            }
        });
    }
    public void initView(){
        Button openBLEBtn=findViewById(R.id.start_scan_btn);
        Button closeBLEBtn=findViewById(R.id.stop_scan_btn);
        openBLEBtn.setOnClickListener(this);
        closeBLEBtn.setOnClickListener(this);
        progressTextView=findViewById(R.id.progress_text_veiw);

    }
    private void scanLeDevice() {
        bluetoothLeScanner.startScan(leScanCallback);
        Log.d(TAG,"scanLeDevice...");
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_scan_btn:
                initBLE();
                //判断设备是否支持低功耗蓝牙
                if (!applyBLE())
                    break;
                //判断蓝牙是否打开
                if(!BLEIsEnable())
                    break;
                getSomePermission();
                getOldDevice();
                scanLeDevice();
                String progress="正在扫描 0%";
                progressTextView.setText(progress);
                break;
            case R.id.stop_scan_btn:
                if(bluetoothLeScanner!=null) {
                    bluetoothLeScanner.stopScan(leScanCallback);
                    progressTextView.setText("扫描结束");
                    Log.d(TAG,"stopScan...");
                }
                break;
            default:
                break;
        }
    }
    public void discovery(){
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
        Log.d(TAG,"discovery...");

    }
    public Boolean applyBLE(){
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(ClientModeActivity.this, "您的设备不支持低功耗蓝牙", Toast.LENGTH_SHORT).show();
            return false;
        }else {
            return true;
        }
    }
    public Boolean BLEIsEnable(){
        if(bluetoothAdapter==null||!bluetoothAdapter.isEnabled()) {
            Toast.makeText(ClientModeActivity.this, "请先打开蓝牙", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        if(bluetoothLeScanner!=null){
            bluetoothLeScanner.stopScan(leScanCallback);
            Log.d(TAG,"stopScan...");
        }
        Log.d(TAG,"onDestroy...");
        super.onDestroy();
    }
}