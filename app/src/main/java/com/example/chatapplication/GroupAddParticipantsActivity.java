package com.example.chatapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.Adapter.UserAdapter;
import com.example.chatapplication.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import static com.example.chatapplication.GroupChatActivity.groupId;

public class GroupAddParticipantsActivity extends AppCompatActivity {

    private ValueEventListener lis;
    public static String groupId1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_add_participants);

        groupId1 = groupId;
        Intent intent = getIntent();
        ArrayList<String> u = intent.getStringArrayListExtra("list");
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add Participants");

        ArrayList<User> mUsers = new ArrayList<>();
        lis = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                for (DataSnapshot s : snapshot.getChildren()) {
                    User user = s.getValue(User.class);
                    assert user != null;
                    if (!user.getId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()) && !u.contains(user.getId())) {
                        FirebaseDatabase.getInstance().getReference("FriendsLists").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.hasChild(user.getId()) && Objects.requireNonNull(snapshot.child(user.getId()).child("status").getValue()).toString().equals("accepted")) {
                                    int da = -1;
                                    for (User ue : mUsers) {
                                        if (ue.getId().equals(user.getId())) {
                                            da = mUsers.indexOf(ue);
                                            mUsers.remove(ue);
                                            break;
                                        }
                                    }
                                    if (da == -1)
                                        mUsers.add(user);
                                    else mUsers.add(da,user);
                                    UserAdapter userAdapter = new UserAdapter(GroupAddParticipantsActivity.this, mUsers, "participant");
                                    recyclerView.setAdapter(userAdapter);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        FirebaseDatabase.getInstance().getReference("Users").addValueEventListener(lis);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseDatabase.getInstance().getReference("Users").removeEventListener(lis);
    }
}