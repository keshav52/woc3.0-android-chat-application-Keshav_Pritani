package com.example.chatapplication.MessageActivityUI;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

    private DatabaseReference friendsListsRef;
    private FirebaseUser fuser;
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
        View view = inflater.inflate(R.layout.fragment_requests, container, false);

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = view.findViewById(R.id.recycler_view);

        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        friendsListsRef = FirebaseDatabase.getInstance().getReference("FriendsLists").child(fuser.getUid());
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
                if(!flag[0]) {
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

    /*@Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(friendsListsRef.child(fuser.getUid()), User.class)
                        .build();

        FirebaseRecyclerAdapter<User, RequestFragmentViewHolder> adapter = new FirebaseRecyclerAdapter<User, RequestFragmentViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RequestFragmentViewHolder holder, int position, @NonNull User model) {
                String userId = getRef(position).getKey();
                DatabaseReference ref = getRef(position).child("status").getRef();
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String status = Objects.requireNonNull(snapshot.getValue()).toString();
                            if (status.equals("received")) {
                                assert userId != null;
                                usersRef.child(userId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        User user = snapshot.getValue(User.class);
                                        assert user != null;
                                        if (!user.getImageURL().equals("default")) {
                                            Glide.with(requireContext()).load(user.getImageURL()).into(holder.profileImage);
                                        }
                                        holder.userName.setText(user.getName());
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
                });
            }

            @NonNull
            @Override
            public RequestFragmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
                return new RequestFragmentViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestFragmentViewHolder extends RecyclerView.ViewHolder {
        private final CardView onlineSymbol;
        TextView userName;
        CircleImageView profileImage;
        ImageView acceptRequest, declineRequest;

        public RequestFragmentViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.username);
            acceptRequest = itemView.findViewById(R.id.acceptRequest);
            declineRequest = itemView.findViewById(R.id.declineRequest);
            profileImage = itemView.findViewById(R.id.profile_image);
            onlineSymbol = itemView.findViewById(R.id.onlineSymbol);
            onlineSymbol.setVisibility(View.GONE);

            acceptRequest.setVisibility(View.VISIBLE);
            declineRequest.setVisibility(View.VISIBLE);
        }
    }
*/
}