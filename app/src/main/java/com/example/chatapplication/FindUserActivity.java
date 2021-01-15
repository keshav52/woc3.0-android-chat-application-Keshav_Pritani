package com.example.chatapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.Model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindUserActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DatabaseReference firebaseReference;
    private FirebaseUser fuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);

        firebaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Users");
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(firebaseReference, User.class)
                        .build();

        FirebaseRecyclerAdapter<User, FindUserViewHolder> adapter =
                new FirebaseRecyclerAdapter<User, FindUserViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindUserViewHolder holder, final int position, @NonNull User model) {
                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());
                        if (!model.getImageURL().equals("default"))
                            Glide.with(getApplicationContext()).load(model.getImageURL()).into(holder.profileImage);
                        if (model.getLastSeen().equals(""))
                            holder.onlineSymbol.setVisibility(View.VISIBLE);

                        holder.itemView.setOnClickListener(view -> {
                            String visit_user_id = getRef(position).getKey();
                            Intent profileIntent = new Intent(FindUserActivity.this, ShowProfileActivity.class);
                            profileIntent.putExtra("userId", visit_user_id);
                            startActivity(profileIntent);
                        });
                    }

                    @NonNull
                    @Override
                    public FindUserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(FindUserActivity.this).inflate(R.layout.user_item, viewGroup, false);
                        return new FindUserViewHolder(view);
                    }
                };

        recyclerView.setAdapter(adapter);

        adapter.startListening();
    }
    public static class FindUserViewHolder extends RecyclerView.ViewHolder {
        private final CardView onlineSymbol;
        TextView userName, userStatus;
        CircleImageView profileImage;


        public FindUserViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.username);
            userStatus = itemView.findViewById(R.id.lastMessage);
            profileImage = itemView.findViewById(R.id.profile_image);
            onlineSymbol = itemView.findViewById(R.id.onlineSymbol);
            onlineSymbol.setVisibility(View.GONE);
        }
    }

    private void status(String status) {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

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