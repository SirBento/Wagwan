package com.example.wagwan;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wagwan.models.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment
{
        private View ContactsView;
        private RecyclerView myContactsList;
        private DatabaseReference ContatsRef, UserRef;
        private FirebaseAuth mAuth;
        private String currentUserId;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId =mAuth.getCurrentUser().getUid();
        ContatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        myContactsList = ContactsView.findViewById(R.id.contact_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));
        return  ContactsView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ContatsRef,Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapater =
                new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model)
                    {

                        String userIDs = getRef(position).getKey();
                        UserRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                               if (dataSnapshot.exists())
                               {
                                   if (dataSnapshot.child("userState").hasChild("state"))
                                   {
                                       String state = dataSnapshot.child("userState").child("state").getValue().toString();

                                       if (state.equals("online"))
                                       {
                                           holder.onlineIcon.setVisibility(View.VISIBLE);
                                       }

                                       else if (state.equals("offline"))
                                       {
                                           holder.onlineIcon.setVisibility(View.INVISIBLE);
                                       }
                                   }
                                   else
                                   {
                                       holder.onlineIcon.setVisibility(View.INVISIBLE);
                                   }

                                   if (dataSnapshot.hasChild("image"))
                                   {
                                       String userprofileImage = dataSnapshot.child("image").getValue().toString();
                                       String profileStatus = dataSnapshot.child("status").getValue().toString();
                                       String profileName = dataSnapshot.child("name").getValue().toString();

                                       holder.UserName.setText(profileName);
                                       holder.UserStatus.setText(profileStatus);
                                       Picasso.get().load(userprofileImage).placeholder(R.drawable.profile_image).into(holder.ProfileImage);

                                   }
                                   else
                                   {
                                       String profileStatus = dataSnapshot.child("status").getValue().toString();
                                       String profileName = dataSnapshot.child("name").getValue().toString();

                                       holder.UserName.setText(profileName);
                                       holder.UserStatus.setText(profileStatus);

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
                    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent,false);
                        ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                        return viewHolder;
                    }

                };
                myContactsList.setAdapter(adapater);
                adapater.startListening();

    }


    public static  class ContactsViewHolder extends RecyclerView.ViewHolder
    {
        TextView UserName, UserStatus;
        CircleImageView ProfileImage;
        ImageView onlineIcon;

        public ContactsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            UserName = itemView.findViewById(R.id.user_profile_name);
            UserStatus = itemView.findViewById(R.id.user_status);
            ProfileImage = itemView.findViewById(R.id.users_profile_image);
            onlineIcon = itemView.findViewById(R.id.user_online_status);
        }
    }

}











