package com.example.chat_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    //firebase auth
    FirebaseAuth firebaseAuth;
    ActionBar actionBar;
    //view email and pass TextView mProfileTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        //Thanh công cụ và tiêu đề
        actionBar =getSupportActionBar();
        actionBar.setTitle("Profile");

        //init
        firebaseAuth=FirebaseAuth.getInstance();

        //bottom navigation
        BottomNavigationView navigationView =findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        //Default luon hien thi mac dinh
        actionBar.setTitle("Profile");//Thay doi tieu de thanh nav
        ProfileFragment fragment1=new ProfileFragment();
        FragmentTransaction ft1=getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content,fragment1,"");
        ft1.commit();

        //init view  mProfileTv=findViewById(R.id.profileTv);
    }
    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener=
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    //Xu ly su kien
                    switch (menuItem.getItemId()){
                        //Home
                     /*   case R.id.nav_home:
                            actionBar.setTitle("Home");//Thay doi tieu de thanh nav
                            HomeFragment fragment1=new HomeFragment();
                            FragmentTransaction ft1=getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.content,fragment1,"");
                            ft1.commit();
                        return true;
                       */ //Profile
                        case R.id.nav_profile:
                            actionBar.setTitle("Profile");
                            //Thay doi tieu de thanh nav
                            ProfileFragment fragment2=new ProfileFragment();
                            FragmentTransaction ft2=getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.content,fragment2,"");
                            ft2.commit();
                            return true;
                        //Users
                        case R.id.nav_users:
                            actionBar.setTitle("Users");
                            //Thay doi tieu de thanh nav
                            UsersFragment fragment3=new UsersFragment();
                            FragmentTransaction ft3=getSupportFragmentManager().beginTransaction();
                            ft3.replace(R.id.content,fragment3,"");
                            ft3.commit();
                            return true;
                   /*     case R.id.nav_chat:
                            actionBar.setTitle("Chats");
                            //Thay doi tieu de thanh nav
                            ChatListFragment fragment4=new ChatListFragment();
                            FragmentTransaction ft4=getSupportFragmentManager().beginTransaction();
                            ft4.replace(R.id.content,fragment4,"");
                            ft4.commit();
                            return true;*/
                    }
                    return false;

                }
            };


    private void checkUserStatus(){
        //get current user
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user !=null){
            //user da dang nhap
            //lAY EMAIL tu firebase sau khi dang nhap
           // mProfileTv.setText(user.getEmail());
        }
        else {
            //user khong dang nhap

           startActivity(new Intent(DashboardActivity.this,MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        //check on start of app
        checkUserStatus();
        super.onStart();
    }


}