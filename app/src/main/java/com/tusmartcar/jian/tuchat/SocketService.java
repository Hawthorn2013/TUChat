package com.tusmartcar.jian.tuchat;

import android.app.Service;
import android.os.*;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.content.*;
import java.io.*;

public class SocketService extends Service {
    public static final int MainActivity_to_SocketService = 1;
    public static final int SocketService_to_MainActiviyt = 2;
    public static final int SocketService_to_SimpleSocket = 3;
    public static final int SimpleSocket_to_SocketService = 4;
    public static final int ConnectionStatus_Disconnected = 5;
    public static final int ConnectionStatus_Connected = 6;

    private SimpleSocket simpleSocket = null;
    public Handler mHandler = null;
    private Handler mainHandler = null;
    private BufferedWriter bufferedWriter = null;
    public SocketService() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg)
            {
                switch (msg.what) {
                    case SimpleSocket_to_SocketService :
                        if (mainHandler != null) {
                            String sMsg = new String((String)( msg.obj));
                            Message.obtain(mainHandler, SocketService_to_MainActiviyt, msg.obj).sendToTarget();
                            try {
                                System.out.println("test-->5");
                                bufferedWriter.newLine();
                                bufferedWriter.write(sMsg);
                                bufferedWriter.flush();
                                System.out.println("test-->6");
                            }
                            catch (Exception e)
                            {
                                System.out.println(e);
                            }
                        }
                        super.handleMessage(msg);
                        break;
                    case ConnectionStatus_Connected :
                        Message.obtain(mainHandler, SocketService.ConnectionStatus_Connected).sendToTarget();
                        break;
                    case ConnectionStatus_Disconnected :
                        Message.obtain(mainHandler, SocketService.ConnectionStatus_Disconnected).sendToTarget();
                        break;
                }
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
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new SendDataBinder();
    }

    class SendDataBinder extends Binder {
        public void SendMsg(String sMsg)
        {
            Message.obtain(simpleSocket.mHandler, SocketService_to_SimpleSocket, sMsg).sendToTarget();
        }

        public SocketService GetService()
        {
            return SocketService.this;
        }

        public void SetMainHandler(Handler mainHandler)
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
