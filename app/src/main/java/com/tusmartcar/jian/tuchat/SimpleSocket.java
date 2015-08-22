package com.tusmartcar.jian.tuchat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import android.os.Handler;
import android.os.Message;

/**
 * Created by Jian on 2015/8/22.
 */
public class SimpleSocket extends Thread
{
    public Handler sendHandler = null;                      //UI线程通过此发送消息
    private String serviceIP = "192.168.43.231";
    private int servicePort = 38282;
    private int bufferLen = 1024;
    private Handler mHandler = null;
    BufferedReader bufferedReader = null;
    private BufferedWriter bufferedWriter = null;
    Socket socket;

    public SimpleSocket(Handler mHandler)
    {
        this.mHandler = mHandler;
        sendHandler = new Handler() {
            @Override
            public void handleMessage(Message msg)
            {
                try
                {
                    bufferedWriter.write((String) (msg.obj));
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
                catch (Exception e)
                {
                    System.out.println(e);
                }
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public void run()
    {
        try
        {
            socket = new Socket(serviceIP, servicePort);
            OutputStream outputStream = socket.getOutputStream();
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            InputStream inputStream = socket.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        try
        {
            while (true)
            {
                String inputStr = bufferedReader.readLine();
                Message msg = new Message();
                msg.what = 1;
                msg.obj = inputStr;
                mHandler.sendMessage(msg);
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }
}
