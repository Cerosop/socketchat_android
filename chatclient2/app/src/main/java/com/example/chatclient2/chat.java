package com.example.chatclient2;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class chat extends AppCompatActivity {
    Button btn_send, btn_leave;
    EditText message_t;
    TextView hi_t, chatroom_t;
    String name, ip, port;
    Socket server;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("test", "10");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViewElement();

        Intent it = this.getIntent();
        if(it != null){
            Bundle bundle = it.getExtras();
            if(bundle != null){
                name = bundle.getString("name");
                if(name != null && !name.equals("")){
                    hi_t.setText("hi " + name);
                }
                ip = bundle.getString("ip");
                port = bundle.getString("port");
                if(ip != null && port != null){
                    chatroom_t.setText("hi " + name);

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.d("test", "0");
//                                server = new Socket("10.0.2.2", 6100);
                                server = new Socket(ip, Integer.parseInt(port));

                                Log.d("test", "1");
                                try {
                                    JSONObject jsonObj = new JSONObject();
                                    try {
                                        jsonObj.put("name", name);
                                        jsonObj.put("message", "");
                                        jsonObj.put("error", "");
                                        // 添加更多的键值对
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
                                    bw.write(jsonObj.toString());
                                    bw.newLine();
                                    bw.flush();
                                } catch (Exception e) {
                                    Log.d("connection", e.toString());
                                }
                                new ChatThread(server).start();
                                Log.d("test", "2");
                            } catch (IOException e) {
                                Log.d("test", "111");
                                Log.d("test", e.toString());
                                throw new RuntimeException(e);
                            }
                        }
                    });
                    thread.start();
                }
            }
        }

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = message_t.getText().toString();
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObj = new JSONObject();
                            try {
                                jsonObj.put("name", name);
                                jsonObj.put("message", message);
                                jsonObj.put("error", "");
                                // 添加更多的键值对
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    chatroom_t.setText(chatroom_t.getText() + "\n" + name + ": " + message);
                                    message_t.setText("");
                                }
                            });
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
                            bw.write(jsonObj.toString());
                            bw.newLine();
                            bw.flush();
                        } catch (Exception e) {
                            Log.d("connection", e.toString());
                        }
                    }
                });

                if(server.isClosed()){
                    chatroom_t.setText(chatroom_t.getText() + "\nServer closed. Please press leave button.");
                    message_t.setText("");
                }
                else {
                    thread.start();
                }
            }
        });

        btn_leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!server.isClosed()){
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jsonObj = new JSONObject();
                                try {
                                    jsonObj.put("name", name);
                                    jsonObj.put("message", "");
                                    jsonObj.put("error", "close");
                                    // 添加更多的键值对
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
                                bw.write(jsonObj.toString());
                                bw.newLine();
                                bw.flush();
                                server.close();
                            }
                            catch (Exception ex){
                                Log.d("connection", ex.toString());
                            }
                        }
                    });
                    thread.start();
                }
                Bundle bundle = new Bundle();
                bundle.putString("name", name);
                bundle.putString("ip", ip);
                bundle.putString("port", port);
                Intent it = new Intent();
                it.putExtras(bundle);
                it.setClass(chat.this, MainActivity.class);
                startActivity(it);
            }
        });
    }

    private void initViewElement(){
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_leave = (Button) findViewById(R.id.btn_leave);
        message_t = (EditText) findViewById(R.id.message_t);
        hi_t = (TextView) findViewById(R.id.hi_t);
        chatroom_t = (TextView) findViewById(R.id.chatroom_t);
    }

    public class ChatThread extends Thread {
        Socket server;
        BufferedReader br;
        public ChatThread(Socket s) {
            super();
            this.server = s;
            Log.d("test", "3");
            try {
                br = new BufferedReader(new InputStreamReader(server.getInputStream(), StandardCharsets.UTF_8));
                Log.d("test", "4");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            super.run();
            String content = null;
            Log.d("test", "5");
            try {
                while ((content = br.readLine()) != null) {
                    Log.d("test", content);
                    JSONObject jsonObj = new JSONObject(content); //轉JSON物件
                    String servername = jsonObj.getString("name");
                    String message = jsonObj.getString("message");
                    String err = jsonObj.getString("error");
                    if(err.equals("")){
                        runOnUiThread(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                chatroom_t.setText(chatroom_t.getText() + "\n" + servername + ": " + message);

                            }
                        });
                    }
                    else{
                        runOnUiThread(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                chatroom_t.setText(chatroom_t.getText() + "\n" + servername + ": Server closed. Please press leave button.");
                            }
                        });
                        server.close();
                        break;
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

    }
}