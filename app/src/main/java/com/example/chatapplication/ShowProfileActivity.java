package com.example.chatapplication;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.chatapplication.Model.User;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowProfileActivity extends AppCompatActivity {

    private String userId;
    private String status = "new";
    private FirebaseUser fuser;
    private DatabaseReference friendsListsRef;
    private Button request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_profile);

        userId = getIntent().getExtras().get("userId").toString();
        CircleImageView profile_image = findViewById(R.id.profile_image);
        TextView username = findViewById(R.id.usernameTextView);
        TextView s = findViewById(R.id.statusTextView);
        request = findViewById(R.id.sendRequest);

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        assert fuser != null;
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        friendsListsRef = FirebaseDatabase.getInstance().getReference("FriendsLists");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                username.setText(user.getName());
                s.setText(user.getStatus());
                if (!user.getImageURL().equals("default")) {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        if (!userId.equals(fuser.getUid())) {
            friendsListsRef.child(fuser.getUid()).addValueEventListener(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild(userId)) {
                        if (Objects.requireNonNull(snapshot.child(userId).child("status").getValue()).toString().equals("sent")) {
                            status = "sent";
                            request.setText("Cancel Request");
                        } else if (Objects.requireNonNull(snapshot.child(userId).child("status").getValue()).toString().equals("rejected")) {
                            status = "rejected";
                            TextView r = findViewById(R.id.requestStatus);
                            r.setVisibility(View.VISIBLE);
                            r.setText("Your Request was Rejected.");
                            request.setVisibility(View.GONE);
//                            request.setEnabled(false);
                        } else if (Objects.requireNonNull(snapshot.child(userId).child("status").getValue()).toString().equals("accepted")) {
                            status = "accepted";
                            TextView r = findViewById(R.id.requestStatus);
                            r.setVisibility(View.VISIBLE);
                            r.setText("Your Request was Accepted.");
                            request.setVisibility(View.GONE);
//                            request.setEnabled(false);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            request.setOnClickListener(v -> {
                request.setEnabled(false);
                if (status.equals("new")) {
                    sendRequest();
                } else if (status.equals("sent")) {
                    cancelRequest();
                }
            });
        } else {
            request.setVisibility(View.INVISIBLE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void cancelRequest() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Canceling Request");
        pd.show();
        new Handler().postDelayed(() -> {
            pd.dismiss();
            friendsListsRef
                    .child(fuser.getUid())
                    .child(userId)
                    .removeValue()
                    .addOnCompleteListener(
                            task -> friendsListsRef
                                    .child(userId)
                                    .child(fuser.getUid())
                                    .removeValue()
                                    .addOnCompleteListener(task1 -> {
                                        request.setEnabled(true);
                                        request.setText("Send Request");
                                        status = "new";
                                    }));
        }, 2000);
    }

    @SuppressLint("SetTextI18n")
    private void sendRequest() {
        AtomicBoolean flag = new AtomicBoolean(true);
        Snackbar.make(getWindow().getDecorView().getRootView(), "Sending Request..", BaseTransientBottomBar.LENGTH_LONG)
                .setDuration(3000)
                .setAction("Undo", v -> {
                    flag.set(false);
                }).show();
        new Handler().postDelayed(() -> {
            if (flag.get())
                friendsListsRef
                        .child(fuser.getUid())
                        .child(userId)
                        .child("status")
                        .setValue("sent")
                        .addOnCompleteListener(
                                task -> friendsListsRef
                                        .child(userId)
                                        .child(fuser.getUid())
                                        .child("status")
                                        .setValue("received")
                                        .addOnCompleteListener(task1 -> {
                                            request.setText("Cancel Request");
                                            status = "sent";
                                            request.setEnabled(true);
                                        }));
            else request.setEnabled(true);
        }, 3000);
    }

    public void backToAllUsers(View view) {
        this.finish();
    }
}