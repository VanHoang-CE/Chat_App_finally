package com.example.chat_app.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app.ChatActivity;
import com.example.chat_app.R;
import com.example.chat_app.models.ModelGetSetDataUsers;
import com.squareup.picasso.Picasso;

import java.util.List;

public  class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>{

    //Khai báo context cung cấp activities,fragment,services đên
    //các file, thư mục hình ảnh bên ngoài;
    Context context;
    List<ModelGetSetDataUsers> usersList;

    //Xây dựng contructor


    public AdapterUsers(Context context, List<ModelGetSetDataUsers> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        //inflate layout row_users.xml

        View view = LayoutInflater.from(context).inflate(R.layout.row_users,viewGroup,false);
        return new MyHolder(view);
    }
//Xác định nội dung dựa trên vị trí của view
    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        //Get data:Lấy dữ liệu
        String hisUID= usersList.get(i).getUid();
        String userImage=usersList.get(i).getImage();
        String userName=usersList.get(i).getName();
        String userEmail=usersList.get(i).getEmail();

        //GỬI DỮ LIỆU
        myHolder.mNameTv.setText(userName);
        myHolder.mEmailTv.setText(userEmail);
        try {
            //Load và hiển thị ảnh trong ứng dụng
            Picasso.get().load(userImage).placeholder(R.drawable.ic_fac2).into(myHolder.mAvatarTv);

        }
        catch (Exception e){

        }
        //Xu lý su kien click item
        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid",hisUID);
                context.startActivity(intent);
               // Toast.makeText(context,""+userEmail,Toast.LENGTH_SHORT).show();

            }
        });
    }



//Hiển thị hết các users
    @Override
    public int getItemCount() {
        return usersList.size();
    }

    //Xem lop chu so huu
    class MyHolder extends RecyclerView.ViewHolder{
        ImageView mAvatarTv;
        TextView mNameTv,mEmailTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init:Tinhs kế thừa
            mAvatarTv=itemView.findViewById(R.id.avatarTv);
            mNameTv=itemView.findViewById(R.id.nameTv);
            mEmailTv=itemView.findViewById(R.id.emailTv);

        }
    }
}
