package com.example.chat_app.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app.R;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/// Adapter có trách nhiệm lấy dữ liệu từ bộ dữ liệu và tạo ra các đối tượng View dựa trên dữ liệu đó.
public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder>{
    //view holder class

    private static final int MSG_TYPE_LEFT=0;
    private static  final  int MSG_TYPE_RIGHT=1;
    Context context;

    List<ModelChat> chatList;
    String imageUrl;


    ///Firebase User
    FirebaseUser fUser;

    FirebaseAuth firebaseAuth;

    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;


    }



    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        ///INFLATE chuyen file xml thanh javacode:row_chat_left.xml,row_chat_right.xml
        if (i==MSG_TYPE_RIGHT){
                View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right,viewGroup,false);
                return  new MyHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left,viewGroup,false);
            return  new MyHolder(view);
        }




    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder,final int i) {




    //Nhan du lieu
        String message=chatList.get(i).getMessage();
        String timeStamp=chatList.get(i).getTimestamp();

      //  String image=chatList.get(i).getType();


        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(" HH:mm aa");
        cal.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = sdf.format(cal.getTime());




        //set Data
        myHolder.messageTv.setText(message);
        myHolder.timeTv.setText(dateTime);

        try {
            Picasso.get().load(imageUrl).into(myHolder.profileTv);

        }catch (Exception e){

        }




        //Click to show delete dialog
        myHolder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("Xóa tin nhắn");
                builder.setMessage("Bạn muốn xóa tin nhắn à :((");
                //delete button
                builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    deleteMessage(i);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss dialog
                        dialog.dismiss();
                    }
                });
                //create and show dialog
                builder.create().show();
            }
        });


        //Set tin nhan nhan/gui cua message
        if (i==chatList.size()-1){
            if (chatList.get(i).isSeen()){
                myHolder.isSeenTv.setText("Đã xem");
            }
            else {
                myHolder.isSeenTv.setText("Đã gửi");
            }
        }
        else{
            myHolder.isSeenTv.setVisibility(View.GONE);
        }



        /////Show images uid
        firebaseDatabase=FirebaseDatabase.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        FirebaseUser user=firebaseAuth.getCurrentUser();
        databaseReference=firebaseDatabase.getReference("Users");
        Query query=databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    String myImage=""+ds.child("image").getValue();
                    try {
                        Picasso.get().load(myImage).placeholder(R.drawable.ic_fac2).into(myHolder.profileTv2);

                    }catch (Exception e){

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }


    //Lấy thời gian click message và thời gian tin nahwns bắ đàu lấy giá trị của 2 mốc sau đó xóa.
    //Cho phep xoa tin nhan cua minh va nguoi nhan
    private void deleteMessage(int position) {
        String myUID=FirebaseAuth.getInstance().getCurrentUser().getUid();


        String msgTimeStamp=chatList.get(position).getTimestamp();
        DatabaseReference dbRef= FirebaseDatabase.getInstance().getReference("Chats");
        Query query=dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    /**?
                     * Xoa tin nhan tu chats
                     * ghi gia tri cho tin nhan nay la"tin nhan nay da bi xoa"
                     */
                    if(ds.child("sender").getValue().equals(myUID)){


                        //xoa tin nhan
                       ds.getRef().removeValue();
                        //ghi gia tri cho tin nhan nay la"tin nhan nay da bi xoa"
                      /*  HashMap<String,Object> hashMap=new HashMap<>();
                        hashMap.put("message","Tin nhắn này đã bị xóa....");
                        ds.getRef().updateChildren(hashMap);*/
                        Toast.makeText(context,"Đã xóa hết rồi :(( ",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(context,"Chỉ được xóa của tin nhắn của bạn thôi....:(( ",Toast.LENGTH_SHORT).show();
                    }



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //get user signed in hien tai
        fUser= FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else
        {
            return MSG_TYPE_LEFT;
        }

    }

    class MyHolder extends RecyclerView.ViewHolder{
    //views
    ImageView profileTv,profileTv2;

    TextView messageTv,timeTv,isSeenTv,test;
    LinearLayout messageLayout;

        public MyHolder(@NonNull View itemView){
            super(itemView);

            profileTv=itemView.findViewById(R.id.profileTv);
            profileTv2=itemView.findViewById(R.id.profileTv2);
            messageTv=itemView.findViewById(R.id.messageTv);
           // test=itemView.findViewById(R.id.test);
            timeTv=itemView.findViewById(R.id.timeTv);
            isSeenTv=itemView.findViewById(R.id.isSeenTv);
            messageLayout=itemView.findViewById(R.id.messageLayout);
        }
    }
}
