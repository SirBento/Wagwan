/*package com.example.wagwan.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wagwan.ImageViwerActivity;
import com.example.wagwan.MainActivity;
import com.example.wagwan.R;
import com.example.wagwan.models.Messages;
import com.example.wagwan.models.Requests;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestsViewHolder> {

    private DatabaseReference ContactsRef,ChatRequestsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    ArrayList<Requests> list;
    Context context;

    public RequestAdapter(ArrayList<Requests> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.users_display_layout, parent,false);
        return new RequestAdapter.RequestsViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull RequestsViewHolder holder, int position) {

        //database references
        ChatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        mAuth =FirebaseAuth.getInstance();
        currentUserID= mAuth.getCurrentUser().getUid();

        //set name, status profilePic and set buttons visible
        final Requests requests = list.get(position);

        Picasso.get().load(requests.getImage()).placeholder(R.drawable.profile_image).into(holder.ProfileImage);
        holder.UserName.setText(requests.getName());
        holder.UserStatus.setText(requests.getStatus());

        if (position >= 0) {
            if (requests.getOnlineStatus().equals("online")) {

                holder.onlineStatus.setVisibility(View.VISIBLE);

            } else if (!requests.getOnlineStatus().equals("offline")) {

                holder.onlineStatus.setVisibility(View.INVISIBLE);
            }
        }

        holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);
        holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);

        final String list_user_id =  requests.getUserId();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CharSequence options[] = new CharSequence[]
                        {
                                "Accept" ,
                                "Cancel"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(requests.getName() + "  Chat Requests");
                builder.setItems(options, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if(which == 0) {
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
                                                                                                            Toast.makeText(context , "Contact Saved", Toast.LENGTH_SHORT).show();

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
                        if(which == 1) {
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
                                                                    Toast.makeText(context , "Contact Deleted", Toast.LENGTH_SHORT).show();

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
    public int getItemCount() {
        return list.size();
    }


    public static  class RequestsViewHolder extends RecyclerView.ViewHolder {
        TextView UserName, UserStatus;
        CircleImageView ProfileImage;
        Button AcceptButton, CancelButton;
        ImageView onlineStatus;

        public RequestsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            UserName = itemView.findViewById(R.id.user_profile_name);
            UserStatus = itemView.findViewById(R.id.user_status);
            ProfileImage = itemView.findViewById(R.id.users_profile_image);
            AcceptButton =itemView.findViewById(R.id.request_accept_btn);
            CancelButton =itemView.findViewById(R.id.request_cancel_btn);
            onlineStatus = itemView.findViewById(R.id.user_online_status);


        }
    }

    public static class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
    {
        private List<Messages> userMessagesList;
        private FirebaseAuth mAuth;
        private DatabaseReference usersRef;

        public MessageAdapter(List<Messages> userMessagesList)
        {
            this.userMessagesList = userMessagesList;
        }

        public class MessageViewHolder extends RecyclerView.ViewHolder
        {
            public TextView senderMessageText, receiverMessageText;
            public CircleImageView receiverProfileImage;
            public ImageView messageSenderPicture, messageReceiverPicture;

            public MessageViewHolder(@NonNull View itemView)
            {
                super(itemView);

                senderMessageText = itemView.findViewById(R.id.sender_message_text);
                receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
                receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
                messageSenderPicture =itemView.findViewById(R.id.message_sender_image_view);
                messageReceiverPicture =itemView.findViewById(R.id.message_receiver_image_view);
            }
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.custom_messages_layout,parent,false);

            mAuth = FirebaseAuth.getInstance();


            return new MessageViewHolder(view);

        }

        @Override
        public void onBindViewHolder(@NonNull final MessageViewHolder holder, @SuppressLint("RecyclerView") final int position) {
            String messageSenderID = mAuth.getCurrentUser().getUid();
            Messages messages = userMessagesList.get(position);

            String fromUserID = messages.getFrom();
            String fromMessageType = messages.getType();
            usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

            usersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("image")) {
                        String receiverImage = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            holder.receiverMessageText.setVisibility(View.GONE);
            holder.receiverProfileImage.setVisibility(View.GONE);
            holder.senderMessageText.setVisibility(View.GONE);
            holder.messageSenderPicture.setVisibility(View.GONE);
            holder.messageReceiverPicture.setVisibility(View.GONE);


            if (fromMessageType.equals("text"))
            {


                if (fromUserID.equals(messageSenderID)) {
                    holder.senderMessageText.setVisibility(View.VISIBLE);

                    holder.senderMessageText.setBackgroundResource(R.drawable.sender_messeges_layout);
                    holder.senderMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());

                } else {

                    holder.receiverProfileImage.setVisibility(View.VISIBLE);
                    holder.receiverMessageText.setVisibility(View.VISIBLE);
                    holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);

                    holder.receiverMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());

                }

            }
            else if (fromMessageType.equals("image"))
            {
                if (fromUserID.equals(messageSenderID))
                {
                    holder.messageSenderPicture.setVisibility(View.VISIBLE);
                    Picasso.get().load(messages.getMessage()).into(holder.messageSenderPicture);

                }
                else
                {
                    holder.receiverProfileImage.setVisibility(View.VISIBLE);
                    holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                    Picasso.get().load(messages.getMessage()).into(holder.messageReceiverPicture);

                }

            }
            else if (fromMessageType.equals("pdf") || fromMessageType.equals("docx") )
             {
                 if (fromUserID.equals(messageSenderID))
                 {
                     holder.messageSenderPicture.setVisibility(View.VISIBLE);
                     holder.messageSenderPicture.setBackgroundResource(R.drawable.file);

                 }
                 else
                 {
                     holder.receiverProfileImage.setVisibility(View.VISIBLE);
                     holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                     holder.messageReceiverPicture.setBackgroundResource(R.drawable.file);

                     holder.itemView.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v)
                         {
                             Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                             holder.itemView.getContext().startActivity(intent);

                         }
                     });

                 }

             }

          if (fromUserID.equals(messageSenderID))
          {
              holder.itemView.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v)
                  {
                      if(userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx"))
                      {
                          CharSequence options[] = new CharSequence[]
                                  {
                                          "Delete For Me",
                                          "Download and View This Document ",
                                          "Cancel",
                                          "Delete For Everyone"
                                  };
                          androidx.appcompat.app.AlertDialog.Builder builder = new  androidx.appcompat.app.AlertDialog.Builder(holder.itemView.getContext());
                          builder.setTitle("Delete Message");
                          builder.setItems(options, new DialogInterface.OnClickListener() {
                              @Override
                              public void onClick(DialogInterface dialog, int which)
                              {
                                  if(which == 0)
                                  {
                                      DeleteSentMessage(position ,holder);
                                      Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                      holder.itemView.getContext().startActivity(intent);
                                  }
                                  else if(which == 1)
                                  {
                                      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                      holder.itemView.getContext().startActivity(intent);

                                  }
                                  else if(which == 3)
                                  {
                                      DeleteMessageForEveryone(position ,holder);
                                      Intent intent = new Intent(holder.itemView.getContext(),MainActivity.class);
                                      holder.itemView.getContext().startActivity(intent);
                                  }

                              }
                          });
                          builder.show();

                      }

                     else if(userMessagesList.get(position).getType().equals("text"))
                      {
                          CharSequence options[] = new CharSequence[]
                                  {
                                          "Delete For Me",
                                          "Cancel",
                                          "Delete For Everyone"
                                  };
                          androidx.appcompat.app.AlertDialog.Builder builder = new  androidx.appcompat.app.AlertDialog.Builder(holder.itemView.getContext());
                          builder.setTitle("Delete Message");
                          builder.setItems(options, new DialogInterface.OnClickListener() {
                              @Override
                              public void onClick(DialogInterface dialog, int which)
                              {
                                  if(which == 0)
                                  {
                                      DeleteSentMessage(position ,holder);
                                      Intent intent = new Intent(holder.itemView.getContext(),MainActivity.class);
                                      holder.itemView.getContext().startActivity(intent);
                                  }

                                  else if(which == 2)
                                  {
                                      DeleteMessageForEveryone(position ,holder);
                                      Intent intent = new Intent(holder.itemView.getContext(),MainActivity.class);
                                      holder.itemView.getContext().startActivity(intent);
                                  }

                              }
                          });
                          builder.show();

                      }

                      else if(userMessagesList.get(position).getType().equals("image"))
                      {
                          CharSequence options[] = new CharSequence[]
                                  {
                                          "Delete For Me",
                                          "View This Image",
                                          "Cancel",
                                          "Delete For Everyone"
                                  };
                          androidx.appcompat.app.AlertDialog.Builder builder = new  androidx.appcompat.app.AlertDialog.Builder(holder.itemView.getContext());
                          builder.setTitle("Delete Message");
                          builder.setItems(options, new DialogInterface.OnClickListener() {
                              @Override
                              public void onClick(DialogInterface dialog, int which)
                              {
                                  if(which == 0)
                                  {
                                      DeleteSentMessage(position ,holder);
                                      Intent intent = new Intent(holder.itemView.getContext(),MainActivity.class);
                                      holder.itemView.getContext().startActivity(intent);

                                  }
                                  else if(which == 1)
                                  {
                                      Intent intent = new Intent(holder.itemView.getContext(), ImageViwerActivity.class);
                                      intent.putExtra("url", userMessagesList.get(position).getMessage());
                                      holder.itemView.getContext().startActivity(intent);

                                  }

                                  else if(which == 3)
                                  {
                                      DeleteMessageForEveryone(position ,holder);
                                      Intent intent = new Intent(holder.itemView.getContext(),MainActivity.class);
                                      holder.itemView.getContext().startActivity(intent);
                                  }

                              }
                          });
                          builder.show();

                      }

                  }
              });

          }
          else
          {
              holder.itemView.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v)
                  {
                      if(userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx"))
                      {
                          CharSequence options[] = new CharSequence[]
                                  {
                                          "Delete For Me",
                                          "Download and View This Document ",
                                          "Cancel"
                                  };
                          androidx.appcompat.app.AlertDialog.Builder builder = new  androidx.appcompat.app.AlertDialog.Builder(holder.itemView.getContext());
                          builder.setTitle("Delete Message");
                          builder.setItems(options, new DialogInterface.OnClickListener() {
                              @Override
                              public void onClick(DialogInterface dialog, int which)
                              {
                                  if(which == 0)
                                  {
                                      DeleteReceiveMessage(position ,holder);
                                      Intent intent = new Intent(holder.itemView.getContext(),MainActivity.class);
                                      holder.itemView.getContext().startActivity(intent);
                                  }
                                  else if(which == 1)
                                  {
                                      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                      holder.itemView.getContext().startActivity(intent);

                                  }


                              }
                          });
                          builder.show();

                      }

                      else if(userMessagesList.get(position).getType().equals("text"))
                      {
                          CharSequence[] options = new CharSequence[]
                                  {
                                          "Delete For Me",
                                          "Cancel"
                                  };
                          androidx.appcompat.app.AlertDialog.Builder builder = new  androidx.appcompat.app.AlertDialog.Builder(holder.itemView.getContext());
                          builder.setTitle("Delete Message");
                          builder.setItems(options, new DialogInterface.OnClickListener() {
                              @Override
                              public void onClick(DialogInterface dialog, int which)
                              {
                                  if(which == 0)
                                  {
                                      DeleteReceiveMessage(position ,holder);

                                  }


                              }
                          });
                          builder.show();

                      }

                      else if(userMessagesList.get(position).getType().equals("image"))
                      {
                          CharSequence options[] = new CharSequence[]
                                  {
                                          "Delete For Me",
                                          "View This Image",
                                          "Cancel"

                                  };
                          androidx.appcompat.app.AlertDialog.Builder builder = new  androidx.appcompat.app.AlertDialog.Builder(holder.itemView.getContext());
                          builder.setTitle("Delete Message");
                          builder.setItems(options, new DialogInterface.OnClickListener() {
                              @Override
                              public void onClick(DialogInterface dialog, int which)
                              {
                                  if(which == 0)
                                  {
                                     DeleteReceiveMessage(position ,holder);

                                      Intent intent = new Intent(holder.itemView.getContext(),MainActivity.class);
                                      holder.itemView.getContext().startActivity(intent);
                                  }
                                  else if(which == 1)
                                  {
                                      Intent intent = new Intent(holder.itemView.getContext(),ImageViwerActivity.class);
                                      intent.putExtra("url", userMessagesList.get(position).getMessage());
                                      holder.itemView.getContext().startActivity(intent);

                                  }



                              }
                          });
                          builder.show();

                      }
                  }
              });

          }

        }

        @Override
        public int getItemCount()
        {
            return userMessagesList.size();
        }



        private void DeleteSentMessage(final int position , final MessageViewHolder holder)
        {
            DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();
            RootRef.child("Messages")
                    .child(userMessagesList.get(position).getFrom())
                    .child(userMessagesList.get(position).getTo())
                    .child(userMessagesList.get(position).getMessageID())
                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(holder.itemView.getContext(), "Message Deleted Successfully", Toast.LENGTH_SHORT).show();
                    }
                    {
                        Toast.makeText(holder.itemView.getContext(), "Error Occurred....", Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }


        private void DeleteReceiveMessage(final int position , final MessageViewHolder holder)
        {
            DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();
            RootRef.child("Messages")
                    .child(userMessagesList.get(position).getTo())
                    .child(userMessagesList.get(position).getFrom())
                    .child(userMessagesList.get(position).getMessageID())
                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(holder.itemView.getContext(), "Message Deleted Successfully", Toast.LENGTH_SHORT).show();
                    }
                    {
                        Toast.makeText(holder.itemView.getContext(), "Error Occurred....", Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }

        private void DeleteMessageForEveryone(final int position , final MessageViewHolder holder)
        {
            final DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();
            RootRef.child("Messages")
                    .child(userMessagesList.get(position).getTo())
                    .child(userMessagesList.get(position).getFrom())
                    .child(userMessagesList.get(position).getMessageID())
                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if (task.isSuccessful())
                    {
                        RootRef.child("Messages")
                                .child(userMessagesList.get(position).getFrom())
                                .child(userMessagesList.get(position).getTo())
                                .child(userMessagesList.get(position).getMessageID())
                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if (task.isSuccessful())
                                {
                                    Toast.makeText(holder.itemView.getContext(), "Message Deleted Successfully", Toast.LENGTH_SHORT).show();

                                }

                            }
                        });

                    }
                    {
                        Toast.makeText(holder.itemView.getContext(), "Error Occurred....", Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }






    }
}
*/



