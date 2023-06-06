package com.example.wagwan;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wagwan.adapters.UserAdapter;
import com.example.wagwan.models.ChatUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment
{
    private View PrivateChatsView;
    private RecyclerView chatsList;
    private DatabaseReference ChatsRef , UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    ArrayList<ChatUsers> list;
    UserAdapter adapter;


    public ChatsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        // Inflate the layout for this fragment
        PrivateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);
        chatsList =  PrivateChatsView.findViewById(R.id.chats_list);

        list = new ArrayList<>();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        adapter = new UserAdapter(list,getContext());
        chatsList.setAdapter(adapter);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                if (snapshot.exists()) {

                for(DataSnapshot dataSnapshot : snapshot.getChildren()){


                    if(!dataSnapshot.getKey().equals(FirebaseAuth.getInstance().getUid())){

                        ChatUsers users = dataSnapshot.getValue(ChatUsers.class);
                         users.setUserId(dataSnapshot.getKey());

                        if (dataSnapshot.child("userState").hasChild("state")) {

                            String state = dataSnapshot.child("userState").child("state").getValue().toString();

                            users.setOnlineStatus(state);
                        }
                        if (!dataSnapshot.child("userState").hasChild("state")) {

                            String state ="offline";

                            users.setOnlineStatus(state);
                        }

                        list.add(users);
                    }


                }
            }
                adapter.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return  PrivateChatsView;

    }

}
