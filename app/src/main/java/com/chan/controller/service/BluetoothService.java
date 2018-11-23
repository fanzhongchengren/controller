package com.chan.controller.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import java.util.List;

public class BluetoothService extends Service {

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String deviceAddress;
    private BluetoothGatt mBluetoothGatt;

    //蓝牙的三种连接状态 断开 正在连接  已连接
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    //定义连接状态 默认 断开
    private int connectionState = STATE_DISCONNECTED;

    //定义传递的 IBinder
    private final IBinder mBinder = new LocalBinder();

    public final static String ACTION_GATT_CONNECTED = "com.chan.controller.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.chan.controller.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.chan.controller.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.chan.controller.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.chan.controller.bluetooth.le.EXTRA_DATA";

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

    public void connectDevice(final String address) {

        if (mBluetoothAdapter == null || address == null) {
            System.out.println("连接蓝牙时失败");
        }
        //如果已经创建了连接mBluetoothGatt，那么无需重新创建，只需使用 connect 方法 重新连接即可
        if (deviceAddress != null && address.equals(deviceAddress) && mBluetoothGatt != null) {
            if (mBluetoothGatt.connect()) {
                connectionState = STATE_CONNECTING;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Toast.makeText(this, "获取设备失败", Toast.LENGTH_SHORT).show();
        }
        //新建连接服务 设置相关变量
        mBluetoothGatt = device.connectGatt(this,false,mGattCallback);
        deviceAddress = address;
        connectionState = STATE_CONNECTING;
    }

    //远程回调函数
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback(){

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                updateBroadcast(ACTION_DATA_AVAILABLE,characteristic);
                System.out.println("haha:"+ characteristic);
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectionState = STATE_CONNECTED;
                updateBroadcast(ACTION_GATT_CONNECTED);
                mBluetoothGatt.discoverServices();
            }else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectionState = STATE_DISCONNECTED;
                updateBroadcast(ACTION_GATT_DISCONNECTED);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                updateBroadcast(ACTION_GATT_SERVICES_DISCOVERED);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            updateBroadcast(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            updateBroadcast(ACTION_DATA_AVAILABLE, rssi);
        }

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

    private void updateBroadcast(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void updateBroadcast(final String action, int rssi) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_DATA,String.valueOf(rssi));
        sendBroadcast(intent);
    }

    private void updateBroadcast(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data) {
                //以十六进制输出,2为指定的输出字段的宽度.如果位数小于2,则左端补0
                System.out.println("haha:+++" + String.format("%02X", byteChar));
                stringBuilder.append(String.format("%02X", byteChar));
            }
            intent.putExtra(EXTRA_DATA, stringBuilder.toString());
            sendBroadcast(intent);
        }

    }

    public void disconnect() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void readRemoteRssi() {
        if (mBluetoothGatt == null || mBluetoothAdapter == null) {
            return;
        }
        mBluetoothGatt.readRemoteRssi();
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothGatt == null || mBluetoothAdapter == null) {
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothGatt == null || mBluetoothAdapter == null) {
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothGatt == null || mBluetoothAdapter == null) {
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }


    public List<BluetoothGattService> getServices() {
        if (mBluetoothGatt == null || mBluetoothAdapter == null) {
            return null;
        }
        return mBluetoothGatt.getServices();
    }

}
