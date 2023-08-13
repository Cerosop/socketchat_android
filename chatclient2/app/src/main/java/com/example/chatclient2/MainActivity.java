package com.example.chatclient2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

public class MainActivity extends AppCompatActivity {
    Button btn_connect;
    EditText name_t, ip_t, port_t;
    String name, ip, port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViewElement();

        Intent it = this.getIntent();
        if(it != null){
            Bundle bundle = it.getExtras();
            if(bundle != null){
                name = bundle.getString("name");
                if(name != null && !name.equals("")){
                    name_t.setText(name);
                }
                port = bundle.getString("port");
                if(port != null && !port.equals("")){
                    port_t.setText(port);
                }
                ip = bundle.getString("ip");
                if(ip != null && !ip.equals("")){
                    ip_t.setText(ip);
                }
            }
        }

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("name", name_t.getText().toString());
                bundle.putString("ip", ip_t.getText().toString());
                bundle.putString("port", port_t.getText().toString());
                Intent it = new Intent();
                it.putExtras(bundle);
                it.setClass(MainActivity.this, chat.class);
                startActivity(it);
            }
        });
    }

    private void initViewElement(){
        btn_connect = (Button) findViewById(R.id.btn_connect);
        name_t = (EditText) findViewById(R.id.name_t);
        ip_t = (EditText) findViewById(R.id.ip_t);
        port_t = (EditText) findViewById(R.id.port_t);
    }
}