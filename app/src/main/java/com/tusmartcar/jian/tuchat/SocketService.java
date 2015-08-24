package com.tusmartcar.jian.tuchat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.*;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.content.*;
import android.support.v4.app.NotificationCompat;

import java.io.*;

public class SocketService extends Service implements ServiceCommunicate{
    public static final int SocketService_to_SimpleSocket = 3;
    public static final int SimpleSocket_to_SocketService = 4;

    private SimpleSocket simpleSocket = null;
    public Handler mHandler = null;
    private Handler mainHandler = null;
    private BufferedWriter bufferedWriter = null;
    private BufferedReader bufferedReader = null;
    private boolean ConnectionStatus = false;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new SendDataBinder();
    }

    class SendDataBinder extends Binder implements ServiceCommunicate.Cmd {
        @Override
        public void sendCmd(int id) {
            switch (id)
            {
                case ServiceCommunicate.Cmd_Clear_History :
                    try {
                        bufferedWriter.close();
                        bufferedWriter = new BufferedWriter(new OutputStreamWriter(openFileOutput(getString(R.string.ChatDataTxt), MODE_PRIVATE)));
                        bufferedWriter.close();
                        bufferedWriter = new BufferedWriter(new OutputStreamWriter(openFileOutput(getString(R.string.ChatDataTxt), MODE_APPEND)));
                    }
                    catch (Exception e) {
                        System.out.println(e);
                    }
                    break;
                case ServiceCommunicate.Cmd_Get_Conn_Status :
                    Message.obtain(mainHandler, ServiceCommunicate.Rev_Conn_Status, ConnectionStatus);
                    break;
                case ServiceCommunicate.Cmd_Get_History :
                    try {
                        bufferedReader = new BufferedReader(new InputStreamReader(openFileInput(getString(R.string.ChatDataTxt))));
                        String msg = null;
                        String msgs = "";
                        while ((msg = bufferedReader.readLine()) != null)
                        {
                            msgs += msg + System.getProperty("line.separator");
                        }
                        Message.obtain(mainHandler, ServiceCommunicate.Rev_History_Data, msgs).sendToTarget();
                    }
                    catch (Exception e)
                    {
                        System.out.println(e);
                    }
                    finally {
                        try
                        {
                            bufferedReader.close();
                        }
                        catch (Exception e)
                        {
                            System.out.println(e);
                        }
                        finally {
                            bufferedReader = null;
                        }
                    }
                    break;
            }
        }

        @Override
        public void sendMsg(String sMsg)
        {
            Message.obtain(simpleSocket.mHandler, SocketService_to_SimpleSocket, sMsg).sendToTarget();
        }

        @Override
        public void setRevHandler(Handler mainHandler)
        {
            SocketService.this.mainHandler = mainHandler;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(openFileOutput(getString(R.string.ChatDataTxt), MODE_APPEND)));
        }
        catch (Exception e) {
            System.out.println(e);
        }
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg)
            {
                switch (msg.what) {
                    case SimpleSocket_to_SocketService :
                        if (mainHandler != null) {
                            String sMsg = new String((String)( msg.obj));
                            Message.obtain(mainHandler, ServiceCommunicate.Rev_New_Msg, msg.obj).sendToTarget();
                            try {
                                bufferedWriter.newLine();
                                bufferedWriter.write(sMsg);
                                bufferedWriter.flush();
                            }
                            catch (Exception e)
                            {
                                System.out.println(e);
                            }
                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                                    SocketService.this).setSmallIcon(R.drawable.style_icons_system_best_do2)
                                    .setContentTitle("新消息")
                                    .setContentText(new String(sMsg));
                            mBuilder.setTicker("New message");//第一次提示消息的时候显示在通知栏上
                            //构建一个Intent
                            Intent resultIntent = new Intent(SocketService.this, MainActivity.class);
                            //封装一个Intent
                            PendingIntent resultPendingIntent = PendingIntent.getActivity(SocketService.this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                            // 设置通知主题的意图
                            mBuilder.setContentIntent(resultPendingIntent);
                            //获取通知管理器对象
                            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            mNotificationManager.notify(0, mBuilder.build());
                        }
                        super.handleMessage(msg);
                        break;
                    case ConnectionStatus_Connected :
                        ConnectionStatus = true;
                        Message.obtain(mainHandler, ServiceCommunicate.Rev_Conn_Status, true).sendToTarget();
                        break;
                    case ConnectionStatus_Disconnected :
                        ConnectionStatus = false;
                        Message.obtain(mainHandler, ServiceCommunicate.Rev_Conn_Status, false).sendToTarget();
                        break;
                }
                super.handleMessage(msg);
            }
        };
        simpleSocket = new SimpleSocket(mHandler);
        simpleSocket.start();
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    Message.obtain(simpleSocket.mHandler, SocketService_to_SimpleSocket, new String("Service lives~_~")).sendToTarget();
                    try
                    {
                        Thread.sleep(10000);
                    }
                    catch (Exception e)
                    {
                        System.out.println(e);
                    }
                }
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            bufferedWriter.close();
        }
        catch (Exception e) {
            System.out.println(e);
        }
        finally {
            bufferedWriter = null;
        }
    }


}