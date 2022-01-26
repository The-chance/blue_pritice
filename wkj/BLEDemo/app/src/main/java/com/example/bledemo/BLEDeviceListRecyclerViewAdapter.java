package com.example.bledemo;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BLEDeviceListRecyclerViewAdapter extends RecyclerView.Adapter<BLEDeviceListRecyclerViewAdapter.ViewHolder> {
    private List<BluetoothDevice> mBLEList;
    static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView textView;
        public ViewHolder(View view){
            super(view);
            textView=view.findViewById(R.id.ble_item_textview);
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext( )).inflate(R.layout.ble_device_recyclerview_item,parent, false) ;
        ViewHolder holder = new ViewHolder(view) ;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothDevice device=mBLEList.get(holder.getLayoutPosition());
                Intent intent=new Intent();
                intent.putExtra("device_of_success_connent",device);
                intent.setClass(parent.getContext(), SuccessConnentActivity.class);
                parent.getContext().startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice bluetoothDevice=mBLEList.get(mBLEList.size()-position-1);
        String BLEItemInformation="BLE adress = "+bluetoothDevice.getAddress()
                                +"\nBLE name = "+bluetoothDevice.getName();
        holder.textView.setText(BLEItemInformation);
    }

    @Override
    public int getItemCount() {
        if (mBLEList==null) return 0;
        return mBLEList.size();
    }

    public BLEDeviceListRecyclerViewAdapter(List<BluetoothDevice> list){
        mBLEList=list;
    }


}
