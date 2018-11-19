package com.chan.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.chan.controller.service.BluetoothService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter;

    //扫描按钮
    private TextView bluetooth_icon;
    private Button search_btn ;
    //扫描状态
    private Boolean flag = false;
    //适配器列表
    private ListView lv;



    int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

//      初始化蓝牙状态
        initBle();
//      初始化按钮
        intBtn();





//        蓝牙列表

        lv = findViewById(R.id.list_view);
        List<Map<String, Object>> li = new ArrayList<Map<String, Object>>();
        for(int i=0; i<15; i++){
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("id", i);
            map.put("title", "项目" + i);
            li.add(map);
        }

        final SimpleAdapter adapter = new SimpleAdapter(this,li,R.layout.list_item,
                new String[]{"title"},new int[]{R.id.title});
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map<String,Object> map = (Map<String,Object>)adapterView.getItemAtPosition(i);
                Toast.makeText(MainActivity.this,map.get("id").toString(),Toast.LENGTH_SHORT).show();
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
        search_btn = findViewById(R.id.search_bluetooth);
        bluetooth_icon = findViewById(R.id.bluetooth_icon);
        final AnimationDrawable anim = (AnimationDrawable) bluetooth_icon.getBackground();
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag = !flag;
                if(flag){
                    //开始扫描
                    search_btn.setText("搜 索 中 …");
                    anim.start();
                }else{
                    //停止扫描
                    search_btn.setText("搜 索 附 近 蓝 牙");
                    anim.selectDrawable(0);
                    anim.stop();
                }
            }
        });
    }

//    蓝牙列表
    private void 


}
