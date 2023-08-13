package com.example.chatserver;

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
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

public class chat extends AppCompatActivity {
    Button btn_send, btn_leave;
    EditText message_t;
    TextView hi_t, chatroom_t;
    String name, tmp_ip, tmp_port;
    List<Socket> clients = new ArrayList<Socket>();
    ServerSocket server;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
            }
        }



        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    server = new ServerSocket(7100);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chatroom_t.setText(name + " start(" + server.getInetAddress().getHostAddress() + ":" + server.getLocalPort() + ")");
                        }
                    });

                    if(server.isClosed())
                        Log.d("test", "jeiov");
                    while (!server.isClosed()) {
                        Log.d("test", "1");
                        Socket client = null;
                        try{
                            client = server.accept();// 使服务端处于监听状态
                        }catch (Exception e){
                            Log.d("test", e.toString());
                        }
                        
                        Log.d("test", "0");
                        clients.add(client);

                        new ChatThread(client).start();

                        BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        String str = br.readLine();
                        JSONObject jsonObj = new JSONObject(str); //轉JSON物件
                        String clientname = jsonObj.getString("name");
                        tmp_ip = client.getInetAddress().getHostAddress();
                        tmp_port = "" + client.getLocalPort();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                chatroom_t.setText(chatroom_t.getText() + "\n" + clientname + " connect(" + tmp_ip + ":" + tmp_port + ")");
                                chatroom_t.setText(chatroom_t.getText() + "\n" + name + ": welcome " + clientname);
                            }
                        });

                        jsonObj = new JSONObject();
                        try {
                            jsonObj.put("name", name);
                            jsonObj.put("message", "welcome " + clientname);
                            jsonObj.put("error", "");
                            // 添加更多的键值对'
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        for (Socket c : clients) {
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
                            bw.write(jsonObj.toString());
                            bw.newLine();
                            bw.flush();
                        }
                    }

                } catch(Exception e) {
                    Log.d("connection", e.toString());
                }
            }
        });
        thread.start();

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

                            for (Socket c : clients) {
                                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
                                bw.write(jsonObj.toString());
                                bw.newLine();
                                bw.flush();
                            }
                        } catch(Exception e) {
                            Log.d("connection", e.toString());
                        }
                    }
                });
                thread.start();
            }
        });

        btn_leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            JSONObject jsonObj = new JSONObject();
                            try {
                                jsonObj.put("name", name);
                                jsonObj.put("message", "");
                                jsonObj.put("error", "close");
                                // 添加更多的键值对
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            for (Socket c : clients) {
                                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
                                bw.write(jsonObj.toString());
                                bw.newLine();
                                bw.flush();
                            }
                            clients.clear();
                            server.close();
                            Bundle bundle = new Bundle();
                            bundle.putString("name", name);
                            Intent it = new Intent();
                            it.putExtras(bundle);
                            it.setClass(chat.this, MainActivity.class);
                            startActivity(it);
                        }
                        catch (Exception e){
                            Log.d("connection", e.toString());
                        }
                    }
                });
                thread.start();
            }
        });
    }

    private void initViewElement(){
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_leave = (Button) findViewById(R.id.btn_leave);
        message_t = (EditText) findViewById(R.id.message_t);
        hi_t = (TextView) findViewById(R.id.hi_t);
        chatroom_t = (TextView) findViewById(R.id.chatroom_t);
        clients.clear();
    }

    public class ChatThread extends Thread {
        Socket client;
        BufferedReader br;
        public ChatThread(Socket c) {
            super();
            this.client = c;
            try {
                br = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            super.run();
            String content = null;
            try {
                while ((content = br.readLine()) != null) {
                    JSONObject jsonObj = new JSONObject(content); //轉JSON物件
                    String clientname = jsonObj.getString("name");
                    String message = jsonObj.getString("message");
                    String err = jsonObj.getString("error");
                    if(err.equals("")){
                        runOnUiThread(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                chatroom_t.setText(chatroom_t.getText() + "\n" + clientname + ": " + message);
                            }
                        });

                        jsonObj = new JSONObject();
                        try {
                            jsonObj.put("name", clientname);
                            jsonObj.put("message", message);
                            jsonObj.put("error", "");
                            // 添加更多的键值对
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        for (Socket c : clients) {
                            if(c != client){
                                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
                                bw.write(jsonObj.toString());
                                bw.newLine();
                                bw.flush();
                            }
                        }
                    }
                    else{
                        clients.remove(client);

                        jsonObj = new JSONObject();
                        try {
                            jsonObj.put("name", name);
                            jsonObj.put("message", clientname + " has left.");
                            jsonObj.put("error", "");
                            // 添加更多的键值对
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                chatroom_t.setText(chatroom_t.getText() + "\n" + name + ": " + clientname + " has left.");
                            }
                        });
                        for (Socket c : clients) {
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
                            bw.write(jsonObj.toString());
                            bw.newLine();
                            bw.flush();
                        }
                        client.close();
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