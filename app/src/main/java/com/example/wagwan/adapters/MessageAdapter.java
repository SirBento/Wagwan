package com.example.wagwan.adapters;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wagwan.ImageViwerActivity;
import com.example.wagwan.MainActivity;
import com.example.wagwan.R;
import com.example.wagwan.models.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private List<Messages> userMessagesList;
    public FirebaseAuth mAuth;
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
                holder.senderMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + "  -  " + messages.getDate());

            } else {

                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);

                holder.receiverMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + "  -  " + messages.getDate());

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
                      AlertDialog.Builder builder = new  AlertDialog.Builder(holder.itemView.getContext());
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
                      AlertDialog.Builder builder = new  AlertDialog.Builder(holder.itemView.getContext());
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
                      AlertDialog.Builder builder = new  AlertDialog.Builder(holder.itemView.getContext());
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
                      AlertDialog.Builder builder = new  AlertDialog.Builder(holder.itemView.getContext());
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
                      CharSequence options[] = new CharSequence[]
                              {
                                      "Delete For Me",
                                      "Cancel"
                              };
                      AlertDialog.Builder builder = new  AlertDialog.Builder(holder.itemView.getContext());
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
                      AlertDialog.Builder builder = new  AlertDialog.Builder(holder.itemView.getContext());
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
