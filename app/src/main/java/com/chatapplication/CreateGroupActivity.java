package com.chatapplication;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import static com.chatapplication.UserProfileActivity.IMAGE_REQUEST;

public class CreateGroupActivity extends AppCompatActivity {

    EditText groupNameEditText, groupDescriptionEditText;
    FloatingActionButton createGroupNextButton;

    String groupIconUrl = "default";
    private Uri imageUri;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        groupNameEditText = findViewById(R.id.groupNameEditText);
        groupDescriptionEditText = findViewById(R.id.groupDescriptionEditText);
        createGroupNextButton = findViewById(R.id.createGroupNextButton);


        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar mToolbar = getSupportActionBar();

        assert mToolbar != null;
        mToolbar.setDisplayHomeAsUpEnabled(true);
        mToolbar.setDisplayShowHomeEnabled(true);
        mToolbar.setTitle("Create New Group");
        mToolbar.setSubtitle(MessageActivity.USERNAME);
    }

    public void openImage(View v) {
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
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("GroupPhotos");

            FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
            assert fuser != null;
            final StorageReference fileReference = storageReference.child(groupNameEditText.getText().toString() + "." + getFileExtension(imageUri));

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
                    groupIconUrl = downloadUri.toString();
                    Glide.with(CreateGroupActivity.this).load(groupIconUrl).into((ImageView) findViewById(R.id.groupIcon));
                } else {
                    Toast.makeText(CreateGroupActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
                pd.dismiss();
                Toast.makeText(this, "Uploaded", Toast.LENGTH_LONG).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(CreateGroupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

    public void createGroup(View view) {

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Creating Group");
        pd.show();

        String userid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");

        String d = new Date().toLocaleString();
        String key = reference.push().getKey();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("id", key);
        hashMap.put("name", groupNameEditText.getText().toString());
        hashMap.put("status", groupDescriptionEditText.getText().toString());
        hashMap.put("imageURL", groupIconUrl);
        hashMap.put("created on", d);
        hashMap.put("creator", userid);
        hashMap.put("lastSeen", "");


        assert key != null;
        reference.child(key).setValue(hashMap).addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                HashMap<String, String> participant = new HashMap<>();
                participant.put("role", "Creator");
                participant.put("joined on", d);
                reference.child(key).child("participants").child(userid).setValue(participant).addOnCompleteListener(task -> {
                    pd.dismiss();
                    Toast.makeText(this, "Group Created", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CreateGroupActivity.this, MessageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
            }
        });
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