package com.example.chatapplication.MessageActivityUI;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.Adapter.UserAdapter;
import com.example.chatapplication.Model.User;
import com.example.chatapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class RequestsFragment extends Fragment {

    private DatabaseReference usersRef;
    private RecyclerView recyclerView;

    public static RequestsFragment getInstance() {
        return new RequestsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        assert fuser != null;
        DatabaseReference friendsListsRef = FirebaseDatabase.getInstance().getReference("FriendsLists").child(fuser.getUid());
        ArrayList<User> mUsers = new ArrayList<>();
        ArrayList<String> u = new ArrayList<>();
        friendsListsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                u.clear();
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (Objects.requireNonNull(snapshot.child("status").getValue()).toString().equals("received")) {
                        u.add(snapshot.getKey());
                    }
                }
                view.findViewById(R.id.mailBoxImage).setVisibility(View.INVISIBLE);
                view.findViewById(R.id.emptyTextView).setVisibility(View.INVISIBLE);
                if (u.size() == 0) {
                    view.findViewById(R.id.mailBoxImage).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.emptyTextView).setVisibility(View.VISIBLE);
                }
                final boolean[] flag = {false};
                for (String u1 : u) {
                    usersRef.child(u1).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            mUsers.add(user);
                            flag[0] = true;
                            UserAdapter userAdapter = new UserAdapter(getContext(), mUsers, "request");
                            recyclerView.setAdapter(userAdapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                if (!flag[0]) {
                    UserAdapter userAdapter = new UserAdapter(getContext(), mUsers, "request");
                    recyclerView.setAdapter(userAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return view;
    }

}