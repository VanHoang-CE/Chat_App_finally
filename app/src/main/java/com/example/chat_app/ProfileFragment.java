package com.example.chat_app;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.FirebaseApp.getInstance;


public class ProfileFragment extends Fragment {


   FirebaseStorage storage = FirebaseStorage.getInstance();
    //firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    //Lưu trữ
    StorageReference storageReference;
    //Đường dẫn nươi lưu trữ ảnh đại diện và bìa
    String storagePath="Users_Profile_Cover_Imgs/";

    //View tu file xml;
    ImageView avatarTv, coverTv;
    TextView nameTv,emailTv,phoneTv;

    FloatingActionButton fab;

    //progress dialog
    ProgressDialog pd;

    //Kiểm tra profile hoặc ảnh bìa
    String profileOrCoverPhoto;


    //Khai bao cac thong so cua anh
    //Han cac thong so cua anh
    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;
    private static final int IMAGE_PICK_GALLERY_CODE=300;
    private static final int IMAGE_PICK_CAMERA_CODE=400;
    //Mang lưu trữ
    String cameraPermissions[];
    String storagePermissions[];

    //uri of picked image
    Uri image_uri;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_profile,container,false);


        //init firebase
        firebaseAuth=FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("Users");


       storageReference=storage.getReference();//Firebase Storage reference

        //init arrays of permissons
        cameraPermissions=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //init views
        avatarTv=view.findViewById(R.id.avatarTv);
        coverTv=view.findViewById(R.id.coverTv);
        nameTv=view.findViewById(R.id.nameTv);
        emailTv=view.findViewById(R.id.emailTv);
        phoneTv=view.findViewById(R.id.phoneTv);

        fab=view.findViewById(R.id.fab);

        //init progress dialog
        pd=new ProgressDialog(getActivity());

        //Lấy thông tin của người dùng hiện tại đã đăng nhập
        //Truy suất bằng email có khóa tương đương với email hiện tại
        //Truy vấn oderbychild, hien thi tung nut
        //ĐỌC DỮ LIỆU TỪ FIREBASE
        Query query=databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Kiem tra cho den khi tim thay du lieu
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    //Duoc nhan du lieu
                    String name=""+ds.child("name").getValue();
                    String email=""+ds.child("email").getValue();
                    String phone=""+ds.child("phone").getValue();
                    String image=""+ds.child("image").getValue();
                    String cover=""+ds.child("cover").getValue();

                    //Day du lieu hien thi
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    //Kiem tra ngoai le
                    try {
                        //Neu nhan duoc anh tu database va hien thi
                        Picasso.get().load(image).into(avatarTv);

                    }catch (Exception e){
                        //Neu co ngoai le thi dat anh mat dinh
                        Picasso.get().load(R.drawable.ic_fac).into(avatarTv);

                    }
                    try {
                        //Neu nhan duoc anh tu database va hien thi
                        Picasso.get().load(cover).into(coverTv);

                    }catch (Exception e){
                        //Neu co ngoai le thi dat anh mat dinh
                     //   Picasso.get().load(R.drawable.ic_fac).into(avatarTv);

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//Xu ly su kien fab_edit
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });
        return view;
    }



//Hiển thị item tùy chọn cho fab
    private void showEditProfileDialog() {
        //Thiet lap thanh thong bao dialog
        String options[]={"Chỉnh sửa đại diện",
                "Chỉnh sửa ảnh nền","Chỉnh sửa tên","" +
                "Thay đổi số điện thoại"};
        //alert dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        //Cai dat tieu de
        builder.setTitle("Chọn cài đặt");
        //Xu ly chon cac item dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            //Xu kien item trong moi click
                if (which ==0){
                //Edit profile clicked
                pd.setMessage("Đang tải ảnh đại diện");
                profileOrCoverPhoto="image";
                showImagePicDialog();
                }
                else if(which ==1){
                 //Edit Cover clicked
                    pd.setMessage("Đang tải ảnh bìa");
                    profileOrCoverPhoto="cover";//Thay dổi ảnh bìa
                    showImagePicDialog();
                }
                else if(which ==2){
                //Edit Name clicked
                    pd.setMessage("Đang sửa tên nè!!!");
                    showNamePhoneUpdateDialog("name");
                }
                else if(which ==3){
                //Edit phone clicked
                    pd.setMessage("Đang sửa số điện thoại luôn nề!!!");
                    showNamePhoneUpdateDialog("phone");
                }
            }
        });
        //Thiet lap va hien dialog
        builder.create().show();
    }


    ///Ve trang trc



//**********************************************Thực hiện xử lý sự kiện cho cài đặt tên và số điện thoại
    private void showNamePhoneUpdateDialog(String key) {
        //Tham số keyname sẽ chứa các giá trị tên người dùng và số điện thoại
        //Xây dựng hộp thoại
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Update"+key);
        //Cài đặt layout
        LinearLayout linearLayout=new LinearLayout(getActivity());
        //Bố trí các ô theo chiều dọc
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        //Thêm editext
        EditText editText=new EditText(getActivity());
        editText.setHint("Enter"+key);//edit name hoặc phone;
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //Thêm button in dialog to update FIREBASE
        builder.setPositiveButton("update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            //input text from edit text
                String value=editText.getText().toString().trim();
                if (!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String,Object> result=new HashMap<>();
                    result.put(key,value);
                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                //Updated và kết thúc tiến trình
                                    pd.dismiss();
                                    Toast.makeText(getActivity()," Updated",Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getActivity(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else{
                    Toast.makeText(getActivity(),"Nhấn enter đi ông iê"+key,Toast.LENGTH_SHORT).show();
                }
            }
        });
        //Thêm button in dialog to cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        ///Create và hiện dialog
        builder.create().show();


    }


/*-------------------------------------XỬ LÝ KIỂM TRA QUYỀN VÀ ĐƯA ẢNH LÊN ĐIỆN THOẠI CỦA SỰ KIỆN ShowImagePicDialog()*/
    //Sự kiện Edit cover
    private void showImagePicDialog() {
        //show dialog camera hoac thu vien anh
        String options[]={"Camera",
                "Thư Viện"};
        //alert dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        //Cai dat tieu de
        builder.setTitle("Chọn Ảnh Từ");
        //Xu ly chon cac item dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Xu kien item trong moi click
                if (which ==0){
                    //Camera clicked
               //     pd.setMessage("Đang tải ảnh đại diện");
                //    showImagePicDialog();
                    if(!checkCameraPermisssion()){
                        requestCameraPermisssion();
                    }
                    else{
                        pickFromCamera();

                    }
                }
                else if(which ==1){
                    //Gallery clicked
                    if (!checkStoragePermisssion()){
                        requestStoragePermisssion();
                    }
                    else {
                        pickFromGallery();
                    }
                }

            }
        });
        //Thiet lap va hien dialog
        builder.create().show();
    }

