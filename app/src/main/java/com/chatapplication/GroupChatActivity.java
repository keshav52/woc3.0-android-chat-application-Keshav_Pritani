package com.chatapplication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chatapplication.Adapter.ChatAdapter;
import com.chatapplication.Model.Chat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.chatapplication.ChatActivity.PDF_REQUEST;
import static com.chatapplication.ChatActivity.WORD_REQUEST;
import static com.chatapplication.ChatActivity.sendNotifiaction;
import static com.chatapplication.GroupAddParticipantsActivity.groupId1;
import static com.chatapplication.UserProfileActivity.IMAGE_REQUEST;

public class GroupChatActivity extends AppCompatActivity {

    public static String groupId, myRole, myName, groupName;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;
    private Uri imageUri;
    private EditText text_send;
    private RecyclerView recyclerView;
    private FirebaseUser fuser;
    private boolean notify;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupid");
        if (groupId == null)
            groupId = groupId1;
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar toolbar = getSupportActionBar();

        Objects.requireNonNull(toolbar).setDisplayHomeAsUpEnabled(true);
        toolbar.setDisplayShowHomeEnabled(true);
        toolbar.setTitle("");

        title = findViewById(R.id.groupTitle);
        TextView part = findViewById(R.id.participants);
        text_send = findViewById(R.id.messageBox);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups").child(groupId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                assert fuser != null;
                ArrayList<String> users = new ArrayList<>();
                final String[] particiapants = {"You"};
                part.setText(particiapants[0]);
                title.setText(Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString());
                groupName = title.getText().toString();
                CircleImageView groupIcon = findViewById(R.id.groupIcon);
                String url = Objects.requireNonNull(dataSnapshot.child("imageURL").getValue()).toString();
                if (!url.equals("default")) {
                    Glide.with(getApplicationContext()).load(url).into(groupIcon);
                }
                for (DataSnapshot snapshot : dataSnapshot.child("participants").getChildren()) {
                    if (!Objects.requireNonNull(snapshot.getKey()).equals(fuser.getUid()))
                        users.add(snapshot.getKey());
                    else {
                        users.add(snapshot.getKey());
                        myRole = Objects.requireNonNull(snapshot.child("role").getValue()).toString();
                    }
                }
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                for (String u : users) {
                    ref.child(u).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (Objects.requireNonNull(snapshot.child("id").getValue()).toString().equals(fuser.getUid())) {
                                myName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                            } else {
                                particiapants[0] += ", " + Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                                part.setText(particiapants[0]);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                readMesagges(groupId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void sendBTNClicked(View view) {
        notify = true;
        String msg = text_send.getText().toString();
        if (!msg.equals("")) {
            sendMessage(fuser.getUid(), msg, "text");
        } else {
            Toast.makeText(GroupChatActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
        }
        text_send.setText("");
    }

    private void sendMessage(String sender, String message, String type) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("type", type);
        hashMap.put("message", message);
        hashMap.put("time", new Date());
        String key = reference.child("Messages").push().getKey();
        assert key != null;
        reference.child("Messages").child(key).setValue(hashMap);

        final String msg = message;
        reference.child("lastSeen").setValue(key);
        reference.child("participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (!fuser.getUid().equals(snapshot.getKey()) && notify) {
                        sendNotifiaction(snapshot.getKey(), title.getText().toString() + ": " + myName, ": " + msg, "New Group Message", groupId, "group", GroupChatActivity.this);
                    }
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMesagges(final String groupId) {
        ArrayList<Chat> mchats = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Messages");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchats.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    assert chat != null;
                    mchats.add(chat);
                }
                ChatAdapter messageAdapter = new ChatAdapter(GroupChatActivity.this, mchats, "group");
                recyclerView.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (uploadTask != null && uploadTask.isInProgress()) {
            Toast.makeText(this, "Upload in preogress", Toast.LENGTH_SHORT).show();
        } else if (data != null && data.getData() != null && resultCode == RESULT_OK) {
            imageUri = data.getData();
            String type;
            switch (requestCode) {
                case IMAGE_REQUEST:
                    type = "image";
                    break;
                case PDF_REQUEST:
                    type = "pdf";
                    break;
                case WORD_REQUEST:
                    type = "word";
                    break;
                default:
                    type = "any";
            }
            uploadFile(type);
        }
    }

    public void addFiles(View view) {
        CharSequence[] type = new CharSequence[]{
                "Images", "PDFs", "MS Word Files", "Any Other Type", "Share Location"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select the type of File you want to Upload:");

        builder.setItems(type, (dialog, which) -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            if (which == 0) {
                intent.setType("image/*");
                startActivityForResult(intent, IMAGE_REQUEST);
            } else if (which == 1) {
                intent.setType("application/pdf");
                startActivityForResult(intent, PDF_REQUEST);
            } else if (which == 2) {
                intent.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                startActivityForResult(intent, WORD_REQUEST);
            } else if (which == 3) {
                intent.setType("*/*");
                startActivityForResult(intent, 0);
            } else if (which == 4) {
                shareLocation();
            }
        });
        builder.show();
    }

    private void shareLocation() {
        LocationManager mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Enable Location").setMessage("Your Loction Service is OFF.\nPlease ON to share the location.")
                    .setPositiveButton("Location Settings", (dialog12, which) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton("Cancel", (dialog1, which) -> {

                    }).show();
        } else {
            final ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Shraing Location");
            pd.show();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                    final Location[] location = {task.getResult()};
                    LocationRequest locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(10000).setFastestInterval(1000).setNumUpdates(1);
                    LocationCallback locationCallback = new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            location[0] = locationResult.getLastLocation();
                            pd.dismiss();
                            sendMessage(fuser.getUid(), location[0].getLatitude() + "," + location[0].getLongitude(), "location");
                        }

                        @Override
                        public void onLocationAvailability(LocationAvailability locationAvailability) {
                            super.onLocationAvailability(locationAvailability);
                        }
                    };
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

                });
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
            }
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    private void uploadFile(String type) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();

        if (imageUri != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("sendUploads");
            final StorageReference fileReference = storageReference.child(queryName(this.getContentResolver(), imageUri) + "." + getFileExtension(imageUri));

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

                    sendMessage(fuser.getUid(), mUri, type);

                } else {
                    Toast.makeText(GroupChatActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
                pd.dismiss();
            }).addOnFailureListener(e -> {
                Toast.makeText(GroupChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                pd.dismiss();
            });
        } else {
            Toast.makeText(this, "No File selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.groupInfo) {
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("groupid", groupId);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void currentUser(String userid) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
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
        currentUser(groupId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        status(new Date().toLocaleString());
        currentUser("none");
    }

}