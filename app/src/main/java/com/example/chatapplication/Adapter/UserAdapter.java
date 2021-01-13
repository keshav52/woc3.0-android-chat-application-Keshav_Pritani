package com.example.chatapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.ChatActivity;
import com.example.chatapplication.Model.Chat;
import com.example.chatapplication.Model.User;
import com.example.chatapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private final Context mContext;
    private final List<User> mUsers;
    private boolean user = false;

    String theLastMessage;

    public UserAdapter(Context mContext, List<User> mUsers, boolean user) {
        this.mUsers = Collections.unmodifiableList(new ArrayList<>(mUsers));
        this.mContext = mContext;
        this.user = user;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final User user = mUsers.get(position);
        if (user != null) {
            holder.username.setText(user.getName());
            if (user.getImageURL().equals("default")) {
                holder.profile_image.setImageResource(R.mipmap.ic_launcher);
            } else {
                Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
            }
            if (!this.user)
                lastMessage(user.getId(), holder.last_msg, holder.lastTime);

            holder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("userid", user.getId());
                mContext.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public ImageView profile_image;
        private TextView last_msg, lastTime;

        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            last_msg = itemView.findViewById(R.id.lastMessage);
            lastTime = itemView.findViewById(R.id.lastTime);
        }
    }

    //check for last message
    private void lastMessage(final String userid, final TextView last_msg, TextView lastTime) {
        theLastMessage = "default";
        final Date[] d = {new Date()};
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (firebaseUser != null && chat != null) {
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                                chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                            theLastMessage = chat.getMessage();
                            d[0] = chat.getTime();
                        }
                    }
                }

                if (theLastMessage != "default") {
                    last_msg.setText(theLastMessage);
                    if (d[0] != null) {
                        Date current = new Date();
                        DateFormat smf = SimpleDateFormat.getDateInstance();
                        String last = smf.format(d[0]);
                        if (smf.format(d[0]).equals(smf.format(current))) {
                            smf = SimpleDateFormat.getTimeInstance();
                            last = smf.format(d[0]);
                        }
                        lastTime.setText(last);
                    }
                    theLastMessage = "default";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }
}