//Yêu cầu khởi động, quyền  của camera hoac lưu trữ
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                //Kiểm tra camera có hoạt động ko
                if (grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                       //Camera hoat dong
                        pickFromCamera();

                    }
                    else{
                        Toast.makeText(getActivity(),"Hãy cho phép quyền truy cập camera và lưu trữ",Toast.LENGTH_SHORT).show();
                    }

                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                //Kiểm tra LƯU TRỮ có hoạt động ko
                if (grantResults.length>0){

                    boolean writeStorageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted){
                        //Camera hoat dong
                        pickFromGallery();

                    }
                    else{
                        Toast.makeText(getActivity(),"Hãy cho phép quyền  lưu trữ",Toast.LENGTH_SHORT).show();
                    }
            }


        }
        break;
        }


}

// thực hiện lấy kết quả trả về từ activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //Phương pháp này sẽ được gọi sau khi chọn ảnh tù camera hoặc thư viện
        if (resultCode==RESULT_OK){
        if(requestCode == IMAGE_PICK_CAMERA_CODE){

            ////////////////////////////////////

            uploadProfileCoverPhoto(image_uri);
        }
        if (requestCode==IMAGE_PICK_GALLERY_CODE){
            image_uri=data.getData();
            uploadProfileCoverPhoto(image_uri);

        }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
/*thêm một biến chuỗi và gán cho nó giá trị "hình ảnh" khi người dùng
"Chỉnh sửa ảnh tiểu sử" và gán giá trị "bìa" khi người dùng nhấp vào "Chỉnh sửa bìa
Tại đây: hình ảnh là key trong mỗi người dùng chứa url của ảnh hồ sơ của người dùng và được upload lên firebase
* */
    private void uploadProfileCoverPhoto(Uri uri) {
        pd.show();
    //Đường dẫn và tên của hình ảnh được lưu trữ trong storage firebase
        String filePathAndName=storagePath+""+profileOrCoverPhoto+"_"+user.getUid();


        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Hinh anh tai toi noi luu tru
                        Task<Uri>uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri=uriTask.getResult();
                        //Kiem tra anh da upload hay chua
                        if (uriTask.isSuccessful()){
                            //image da upload
                            //Thêm ảnh đã update vào database

                            //ProfilePhoto
                            /*UPDATE ẢNH LÊN FIREBASE
                            * Tham số đầu tiên là profileorCoverPhoto có giá trị "hình ảnh" hoặc
                            là các khóa trong cơ sở dữ liệu của users
                            Tham số thứ hai chứa url của hình ảnh được lưu trữ trong firebase
                            url sẽ được lưu dưới dạng giá trị đối với khóa "hình ảnh" hoặc "bìa" **/
                            HashMap<String,Object> results= new HashMap<>();
                            results.put(profileOrCoverPhoto,downloadUri.toString());
                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //Sucess
                                            pd.dismiss();
                                            Toast.makeText(getActivity(),"Chờ xí ảnh đang tải....",Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //Error dong tien trinh
                                            pd.dismiss();
                                            Toast.makeText(getActivity(),"Lỗi trong quá trình tải ảnh rồi bro....",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        else
                        {
                            //error
                            pd.dismiss();
                            Toast.makeText(getActivity(),"Error",Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }
    //Lấy ảnh từ thiết bị camera
    private void pickFromCamera() {
        //Lấy ảnh từ thiết bị camera
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Ảnh tạm thời");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Mô tả tạm thời");
    //put image
        image_uri=getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
    //Intent to start camera
        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
    }

//Lấy ảnh từ thiết bị lưu trữ
    private void pickFromGallery() {
        Intent galleryIntent=new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);
    }
    //KIỂM tra quyền lưu trữ
    private boolean checkStoragePermisssion(){
        //Kiểm tra quyền lưu trữ bật hay chưa
        boolean result= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    //Yêu cầu quền lưu trữ chạy
    private void requestStoragePermisssion(){
        requestPermissions(storagePermissions,STORAGE_REQUEST_CODE);
    }

    //KIỂM tra quyền camera
    private boolean checkCameraPermisssion(){
        //Kiểm tra quyền CAMERA bật hay chưa
        boolean result= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)
                ==(PackageManager.PERMISSION_GRANTED);

        boolean result1= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }


    //Yêu cầu quyền camera chạy
    private void requestCameraPermisssion(){
        requestPermissions(cameraPermissions,CAMERA_REQUEST_CODE);
    }


//**************************Menu
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
        inflater.inflate(R.menu.menu_logout,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    //Xử lý sự kiện click logout

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if (id==R.id.action_logout2){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

}