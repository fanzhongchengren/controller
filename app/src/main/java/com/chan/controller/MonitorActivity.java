package com.chan.controller;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chan.controller.service.BluetoothService;

import java.util.List;


/**
 * Created by chenzhengyang on 2018/11/12.
 */
public class MonitorActivity extends AppCompatActivity {

    //蓝牙4.0的UUID,其中0000ffe1-0000-1000-8000-00805f9b34fb是广州汇承信息科技有限公司08蓝牙模块的UUID
    public static String HEART_RATE_MEASUREMENT = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static String EXTRAS_DEVICE_RSSI = "RSSI";

    //数据
    private Bundle b;
    //蓝牙名、地址、信号
    private String deviceName;
    private String deviceAddress;
    private String deviceRssi;

    //管理后台的 BluetoothService
    private static BluetoothService mBluetoothService;

    private Handler mHandler = new Handler();

    //发送指令
    private Button sendBtn;
    private final byte cmd[] = {(byte)0x11, (byte)0x22, (byte)0x33, (byte)0x44, (byte)0x55};
    private BluetoothGattCharacteristic command;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_monitor);

        b = getIntent().getExtras();
        deviceName = b.getString(EXTRAS_DEVICE_NAME);
        deviceAddress = b.getString(EXTRAS_DEVICE_ADDRESS);
        deviceRssi = b.getString(EXTRAS_DEVICE_RSSI);

        //启动蓝牙
        Intent serviceIntent = new Intent(this, BluetoothService.class);
        bindService(serviceIntent, mServiceConnection,BIND_AUTO_CREATE);

        initBtn();

    }

    private void initBtn() {
        sendBtn = findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    command.setValue(cmd);
                    mBluetoothService.writeCharacteristic(command);
                }catch(Exception e) {

                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        unregisterReceiver(bluetoothBroadcastReciver);
        mBluetoothService.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(bluetoothBroadcastReciver, bIntentFilter());
        if (mBluetoothService != null) {
            mBluetoothService.connectDevice(deviceAddress);
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Toast.makeText(MonitorActivity.this, "onServiceConnected", Toast.LENGTH_SHORT).show();
            //建立连接后 获取服务（自定义 getService 实现）
            mBluetoothService = ((BluetoothService.LocalBinder) iBinder).getService();
            if (!mBluetoothService.init()) {
                Toast.makeText(MonitorActivity.this, "打开蓝牙失败", Toast.LENGTH_SHORT).show();
                finish();
            }
            mBluetoothService.connectDevice(deviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Toast.makeText(MonitorActivity.this, "服务断开", Toast.LENGTH_SHORT).show();
            mBluetoothService = null;
        }

        @Override
        public void onBindingDied(ComponentName name) {
            Toast.makeText(MonitorActivity.this, "onBindingDied", Toast.LENGTH_SHORT).show();
        }
    };

    private static IntentFilter bIntentFilter () {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private BroadcastReceiver bluetoothBroadcastReciver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            TextView status = findViewById(R.id.status);
            TextView address = findViewById(R.id.address);
            status.setText(deviceAddress);
            address.setText(action);
            if (BluetoothService.ACTION_GATT_CONNECTED.equals(action)) {
                //连接成功
                System.out.println("haha:连接成功");
//                mBluetoothService.setCharacteristicNotification(characterisic,true);
                mBluetoothService.readRemoteRssi();
            }else if (BluetoothService.ACTION_DATA_AVAILABLE.equals(action)) {
                //有效数据
                System.out.println("haha:有效数据"+intent.getExtras().getString(
                        mBluetoothService.EXTRA_DATA));
            }else if (BluetoothService.ACTION_GATT_DISCONNECTED.equals(action)) {
                //断开连接
                System.out.println("haha:断开连接");
            }else if (BluetoothService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                //发现服务
                System.out.println("haha:发现服务");
                List<BluetoothGattService> gattServices = mBluetoothService.getServices();
                for (BluetoothGattService gattService : gattServices) {
                    List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                    for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                        if (gattCharacteristic.getUuid().toString().equals(HEART_RATE_MEASUREMENT)) {
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mBluetoothService.readCharacteristic(gattCharacteristic);
                                }
                            }, 200);
                            mBluetoothService.setCharacteristicNotification(gattCharacteristic, true);
                            command = gattCharacteristic;
                        }
                    }
                }

            }
        }
    };

}
