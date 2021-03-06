package com.chatapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.chaos.view.PinView;
import com.chatapplication.Model.User;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "Keshav Pritani";
    FirebaseUser firebaseUser;
    private PinView pinView;
    private Button next;
    private TextView topText, textU;
    private EditText name, phoneNo, status;
    private ConstraintLayout first, second;
    private FirebaseAuth auth;
    private DatabaseReference reference;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String codeBySystem;

    @Override
    protected void onStart() {
        super.onStart();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //check if user is null
        if (firebaseUser != null) {
            Intent intent = new Intent(this, MessageActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_register);

        topText = findViewById(R.id.topText);
        pinView = findViewById(R.id.pinView);
        next = findViewById(R.id.button);
        name = findViewById(R.id.nameTextBox);
        phoneNo = findViewById(R.id.phoneTextBox);
        status = findViewById(R.id.stautsTextBox);
        first = findViewById(R.id.constraintLayout);
        second = findViewById(R.id.constraintLayout1);
        textU = findViewById(R.id.textView_noti);
        second.setVisibility(View.INVISIBLE);
        auth = FirebaseAuth.getInstance();
        next.setOnClickListener(this);

        initCalls();
    }

    private void initCalls() {

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NotNull PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);
                Toast.makeText(RegisterActivity.this, "onVerificationCompleted:" + credential, Toast.LENGTH_LONG).show();
                pinView.setText(credential.getSmsCode());
                PhoneAuthCredential credential1 = PhoneAuthProvider.getCredential(codeBySystem, Objects.requireNonNull(credential.getSmsCode()));
                signInWithPhoneAuthCredential(credential1);
            }

            @Override
            public void onVerificationFailed(@NotNull FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);
                Toast.makeText(RegisterActivity.this, "OTP Sent", Toast.LENGTH_LONG).show();
                codeBySystem = verificationId;
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser firebaseUser = auth.getCurrentUser();

                        pinView.setLineColor(Color.GREEN);
                        textU.setText("OTP Verified");
                        textU.setTextColor(Color.GREEN);
                        next.setText("Next");
                        assert firebaseUser != null;
                        String userid = firebaseUser.getUid();

                        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("id", userid);
                        hashMap.put("name", name.getText().toString());
                        hashMap.put("phoneNo", phoneNo.getText().toString());
                        hashMap.put("imageURL", "default");
                        hashMap.put("status", status.getText().toString());
                        hashMap.put("lastSeen", "");
                        hashMap.put("search", name.getText().toString().toLowerCase());
                        reference.setValue(hashMap).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Intent intent = new Intent(RegisterActivity.this, MessageActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        });
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            pinView.setLineColor(Color.RED);
                            textU.setText("X Incorrect OTP");
                            textU.setTextColor(Color.RED);
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (next.getText().equals("Let's go!")) {
            String name = this.name.getText().toString();
            String pno = phoneNo.getText().toString();
            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(pno)) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean flag = true;
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            User user = snap.getValue(User.class);
                            assert user != null;
                            if (pno.equals(user.getPhoneNo())) {
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            try {
                                PhoneAuthOptions options =
                                        PhoneAuthOptions.newBuilder(auth)
                                                .setPhoneNumber(pno)       // Phone number to verify
                                                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                                .setActivity(RegisterActivity.this)                 // Activity (for callback binding)
                                                .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                                                .build();
                                PhoneAuthProvider.verifyPhoneNumber(options);
                                next.setText("Verify");
                                first.setVisibility(View.GONE);
                                second.setVisibility(View.VISIBLE);
                                topText.setText("I Still don't trust you.\nTell me something that only two of us know.");
                                reference.removeEventListener(this);
                            } catch (Exception e) {
                                Log.d(TAG, e.toString());
                            }
                        } else
                            Toast.makeText(RegisterActivity.this, "This Phone number is already registered with us,Please Login.", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            } else {
                Toast.makeText(RegisterActivity.this, "Please enter the details", Toast.LENGTH_LONG).show();
            }
        } else if (next.getText().equals("Verify")) {
            String OTP = pinView.getText().toString();
            PhoneAuthCredential credential1 = PhoneAuthProvider.getCredential(codeBySystem, OTP);
            textU.setText("Verifying");
            signInWithPhoneAuthCredential(credential1);
        } else if (next.getText().equals("Next")) {
            RegisterActivity.this.finish();
        }
    }
}