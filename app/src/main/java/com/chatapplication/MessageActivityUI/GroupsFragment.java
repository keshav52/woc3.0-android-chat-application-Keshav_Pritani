package com.chatapplication.MessageActivityUI;

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

public class GroupsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<User> mUsers;
    private View view;

    public static GroupsFragment getInstance() {
        return new GroupsFragment();
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

        readGroups();
        return view;
    }

    private void readGroups() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                assert firebaseUser != null;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("participants").child(firebaseUser.getUid()).exists()) {
                        User user = snapshot.getValue(User.class);
                        mUsers.add(user);
                    }
                }
                view.findViewById(R.id.mailBoxImage).setVisibility(View.INVISIBLE);
                view.findViewById(R.id.emptyTextView).setVisibility(View.INVISIBLE);
                UserAdapter userAdapter = new UserAdapter(getContext(), mUsers, "group");
                recyclerView.setAdapter(userAdapter);
                if (mUsers.size() == 0) {
                    view.findViewById(R.id.mailBoxImage).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.emptyTextView).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}