package com.chan.controller;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chan.controller.service.BluetoothService;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by chenzhengyang on 2018/11/12.
 */
public class MonitorActivity extends AppCompatActivity implements OnChartValueSelectedListener {

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
    private TextView upBtn;
    private TextView downBtn;
    private TextView leftBtn;
    private TextView rightBtn;
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


        LineChart mLineChart = findViewById(R.id.lineChart);
        //显示边界
//        mLineChart.setDrawBorders(true);
        //设置数据
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            entries.add(new Entry(i, (float) (Math.random()) * 80));
        }
        //一个LineDataSet就是一条线
        LineDataSet lineDataSet = new LineDataSet(entries, "温度");
        LineData data = new LineData(lineDataSet);
        mLineChart.setData(data);

        BarChart mBarChart = findViewById(R.id.barChart);
        mBarChart.setOnChartValueSelectedListener(this);

        mBarChart.setDrawBarShadow(false);
        mBarChart.setDrawValueAboveBar(true);

        mBarChart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mBarChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        mBarChart.setPinchZoom(false);

        mBarChart.setDrawGridBackground(false);
        // chart.setDrawYLabels(false);

        //x轴数据
        List<String> xData = new ArrayList<>();
        for (int i = 0; i <= 20; i++) {
            xData.add(String.valueOf(i));
        }
        //y轴数据集合
        List<List<Float>> yBarDatas = new ArrayList<>();
        //4种直方图
        for (int i = 0; i < 4; i++) {
            //y轴数
            List<Float> yData = new ArrayList<>();
            for (int j = 0; j <= 20; j++) {
                yData.add((float) (Math.random() * 100));
            }
            yBarDatas.add(yData);
        }
        //名字集合
        List<String> barNames = new ArrayList<>();
        barNames.add("直方图一");
        barNames.add("直方图二");
        barNames.add("直方图三");
        barNames.add("直方图四");
        //颜色集合
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.BLUE);
        colors.add(Color.RED);
        colors.add(Color.YELLOW);
        colors.add(Color.CYAN);

        mBarChart.setData(getBarData(yBarDatas.get(0),barNames.get(0),colors.get(0)));



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

        upBtn = findViewById(R.id.up);
        downBtn = findViewById(R.id.down);
        leftBtn = findViewById(R.id.left);
        rightBtn = findViewById(R.id.right);
        upBtn.setOnClickListener(new View.OnClickListener() {
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
            TextView devName = findViewById(R.id.device_name);
            devName.setText("设备：" + deviceName);
            if (BluetoothService.ACTION_GATT_CONNECTED.equals(action)) {
                //连接成功
                System.out.println("haha:连接成功");
                status.setText("状态：已连接");
                mBluetoothService.readRemoteRssi();
            }else if (BluetoothService.ACTION_DATA_AVAILABLE.equals(action)) {
                //有效数据
                System.out.println("haha:有效数据"+intent.getExtras().getString(
                        mBluetoothService.EXTRA_DATA));
            }else if (BluetoothService.ACTION_GATT_DISCONNECTED.equals(action)) {
                //断开连接
                System.out.println("haha:断开连接");
                status.setText("状态：已断开");
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

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }


    private BarData getBarData(List<Float> barChartY, String barName, int barColor) {
        BarData barData = new BarData();
        ArrayList<BarEntry> yValues = new ArrayList<>();
        for (int i = 0; i < barChartY.size(); i++) {
            yValues.add(new BarEntry(i, barChartY.get(i)));
        }

        BarDataSet barDataSet = new BarDataSet(yValues, barName);
        barDataSet.setColor(barColor);
        barDataSet.setValueTextSize(10f);
        barDataSet.setValueTextColor(barColor);
        barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        barData.addDataSet(barDataSet);

        return barData;
    }

}
