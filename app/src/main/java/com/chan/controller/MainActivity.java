package com.chan.controller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);


//        查询按钮
        Button searchbtn = (Button) findViewById(R.id.search_bluetooth);
//        searchbtn.setOnClickListener();




//        蓝牙列表
        ListView lv = (ListView) findViewById(R.id.list_view);
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


}