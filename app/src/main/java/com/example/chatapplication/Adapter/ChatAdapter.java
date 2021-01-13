package com.example.chatapplication.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.Model.Chat;
import com.example.chatapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;


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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = mChats.get(position);

        holder.show_message.setText(chat.getMessage());

        if (imageUrl.equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(imageUrl).into(holder.profile_image);
        }
        if (position == mChats.size() - 1) {
            if (chat.isIsseen()) {
                holder.seenStatus.setImageResource(R.drawable.ic_seen);
                holder.seenStatusText.setText("Seen");
            } else {
                holder.seenStatus.setImageResource(R.drawable.ic_delivered);
                holder.seenStatusText.setText("Delivered");
            }
        } else {
            holder.seenStatus.setVisibility(View.GONE);
            holder.seenStatusText.setVisibility(View.GONE);
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

        public TextView show_message, seenStatusText;
        public ImageView profile_image;
        public ImageView seenStatus;

        public ViewHolder(View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            seenStatusText = itemView.findViewById(R.id.seenStatusText);
            seenStatus = itemView.findViewById(R.id.seenStatus);
        }

    }
}