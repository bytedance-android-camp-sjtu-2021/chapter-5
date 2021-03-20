package com.bytedance.network.socket;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bytedance.network.R;
import com.bytedance.network.socket.tcp.ClientSocketThread;
import com.bytedance.network.socket.tcp.ServerListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class SocketTestActivity extends AppCompatActivity {
    private TextView text;
    public static Toast toast = null;
    private EditText edit;
    private Button btn;
    private ClientSocketThread sendThread;
    private ServerListener serverListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);
        edit =  findViewById(R.id.edit);
        btn = findViewById(R.id.btn_send);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendThread==null || !sendThread.isAlive()){
                    sendThread = new ClientSocketThread(SocketTestActivity.this);
                    sendThread.start();
                }
                sendThread.sendMsg(edit.getText().toString());
            }
        });
        //启动服务器监听线程
        serverListener = new ServerListener(this);
        serverListener.start();
    }

    @Override
    protected void onDestroy() {
        serverListener.stopServer();
        sendThread.disconnect();
        super.onDestroy();
    }

    // 获取本机IPv4地址
    public static String getLocalHostIp() {
        String ipaddress = "";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements()) {
                NetworkInterface nif = en.nextElement();// 得到每一个网络接口绑定的所有ip
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                while (inet.hasMoreElements()) {
                    InetAddress ip = inet.nextElement();
                    if (!ip.isLoopbackAddress() && ip instanceof Inet4Address) {
                        return ipaddress = ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            System.out.print("获取IP失败");
            e.printStackTrace();
        }
        return ipaddress;
    }
}
