package com.example.wagwan;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wagwan.models.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestFragment extends Fragment
{

    private View RequestFragmentView ;
    private RecyclerView myRequestsList;
    private DatabaseReference ChatRequestsRef , UsersRef,ContactsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;


    public RequestFragment()
    {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestFragmentView = inflater.inflate(R.layout.fragment_request, container, false);

       ChatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
       ContactsRef =FirebaseDatabase.getInstance().getReference().child("Contacts");
       mAuth =FirebaseAuth.getInstance();
       currentUserID= mAuth.getCurrentUser().getUid();
       UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

       myRequestsList = RequestFragmentView.findViewById(R.id.chat_requests_list);
       myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));


        return  RequestFragmentView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ChatRequestsRef.child(currentUserID),Contacts.class)
                        .build();


        FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> adapater =
                new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {


                    @Override
                    protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Contacts model)
                    {
                        holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);


                        final String list_user_id = getRef(position).getKey();
                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();


                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if (dataSnapshot.exists())
                                {
                                    String type =dataSnapshot.getValue().toString();

                                    if (type.equals("received"))
                                    {
                                        UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener()
                                        {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                            {
                                                if(dataSnapshot.hasChild("image"))
                                                {

                                                    final String requestProfileImage = dataSnapshot.child("image").getValue().toString();


                                                    Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(holder.ProfileImage);

                                                }
                                                final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                final String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                                holder.UserName.setText(requestUserName);
                                                holder.UserStatus.setText("Wants To Connect With You");


                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v)
                                                    {
                                                        CharSequence options[] = new CharSequence[]
                                                                {
                                                                        "Accept" ,
                                                                        "Cancel"
                                                                };
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                        builder.setTitle(requestUserName + "  Chat Requests");
                                                        builder.setItems(options, new DialogInterface.OnClickListener()
                                                        {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which)
                                                            {
                                                                if(which == 0)
                                                                {
                                                                    ContactsRef.child(currentUserID).child(list_user_id).child("Contacts")
                                                                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>()
                                                                    {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                        {
                                                                            if (task.isSuccessful())
                                                                            {
                                                                                ContactsRef.child(list_user_id).child(currentUserID).child("Contacts")
                                                                                        .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                    {
                                                                                        if (task.isSuccessful())
                                                                                        {
                                                                                            ChatRequestsRef.child(currentUserID).child(list_user_id)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                                                        {
                                                                                                            if(task.isSuccessful())
                                                                                                            {
                                                                                                                ChatRequestsRef.child(list_user_id).child(currentUserID)
                                                                                                                        .removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                                                                            {
                                                                                                                                if(task.isSuccessful())
                                                                                                                                {
                                                                                                                                    Toast.makeText(getContext() , "Contact Saved", Toast.LENGTH_SHORT).show();

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

                                                                        }
                                                                    });

                                                                }
                                                                if(which == 1)
                                                                {
                                                                    ChatRequestsRef.child(currentUserID).child(list_user_id)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        ChatRequestsRef.child(list_user_id).child(currentUserID)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                                    {
                                                                                                        if(task.isSuccessful())
                                                                                                        {
                                                                                                            Toast.makeText(getContext() , "Contact Deleted", Toast.LENGTH_SHORT).show();

                                                                                                        }

                                                                                                    }
                                                                                                });

                                                                                    }

                                                                                }
                                                                            });

                                                                }

                                                            }
                                                        });
                                                        builder.show();

                                                    }
                                                });

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError)
                                            {

                                            }
                                        });

                                    } else if(type.equals("sent"))
                                    {
                                        Button request_sent_btn = holder.itemView.findViewById(R.id.request_accept_btn);
                                        request_sent_btn.setText("Req Sent");
                                        holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.INVISIBLE);

                                        UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener()
                                        {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                            {
                                                if(dataSnapshot.hasChild("image"))
                                                {

                                                    final String requestProfileImage = dataSnapshot.child("image").getValue().toString();


                                                    Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(holder.ProfileImage);

                                                }
                                                final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                final String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                                holder.UserName.setText(requestUserName);
                                                holder.UserStatus.setText("Request sent to "+ requestUserName );


                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v)
                                                    {
                                                        CharSequence options[] = new CharSequence[]
                                                                {
                                                                        "Cancel Chat Request"

                                                                };
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                        builder.setTitle("Sent Request");
                                                        builder.setItems(options, new DialogInterface.OnClickListener()
                                                        {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which)
                                                            {
                                                                if(which == 0)
                                                                {
                                                                    ChatRequestsRef.child(currentUserID).child(list_user_id)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        ChatRequestsRef.child(list_user_id).child(currentUserID)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                                    {
                                                                                                        if(task.isSuccessful())
                                                                                                        {
                                                                                                            Toast.makeText(getContext() , "Chat Request Cancelled", Toast.LENGTH_SHORT).show();

                                                                                                        }

                                                                                                    }
                                                                                                });

                                                                                    }

                                                                                }
                                                                            });

                                                                }

                                                            }
                                                        });
                                                        builder.show();

                                                    }
                                                });

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError)
                                            {

                                            }
                                        });

                                    }

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError)
                            {

                            }
                        });

                    }
                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent,false);
                        RequestsViewHolder viewHolder = new RequestsViewHolder(view);
                        return viewHolder;
                    }


                };
        myRequestsList.setAdapter(adapater);
        adapater.startListening();




    }





    public static  class RequestsViewHolder extends RecyclerView.ViewHolder {
        TextView UserName, UserStatus;
        CircleImageView ProfileImage;
        Button AcceptButton, CancelButton;

        public RequestsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            UserName = itemView.findViewById(R.id.user_profile_name);
            UserStatus = itemView.findViewById(R.id.user_status);
            ProfileImage = itemView.findViewById(R.id.users_profile_image);
            AcceptButton =itemView.findViewById(R.id.request_accept_btn);
            CancelButton =itemView.findViewById(R.id.request_cancel_btn);


        }
    }


    }

