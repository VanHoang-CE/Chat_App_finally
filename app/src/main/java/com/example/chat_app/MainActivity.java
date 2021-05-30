package com.example.chat_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    //view
    Button mRegisterBtn,mLoginBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //init views
        mRegisterBtn=findViewById(R.id.register_btn);
        mLoginBtn=findViewById(R.id.login_btn);
        //Xử lý sự kiện
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //cho pep dang ky
                startActivity(new Intent(MainActivity.this,RegisterActivity.class));
            }
        });
        //Xử lý sự kiện
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cho pep dang nhap
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
            }
        });
    }
}