package com.example.chatapplication;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.Adapter.ChatAdapter;
import com.example.chatapplication.Model.Chat;
import com.example.chatapplication.Model.User;
import com.example.chatapplication.Notifications.APIService;
import com.example.chatapplication.Notifications.Client;
import com.example.chatapplication.Notifications.Data;
import com.example.chatapplication.Notifications.MyResponse;
import com.example.chatapplication.Notifications.Sender;
import com.example.chatapplication.Notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private final int IMAGE_REQUEST = 101, PDF_REQUEST = 102, WORD_REQUEST = 103;
    CircleImageView profile_image;
    TextView username;
    FirebaseUser fuser;
    DatabaseReference reference;
    Intent intent;
    String userid;
    EditText text_send;
    RecyclerView recyclerView;
    ChatAdapter messageAdapter;
    List<Chat> mchats;
    APIService apiService;
    boolean notify = false;
    ValueEventListener seenListener;
    private Uri imageUri;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        intent = getIntent();
        userid = intent.getStringExtra("userid");
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        profile_image = findViewById(R.id.profile_image_chat);
        username = findViewById(R.id.username_chat);

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                username.setText(user.getName());
                TextView online = findViewById(R.id.activeTextView);
                if (!user.getLastSeen().equals("")) {
                    Date d = new Date(user.getLastSeen());
                    Date current = new Date();
                    DateFormat smf = SimpleDateFormat.getDateInstance();
                    String lastSeen = smf.format(d);
                    if (smf.format(d).equals(smf.format(current))) {
                        smf = SimpleDateFormat.getTimeInstance();
                        lastSeen = smf.format(d);
                    }
                    online.setText("Last Seen on " + lastSeen);
                    findViewById(R.id.onlineSymbol).setVisibility(View.GONE);
                } else {
                    online.setText("Online");
                    findViewById(R.id.onlineSymbol).setVisibility(View.VISIBLE);
                }
                if (user.getImageURL().equals("default")) {
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    //and this
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }

                readMesagges(fuser.getUid(), userid, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        text_send = findViewById(R.id.messageBox);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        seenMessage(userid);
    }

    public void sendBTNClicked(View view) {
        notify = true;
        String msg = text_send.getText().toString();
        if (!msg.equals("")) {
            sendMessage(fuser.getUid(), userid, msg, "text");
        } else {
            Toast.makeText(ChatActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
        }
        text_send.setText("");
    }

    private void seenMessage(final String userid) {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender, final String receiver, String message, String type) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("type", type);
        hashMap.put("message", message);
        hashMap.put("time", new Date());
        hashMap.put("isseen", false);

        reference.child("Chats").push().setValue(hashMap);

        final String msg = message;

        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    assert user != null;
                    sendNotifiaction(receiver, user.getName(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMesagges(final String myid, final String userid, final String imageurl) {
        mchats = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchats.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)) {
                        mchats.add(chat);
                    }

                    messageAdapter = new ChatAdapter(ChatActivity.this, mchats, imageurl);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("lastSeen", status);

        reference.updateChildren(hashMap);
    }

    private void sendNotifiaction(String receiver, final String username, final String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fuser.getUid(), R.mipmap.ic_launcher, username + ": " + message, "New Message",
                            userid);

                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(ChatActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void currentUser(String userid) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("");
        currentUser(userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status(new Date().toLocaleString());
        currentUser("none");
    }

    public void message(View view) {
        this.finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (uploadTask != null && uploadTask.isInProgress()) {
            Toast.makeText(this, "Upload in preogress", Toast.LENGTH_SHORT).show();
        } else if (data != null && data.getData() != null && resultCode == RESULT_OK) {
            imageUri = data.getData();
            String type;
            switch (requestCode ) {
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
        ImageButton btn = findViewById(R.id.addFileImageView);
        CharSequence[] type = new CharSequence[]{
                "Images", "PDFs", "MS Word Files", "Any Other Type"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select the type of File you want to Upload:");

        builder.setItems(type, (dialog, which) -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            if (which == 0) {
                intent.setType("image/*");
                startActivityForResult(intent, IMAGE_REQUEST);
            }
            else if (which == 1) {
                intent.setType("application/pdf");
                startActivityForResult(intent, PDF_REQUEST);
            }
            else if (which == 2) {
                intent.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                startActivityForResult(intent, WORD_REQUEST);
            }
            else if(which == 3)
            {
                intent.setType("*/*");
                startActivityForResult(intent, 0);
            }
        });
        builder.show();
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
            final StorageReference fileReference = storageReference.child(queryName(this.getContentResolver(),imageUri) + "." + getFileExtension(imageUri));

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

                    sendMessage(fuser.getUid(), userid, mUri, type);

                } else {
                    Toast.makeText(ChatActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
                pd.dismiss();
//                Toast.makeText(this, "Sent", Toast.LENGTH_LONG).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                pd.dismiss();
            });
        } else {
            Toast.makeText(this, "No File selected", Toast.LENGTH_SHORT).show();
        }
    }
}