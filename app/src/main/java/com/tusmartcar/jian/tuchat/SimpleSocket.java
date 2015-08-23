package com.tusmartcar.jian.tuchat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
    public Handler mHandler = null;
    private String serviceIP = "LITTLECAT-PC2";
    private int servicePort = 38282;
    private int bufferLen = 1024;
    private Handler revHandler = null;
    BufferedReader bufferedReader = null;
    private BufferedWriter bufferedWriter = null;
    Socket socket;

    public SimpleSocket(Handler revHandler)
    {
        this.revHandler = revHandler;
        mHandler = new Handler() {
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
        while (true) {
            try {
                socket = new Socket(serviceIP, servicePort);
                OutputStream outputStream = socket.getOutputStream();
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                InputStream inputStream = socket.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                Message.obtain(revHandler, SocketService.ConnectionStatus_Connected).sendToTarget();
                try {
                    while (true) {
                        String inputStr = bufferedReader.readLine();
                        if (inputStr == null)
                        {
                            //很遗憾，服务端已关闭Socket
                            break;
                        }
                        Message.obtain(revHandler, SocketService.SimpleSocket_to_SocketService, inputStr).sendToTarget();
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            } catch (Exception e) {
                System.out.println(e);
            }
            Message.obtain(revHandler, SocketService.ConnectionStatus_Disconnected).sendToTarget();
            try {
                bufferedReader.close();
                bufferedWriter.close();
                socket.close();
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                bufferedReader = null;
                bufferedWriter = null;
                socket = null;
            }
            try {
                Thread.sleep(1000);
            }
            catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}
