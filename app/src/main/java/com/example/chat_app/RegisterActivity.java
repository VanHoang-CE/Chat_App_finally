package com.example.chat_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //
    EditText mEmailEt,mPasswordEt;
    Button mRegisterBtn;
    TextView mHaveAccountTv;
    //Tien trinh hien thi khi dang ky nguoi dung
    ProgressDialog progressDialog;
    //add firebaseAuth
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Thanh công cụ và tiêu đề
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Create Account");
        //Back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        //init:Trong do
        mEmailEt=findViewById(R.id.EmailEt);
        mPasswordEt=findViewById(R.id.PasswordEt);
        mRegisterBtn=findViewById(R.id.registerBtn);
        mHaveAccountTv =findViewById(R.id.have_accountTv);

        mAuth = FirebaseAuth.getInstance();

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng ký....");
        //Xu ly su kien
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            String email=mEmailEt.getText().toString().trim();
            String password=mPasswordEt.getText().toString().trim();
            //Bao loi
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    mEmailEt.setError("Nhap Email");
                    mEmailEt.setFocusable(true);
                }
                else if (password.length()<6){
                    mPasswordEt.setError("Password >= 6 charater");
                }
                else
                    registerUser(email,password);
            }
        });

        //Xử lý sự kiện have login
        mHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();
            }
        });
    }


    //Coppy Firebase
    private void registerUser(String email, String password) {
        //neu email va pass hop le, thi hien thi tien trinh dang ky
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();

                            //Get user and email from tac gia
                            String email=user.getEmail();
                            String uid=user.getUid();
                            //Khi user da dang ky
                            //su dung hashMap
                            HashMap<Object,String> hashMap=new HashMap<>();
                            //Dat thong tin trong hashashMap.put("typingTo","noOne");hmap
                            hashMap.put("email",email);
                            hashMap.put("uid",uid);
                            hashMap.put("name","");
                            hashMap.put("onlineStatus","online");
                            hashMap.put("typingTo","noOne");
                            hashMap.put("phone","");
                            hashMap.put("image","");
                            hashMap.put("cover","");

                            //Firebase database
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            //Dan toi nguoi dung user data
                            DatabaseReference reference=database.getReference("Users");
                            //Đặt dữ liệu vào bên trong hashmap databse
                            reference.child(uid).setValue(hashMap);


                            Toast.makeText(RegisterActivity.this, "Chờ tý nha bro...\n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Neu tien trinh bi loi hoac bi bo qua thi se hien thi
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();//Tro ve trang truoc
        return  super.onSupportNavigateUp();
    }
}