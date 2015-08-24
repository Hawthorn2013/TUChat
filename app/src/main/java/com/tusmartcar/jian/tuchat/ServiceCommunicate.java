package com.tusmartcar.jian.tuchat;

import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

/**
 * Created by Jian on 2015/8/24.
 */
public interface ServiceCommunicate {
    static final int SocketService_to_SimpleSocket = 3;
    static final int SimpleSocket_to_SocketService = 4;
    static final int ConnectionStatus_Disconnected = 5;
    static final int ConnectionStatus_Connected = 6;

    static final int Cmd_Clear_History = 1;
    static final int Cmd_Get_History = 2;
    static final int Cmd_Get_Conn_Status = 3;

    static final int Rev_Clear_History = 1;
    static final int Rev_History_Data = 2;
    static final int Rev_Conn_Status = 3;
    static final int Rev_New_Msg = 4;
    interface Cmd
    {
        abstract void sendMsg(String msg);
        abstract void sendCmd(int id);
        abstract void setRevHandler(Handler handler);
    }
}
