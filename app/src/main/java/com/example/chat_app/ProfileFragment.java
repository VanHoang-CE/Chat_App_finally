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
    //L??u tr???
    StorageReference storageReference;
    //???????ng d???n n????i l??u tr??? ???nh ?????i di???n v?? b??a
    String storagePath="Users_Profile_Cover_Imgs/";

    //View tu file xml;
    ImageView avatarTv, coverTv;
    TextView nameTv,emailTv,phoneTv;

    FloatingActionButton fab;

    //progress dialog
    ProgressDialog pd;

    //Ki???m tra profile ho???c ???nh b??a
    String profileOrCoverPhoto;


    //Khai bao cac thong so cua anh
    //Han cac thong so cua anh
    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;
    private static final int IMAGE_PICK_GALLERY_CODE=300;
    private static final int IMAGE_PICK_CAMERA_CODE=400;
    //Mang l??u tr???
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

        //L???y th??ng tin c???a ng?????i d??ng hi???n t???i ???? ????ng nh???p
        //Truy su???t b???ng email c?? kh??a t????ng ??????ng v???i email hi???n t???i
        //Truy v???n oderbychild, hien thi tung nut
        //?????C D??? LI???U T??? FIREBASE
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



//Hi???n th??? item t??y ch???n cho fab
    private void showEditProfileDialog() {
        //Thiet lap thanh thong bao dialog
        String options[]={"Ch???nh s???a ?????i di???n",
                "Ch???nh s???a ???nh n???n","Ch???nh s???a t??n","" +
                "Thay ?????i s??? ??i???n tho???i"};
        //alert dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        //Cai dat tieu de
        builder.setTitle("Ch???n c??i ?????t");
        //Xu ly chon cac item dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            //Xu kien item trong moi click
                if (which ==0){
                //Edit profile clicked
                pd.setMessage("??ang t???i ???nh ?????i di???n");
                profileOrCoverPhoto="image";
                showImagePicDialog();
                }
                else if(which ==1){
                 //Edit Cover clicked
                    pd.setMessage("??ang t???i ???nh b??a");
                    profileOrCoverPhoto="cover";//Thay d???i ???nh b??a
                    showImagePicDialog();
                }
                else if(which ==2){
                //Edit Name clicked
                    pd.setMessage("??ang s???a t??n n??!!!");
                    showNamePhoneUpdateDialog("name");
                }
                else if(which ==3){
                //Edit phone clicked
                    pd.setMessage("??ang s???a s??? ??i???n tho???i lu??n n???!!!");
                    showNamePhoneUpdateDialog("phone");
                }
            }
        });
        //Thiet lap va hien dialog
        builder.create().show();
    }


    ///Ve trang trc



//**********************************************Th???c hi???n x??? l?? s??? ki???n cho c??i ?????t t??n v?? s??? ??i???n tho???i
    private void showNamePhoneUpdateDialog(String key) {
        //Tham s??? keyname s??? ch???a c??c gi?? tr??? t??n ng?????i d??ng v?? s??? ??i???n tho???i
        //X??y d???ng h???p tho???i
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Update"+key);
        //C??i ?????t layout
        LinearLayout linearLayout=new LinearLayout(getActivity());
        //B??? tr?? c??c ?? theo chi???u d???c
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        //Th??m editext
        EditText editText=new EditText(getActivity());
        editText.setHint("Enter"+key);//edit name ho???c phone;
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //Th??m button in dialog to update FIREBASE
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
                                //Updated v?? k???t th??c ti???n tr??nh
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
                    Toast.makeText(getActivity(),"Nh???n enter ??i ??ng i??"+key,Toast.LENGTH_SHORT).show();
                }
            }
        });
        //Th??m button in dialog to cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        ///Create v?? hi???n dialog
        builder.create().show();


    }


