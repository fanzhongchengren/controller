package com.chan.controller;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
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
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private List<Map<String, Object>> li = new ArrayList<>();
    private SimpleAdapter adapter;

    //创建一个消息处理函数
    private Handler mHandler;

    //intent 数据标志
    int REQUEST_ENABLE_BT = 1;
    //定义蓝牙扫描周期
    private static final long SCAN_PERIOD = 5000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);


        search_btn = findViewById(R.id.search_bluetooth);
        bluetooth_icon = findViewById(R.id.bluetooth_icon);
        anim = (AnimationDrawable) bluetooth_icon.getBackground();


        //Android6.0需要动态申请权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},1);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
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
        adapter = new SimpleAdapter(this,li,R.layout.list_item,
                new String[]{"device"},new int[]{R.id.title});
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map<String,Object> map = (Map<String,Object>)adapterView.getItemAtPosition(i);
                Toast.makeText(MainActivity.this,map.get("title").toString(),Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this,MonitorActivity.class);
                startActivity(intent);
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
        search_btn.setText("搜 索 中 …");
        anim.start();
    }
    private void stopScanAnim(){
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
                    Toast.makeText(MainActivity.this, "扫描结束", Toast.LENGTH_SHORT).show();
                    scanning = false;
                    stopScanAnim();
                    mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
                }
            },SCAN_PERIOD);
            scanning = true;
            mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
        }else{
            scanning = false;
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            Map<String,Object> map = new HashMap<>();
            map.put("device", result);
            li.add(map);
            adapter.notifyDataSetChanged();

            System.out.println("haha"+result.getDevice().toString());

            Toast.makeText(MainActivity.this, "onScanResult", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Toast.makeText(MainActivity.this, "onBatchScanResults", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Toast.makeText(MainActivity.this, "onScanFailed", Toast.LENGTH_SHORT).show();
            System.out.println("haha:" + errorCode);
        }
    };


}
