package com.chan.controller.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import static android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED;

public class BluetoothService extends Service {

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String deviceAddress;
    private BluetoothGatt mBluetoothGatt;

    //定义连接状态 默认 断开
    private int connectionState = STATE_DISCONNECTED;

    //蓝牙的三种连接状态 断开 正在连接  已连接
    private static final int STATE_DISCONNECTER = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    //定义传递的 IBinder
    private final IBinder mBinder = new LocalBinder();


    public BluetoothService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Toast.makeText(this, "onBind", Toast.LENGTH_SHORT).show();
        return mBinder;
    }

//    mBluetoothGatt = mBluetoothAdapter.getR



    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }


    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    public boolean connectDevice(final String address) {

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mBluetoothGatt = device.connectGatt(this,false,mGattCallback);
        return false;
    }

    //远程回调函数
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback(){

    };


    public boolean init() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        }
        return true;
    }


}
