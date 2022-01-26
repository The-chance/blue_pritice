package com.example.bledemo;

import static com.example.bledemo.ChooseModeActivity.UUID_CHAR_READ;
import static com.example.bledemo.ChooseModeActivity.UUID_CHAR_WRITE;
import static com.example.bledemo.ChooseModeActivity.UUID_SERVER;

import androidx.annotation.UiThread;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.List;

public class SuccessConnentActivity extends AppCompatActivity implements View.OnClickListener {

    private BluetoothDevice device;
    private RecyclerView messageRecyclerView;
    private List<String> messageList;
    private Button sendMessageBtnClient;
    private EditText editTextClient;
    private BluetoothGatt bluetoothGatt;
    private String TAG="ClientModeActivity";
    private MessageRecyclerViewAdapter adapter;
    private ActionBar actionBar;
    private static int  retryTime=0;
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if(status==0){
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.e(TAG, "onConnectionStateChange 连接成功");
                    upDataUI("客户端——连接成功");
                    //查找服务
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                    upDataUI("客户端——连接中...");
                    Log.e(TAG, "onConnectionStateChange 连接中......");
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    upDataUI("客户端——连接断开");
                    Log.e(TAG, "onConnectionStateChange 连接断开");
                    if(gatt!=null){
                        gatt.close();
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                    Log.e(TAG, "onConnectionStateChange 连接断开中......");
                }
            }else{
                gatt.disconnect();
                gatt.close();
                try
                {
                    Thread.sleep(12000);//单位：毫秒
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(status == 133){
                    //提示：此处我们可以根据全局的一个flag，去重试连接一次，以增大连接成功率
                    if(!(retryTime==1)){
                        retryTime++;
                        Log.d(TAG,"status is 133,retry connect,retryTime is "+retryTime);
                        bluetoothGatt = device.connectGatt(SuccessConnentActivity.this, false, bluetoothGattCallback,BluetoothDevice.TRANSPORT_AUTO);
                    }else{
                        upDataUI("客户端——连接失败");
                    }
                }
            }

        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            //设置读特征值的监听，接收服务端发送的数据
            BluetoothGattService service = bluetoothGatt.getService(UUID_SERVER);
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_CHAR_READ);
            boolean b = bluetoothGatt.setCharacteristicNotification(characteristic, true);
            Log.e(TAG, "onServicesDiscovered 设置通知 " + b);
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            String data = new String(characteristic.getValue());
            messageList.add(data);
            adapter.notifyItemInserted(messageList.size()-1);
            messageRecyclerView.scrollToPosition(messageList.size()-1);
            Log.e(TAG, "onCharacteristicChanged 接收到了数据 " + data);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success_connent);
        Intent intent = getIntent();
        device=intent.getParcelableExtra("device_of_success_connent");
        //device就是我们扫描回调内拿到的设备（BleChatServer）
        bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback,BluetoothDevice.TRANSPORT_LE);
//        if(bluetoothGatt!=null){
//            bluetoothGatt.close();
//        }
//        Log.d(TAG,"bluetoothGatt.toString()--->"+bluetoothGatt.toString());
//        for(BluetoothGattService service:bluetoothGatt.getServices()){
//            Log.d(TAG,"UUID_Service--->"+service.getUuid());
//            for(BluetoothGattCharacteristic characteristic:service.getCharacteristics()){
//                Log.d(TAG,"UUID_Characteristic--->"+service.getUuid());
//            }
//        }
        initView();
    }

    private void initView() {
        actionBar=getSupportActionBar();
        sendMessageBtnClient=findViewById(R.id.send_message_btn_client);
        sendMessageBtnClient.setOnClickListener(this);
        editTextClient=findViewById(R.id.message_editTxet_client);
        messageRecyclerView=findViewById(R.id.receive_message_from_service_recyclerView);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        messageRecyclerView.setLayoutManager(layoutManager);
        adapter=new MessageRecyclerViewAdapter(messageList);
        messageRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.send_message_btn_client){
            String message=editTextClient.getText().toString();
            sendMessage(message);
            editTextClient.setText("");
        }
    }
    /**
     * 发送数据
     *
     * @param "message”
     */
    public void sendMessage(String message){
        if(!message.equals("")){
            //找到服务
            BluetoothGattService service = bluetoothGatt.getService(UUID_SERVER);
            //拿到写的特征值
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_CHAR_WRITE);
            bluetoothGatt.setCharacteristicNotification(characteristic, true);
            characteristic.setValue(message.getBytes());
            bluetoothGatt.writeCharacteristic(characteristic);
            Log.e(TAG, "sendData 发送数据成功");
        }
    }
    private void upDataUI(String string){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                actionBar.setTitle(string);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(bluetoothGatt!=null){
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
        super.onDestroy();
    }
}