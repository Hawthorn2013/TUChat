package com.tusmartcar.jian.tuchat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import android.os.Handler;
import android.os.Message;

/**
 * Created by Jian on 2015/8/22.
 */
public class SimpleSocket extends Thread
{
    private String serviceIP = "192.168.43.231";
    private int servicePort = 38282;
    private int bufferLen = 1024;
    private Handler mHandler = null;
    public SimpleSocket(Handler mHandler)
    {
        this.mHandler = mHandler;
    }
    @Override
    public void run()
    {
        try
        {
            Socket socket = new Socket(serviceIP, servicePort);
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String outStr = "I am Nexus 5.";
            outputStream.write(outStr.getBytes("UTF-8"));
            outputStream.flush();
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
            System.out.println(e.getMessage());
        }
    }
}
