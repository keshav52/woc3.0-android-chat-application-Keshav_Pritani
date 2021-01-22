package com.chatapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chatapplication.Adapter.UserAdapter;
import com.chatapplication.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import static com.chatapplication.GroupChatActivity.groupId;
import static com.chatapplication.GroupChatActivity.myRole;

public class GroupParticipantsListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<String> u;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_participants_list);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Participants List");

        readUsers();
    }

    private void readUsers() {
        ArrayList<User> mUsers = new ArrayList<>();
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        assert firebaseUser != null;
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("participants");

        u = new ArrayList<>();
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                u.clear();
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    u.add(snapshot.getKey());
                }
                for (String u1 : u) {
                    reference.child(u1).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                            User user = dataSnapshot1.getValue(User.class);
                            assert user != null;
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
                            else mUsers.add(da, user);
                            UserAdapter userAdapter = new UserAdapter(GroupParticipantsListActivity.this, mUsers, "participant");
                            recyclerView.setAdapter(userAdapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_menu, menu);
        MenuItem item = menu.getItem(0);
        item.setIcon(R.drawable.ic_baseline_add_circle_outline_24);
        if (!myRole.equals("Creator") && !myRole.equals("Admin")) {
            item.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.groupInfo) {
            Intent intent = new Intent(this, GroupAddParticipantsActivity.class);
            intent.putExtra("groupid", groupId);
            intent.putExtra("list", u);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    private void status(String status) {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("lastSeen", status);

        reference1.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status(new Date().toLocaleString());
    }
}