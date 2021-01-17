package com.example.chatapplication;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.chatapplication.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.chatapplication.GroupChatActivity.groupId;
import static com.example.chatapplication.GroupChatActivity.myRole;

public class UserProfileActivity extends AppCompatActivity {

    public static final int IMAGE_REQUEST = 1;
    StorageReference storageReference;
    DatabaseReference reference;
    FirebaseUser fuser;
    private Uri imageUri;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;
    private EditText statusEdit, usernameEdit;
    private String name;
    private String groupid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);


        Intent intent = getIntent();
        groupid = intent.getStringExtra("groupid");
        CircleImageView profile_image = findViewById(R.id.profile_image);
        TextView username = findViewById(R.id.usernameTextView);
        TextView status = findViewById(R.id.statusTextView);
        usernameEdit = findViewById(R.id.usernameEditText);
        statusEdit = findViewById(R.id.statusEditText);
        Button profile_imageButton = findViewById(R.id.changeProfilePhoto);

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        assert fuser != null;
        reference = FirebaseDatabase.getInstance().getReference();
        String store = "";
        name = "";
        if (groupid != null) {
            reference = reference.child("Groups").child(groupid);
            store = "GroupPhotos";
            findViewById(R.id.participantsList).setVisibility(View.VISIBLE);
        } else {
            reference = reference.child("Users").child(fuser.getUid());
            store = "UserPhotos";
            name = fuser.getUid();
        }
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                username.setText(user.getName());
                status.setText(user.getStatus());
                if (groupid != null) {
                    TextView temp = findViewById(R.id.createdTextView);
                    findViewById(R.id.leaveGroup).setVisibility(View.VISIBLE);
                    temp.setVisibility(View.VISIBLE);
                    temp.setText("Created on " + Objects.requireNonNull(dataSnapshot.child("created on").getValue()).toString());
                }
                if (groupid == null || !myRole.equals("Member")) {
                    usernameEdit.setText(user.getName());
                    statusEdit.setText(user.getStatus());
                } else {
                    findViewById(R.id.full_name_profile).setVisibility(View.GONE);
                    findViewById(R.id.status_profile).setVisibility(View.GONE);
                    findViewById(R.id.changeProfilePhoto).setVisibility(View.GONE);
                    findViewById(R.id.updateProfile).setVisibility(View.GONE);
                }
                name = user.getName();
                if (!user.getImageURL().equals("default")) {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        storageReference = FirebaseStorage.getInstance().getReference(store);
        profile_imageButton.setOnClickListener(view -> openImage());
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();

        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(name + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }

                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    assert downloadUri != null;
                    String mUri = downloadUri.toString();

                    if (groupid != null)
                        reference = FirebaseDatabase.getInstance().getReference("Groups").child(groupid);
                    else
                        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("imageURL", "" + mUri);
                    reference.updateChildren(map);

                } else {
                    Toast.makeText(UserProfileActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
                pd.dismiss();
                Toast.makeText(this, "Uploaded", Toast.LENGTH_LONG).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                pd.dismiss();
            });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(this, "Upload in preogress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }

    public void updateDetails(View view) {
        DatabaseReference reference1;
        if (groupid != null)
            reference1 = FirebaseDatabase.getInstance().getReference("Groups").child(groupid);
        else
            reference1 = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", usernameEdit.getText().toString());
        hashMap.put("status", statusEdit.getText().toString());
        reference1.updateChildren(hashMap);
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Updating");
        pd.show();
        new Handler().postDelayed(() -> {
            pd.dismiss();
            Toast.makeText(this, "Updated", Toast.LENGTH_LONG).show();
        }, 2000);
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

    public void backToHome(View view) {
        this.finish();
    }

    public void participants(View view) {
        startActivity(new Intent(this, GroupParticipantsListActivity.class));
    }

    public void leaveGroup(View view) {
        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("participants").child(fuser.getUid()).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Group Left", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
        startActivity(new Intent(this,MessageActivity.class));
        this.finish();
    }
}