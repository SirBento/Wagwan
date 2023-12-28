package com.example.wagwan.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wagwan.ChatActivity;
import com.example.wagwan.R;
import com.example.wagwan.models.ChatUsers;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends  RecyclerView.Adapter<UserAdapter.ViewHolder> {
    ArrayList<ChatUsers> list;
    Context context;

    public UserAdapter(ArrayList<ChatUsers> list, Context context) {
        this.list = list;
        this.context = context; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.users_display_layout, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final ChatUsers users = list.get(position);
        Picasso.get().load(users.getImage()).placeholder(R.drawable.profile_image).into(holder.ProfileImage);
        holder.UserName.setText(users.getName());
        holder.UserStatus.setText(users.getStatus());

        // changed from ==0 to >=0 or !=something
        if (position >= 0) {
            if (users.getOnlineStatus().equals("online")) {

                holder.onlineStatus.setVisibility(View.VISIBLE);

            } else if (!users.getOnlineStatus().equals("offline")) {

                holder.onlineStatus.setVisibility(View.INVISIBLE);
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent ChatIntent = new Intent(context, ChatActivity.class);
                ChatIntent.putExtra("visit_user_id",users.getUserId());
                ChatIntent.putExtra("visit_user_name",users.getName());
                ChatIntent.putExtra("visit_user_image", users.getImage());
                context.startActivity(ChatIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        TextView UserName, UserStatus;
        CircleImageView ProfileImage;
        ImageView onlineStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        // assigning the holder with the user display
            UserName = itemView.findViewById(R.id.user_profile_name);
            UserStatus = itemView.findViewById(R.id.user_status);
            ProfileImage = itemView.findViewById(R.id.users_profile_image);
            onlineStatus = itemView.findViewById(R.id.user_online_status);
        }
    }
}
