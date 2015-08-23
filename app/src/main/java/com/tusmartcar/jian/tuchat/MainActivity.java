package com.tusmartcar.jian.tuchat;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.*;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    //中文测试
    private TextView mainShowText = null;
    private EditText mainEditText = null;
    private Button mainTextSend = null;
    private SimpleSocket simpleSocket = null;
    private Handler mHandler = null;
    private SocketService socketService = null;
    private SocketService.SendDataBinder sendDataBinder = null;
    private ServiceConnection conn = null;
    private TextView ConnectionStatus = null;
    private BufferedReader bufferedReader = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainShowText = (TextView)findViewById(R.id.MainShowText);
        mainEditText = (EditText)findViewById(R.id.MainEditText);
        mainTextSend = (Button)findViewById(R.id.MainTextSend);
        mainTextSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDataBinder.SendMsg(new String("I am Nexus 5."));
            }
        });

        ConnectionStatus = (TextView)findViewById(R.id.ConnectionStatus);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg)
            {
                switch (msg.what) {
                    case SocketService.SocketService_to_MainActiviyt :
                        mainShowText.setText((String)(msg.obj) + System.getProperty("line.separator") + mainShowText.getText());
                        break;
                    case SocketService.ConnectionStatus_Connected :
                        ConnectionStatus.setText("已连接^-^");
                        ConnectionStatus.setBackgroundColor(Color.GREEN);
                        break;
                    case SocketService.ConnectionStatus_Disconnected :
                        ConnectionStatus.setText("断线重连...");
                        ConnectionStatus.setBackgroundColor(Color.RED);
                }
                super.handleMessage(msg);
            }
        };
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, SocketService.class);
        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                sendDataBinder = (SocketService.SendDataBinder)service;
                sendDataBinder.SetMainHandler(mHandler);
                sendDataBinder.SendMsg(new String("Service Start"));
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(intent, conn, BIND_AUTO_CREATE);

        //显示历史消息
        try
        {
            bufferedReader = new BufferedReader(new InputStreamReader(openFileInput(getString(R.string.ChatDataTxt))));
            String s = null;
            while ((s = bufferedReader.readLine()) != null)
            {
                mainShowText.setText(s + System.getProperty("line.separator") + mainShowText.getText());
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        finally {
            try {
                bufferedReader.close();
            }
            catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
