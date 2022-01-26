package com.example.bledemo;

import static android.bluetooth.le.AdvertiseSettings.ADVERTISE_MODE_LOW_POWER;
import static com.example.bledemo.ChooseModeActivity.UUID_CHAR_READ;
import static com.example.bledemo.ChooseModeActivity.UUID_CHAR_WRITE;
import static com.example.bledemo.ChooseModeActivity.UUID_DESCRIPTOR;
import static com.example.bledemo.ChooseModeActivity.UUID_SERVER;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ServerModeActivity extends AppCompatActivity implements View.OnClickListener{


    private final BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            ServerModeActivity.this.device = device;
            String state = "";
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                state = "连接成功";
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                state = "连接断开";

            }
            Log.e(TAG, "onConnectionStateChange device=" + device.toString() + " status=" + status + " newState=" + state);
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());

        }
        @Override
        public void onServiceAdded(int status,BluetoothGattService service){
            super.onServiceAdded(status,service);
            Log.e(TAG,"添加服务成功，安卓系统回调该onServiceAdded()方法");
        }
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            if(characteristic.getValue()==null){
                Log.e(TAG, "characteristic.getValue() is null");
                return;
            }
            String data = new String(value);
            Log.e(TAG, "收到了客户端发过来的数据 " + data);
            upDataUI(data,2);
            //告诉客户端发送成功
            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
        }
    };

    private AdvertiseCallback advertiseCallback;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private static String TAG="ServerModeActivity";
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private EditText editTextService;
    private Button sendMessageBtnService;
    private RecyclerView messageRecyclerView;
    private MessageRecyclerViewAdapter adapter;
    private TextView serviceStatusTextView;
    private BluetoothGattServer bluetoothGattServer;
    private BluetoothDevice device;
    private List<String> messageList=new ArrayList<>();
    private BluetoothGattService gattService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_mode);
        initView();
        initBLE();
        addService();
        startAdvertising("来自服务端的蓝牙服务");
    }
    public void initView(){
        serviceStatusTextView=findViewById(R.id.show_status_message_textview);
        sendMessageBtnService = findViewById(R.id.send_message_btn_service);
        sendMessageBtnService.setOnClickListener(this);
        editTextService=findViewById(R.id.message_editTxet_service);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        messageRecyclerView=findViewById(R.id.receive_message_from_client_recyclerView);
        messageRecyclerView.setLayoutManager(layoutManager);
        editTextService =findViewById(R.id.message_editTxet_service);
        sendMessageBtnService =findViewById(R.id.send_message_btn_service);
        adapter=new MessageRecyclerViewAdapter(messageList);
        messageRecyclerView.setAdapter(adapter);
    }
    public void initBLE(){
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }
    /**
     * 创建Ble服务端，接收连接
     */
    public void startAdvertising(String name) {
        //BLE广告设置
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setConnectable(true)//是否可被连接
                .setAdvertiseMode(ADVERTISE_MODE_LOW_POWER)
                .setTimeout(0)//连接超时时间
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)//信号强度，发射功率
                .build();
        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)    //是否在广播中携带设备的名称
                .setIncludeTxPowerLevel(true)  //是否在广播中携带信号强度
                .addServiceUuid(new ParcelUuid(UUID_SERVER))
                .build();
        bluetoothAdapter.setName(name);
        //通过UUID_SERVERE构建
        AdvertiseData scanResponseData = new AdvertiseData.Builder()
                .addServiceUuid(new ParcelUuid(UUID_SERVER))
                .setIncludeTxPowerLevel(true)  //是否在广播中携带设备的名称
                .build();
        //广告创建后的回调
        advertiseCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                Log.e(TAG, "服务广播开启成功 " + settingsInEffect.toString());
                upDataUI("服务广播开启成功",1);
            }
            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
                Log.e(TAG, "Failed to add BLE advertisement, reason: " + errorCode);
                upDataUI("服务广播开启失败 errorCode="+errorCode,1);
            }
        };
        //开启服务
        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        if (bluetoothLeAdvertiser != null) {
            bluetoothLeAdvertiser.startAdvertising(settings, advertiseData, advertiseCallback);
            //bluetoothLeAdvertiser.startAdvertising(settings, advertiseData,scanResponseData, advertiseCallback);
        }else {
            Log.d(TAG,"bluetoothLeAdvertiser is null");
        }
    }

    /**
     * 添加读写服务UUID，特征值等
     */
    private void addService() {
        gattService = new BluetoothGattService(UUID_SERVER, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        //只读的特征值
        BluetoothGattCharacteristic characteristicRead = new BluetoothGattCharacteristic(UUID_CHAR_READ,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        //添加可读characteristic的descriptor
        BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(UUID_DESCRIPTOR, BluetoothGattCharacteristic.PERMISSION_WRITE);
        characteristicRead.addDescriptor(descriptor);
        //只写的特征值
        BluetoothGattCharacteristic characteristicWrite = new BluetoothGattCharacteristic(UUID_CHAR_WRITE,
                BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_READ
                        | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ);
        //将特征值添加至服务里
        gattService.addCharacteristic(characteristicRead);
        gattService.addCharacteristic(characteristicWrite);
        //监听客户端的连接
        bluetoothGattServer = bluetoothManager.openGattServer(this, gattServerCallback);
        //添加服务
        bluetoothGattServer.addService(gattService);
        //bluetoothGattServer.notifyCharacteristicChanged(device,characteristicWrite,true);
    }


    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.send_message_btn_service){
            String message=editTextService.getText().toString();
            sendMessage(message);
            editTextService.setText("");
        }
    }
    /**
     * 发送数据
     *
     * @param "message”
     */
    public void sendMessage(String message){
        if(!message.equals("")){
            if(gattService==null){
                Toast.makeText(ServerModeActivity.this, "gattService is null", Toast.LENGTH_SHORT).show();
                return;
            }
            gattService.getCharacteristic(UUID_CHAR_WRITE).setValue(message);
        }
    }

    @Override
    protected void onDestroy() {
        if(bluetoothAdapter!=null){
        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
        Log.d(TAG,"stop advertising");
        }
        super.onDestroy();
    }
    private void upDataUI(String string,int code){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(code==1) {
                    serviceStatusTextView.setText(string);
                }else if(code==2){
                    messageList.add(string);
                    adapter.notifyItemInserted(messageList.size()-1);
                    messageRecyclerView.scrollToPosition(messageList.size()-1);
                }
            }
        });
    }
}