package com.example.bledemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d(TAG,"ScanCallback   onScanResult");
            BluetoothDevice device=result.getDevice();
            if(device!=null){
                count++;
                Log.d(TAG,"------- "+count+" -------");
                Log.d(TAG,"name--->"+device.getName());
                Log.d(TAG,"address--->"+device.getAddress());
                String progress="正在扫描 "+count*10+"%";
                progressTextView.setText(progress);
                if(BLElist.size()!=0){
                    Boolean isExist=false;
                    for(BluetoothDevice bluetoothDevice:BLElist){
                        if(device.getAddress().equals(bluetoothDevice.getAddress())){
                            isExist=true;
                            break;
                        }
                    }
                    if(!isExist)
                    BLElist.add(device);
                }else{
                    BLElist.add(device);
                }
                if(bluetoothLeScanner!=null&&count==10){
                    bluetoothLeScanner.stopScan(leScanCallback);
                    progressTextView.setText("扫描完成");
                    count=0;
                    Log.d(TAG,"stopScan...");
                    buildRecyclerView(BLElist);
                }
            }
        }

    };
    private TextView progressTextView;
    private static int count=0;
    private static String TAG="MainActivity";
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning;
    private Handler handler = new Handler();
    private static final long SCAN_PERIOD = 10000;//扫描10s
    private List<BluetoothDevice> BLElist=new ArrayList<>();
   // private IntentFilter intentFilter;
    //private BLEReceiver bleReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
//        intentFilter=new IntentFilter();
//        intentFilter.addAction("BluetoothDevice.ACTION_FOUND");
//        bleReceiver=new BLEReceiver();
//        registerReceiver(bleReceiver,intentFilter);

    }
    public void permissionIsEnable(){
        if((ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},1);
        }
//        if((ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED)){
//            ActivityCompat.requestPermissions(MainActivity.this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},1);
//        }
    }
//    class BLEReceiver extends BroadcastReceiver{
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.d(TAG,"Found");
//            BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//            BLElist.add(device);
//            buildRecyclerView(BLElist);
//        }
//    }

    public void buildRecyclerView(List<BluetoothDevice> list){
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        BLEListRecyclerAdapter adapter = new BLEListRecyclerAdapter(list);
        recyclerView.setAdapter(adapter);
    }
    public void initView(){
        Button openBLEBtn=findViewById(R.id.open_BLE_btn);
        Button closeBLEBtn=findViewById(R.id.close_BLE_btn);
        openBLEBtn.setOnClickListener(this);
        closeBLEBtn.setOnClickListener(this);
        progressTextView=findViewById(R.id.progress_text_veiw);

    }
    private void scanLeDevice() {
//            if(bluetoothLeScanner==null) Log.d(TAG,"bluetoothLeScanner--->null");
//            else if(leScanCallback==null) Log.d(TAG,"leScanCallback--->null");
            bluetoothLeScanner.startScan(leScanCallback);
        Log.d(TAG,"scanLeDevice...");
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.open_BLE_btn:
                bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                bluetoothAdapter = bluetoothManager.getAdapter();
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                //得到本机已配对过的devices
//                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
//                if (pairedDevices.size() > 0) {
//                    for (BluetoothDevice device: pairedDevices) {
//                        String deviceName = device.getName();
//                        String deviceMACAddress = device.getAddress();
//                        Log.d(TAG,"pairedDevices Name --->"+deviceName);
//                        Log.d(TAG,"pairedDevices Address --->"+deviceMACAddress);
//                        BLElist.add(device);
//                    }
//                    buildRecyclerView(BLElist);
//                }
                //判断设备是否支持低功耗蓝牙
                if (!applyBLE())
                    break;
                //判断蓝牙是否打开
                if(!BLEIsEnable())
                    break;
                permissionIsEnable();
                scanLeDevice();
                String progress="正在扫描 0%";
                progressTextView.setText(progress);
                //discovery();
                break;
            case R.id.close_BLE_btn:
                if(bluetoothLeScanner!=null) {
                    bluetoothLeScanner.stopScan(leScanCallback);
                    Log.d(TAG,"stopScan...");
                }
                if (bluetoothAdapter!=null)
                if(bluetoothAdapter.isDiscovering()){
                    bluetoothAdapter.cancelDiscovery();
                    Log.d(TAG,"cancelDiscovery...");
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
            Toast.makeText(MainActivity.this, "您的设备不支持低功耗蓝牙", Toast.LENGTH_SHORT).show();
            return false;
        }else {
            return true;
        }
    }
    public Boolean BLEIsEnable(){
        if(bluetoothAdapter==null||!bluetoothAdapter.isEnabled()) {
            Toast.makeText(MainActivity.this, "请先打开蓝牙", Toast.LENGTH_SHORT).show();
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
        //unregisterReceiver(bleReceiver);
        if (bluetoothAdapter!=null)
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG,"cancelDiscovery...");
        }
        Log.d(TAG,"onDestroy...");
        super.onDestroy();
    }
}