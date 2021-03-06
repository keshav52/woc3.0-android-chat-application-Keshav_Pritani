package com.chatapplication;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chatapplication.Adapter.ChatAdapter;
import com.chatapplication.Model.Chat;
import com.chatapplication.Model.User;
import com.chatapplication.Notifications.APIService;
import com.chatapplication.Notifications.Client;
import com.chatapplication.Notifications.Data;
import com.chatapplication.Notifications.MyResponse;
import com.chatapplication.Notifications.Sender;
import com.chatapplication.Notifications.Token;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    public static final int IMAGE_REQUEST = 101, PDF_REQUEST = 102, WORD_REQUEST = 103;
    public static final byte[] encrytionKey = {2, 41, -53, 124, -58, -42, 5, 62, 69, 12, -94, 31, -43, 12, 50, 34};
    FusedLocationProviderClient fusedLocationProviderClient;
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
    boolean notify = false;
    ValueEventListener seenListener;
    private Uri imageUri;

    private StorageTask<UploadTask.TaskSnapshot> uploadTask;

    public static void sendNotifiaction(String receiver, final String username, final String message, String title, String whatToOpen, String type, Context c) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(whatToOpen, R.mipmap.launcher_icon, username + message, title,
                            receiver, type);
                    APIService apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

                    assert token != null;
                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(@NotNull Call<MyResponse> call, @NotNull Response<MyResponse> response) {

                                }

                                @Override
                                public void onFailure(@NotNull Call<MyResponse> call, @NotNull Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("GetInstance")
    public static String encryptMessage(String message) {
        Cipher cipher;
        SecretKeySpec secretKeySpec = new SecretKeySpec(encrytionKey, "AES");

        String finalMessage = null;
        try {
            cipher = Cipher.getInstance("AES");
            byte[] messageByte = message.getBytes();
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encrytedByte = cipher.doFinal(messageByte);
            finalMessage = new String(encrytedByte, StandardCharsets.ISO_8859_1);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return finalMessage;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("GetInstance")
    public static String decryptMessage(String message) {
        String finalMessage = null;
        try {
            Cipher decipher = Cipher.getInstance("AES");
            byte[] messageByte = message.getBytes(StandardCharsets.ISO_8859_1);
            SecretKeySpec secretKeySpec = new SecretKeySpec(encrytionKey, "AES");
            decipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] encrytedByte = decipher.doFinal(messageByte);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                finalMessage = new String(encrytedByte, StandardCharsets.ISO_8859_1);
            }
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return finalMessage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

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
                if (!user.getImageURL().equals("default")) {
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

        seenMessage(userid);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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
                    assert chat != null;
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("GetInstance")
    private void sendMessage(String sender, final String receiver, String message, String type) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("type", type);
        hashMap.put("message", encryptMessage(message));
        hashMap.put("time", new Date());
        hashMap.put("isseen", false);
        String key = reference.child("Chats").push().getKey();
        assert key != null;
        reference.child("Chats").child(key).setValue(hashMap);

        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatsLists");
        chatRef.child(sender).child(receiver).child("last_message").setValue(key);
        chatRef.child(receiver).child(sender).child("last_message").setValue(key);
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    assert user != null;
                    String msg;
                    if (type.equals("text"))
                        msg = message;
                    else {
                        msg = "Sent ";
                        if (type.equals("image")) msg += "an";
                        else msg += "a";
                        msg += ' ' + type;
                        if (!type.equals("image") && !type.equals("location"))
                            msg += " document";
                    }
                    sendNotifiaction(receiver, user.getName(), ": " + msg, "New Message", fuser.getUid(), "chat", ChatActivity.this);
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
                    assert chat != null;
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)) {
                        mchats.add(chat);
                    }
                }

                messageAdapter = new ChatAdapter(ChatActivity.this, mchats, imageurl);
                recyclerView.setAdapter(messageAdapter);
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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
            notify = true;
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
                notify=true;
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
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                final ProgressDialog pd = new ProgressDialog(this);
                pd.setMessage("Shraing Location");
                pd.show();
                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                    final Location[] location = {task.getResult()};
                    LocationRequest locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(10000).setFastestInterval(1000).setNumUpdates(1);
                    LocationCallback locationCallback = new LocationCallback() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            location[0] = locationResult.getLastLocation();
                            pd.dismiss();
                            sendMessage(fuser.getUid(), userid, location[0].getLatitude() + "," + location[0].getLongitude(), "location");
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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

                    sendMessage(fuser.getUid(), userid, mUri, type);

                } else {
                    Toast.makeText(ChatActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
                pd.dismiss();
            }).addOnFailureListener(e -> {
                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                pd.dismiss();
            });
        } else {
            Toast.makeText(this, "No File selected", Toast.LENGTH_SHORT).show();
        }
    }
}