package com.example.chatapplication.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.ImportantMessageActivity;
import com.example.chatapplication.Model.Chat;
import com.example.chatapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private final String imageUrl;
    private final Context mContext;
    private final List<Chat> mChats;
    FirebaseUser fuser;
    int viewType;

    public ChatAdapter(Context mContext, List<Chat> mChats, String imageUrl) {
        this.mChats = mChats;
        this.mContext = mContext;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        this.viewType = viewType;
        if (viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_message_right, parent, false);
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_message_left, parent, false);
        }
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = mChats.get(position);
        String type = chat.getType();
        DBHelper DB = new DBHelper(holder.itemView.getContext());
        if (!imageUrl.equals("imp")) {
            holder.itemView.setOnLongClickListener(v -> {
                if (DB.insertData(chat.getSender(), chat.getReceiver(), chat.getMessage(), chat.getType(), chat.getTime().toLocaleString()))
                    Toast.makeText(mContext, "This Message was added to Important Messages", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(mContext, "This Message was not added to Important Messages", Toast.LENGTH_LONG).show();
                return true;
            });
        } else {
            holder.itemView.setOnLongClickListener(v -> {
                if (DB.deleteData(chat.getMessage())) {
                    Toast.makeText(mContext, "This Message was deleted from Important Messages", Toast.LENGTH_LONG).show();
                    mContext.startActivity(new Intent(mContext, ImportantMessageActivity.class));
                    ((Activity)mContext).finish();
                }
                else
                    Toast.makeText(mContext, "This Message was not deleted from Important Messages", Toast.LENGTH_LONG).show();
                return true;
            });
        }
        if (type.equals("text")) {
            holder.imageView.setVisibility(View.GONE);
            holder.imageSeenStatus.setVisibility(View.GONE);
            holder.imageSeenStatusText.setVisibility(View.GONE);
            holder.show_message.setText(chat.getMessage());
            holder.msgTime.setText(chat.getTime().getHours() + ":" + chat.getTime().getMinutes());
        } else {
            holder.show_message.setVisibility(View.GONE);
            holder.seenStatus.setVisibility(View.GONE);
            holder.seenStatusText.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
            holder.msgTime2.setVisibility(View.VISIBLE);
            holder.msgTime.setVisibility(View.GONE);
            holder.msgTime2.setText(chat.getTime().getHours() + ":" + chat.getTime().getMinutes());
            holder.imageType.setVisibility(View.VISIBLE);
            String first = type.substring(0, 1);
            String remaining = type.substring(1);
            holder.imageType.setText(first.toUpperCase() + remaining);
            switch (type) {
                case "image":
                    holder.imageType.setVisibility(View.GONE);
                    Glide.with(mContext).load(chat.getMessage()).into(holder.imageView);
                    break;
                case "pdf":
                    holder.imageView.setImageResource(R.drawable.pdf_image);
                    break;
                case "word":
                    holder.imageView.setImageResource(R.drawable.doc_image);
                    break;
                case "location":
                    holder.imageView.setImageResource(R.drawable.location_image);
                    break;
                default:
                    holder.imageView.setImageResource(R.drawable.other_file_image);
            }
            if (!imageUrl.equals("imp")) {
                holder.imageView.setOnLongClickListener(v -> {
                    if (DB.insertData(chat.getSender(), chat.getReceiver(), chat.getMessage(), chat.getType(), chat.getTime().toLocaleString()))
                        Toast.makeText(mContext, "This Message was added to Important Messages", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(mContext, "This Message was not added to Important Messages", Toast.LENGTH_LONG).show();
                    return true;
                });
            } else {
                holder.imageView.setOnLongClickListener(v -> {
                    if (DB.deleteData(chat.getMessage())) {
                        Toast.makeText(mContext, "This Message was deleted from Important Messages", Toast.LENGTH_LONG).show();
                        mContext.startActivity(new Intent(mContext, ImportantMessageActivity.class));
                        ((Activity)mContext).finish();
                    }
                    else
                        Toast.makeText(mContext, "This Message was not deleted from Important Messages", Toast.LENGTH_LONG).show();
                    return true;
                });
            }

            holder.imageView.setOnClickListener(v -> {
                if (!chat.getType().equals("location")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(chat.getMessage()));
                    holder.imageView.getContext().startActivity(intent);
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("geo:" + chat.getMessage()));
                    Intent chooser = Intent.createChooser(i, "Launch Maps");
                    holder.itemView.getContext().startActivity(chooser);
                }
            });
        }
        if (viewType == MSG_TYPE_LEFT) {
            if (imageUrl.equals("group") || imageUrl.equals("imp")) {
                holder.nameTextView.setVisibility(View.VISIBLE);
                FirebaseDatabase.getInstance().getReference("Users").child(chat.getSender()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String img = Objects.requireNonNull(snapshot.child("imageURL").getValue()).toString();
                        if (!img.equals("default") && !img.isEmpty()) {
                            try {
                                Glide.with(mContext).load(img).into(holder.profile_image);
                            } catch (Exception ignored) {
                            }
                        }
                        holder.nameTextView.setText(Objects.requireNonNull(snapshot.child("name").getValue()).toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } else if (!imageUrl.equals("default")) {
                Glide.with(mContext).load(imageUrl).into(holder.profile_image);
            }
        }
        if (!imageUrl.equals("group") && position == mChats.size() - 1 && viewType != MSG_TYPE_LEFT) {
            if (type.equals("text")) {
                holder.seenStatus.setVisibility(View.VISIBLE);
                holder.seenStatusText.setVisibility(View.VISIBLE);
            } else {
                holder.imageSeenStatus.setVisibility(View.VISIBLE);
                holder.imageSeenStatusText.setVisibility(View.VISIBLE);
            }
            if (chat.isIsseen()) {
                if (type.equals("text")) {
                    holder.seenStatus.setImageResource(R.drawable.ic_seen);
                    holder.seenStatusText.setText("Seen");
                } else {
                    holder.imageSeenStatus.setImageResource(R.drawable.ic_seen);
                    holder.imageSeenStatusText.setText("Seen");
                }
            } else if (type.equals("text")) {
                holder.seenStatus.setImageResource(R.drawable.ic_delivered);
                holder.seenStatusText.setText("Delivered");
            } else {
                holder.imageSeenStatus.setImageResource(R.drawable.ic_delivered);
                holder.imageSeenStatusText.setText("Delivered");
            }
        } else {
            holder.seenStatus.setVisibility(View.GONE);
            holder.seenStatusText.setVisibility(View.GONE);
            holder.imageSeenStatus.setVisibility(View.GONE);
            holder.imageSeenStatusText.setVisibility(View.GONE);
        }

    }


    @Override
    public int getItemCount() {
        return mChats.size();
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChats.get(position).getSender().equals(fuser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView nameTextView, show_message, seenStatusText, imageSeenStatusText, msgTime, msgTime2, imageType;
        public ImageView profile_image, seenStatus, imageView, imageSeenStatus;

        public ViewHolder(View itemView) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.nameTextView);
            show_message = itemView.findViewById(R.id.show_message);
            msgTime = itemView.findViewById(R.id.msg_time);
            msgTime2 = itemView.findViewById(R.id.image_msg_time);
            profile_image = itemView.findViewById(R.id.profile_image);
            seenStatusText = itemView.findViewById(R.id.seenStatusText);
            seenStatus = itemView.findViewById(R.id.seenStatus);
            imageView = itemView.findViewById(R.id.imageView);
            imageSeenStatusText = itemView.findViewById(R.id.imageSeenStatusText);
            imageSeenStatus = itemView.findViewById(R.id.imageSeenStatus);
            imageType = itemView.findViewById(R.id.imageText);
        }

    }
}