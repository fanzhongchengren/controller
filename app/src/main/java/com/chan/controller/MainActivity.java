package com.chan.controller;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //蓝牙适配器
    BluetoothAdapter mBluetoothAdapter;

    //扫描按钮
    private TextView bluetooth_icon;
    private Button search_btn ;
    private AnimationDrawable anim;
    //按钮状态
    private Boolean flag = false;
    //扫描状态
    private Boolean scanning = false;
    //适配器列表
    private ListView lv;
    private BluetoothListAdapter adapter;

    //创建一个消息处理函数
    private Handler mHandler;

    //intent 数据标志
    int REQUEST_ENABLE_BT = 1;
    //定义蓝牙扫描周期
    private static final long SCAN_PERIOD = 10000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        search_btn = findViewById(R.id.search_bluetooth);
        bluetooth_icon = findViewById(R.id.bluetooth_icon);
        anim = (AnimationDrawable) bluetooth_icon.getBackground();

        //Android5.0及以上 还 需要动态申请权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},1);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Toast.makeText(this, "打开权限", Toast.LENGTH_SHORT).show();
            }
        }

        //初始化蓝牙状态
        initBle();

        //初始化按钮
        intBtn();

        //初始化UI
        mHandler = new Handler();
        //蓝牙列表
        lv = findViewById(R.id.list_view);
        adapter = new BluetoothListAdapter();
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final BluetoothDevice d = adapter.getDevice(i);
                if (d == null) { return; }
                final Intent intent = new Intent(MainActivity.this,MonitorActivity.class);
                intent.putExtra(MonitorActivity.EXTRAS_DEVICE_NAME, d.getName());
                intent.putExtra(MonitorActivity.EXTRAS_DEVICE_ADDRESS, d.getAddress());
                intent.putExtra(MonitorActivity.EXTRAS_DEVICE_RSSI, adapter.rssis.get(i).toString());
                if (scanning) {
                    stopScanAnim();
                    mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
                    scanning = false;
                }
                try{
                    startActivity(intent);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

//    初始化蓝牙
    private void initBle(){
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this,"设备不支持BLE类型蓝牙",Toast.LENGTH_SHORT).show();
            finish();
        }
        //否则，获取设备服务和蓝牙适配器
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()){
            Intent enableBLEIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBLEIntent,REQUEST_ENABLE_BT);
        }
    }

//    初始化扫描按钮
    private void intBtn(){
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag = !flag;
                if(flag){
                    //开始扫描
                    startScanAnim();
                    scanDevice(true);
                }else{
                    //停止扫描
                    stopScanAnim();
                    scanDevice(false);
                }
            }
        });
    }

    private void startScanAnim(){
        flag = true;
        search_btn.setText("搜 索 中 …");
        anim.start();
    }
    private void stopScanAnim(){
        flag = false;
        search_btn.setText("搜 索 附 近 蓝 牙");
        anim.selectDrawable(0);
        anim.stop();
    }

//    蓝牙列表
    private void scanDevice(final boolean enable){
        if(enable){
            //一个扫描周期过后需要停止扫描   如何保证不会添加多个消息 ？
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScanAnim();
                    mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
                    scanning = false;
                }
            },SCAN_PERIOD);
            mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
            scanning = true;
        }else{
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
            scanning = false;
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            adapter.addDevice(result.getDevice(),result.getRssi());
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Toast.makeText(MainActivity.this, "onBatchScanResults", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Toast.makeText(MainActivity.this, "onScanFailed: "+ errorCode, Toast.LENGTH_SHORT).show();
        }
    };

    private class BluetoothListAdapter extends BaseAdapter {

        private ArrayList<BluetoothDevice> mDevices;
        private ArrayList<Integer> rssis;
        private LayoutInflater mInflator;

        public BluetoothListAdapter(){
            super();
            rssis = new ArrayList<>();
            mDevices = new ArrayList<>();
            mInflator = getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device, int rssi) {
            if (!mDevices.contains(device)) {
                mDevices.add(device);
                rssis.add(rssi);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mDevices.get(position);
        }

        public void clear() {
            mDevices.clear();
            rssis.clear();
        }

        @Override
        public int getCount() {
            return mDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = mInflator.inflate(R.layout.list_item,null);
            TextView device_address = view.findViewById(R.id.device_address);
            TextView device_name = view.findViewById(R.id.device_name);
            TextView device_rssi = view.findViewById(R.id.device_rssi);

            BluetoothDevice device = mDevices.get(i);
            device_address.setText("MAC: " + device.getAddress());
            device_name.setText(device.getName());
            device_rssi.setText("RSSI: " + rssis.get(i));

            return view;
        }
    }

}
