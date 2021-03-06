package com.chatapplication.MessageActivityUI;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chatapplication.Adapter.UserAdapter;
import com.chatapplication.FindUserActivity;
import com.chatapplication.Model.User;
import com.chatapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserFragment extends Fragment {

    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<User> mUsers;
    private View view;

    public static UserFragment getInstance() {
        return new UserFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mUsers = new ArrayList<>();

        readUsers();
        return view;
    }

    private void readUsers() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        assert firebaseUser != null;
        DatabaseReference friendsReference = FirebaseDatabase.getInstance().getReference("FriendsLists").child(firebaseUser.getUid());

        ArrayList<String> u = new ArrayList<>();
        friendsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                u.clear();
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (Objects.requireNonNull(snapshot.child("status").getValue()).toString().equals("accepted")) {
                        u.add(snapshot.getKey());
                    }
                }
                userAdapter = new UserAdapter(getContext(), mUsers, "user");
                recyclerView.setAdapter(userAdapter);
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
                            userAdapter = new UserAdapter(getContext(), mUsers, "user");
                            recyclerView.setAdapter(userAdapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                view.findViewById(R.id.findUserButton).setVisibility(View.INVISIBLE);
                if (u.size() == 0) {
                    view.findViewById(R.id.findUserButton).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.findUserButton).setOnClickListener(v -> startActivity(new Intent(view.getContext(), FindUserActivity.class)));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}