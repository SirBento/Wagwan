package com.example.wagwan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.util.HashMap;
import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity
{

   private Button UpdateAccountSettings;
   private EditText userName,userStatus;
   private CircleImageView userProfileImage;
   private FirebaseAuth mAuth;
   private String currentUserID;
   private DatabaseReference RootRef;
   private static final  int GalleryPick = 1 ;
   private StorageReference UserProfileImagesRef;
   private ProgressDialog loadingBar;
   private Toolbar SettingsToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        currentUserID = mAuth.getCurrentUser().getUid();
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        loadingBar = new ProgressDialog(SettingsActivity.this);


        InitializeFields();

        userName.setVisibility(View.INVISIBLE);

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UpdateSettings();
            }
        });

        RetrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent GalleryIntent = new Intent();
                GalleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                GalleryIntent.setType("image/*");
                startActivityForResult(GalleryIntent,GalleryPick);

            }
        });

    }




    private void InitializeFields() {
        UpdateAccountSettings = findViewById(R.id.update_settings_button);
        userName = findViewById(R.id.set_user_name);
        userStatus =findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.set_profile_image);
        SettingsToolBar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         if (requestCode ==GalleryPick  && resultCode == RESULT_OK && data != null )
         {
             //Allow user to crop their profile picture
             Uri ImageUri = data.getData();
             CropImage.activity()
                     .setGuidelines(CropImageView.Guidelines.ON)
                     .setAspectRatio(1,1)
                     .start(SettingsActivity.this);
         }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            // uploading user profile
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

           if (resultCode == RESULT_OK)
           {
               loadingBar.setTitle("Set Profile Image");
               loadingBar.setMessage("Please wait, were are updating your profile... ");
               loadingBar.setCanceledOnTouchOutside(false);
               loadingBar.show();

                Uri resultUri = result.getUri();
               userProfileImage.setImageURI(resultUri);
               StorageReference filePath = UserProfileImagesRef.child(currentUserID + ".jpg");


               filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>(){
                           @Override
                           public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                           {
                               final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();

                               firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>()
                               {
                                   @Override
                                   public void onSuccess(Uri uri)
                                   {

                                       Toast.makeText(SettingsActivity.this, "Profile Picture Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                       final String downloadUrl = uri.toString();
                                       // complete the rest of your code

                                       RootRef.child("Users").child(currentUserID).child("image").setValue(downloadUrl)
                                               .addOnCompleteListener(new OnCompleteListener<Void>()
                                               {
                                                   @Override
                                                   public void onComplete(@NonNull Task<Void> task)
                                                   {
                                                       if (task.isSuccessful()){

                                                           Toast.makeText(SettingsActivity.this, "Image Saved To The Database Successfully", Toast.LENGTH_SHORT).show();
                                                           loadingBar.dismiss();

                                                       }else{

                                                           Toast.makeText(SettingsActivity.this, "Error: Please check your internet connection", Toast.LENGTH_SHORT).show();
                                                           loadingBar.dismiss();
                                                       }

                                                   }
                                               });
                                   }

                               });
                           }

                           });
                       }

           }
        }


    private void UpdateSettings()
    {
        String setUsername = userName.getText().toString();
        String setUserStatus = userStatus.getText().toString();
        RootRef = FirebaseDatabase.getInstance().getReference();

        if(TextUtils.isEmpty(setUsername))
        {
            Toast.makeText(this, "Please Enter Your UserName.....!!!", Toast.LENGTH_SHORT).show();
            YoYo.with(Techniques.Shake).duration(1000).repeat(2).playOn(userName);
        }

        if(TextUtils.isEmpty(setUserStatus))
        {
            Toast.makeText(this, "Please Enter Your Status.....!!!", Toast.LENGTH_SHORT).show();
            YoYo.with(Techniques.Shake).duration(1000).repeat(2).playOn(userStatus);
        }

        else
        {
            HashMap<String ,Object> profileMap = new HashMap<>();
            profileMap.put("uid",currentUserID);
            profileMap.put("name",setUsername);
            profileMap.put("status",setUserStatus);
            RootRef.child("Users").child(currentUserID).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                SendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();

                            }else {
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error: "+ message, Toast.LENGTH_SHORT).show();
                            }


                        }
                    });



        }


    }

    private void SendUserToMainActivity()
    {
        Intent MainIntent = new Intent(SettingsActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }

    private void RetrieveUserInfo()
    {
        String currentUserId = mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener()
        {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

                if ((dataSnapshot.exists()) &&(dataSnapshot.hasChild("name")) &&(dataSnapshot.hasChild("image")) )
                {
                    String retriveUserName = dataSnapshot.child("name").getValue().toString();
                    String retriveUserStatus = dataSnapshot.child("status").getValue().toString();
                    String retriveProfileImage = dataSnapshot.child("image").getValue().toString();

                    userName.setText(retriveUserName);
                    userName.setText(retriveUserStatus);
                    Picasso.get().load(retriveProfileImage).into(userProfileImage);
                }else if ((dataSnapshot.exists()) &&(dataSnapshot.hasChild("name"))){

                    String retriveUserName = dataSnapshot.child("name").getValue().toString();
                    String retriveUserStatus = dataSnapshot.child("status").getValue().toString();

                    userName.setText(retriveUserName);
                    userName.setText(retriveUserStatus);
                }else{
                    userName.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this, "Please Update Your Profile Information...!!!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Toast.makeText(SettingsActivity.this, "Please check your internet connection!!!", Toast.LENGTH_LONG).show();

            }
        });
    }

}
