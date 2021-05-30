package com.example.chat_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    //Khai bao
    private static final int RC_SIGN_IN=100;
    GoogleSignInClient mGoogleSignInClient;


    EditText mEmailEt,mPasswordEt;
    TextView notHaveAccountTv, mRecoverPassTv;
    Button mLoginBtn;
    SignInButton mGoogleLoginBtn;

    private FirebaseAuth mAuth;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Thanh công cụ và tiêu đề
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Login");
        //Back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Cấu hình Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient=GoogleSignIn.getClient(this,gso);


        //init
        mEmailEt=findViewById(R.id.EmailEt);
        mPasswordEt=findViewById(R.id.PasswordEt);
        notHaveAccountTv=findViewById(R.id.nothave_accountTv);
        mLoginBtn=findViewById(R.id.loginBtn);
        mRecoverPassTv=findViewById(R.id.recoverPassTv);
        mGoogleLoginBtn=findViewById(R.id.googleLoginBtn);

        //Khoi tao mAuth cua firebase(Tai khoan)
        mAuth = FirebaseAuth.getInstance();

        //Xử lý sự kiện login
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //Nhap du lieu
                String email= mEmailEt.getText().toString();
                String passw=mPasswordEt.getText().toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    mEmailEt.setError("Email Không hợp lệ");
                    mEmailEt.setFocusable(true);
                }
                else
                {
                    loginUser(email,passw);

                }
            }
        });
        //Khong co tai khoan dang nhap
        notHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                finish();
            }
        });
        //Xu ly click dang ky lai
        mRecoverPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswordDialog();
            }
        });
        //Xu ly su kien cho click googleSignin
        mGoogleLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });


        //init tien trinh dang nhap
        pd=new ProgressDialog(this);

    }
////Xu ly dang ky lai
    private void showRecoverPasswordDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Khôi phục password");


        LinearLayout linearLayout=new LinearLayout(this);
       EditText emailEt=new EditText(this);
       emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEt.setMinEms(16);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);
       //Nut khoi phuc
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            //input email
                String email =emailEt.getText().toString().trim();
                beginRecovery(email);
            }
        });
        //Nut Cancel
        builder.setNegativeButton( "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            }
        });
        //Show hop thoai tien trinh
        builder.create().show();
    }
////Xu ly dang ky lai email lay tu firebase
    private void beginRecovery(String email) {
        //Show tien trinh
        pd.setMessage("Đang gửi email...");

        pd.show();

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.dismiss();
            if(task.isSuccessful()){
                Toast.makeText(LoginActivity.this,"Gửi Email thành công, vui lòng check mail!!!",Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(LoginActivity.this,"Thất bại....",Toast.LENGTH_SHORT).show();
            }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            //Ngoai le bat loi
                pd.dismiss();
                Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }


    ///Xu ly dang nhap user
    private void loginUser(String email, String passw) {
        //Show tien trinh
        pd.setMessage("Đang đăng nhập...");

        pd.show();

        mAuth.signInWithEmailAndPassword(email, passw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //bo qua tien trinh
                            pd.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            //user da dang nhap, se chuyen thanh trang profile
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            pd.dismiss();

                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                //Show loi
                Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();//Tro ve trang truoc
        return  super.onSupportNavigateUp();
    }
///Check bắt lỗi Sự kiện
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                           // Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            if(task.getResult().getAdditionalUserInfo().isNewUser()){
                                //Get user and email from tac gia
                                String email=user.getEmail();
                                String uid=user.getUid();
                                //Khi user da dang ky
                                //su dung hashMap
                                HashMap<Object,String> hashMap=new HashMap<>();
                                //Dat thong tin trong hashmap
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
                                //Đặt dữ liệu vào bên trong child chứa uid databse
                                reference.child(uid).setValue(hashMap);
                            }




                            //Show email
                            Toast.makeText(LoginActivity.this,""+user.getEmail(),Toast.LENGTH_SHORT).show();
                            //user da dang nhap, se chuyen thanh trang DashboardActivity
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                           // updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this,"Đăng nhập thất bại :((........",Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}