package com.example.bledemo;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BLEListRecyclerAdapter extends RecyclerView.Adapter<BLEListRecyclerAdapter.ViewHolder> {
    private List<BluetoothDevice> mBLEList;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext( )).inflate(R.layout.recyclerview_item_layout,parent, false) ;
        ViewHolder holder = new ViewHolder(view) ;
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

    public BLEListRecyclerAdapter(List<BluetoothDevice> list){
        mBLEList=list;
    }
    static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView textView;
        public ViewHolder(View view){
            super(view);
            textView=view.findViewById(R.id.ble_item_textview);
        }
    }
}
