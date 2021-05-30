package com.example.chat_app;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.chat_app.Adapters.AdapterUsers;
import com.example.chat_app.models.ModelGetSetDataUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {
    //firebase auth
    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    //Lay dữ liệu ModelUsers
    List<ModelGetSetDataUsers> usersList;

    public UsersFragment() {
        // Required empty public constructor
    }

//Thực hiện giao diện view là giao diện xml tương ứng
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View  view =inflater.inflate(R.layout.fragment_users, container, false);


        //init
        firebaseAuth=FirebaseAuth.getInstance();

        //init recyclerview
        recyclerView =view.findViewById(R.id.user_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //init user list
        usersList =new ArrayList<>();

        //getAll Users
        getAllUsers();

        return view;
    }
    private void getAllUsers(){
        //Nhận users hiện tại
        FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        //Get path of database name users
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        //Get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelGetSetDataUsers modelUser=ds.getValue(ModelGetSetDataUsers.class);

                    //Nhận tất cả người dùng, trừ người dùng đang đăng nhập

                    if (!modelUser.getUid().equals(fUser.getUid())){
                        usersList.add(modelUser);
                    }
                    adapterUsers=new AdapterUsers(getActivity(),usersList);
                    //Đẩy tất cả dữ liệu lên recycler view
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void searchUsers(String query) {
        //Nhận users hiện tại
        FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        //Get path of database name users
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        //Get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelGetSetDataUsers modelUser=ds.getValue(ModelGetSetDataUsers.class);

                    //Nhận tất cả người dùng tìm kiếm, trừ người dùng đang đăng nhập

                    if (!modelUser.getUid().equals(fUser.getUid())){

                        if (modelUser.getName().toLowerCase().contains(query.toLowerCase())
                        //||modelUser.getEmail().toLowerCase().contains(query.toLowerCase())
                        ){
                            usersList.add(modelUser);

                        }

                    }
                    adapterUsers=new AdapterUsers(getActivity(),usersList);
                    //Refesh adapter
                    adapterUsers.notifyDataSetChanged();

                    //Đẩy tất cả dữ liệu lên recycler view
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

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

            startActivity(new Intent(getActivity(),MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);//Show menu
        super.onCreate(savedInstanceState);
    }

    //Dua menu vao trang nay

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);

        //Searchview
        MenuItem item=menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //Called khi nhấn keyboard
                //Truy vấn tìm kiếm ko trống
                if (!TextUtils.isEmpty(s.trim())){
                    //Search
                    searchUsers(s);
                }
                else {
                    //Truy van tim kiem trong-> nhan users
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s.trim())){
                    //Search
                    searchUsers(s);
                }
                else {
                    //Truy van tim kiem trong-> nhan users
                    getAllUsers();
                }
                //Called khi nhấn bất kỳ ký tự nào

                return false;
            }
        });

         super.onCreateOptionsMenu(menu, inflater);
    }

    //Xử lý sự kiện click logout

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