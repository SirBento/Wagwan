 package com.example.wagwan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

 public class ChatRoomChatActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private ImageButton SendMessageButton;
    private EditText UserMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;
    private DatabaseReference UsersRef,ChatRoomRef,ChatRoomMessageKeyRef;

    private FirebaseAuth mAuth;
    private String currentChatRoomName,currentUserID, currentUserName,currentDate,currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_chat);

        currentChatRoomName =getIntent().getExtras().get("ChatRoomName").toString();

        mAuth =FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRoomRef = FirebaseDatabase.getInstance().getReference().child("ChatRooms").child(currentChatRoomName);

        InitializeFields();

        GetUserInfo();

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SaveMessageInfoToDatabase();
                UserMessageInput.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }


        });

    }


    
     @Override
     protected void onStart()
     {
         ChatRoomRef.addChildEventListener(new ChildEventListener() {
             @Override
             public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
             {
                 if(dataSnapshot.exists())
                 { DisplayMessages(dataSnapshot);}
             }

             @Override
             public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
             {
                 if(dataSnapshot.exists())
                 {DisplayMessages(dataSnapshot);}
             }

             @Override
             public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

             @Override
             public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {}
         });
         super.onStart();
     }



     private void InitializeFields()
     {
         mToolBar = findViewById(R.id.chatRoom_chat_bar_layout);
         setSupportActionBar(mToolBar);
         getSupportActionBar().setTitle(currentChatRoomName);

         SendMessageButton =findViewById(R.id.send_message_button);
         displayTextMessages = findViewById(R.id.chatRoom_chat_text_display);
         mScrollView =  findViewById(R.id.my_scroll_view);
         UserMessageInput = findViewById(R.id.input_chatroom_message);

     }




     private void GetUserInfo()
     {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                 if (dataSnapshot.exists())
                 {
                     currentUserName = dataSnapshot.child("name").getValue().toString();
                 }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
     }


     private void SaveMessageInfoToDatabase()
     {
         String message = UserMessageInput.getText().toString();
         String messageKey = ChatRoomRef.push().getKey();

         if (TextUtils.isEmpty(message))
         {

             Toast.makeText(ChatRoomChatActivity.this, "Please Write A Message First...", Toast.LENGTH_SHORT).show();
             YoYo.with(Techniques.Shake).duration(1000).repeat(2).playOn(UserMessageInput);
         }

         else
         {
             Calendar calForDate = Calendar.getInstance();
             SimpleDateFormat currentDateFormat = new SimpleDateFormat("MM-dd-yy");
             currentDate = currentDateFormat.format(calForDate.getTime());


             Calendar calForTime = Calendar.getInstance();
             SimpleDateFormat currentTmeFormat = new SimpleDateFormat("hh:mm a");
             currentTime = currentTmeFormat.format(calForTime.getTime());


             HashMap<String,Object> chatRoomMessageKey = new HashMap<>();
             ChatRoomRef.updateChildren(chatRoomMessageKey);
             ChatRoomMessageKeyRef =ChatRoomRef.child(messageKey);

             HashMap<String,Object> MessageInfoMap = new HashMap<>();
             MessageInfoMap.put("name",currentUserName);
             MessageInfoMap.put("message",message);
             MessageInfoMap.put("date",currentDate);
             MessageInfoMap.put("time",currentTime);

             ChatRoomMessageKeyRef.updateChildren(MessageInfoMap);
         }

     }

     private void DisplayMessages(DataSnapshot dataSnapshot)
     {
         Iterator iterator = dataSnapshot.getChildren().iterator();

         while (iterator.hasNext())
         {
            String ChatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String ChatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String ChatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String ChatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessages.append(ChatName + ":\n" +ChatMessage + "\n " + ChatTime + "     " + ChatDate + "\n\n\n");
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
         }
     }


 }
