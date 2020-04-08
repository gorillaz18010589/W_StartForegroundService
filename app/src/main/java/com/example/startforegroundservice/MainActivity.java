package com.example.startforegroundservice;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
//參考影片:https://www.youtube.com/watch?v=xxC4-BLNSSc
//目的:產生推撥,利用前台活在背景,每秒將到數時間推撥
//1.XML配置
//2.Service創建,並且在ManiFast開啟前景權限
//3.要前景權限
//4.按下按鈕將使用者輸入的參數intent給Service接收
//6.取得指定的Service傳回來的intent
//7.廣播配置,每秒都接收
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnStart;
    private EditText editTxt;
    private TextView txtMsg,txtService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //3.要前景權限
        btnStart = findViewById(R.id.btnStart);
        editTxt = findViewById(R.id.editNum);
        txtMsg = findViewById(R.id.txtMsg);
        txtService = findViewById(R.id.txtService);
        btnStart.setOnClickListener(this);


        //6.取得指定的Service傳回來的intent
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("Counter"); //篩選器取得ActionCounter

        //7.廣播配置,每秒都接收
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Integer integerTime = intent.getIntExtra("timeRemaining",-1); //取得SerVice傳來的每秒參數
                txtMsg.setText(integerTime.toString()); //將每秒顯示在txtView
                Log.v("hank","onReceive:" + "/integerTime:" + integerTime.toString());
            }
        };

        //8.註冊廣播
        registerReceiver(broadcastReceiver,intentFilter);//註冊廣播(1.Receiver類別 ,2.自訂的篩選器)
    }





    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                mStartService();
                break;
        }
    }

    //4.按下按鈕將使用者輸入的參數intent給Service接收
    private void mStartService() {
        Intent intentService = new Intent(this,MyService.class);
        Integer integerTimeSet =  Integer.parseInt(editTxt.getText().toString()); //使用者輸入的剩餘時間參數
        intentService.putExtra("TimaValue",integerTimeSet);
        startService(intentService);
    }


    public void isServiceLive(View view) {
        boolean isWorked = MyService.isWorked(this);
        txtService.setText(isWorked +"");
        Log.v("hank","" + isWorked);
    }
}
