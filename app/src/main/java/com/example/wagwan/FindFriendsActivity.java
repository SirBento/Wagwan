package com.example.wagwan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.wagwan.models.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView FindFreindsRecyclerList;
    private DatabaseReference UserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        FindFreindsRecyclerList = findViewById(R.id.find_friends_recycler_list);
        FindFreindsRecyclerList.setLayoutManager(new LinearLayoutManager(this));


        mToolbar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");


    }
/*
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }
*/
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(UserRef, Contacts.class)
                        .build();


        FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {


                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contacts model) {
                        final String visit_user_id = getRef(position).getKey();

                        //list of all users excluding the current user
                        if(!visit_user_id.equals(FirebaseAuth.getInstance().getUid())) {

                            //enhance the list to remove those that don't have their names and status set
                          if(!(model.getName().isEmpty()||model.getStatus().isEmpty()
                                || model.getName().equals(null)||model.getStatus().equals(null)
                                      || model.getName().equals("")||model.getStatus().equals(""))){

                            holder.UserName.setText(model.getName());
                            holder.UserStatus.setText(model.getStatus());
                            Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.ProfileImage);

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent ProfileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                                    ProfileIntent.putExtra("visit_user_id", visit_user_id);
                                    startActivity(ProfileIntent);

                                }
                            });
                         }
                        }
                    }
                    @NonNull
                    @Override
                    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        FindFriendViewHolder viewHolder = new FindFriendViewHolder(view);
                        return viewHolder;
                    }



                };
        FindFreindsRecyclerList.setAdapter(adapter);
        adapter.startListening();


    }


    public static class FindFriendViewHolder extends RecyclerView.ViewHolder {
        TextView UserName, UserStatus;
        CircleImageView ProfileImage;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            UserName = itemView.findViewById(R.id.user_profile_name);
            UserStatus = itemView.findViewById(R.id.user_status);
            ProfileImage = itemView.findViewById(R.id.users_profile_image);


        }
    }

}

