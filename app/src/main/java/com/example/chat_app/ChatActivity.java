package com.example.chat_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chat_app.Adapters.AdapterChat;
import com.example.chat_app.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {


    //Views from xml
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileTv,profileTv2;
    TextView nameTv, userStatusTv,test;
    EditText messageEt;

    ImageButton sendBtn;
    //Kiem tra user da gui tin nhan chua
    ValueEventListener seenListener;

    DatabaseReference userRefForSeen;


   //Adapter lay du lieu tao doi tuong view cho chatActivity
    List<ModelChat> chatList;
    AdapterChat adapterChat;



    FirebaseAuth firebaseAuth;

    FirebaseDatabase firebaseDatabase;

    DatabaseReference usersDbRef;
    DatabaseReference databaseReference;


    String hisUid;
    String myUid;
    String hisImage;
   // String myImage;


    public boolean onSupportNavigateUp(){
        onBackPressed();//Tro ve trang truoc
        return  super.onSupportNavigateUp();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        //init views
        //tuy chon dang xuat
        Toolbar toolbar =findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView=findViewById(R.id.chat_recyclerView);
        profileTv=findViewById(R.id.profileTv);
        test=findViewById(R.id.test);
        nameTv=findViewById(R.id.nameTv);
        userStatusTv=findViewById(R.id.userStatusTv);
        messageEt=findViewById(R.id.messageEt);
        sendBtn=findViewById(R.id.sendBtn);


        ///Layout Linerlayout for RecyclerView
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        //Recyclerview
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        Intent intent=getIntent();
        hisUid=intent.getStringExtra("hisUid");


        //firebase author instance
        firebaseAuth=FirebaseAuth.getInstance();

      //  user1=firebaseAuth.getCurrentUser();
        firebaseDatabase=FirebaseDatabase.getInstance();
        usersDbRef=firebaseDatabase.getReference("Users");
        databaseReference=firebaseDatabase.getReference("Users");


//Search user to get that user's info
        Query userQuery = usersDbRef.orderByChild("uid").equalTo(hisUid);
                //get user picture and name
        //Một snapshot ảnh chụp sanpshot tại thời điểm đó ghi lại một cách chính xác và đồng thời trạng thái của cơ sở dữ liệu nguồn được chụp
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override

            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //check until required ifo is receved
                for (DataSnapshot ds:dataSnapshot.getChildren() ){
                    //get data
                    String name=""+ds.child("name").getValue();
                     hisImage=""+ds.child("image").getValue();

                   String typingStatus=""+ds.child("typingTo").getValue();
                     //get value of onlinestatus
                    String onlinestatus=""+ds.child("onlineStatus").getValue();

                    if (typingStatus.equals(myUid)){
                        userStatusTv.setText("Đang nhập....");
                    }
                    else
                    {
                        if (onlinestatus.equals("online")){
                            userStatusTv.setText(onlinestatus);
                        }
                        else
                        {
                            Calendar cal = Calendar.getInstance();
                            SimpleDateFormat sdf = new SimpleDateFormat(" HH:mmaa dd-MM ");
                            cal.setTimeInMillis(Long.parseLong(onlinestatus));
                            String dateTime = sdf.format(cal.getTime());
                            userStatusTv.setText("Online lần cuối vào lúc:"+dateTime);

                        }
                    }

                    //Set Data
                    nameTv.setText(name);

                    try {
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_fac).into(profileTv);

                    }catch (Exception e){
                        Picasso.get().load(R.drawable.ic_fac).into(profileTv);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //handle click button btn send message
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get text from edit text
                String message =messageEt.getText().toString().trim();
                if (TextUtils.isEmpty(message)){
                    //text trong
                    Toast.makeText(ChatActivity.this,"Tin nhắn trống làm sao gửi ba",Toast.LENGTH_SHORT);
                }
                else {
                    sendMessage(message);
                }
            }
        });
        //Check edit text change listener
        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length()==0){
                    checkTypingStatus("noOne");
                }
                else
                {
                    checkTypingStatus(hisUid);//uid nguời gửi
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        readMessages();
        seenMessages();
   // sendImagesMyUid();

    }

    private void seenMessages() {
        //doc database
        userRefForSeen=FirebaseDatabase.getInstance().getReference("Chats");
        seenListener=userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelChat chat=ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)){
                        HashMap<String,Object> hasSeenHashMap=new HashMap<>();
                        hasSeenHashMap.put("seen",true );
                        //Update len database
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Nhận  thông tin tin nhan tu database thông qua datasnapshot ở child chats
    private void readMessages() {
        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid) ||
                            chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)) {
                        chatList.add(chat);
                    }

                    //adapter
                    adapterChat = new AdapterChat(ChatActivity.this, chatList, hisImage);
                    adapterChat.notifyDataSetChanged();
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //HashMap cung cấp hoạt động put(key, value) để lưu trữ và get(key) để lấy ra giá trị từ HashMap sau do đưa dữ liệu lên firebase
    private void sendMessage(String message) {
        DatabaseReference databaseReference =FirebaseDatabase.getInstance().getReference();

        String timestamp=String.valueOf(System.currentTimeMillis());

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("sender",myUid);
        hashMap.put("receiver",hisUid);
        hashMap.put("message",message);
        hashMap.put("timestamp",timestamp);
        hashMap.put("seen",false);
        databaseReference.child("Chats").push().setValue(hashMap);

        //reset edit text sau khi nhap
        messageEt.setText("");
    }

    private void checkUserStatus(){
        //get current user
        FirebaseUser user=firebaseAuth.getCurrentUser();

        if (user !=null){
            //user da dang nhap
            //lAY EMAIL tu firebase sau khi dang nhap
          //   mProfileTv.setText(user.getEmail());
            myUid=user.getUid();

        }
        else {
            //user khong dang nhap

            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }

    //Kiem tra tình trạng online hay ko và đóng gói đưa lên firebase
    private void checkOnlineStatus(String status){
        DatabaseReference dbRef=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("onlineStatus",status);

        //Updatedatabase
        dbRef.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing){
        DatabaseReference dbRef=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("typingTo",typing);

        //Updatedatabase
        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        //set online
        checkOnlineStatus("online");
        super.onStart();
    }
    //dung lai cho tác vụ khác
    protected void onPause(){
        super.onPause();
        //set ofline with last seen timestamp
        String timestamp=String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
        checkTypingStatus("noOne");
        userRefForSeen.removeEventListener(seenListener);
    }

    //Quay lai tac vu đang dang dở
    protected void onResume(){
        super.onResume();
        //set ofline with last seen timestamp
        checkOnlineStatus("online");

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //đổi đói tượng layout thành view
        getMenuInflater().inflate(R.menu.menu_main,menu);
        //Hide searchview as we dont need it here
        menu.findItem(R.id.action_search).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if (id==R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}