package com.example.chatserver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

public class MainActivity extends AppCompatActivity {
    Button btn_connect;
    EditText name_t;
    String name;

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
            }
        }

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("name", name_t.getText().toString());
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
    }
}
//test