/*-------------------------------------X??? L?? KI???M TRA QUY???N V?? ????A ???NH L??N ??I???N THO???I C???A S??? KI???N ShowImagePicDialog()*/
    //S??? ki???n Edit cover
    private void showImagePicDialog() {
        //show dialog camera hoac thu vien anh
        String options[]={"Camera",
                "Th?? Vi???n"};
        //alert dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        //Cai dat tieu de
        builder.setTitle("Ch???n ???nh T???");
        //Xu ly chon cac item dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Xu kien item trong moi click
                if (which ==0){
                    //Camera clicked
               //     pd.setMessage("??ang t???i ???nh ?????i di???n");
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

//Y??u c???u kh???i ?????ng, quy???n  c???a camera hoac l??u tr???
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                //Ki???m tra camera c?? ho???t ?????ng ko
                if (grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                       //Camera hoat dong
                        pickFromCamera();

                    }
                    else{
                        Toast.makeText(getActivity(),"H??y cho ph??p quy???n truy c???p camera v?? l??u tr???",Toast.LENGTH_SHORT).show();
                    }

                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                //Ki???m tra L??U TR??? c?? ho???t ?????ng ko
                if (grantResults.length>0){

                    boolean writeStorageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted){
                        //Camera hoat dong
                        pickFromGallery();

                    }
                    else{
                        Toast.makeText(getActivity(),"H??y cho ph??p quy???n  l??u tr???",Toast.LENGTH_SHORT).show();
                    }
            }


        }
        break;
        }


}

// th???c hi???n l???y k???t qu??? tr??? v??? t??? activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //Ph????ng ph??p n??y s??? ???????c g???i sau khi ch???n ???nh t?? camera ho???c th?? vi???n
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
/*th??m m???t bi???n chu???i v?? g??n cho n?? gi?? tr??? "h??nh ???nh" khi ng?????i d??ng
"Ch???nh s???a ???nh ti???u s???" v?? g??n gi?? tr??? "b??a" khi ng?????i d??ng nh???p v??o "Ch???nh s???a b??a
T???i ????y: h??nh ???nh l?? key trong m???i ng?????i d??ng ch???a url c???a ???nh h??? s?? c???a ng?????i d??ng v?? ???????c upload l??n firebase
* */
    private void uploadProfileCoverPhoto(Uri uri) {
        pd.show();
    //???????ng d???n v?? t??n c???a h??nh ???nh ???????c l??u tr??? trong storage firebase
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
                            //Th??m ???nh ???? update v??o database

                            //ProfilePhoto
                            /*UPDATE ???NH L??N FIREBASE
                            * Tham s??? ?????u ti??n l?? profileorCoverPhoto c?? gi?? tr??? "h??nh ???nh" ho???c
                            l?? c??c kh??a trong c?? s??? d??? li???u c???a users
                            Tham s??? th??? hai ch???a url c???a h??nh ???nh ???????c l??u tr??? trong firebase
                            url s??? ???????c l??u d?????i d???ng gi?? tr??? ?????i v???i kh??a "h??nh ???nh" ho???c "b??a" **/
                            HashMap<String,Object> results= new HashMap<>();
                            results.put(profileOrCoverPhoto,downloadUri.toString());
                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //Sucess
                                            pd.dismiss();
                                            Toast.makeText(getActivity(),"Ch??? x?? ???nh ??ang t???i....",Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //Error dong tien trinh
                                            pd.dismiss();
                                            Toast.makeText(getActivity(),"L???i trong qu?? tr??nh t???i ???nh r???i bro....",Toast.LENGTH_SHORT).show();
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
    //L???y ???nh t??? thi???t b??? camera
    private void pickFromCamera() {
        //L???y ???nh t??? thi???t b??? camera
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"???nh t???m th???i");
        values.put(MediaStore.Images.Media.DESCRIPTION,"M?? t??? t???m th???i");
    //put image
        image_uri=getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
    //Intent to start camera
        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
    }

//L???y ???nh t??? thi???t b??? l??u tr???
    private void pickFromGallery() {
        Intent galleryIntent=new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);
    }
    //KI???M tra quy???n l??u tr???
    private boolean checkStoragePermisssion(){
        //Ki???m tra quy???n l??u tr??? b???t hay ch??a
        boolean result= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    //Y??u c???u qu???n l??u tr??? ch???y
    private void requestStoragePermisssion(){
        requestPermissions(storagePermissions,STORAGE_REQUEST_CODE);
    }

    //KI???M tra quy???n camera
    private boolean checkCameraPermisssion(){
        //Ki???m tra quy???n CAMERA b???t hay ch??a
        boolean result= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)
                ==(PackageManager.PERMISSION_GRANTED);

        boolean result1= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }


    //Y??u c???u quy???n camera ch???y
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
    //X??? l?? s??? ki???n click logout

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