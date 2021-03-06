package com.tusmartcar.jian.tuchat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.*;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    //中文测试
    private TextView mainShowText = null;
    private EditText mainEditText = null;
    private Button mainTextSend = null;
    private Handler mHandler = null;
    private ServiceCommunicate.Cmd sendDataBinder = null;
    private ServiceConnection conn = null;
    private TextView ConnectionStatus = null;
    private CheckBox cbAutoScroll = null;
    private Button btnClearHistory = null;
    private ScrollView svMainShowText = null;
    private Button btnTest = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainShowText = (TextView)findViewById(R.id.MainShowText);
        mainEditText = (EditText)findViewById(R.id.MainEditText);
        cbAutoScroll = (CheckBox)findViewById(R.id.cbAutoScroll);
        btnTest = (Button)findViewById(R.id.btnTest);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDataBinder.sendCmd(ServiceCommunicate.Cmd_Get_History);
            }
        });
        cbAutoScroll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            svMainShowText.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                }
            }
        });
        btnClearHistory = (Button)findViewById(R.id.btnClearHistory);
        btnClearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDataBinder.sendCmd(ServiceCommunicate.Cmd_Clear_History);
                mainShowText.setText("");
            }
        });
        mainTextSend = (Button)findViewById(R.id.MainTextSend);
        mainTextSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDataBinder.sendMsg(mainEditText.getText().toString());
                mainEditText.setText("");
            }
        });
        svMainShowText = (ScrollView)findViewById(R.id.svMainShowText);

        ConnectionStatus = (TextView)findViewById(R.id.ConnectionStatus);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg)
            {
                switch (msg.what) {
                    case ServiceCommunicate.Rev_New_Msg :
                        mainShowText.setText(mainShowText.getText() + System.getProperty("line.separator") + (String)(msg.obj));
                        if (cbAutoScroll.isChecked()) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    svMainShowText.fullScroll(ScrollView.FOCUS_DOWN);
                                }
                            });
                        }
                        break;
                    case ServiceCommunicate.Rev_Conn_Status :
                        if ((boolean)msg.obj) {
                            ConnectionStatus.setText("已连接^-^");
                            ConnectionStatus.setBackgroundColor(Color.GREEN);
                        }
                        else {
                            ConnectionStatus.setText("断线重连...");
                            ConnectionStatus.setBackgroundColor(Color.RED);
                        }
                        break;
                    case ServiceCommunicate.Rev_History_Data :
                        mainShowText.setText(new String((String)msg.obj) + System.getProperty("line.separator"));
                        break;
                }
                super.handleMessage(msg);
            }
        };
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, SocketService.class);
        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                sendDataBinder = (ServiceCommunicate.Cmd)service;
                sendDataBinder.setRevHandler(mHandler);
                sendDataBinder.sendMsg(new String("Service Start"));
                sendDataBinder.sendCmd(ServiceCommunicate.Cmd_Get_History);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(intent, conn, BIND_AUTO_CREATE);
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
