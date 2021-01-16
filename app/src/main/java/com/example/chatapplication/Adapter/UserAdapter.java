package com.example.chatapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.ChatActivity;
import com.example.chatapplication.GroupChatActivity;
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
import java.util.Objects;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private final Context mContext;
    private final List<User> mUsers;
    private final String user;
    String theLastMessage;

    public UserAdapter(Context mContext, List<User> mUsers, String user) {
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
        assert user != null;
        holder.username.setText(user.getName());
        if (!user.getImageURL().equals("default")) {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
        }
        switch (this.user) {
            case "chat":
                lastChatMessage(user.getId(), holder.last_msg, holder.lastTime, holder.username);
                break;
            case "group":
                lastGroupMessage(user.getId(), holder.last_msg, holder.lastTime);
                break;
            case "request":
                holder.acceptRequest.setVisibility(View.VISIBLE);
                holder.declineRequest.setVisibility(View.VISIBLE);
                FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
                assert fuser != null;
                String currentId = fuser.getUid();
                DatabaseReference friendsListsRef = FirebaseDatabase.getInstance().getReference("FriendsLists");
                holder.acceptRequest.setOnClickListener(v -> friendsListsRef.child(user.getId()).child(currentId).child("status").setValue("accepted").addOnCompleteListener(task -> friendsListsRef.child(currentId).child(user.getId()).child("status").setValue("accepted").addOnCompleteListener(task2 -> Toast.makeText(mContext, "Friend Added", Toast.LENGTH_LONG).show())));
                holder.declineRequest.setOnClickListener(v -> friendsListsRef.child(user.getId()).child(currentId).child("status").setValue("rejected").addOnCompleteListener(task -> friendsListsRef.child(currentId).child(user.getId()).removeValue().addOnCompleteListener(task1 -> Toast.makeText(mContext, "Request Cancelled", Toast.LENGTH_LONG).show())));
                break;
        }
        if (!this.user.equals("group") && user.getLastSeen().equals(""))
            holder.onlineSymbol.setVisibility(View.VISIBLE);
        else
            holder.onlineSymbol.setVisibility(View.INVISIBLE);

        if (this.user.equals("chat") || this.user.equals("user")) {
            holder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("userid", user.getId());
                mContext.startActivity(intent);
            });
        } else if (this.user.equals("group")) {
            holder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(mContext, GroupChatActivity.class);
                intent.putExtra("groupid", user.getId());
                mContext.startActivity(intent);
            });
        }
    }

    private void lastGroupMessage(String id, TextView last_msg, TextView lastTime) {

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    private void lastChatMessage(final String userid, final TextView last_msg, TextView lastTime, TextView username) {
        theLastMessage = "default";
        final Date[] d = {new Date()};
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatsLists").child(firebaseUser.getUid()).child(userid);
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats").child(Objects.requireNonNull(snapshot.child("last_message").getValue()).toString());
                final boolean[] flag = {false};
                final boolean[] seen = {false};
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        Chat chat = dataSnapshot.getValue(Chat.class);
                        if (chat != null) {
                            if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                                    chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                                theLastMessage = "";
                                if (chat.getSender().equals(firebaseUser.getUid()))
                                    theLastMessage += "You: ";
                                if (chat.getType().equals("text"))
                                    theLastMessage += chat.getMessage();
                                else {
                                    theLastMessage += "Sent ";
                                    if (chat.getType().equals("image")) theLastMessage += "an ";
                                    else theLastMessage += "a ";
                                    theLastMessage += chat.getType();
                                    if (!chat.getType().equals("image"))
                                        theLastMessage += " document";
                                }
                                d[0] = chat.getTime();
                                if (chat.getReceiver().equals(firebaseUser.getUid())) {
                                    flag[0] = true;
                                    seen[0] = chat.isIsseen();
                                } else flag[0] = false;
                            }
                        }

                        if (!theLastMessage.equals("default")) {
                            last_msg.setText(theLastMessage);
                            if (flag[0] && !seen[0]) {
                                last_msg.setTypeface(null, Typeface.BOLD);
                                lastTime.setTypeface(null, Typeface.BOLD);
                                username.setTypeface(null, Typeface.BOLD);
                                last_msg.setTextSize(16);
                            }
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final CardView onlineSymbol;
        private final TextView last_msg;
        private final TextView lastTime;
        public TextView username;
        public ImageView profile_image, acceptRequest, declineRequest;

        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            last_msg = itemView.findViewById(R.id.lastMessage);
            lastTime = itemView.findViewById(R.id.lastTime);
            onlineSymbol = itemView.findViewById(R.id.onlineSymbol);
            acceptRequest = itemView.findViewById(R.id.acceptRequest);
            declineRequest = itemView.findViewById(R.id.declineRequest);
        }
    }
}