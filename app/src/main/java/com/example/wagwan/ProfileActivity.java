package com.example.wagwan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserId , Current_State , senderUserId;
    private CircleImageView UserProfileImage;
    private TextView UserProfileName,UserProfileStatus;
    private Button sendMessageRequestButton,declineMessageRequestButton;
    private DatabaseReference UserRef , ChatRequestRef, ContatsRef, NotificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // making and assigning database instances
        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        ContatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");


      // getting the id of the account clicked by the app user
        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();

        // getting current user id
        senderUserId = mAuth.getCurrentUser().getUid();

        // getting and assigning layout components
        UserProfileImage = findViewById(R.id.visit_profile_image);
        UserProfileName  =findViewById(R.id.visit_user_name);
        UserProfileStatus =  findViewById(R.id.visit_profile_status);
        sendMessageRequestButton = findViewById(R.id.send_message_request_button);
        declineMessageRequestButton = findViewById(R.id.decline_message_request_button);
        Current_State ="new";

        RetriveUserInfo();


    }

    private void RetriveUserInfo()
    {
       UserRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot)
           {
               //display user details if the user has a profile picture set
               if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image")) )
               {
                   String UserImage = dataSnapshot.child("image").getValue().toString();
                   String UserName = dataSnapshot.child("name").getValue().toString();
                   String UserStatus = dataSnapshot.child("status").getValue().toString();

                   Picasso.get().load(UserImage).placeholder(R.drawable.profile_image).into(UserProfileImage);
                   UserProfileName.setText(UserName);
                   UserProfileStatus.setText(UserStatus);

                   ManageChatRequest();

               } else //display user details if the user does not have a profile picture set
               {

                   String UserName = dataSnapshot.child("name").getValue().toString();
                   String UserStatus = dataSnapshot.child("status").getValue().toString();
                   UserProfileName.setText(UserName);
                   UserProfileStatus.setText(UserStatus);
                   ManageChatRequest();
               }

           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError)
           {

           }
       });
    }

    private void ManageChatRequest()
    {
        ChatRequestRef.child(senderUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        //check if the request type exist
                        if(dataSnapshot.exists()){

                            if(dataSnapshot.hasChild(receiverUserId)){
                                String request_type =dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();
                                if (request_type.equals("sent") ){
                                    Current_State = "request_sent";
                                    sendMessageRequestButton.setText("Cancel Chat Request");
                                }
                                else if (request_type .equals("received"))
                                {
                                    Current_State = "request_received";
                                    sendMessageRequestButton.setText("Accept Chat Request");
                                    declineMessageRequestButton.setVisibility(View.VISIBLE);
                                    declineMessageRequestButton.setEnabled(true);

                                    declineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            CancelChatRequest();

                                        }
                                    });
                                }


                            }else {
                                ContatsRef.child(senderUserId)
                                        .addListenerForSingleValueEvent(new ValueEventListener()
                                        {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                            {
                                                if (dataSnapshot.hasChild(receiverUserId))
                                                {
                                                    Current_State ="friends";
                                                    sendMessageRequestButton.setText("Remove This Contact");
                                                }

                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError)
                                            {

                                            }
                                        });

                            }

                        }else{

                            //create a new request type node
                            HashMap reqType = new HashMap<>();
                            reqType.put("request_type", Current_State);
                            ChatRequestRef.child(senderUserId).child(receiverUserId).updateChildren(reqType);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if (!senderUserId.equals(receiverUserId))
        {
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    sendMessageRequestButton.setEnabled(false);

                    if (Current_State.equals("new"))
                    {
                           SendChatRequest();
                    }
                    if (Current_State.equals("request_sent"))
                    {
                         CancelChatRequest();
                    }
                    if (Current_State.equals("request_received"))
                    {
                        AcceptChatRequest();
                    }
                    if (Current_State.equals("friends"))
                    {
                        RemoveSpecificContact();
                    }

                }
            });

        }else {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveSpecificContact()
    {
        ContatsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {

                        if (task.isSuccessful())
                        {
                            ContatsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                Current_State= "new";
                                                sendMessageRequestButton.setText("Send Message");
                                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                declineMessageRequestButton.setEnabled(false);

                                            }

                                        }
                                    });

                        }


                    }
                });

    }

    private void AcceptChatRequest()
    {
        ContatsRef.child(senderUserId).child(receiverUserId)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            ContatsRef.child(receiverUserId).child(senderUserId)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                ContatsRef.child(senderUserId).child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    ContatsRef.child(receiverUserId).child(senderUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    sendMessageRequestButton.setEnabled(true);
                                                                                    Current_State ="friends";
                                                                                    sendMessageRequestButton.setText("Remove This Contact");

                                                                                    declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                                    declineMessageRequestButton.setEnabled(false);

                                                                                }
                                                                            });
                                                                }

                                                            }
                                                        });
                                            }

                                        }
                                    });

                        }

                    }
                });
    }


    private void SendChatRequest()
    {
         ChatRequestRef.child(senderUserId).child(receiverUserId)
                 .child("request_type").setValue("sent")
                 .addOnCompleteListener(new OnCompleteListener<Void>() {
                     @Override
                     public void onComplete(@NonNull Task<Void> task)
                     {

                         if (task.isSuccessful())
                         {
                             ChatRequestRef.child(receiverUserId).child(senderUserId)
                                     .child("request_type").setValue("received")
                                     .addOnCompleteListener(new OnCompleteListener<Void>() {
                                         @Override
                                         public void onComplete(@NonNull Task<Void> task)
                                         {
                                             if (task.isSuccessful())
                                             {
                                                 HashMap<String,String> chatNotificationMap = new HashMap<>();
                                                 chatNotificationMap.put("from",senderUserId);
                                                 chatNotificationMap.put("type","request");

                                                 NotificationRef.child(receiverUserId).push()
                                                         .setValue(chatNotificationMap)
                                                         .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                             @Override
                                                             public void onComplete(@NonNull Task<Void> task)
                                                             {
                                                                 if (task.isSuccessful())
                                                                 {
                                                                     sendMessageRequestButton.setEnabled(true);
                                                                     Current_State ="request_sent";
                                                                     sendMessageRequestButton.setText("Cancel  Chat Request");

                                                                 }

                                                             }
                                                         });

                                             }

                                         }
                                     });

                         }

                     }
                 });
    }

    private void CancelChatRequest()
    {
        ChatRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {

                        if (task.isSuccessful())
                        {
                            ChatRequestRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                Current_State= "new";
                                                sendMessageRequestButton.setText("Send Message");
                                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                declineMessageRequestButton.setEnabled(false);

                                            }

                                        }
                                    });

                        }


                    }
                });
    }









}
