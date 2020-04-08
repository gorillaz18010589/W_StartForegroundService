package com.example.startforegroundservice;
//參考影片:https://www.youtube.com/watch?v=xxC4-BLNSSc
//5.onStartCommand取得使用者輸入的剩餘時間參數,排定每秒,剩餘時間都-1,每秒都設定Action intent給廣播接收
//9.用Notifaction利用前景,每秒排成,將倒數時間顯示在推波通知上
//10.創建推撥,設定Title內容,內容設定為倒數時間
//11.將推撥設定於前警啟動
//12.建立推撥頻道
//13.設定當使用者點及下去到App

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {
    private String TAG = "hank";

    private final static String CHANNEL_ID = "NotificationChannelId";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "Service:onCreate()");
    }

    //5.onStartCommand取得使用者輸入的剩餘時間參數,排定每秒,剩餘時間都-1,每秒都設定Action intent給廣播接收
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "Service:onStartCommand()");

        //取得使用者輸入的剩餘時間參數
        final Integer[] timeRemaining = {intent.getIntExtra("TimaValue", -1)};//取得Acitivty取得的使用者輸入剩餘時間

        //排定每秒,剩餘時間都-1
        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                //設定IntenAction =>給Activity廣播接收處理
                Intent intentLocal = new Intent();
                intentLocal.setAction("Counter");

                timeRemaining[0]--; //每一秒剩餘時間都-1


                //9.用Notifaction利用前景,每秒排成,將倒數時間顯示在推波通知上
                NotificationUpdate(timeRemaining[0]);

                //如果時間小於等於0任務解除
                if (timeRemaining[0] <= 0) {
                    timer.cancel();
                    timer.purge();
                }

                Log.v(TAG, "scheduleAtFixedRate=>時間倒數TASK:" + timeRemaining[0].toString());
                //將每秒的剩餘時間參數,用Activity intent傳給廣播接受
                intentLocal.putExtra("timeRemaining", timeRemaining[0]);
                sendBroadcast(intentLocal);
            }
        }, 0, 1000);
        return super.onStartCommand(intent, flags, startId);
    }

    //startForeground(int id, Notification notification)://開始創建於前景(1.id ,2.要被設置於前景的推撥)

    //NotificationCompat物件,處理Notification的訊息跟ui設定等
    //NotificationCompat.Builder(Context context,String channelId):Notification物件Builder(1.context,2.頻道id)
    //NotificationCompat.setContentTitle(CharSequence title)://設定推撥Title
    //NotificationCompat.setContentText(CharSequence text)://設定推撥內容
    //NotificationCompat.setSmallIcon(int icon): //設定推撥icon
    //NotificationCompat.setContentIntent(PendingIntent intent)://設定使用者點及推撥可以跳轉的Intent


    //NotificationManager.createNotificationChannel(@NonNull NotificationChannel channel):推撥經理人創建頻道(1.要被創建的推撥頻道)
    //NotificationChannel(String id, CharSequence name, @Importance int importance):頻道建構(1.要連接的頻道ID, 2.頻道名稱, 3.頻道狀態)(建構式)
    // @Nullable <T> T getSystemService(@NonNull Class<T> serviceClass):取得系統服務員(要使用的Service類別)
    //9.用Notifaction利用前景,每秒排成,將倒數時間顯示在推波通知上
    private void NotificationUpdate(Integer time) {
        try {

            //13.設定當使用者點及下去到App
            Intent notficationIntent = new Intent(this, MainActivity.class);
            final PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[]{notficationIntent}, 0); //從PendingIntent,取得這個Activity

            //10.創建推撥,設定Title內容,內容設定為倒數時間
            final Notification[] notifications = {new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("ContentTitle") //設定推撥Title
                    .setContentText("ContentText" + "倒數時間:" + time.toString()) //設定推撥內容
                    .setSmallIcon(R.drawable.ic_launcher_foreground) //設定推撥icon
                    .setContentIntent(pendingIntent) //設定使用者點及推撥可以跳轉的Intent
                    .build()}; //建立推撥

            //11.將推撥設定於前警啟動
            startForeground(1, notifications[0]); //將推撥設定於前警啟動(1.id ,2.準備好的Notfication設定)

            //12.建立推撥頻道
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {  //版本大於8的話
                NotificationChannel notificationChannel = new NotificationChannel(
                        CHANNEL_ID, //1.要連接的頻道ID
                        "My Counter Service", //2.頻道名稱
                        NotificationManager.IMPORTANCE_DEFAULT); //頻道狀態
                NotificationManager notificationManager = getSystemService(NotificationManager.class); //取得推撥經理人
                notificationManager.createNotificationChannel(notificationChannel);//創建一個推撥頻道(自己寫的推撥頻道物件實體)
            }

            Log.v(TAG, "NotificationUpdate");
        } catch (Exception e) {
            Log.v(TAG, "錯誤:" + e.toString());
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "Service:onDestroy()");
    }

    //檢查我這隻APP是否SerVice還存活著
    public static boolean isWorked(Context context) {
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString().equals("MyService")) ;
            {
                return true;
            }
        }
        return false;
    }
}
