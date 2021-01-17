package com.example.chatapplication.Adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.example.chatapplication.ChatActivity.sendNotifiaction;
import static com.example.chatapplication.GroupChatActivity.groupId;
import static com.example.chatapplication.GroupChatActivity.groupName;
import static com.example.chatapplication.GroupChatActivity.myRole;


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
            case "participant":
                holder.roleTextView.setVisibility(View.VISIBLE);
                DatabaseReference commonRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("participants");
                commonRef.child(user.getId()).addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            holder.roleTextView.setText("(" + Objects.requireNonNull(snapshot.child("role").getValue()).toString() + ")");
                            holder.roleTextView.setTypeface(null, Typeface.BOLD);
                            holder.roleTextView.setTextSize(16);
                        } else {
                            holder.roleTextView.setText("(Not a Member)");
                            holder.roleTextView.setTypeface(null);
                            holder.roleTextView.setTextSize(14);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                holder.itemView.setOnClickListener(v ->
                        commonRef.child(user.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                String[] options;
                                if (snapshot.exists()) {
                                    String role = Objects.requireNonNull(snapshot.child("role").getValue()).toString();
                                    builder.setTitle("Choose Role");
                                    if (myRole.equals("Creator")) {
                                        if (role.equals("Admin")) {
                                            options = new String[]{"Remove Admin Rights of the Participant", "Remove Participant from the Group"};
                                            builder.setItems(options, (dialog, which) -> {
                                                if (which == 0) {
                                                    removeAdminRights(snapshot.getKey());
                                                } else if (which == 1) {
                                                    removeParticipant(snapshot.getKey());
                                                }
                                            }).show();
                                        } else if (role.equals("Member")) {
                                            options = new String[]{"Make Admin to this Participant", "Remove Participant from the Group"};
                                            builder.setItems(options, (dialog, which) -> {
                                                if (which == 0) {
                                                    makeAdmin(snapshot.getKey());
                                                } else if (which == 1) {
                                                    removeParticipant(snapshot.getKey());
                                                }
                                            }).show();
                                        }
                                    } else if (myRole.equals("Admin")) {
                                        if (role.equals("Admin")) {
                                            options = new String[]{"Remove Admin Rights of the Participant", "Remove Participant from the Group"};
                                            builder.setItems(options, (dialog, which) -> {
                                                if (which == 0) {
                                                    removeAdminRights(snapshot.getKey());
                                                } else if (which == 1) {
                                                    removeParticipant(snapshot.getKey());
                                                }
                                            }).show();
                                        }
                                        if (role.equals("Member")) {
                                            options = new String[]{"Make Admin to this Participant", "Remove Participant from the Group"};
                                            builder.setItems(options, (dialog, which) -> {
                                                if (which == 0) {
                                                    makeAdmin(snapshot.getKey());
                                                } else if (which == 1) {
                                                    removeParticipant(snapshot.getKey());
                                                }
                                            }).show();
                                        } else if (role.equals("Creator")) {
                                            Toast.makeText(mContext, "You cannot modify the role of the Creator", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                } else {
                                    builder.setTitle("Add Participant").setMessage("Add this user to the Group")
                                            .setPositiveButton("Add", (dialog, which) -> addParticipant(snapshot.getKey()))
                                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                            .show();
                                }
                            }

                            private void makeAdmin(String key) {
                                commonRef.child(key).child("role").setValue("Admin")
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(mContext, user.getName() + " is now Admin", Toast.LENGTH_SHORT).show();
                                            sendNotifiaction(key, "You are now admin of this group", "", groupName, groupId, "group", mContext);
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show());
                            }

                            private void addParticipant(String key) {
                                HashMap<String, String> participant = new HashMap<>();
                                participant.put("role", "Member");
                                participant.put("joined on", new Date().toLocaleString());
                                commonRef.child(key).setValue(participant)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(mContext, "User Added to the Group as a Member", Toast.LENGTH_LONG).show();
                                            sendNotifiaction(key, "You were add to a new Group", "", groupName, groupId, "group", mContext);
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show());
                            }

                            private void removeParticipant(String key) {
                                commonRef.child(key).removeValue()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(mContext, user.getName() + " is removed from the Group", Toast.LENGTH_SHORT).show();
                                            sendNotifiaction(key, "You were removed from this Group", "", groupName, "1", "home", mContext);
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show());
                            }

                            private void removeAdminRights(String key) {
                                commonRef.child(key).child("role").setValue("Member")
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(mContext, user.getName() + "'s Admin rights were Removed", Toast.LENGTH_SHORT).show();
                                            sendNotifiaction(key, "Your Admin rights were removed", "", groupName, groupId, "group", mContext);
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        }));
            case "user":
                holder.last_msg.setText(user.getStatus());
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
        theLastMessage = "default";
        final Date[] d = {new Date()};
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups").child(id);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!Objects.requireNonNull(snapshot.child("lastSeen").getValue()).toString().equals("")) {
                    DatabaseReference reference = ref.child("Messages").child(Objects.requireNonNull(snapshot.child("lastSeen").getValue()).toString());
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Chat chat = dataSnapshot.getValue(Chat.class);
                            if (chat != null) {
                                FirebaseDatabase.getInstance().getReference("Users").child(chat.getSender()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        theLastMessage = "";
                                        if (chat.getSender().equals(firebaseUser.getUid()))
                                            theLastMessage += "You: ";
                                        else {
                                            theLastMessage += Objects.requireNonNull(snapshot.child("name").getValue()).toString() + ": ";
                                        }
                                        if (chat.getType().equals("text"))
                                            theLastMessage += chat.getMessage();
                                        else {
                                            theLastMessage += "Sent ";
                                            if (chat.getType().equals("image"))
                                                theLastMessage += "an ";
                                            else theLastMessage += "a ";
                                            theLastMessage += chat.getType();
                                            if (!chat.getType().equals("image"))
                                                theLastMessage += " document";
                                        }
                                        d[0] = chat.getTime();

                                        if (!theLastMessage.equals("default")) {
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
                            } else
                                reference.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }

                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
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
        public TextView username, roleTextView;
        public ImageView profile_image, acceptRequest, declineRequest;

        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            last_msg = itemView.findViewById(R.id.lastMessage);
            lastTime = itemView.findViewById(R.id.lastTime);
            roleTextView = itemView.findViewById(R.id.participantRole);
            onlineSymbol = itemView.findViewById(R.id.onlineSymbol);
            acceptRequest = itemView.findViewById(R.id.acceptRequest);
            declineRequest = itemView.findViewById(R.id.declineRequest);
        }
    }
}