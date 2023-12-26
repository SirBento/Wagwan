package com.example.wagwan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity
{
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID;
    private Toolbar MainPageToolBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        MainPageToolBar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(MainPageToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Wagwan");


        ViewPager myViewPager = findViewById(R.id.main_tabs_pager);
        TabsAccessorAdapter myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);

        TabLayout myTabLayout = findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

    }



    @Override
    protected void onStop(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
        {
            updateUserStatus("offline");
        }

        super.onStop();
    }


    @Override
    protected void onDestroy(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
        {
            updateUserStatus("offline");
        }
        super.onDestroy();
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
         super.onOptionsItemSelected(item);
         if (item.getItemId()== R.id.main_logout_option)
         {
             updateUserStatus("offline");

            mAuth.signOut();
            SendUserToLoginActivity();
         }

        if (item.getItemId()== R.id.main_find_friends_option)
        {
            SendUserToFindFriendsActivity();
        }
        if (item.getItemId()== R.id.main_create_chat_room_option)
        {
              RequestNewGroup();
        }

        if (item.getItemId()== R.id.main_settings_option)
        {
            SendUserToSettingsActivity();
        }
        //close the app when the back arrow is clicked on the main page
        if (item.getItemId()== android.R.id.home )
        {
            onBackPressed();
            finish();
        }

        return true;

    }


    private void SendUserToLoginActivity()
    {
        Intent LoginIntent = new Intent(MainActivity.this,LoginActivity.class);
        LoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(LoginIntent);
        finish();
    }


    private void SendUserToSettingsActivity()
    {
        Intent SettingsIntent = new Intent(MainActivity.this,SettingsActivity.class);

        startActivity(SettingsIntent);

    }

    private void SendUserToFindFriendsActivity()
    {
        Intent FindFriendsIntent = new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(FindFriendsIntent);

    }


    private void RequestNewGroup()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter ChatRoom Name : ");

        final EditText chatRoomField = new EditText(MainActivity.this);
        chatRoomField.setHint("e.g  Friend Zone");
        builder.setView(chatRoomField);

        builder.setPositiveButton("Create ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String chatRoomName = chatRoomField.getText().toString();

                if(TextUtils.isEmpty(chatRoomName))
                {  Toast.makeText(MainActivity.this, "Please Give A ChatRoom Name.... ", Toast.LENGTH_SHORT).show(); }
                else
                { CreateNewChatRoom(chatRoomName);}
            }
        });


        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            { dialog.cancel(); }
        });

        builder.show();
    }

    private void CreateNewChatRoom(final String chatRoomName)
    {
        RootRef.child("ChatRooms").child(chatRoomName).setValue("")

                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this, chatRoomName + " ChatRoom was created successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void updateUserStatus(String state){
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MM-dd- yy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String , Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);

    }


}
