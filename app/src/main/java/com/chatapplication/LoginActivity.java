package com.chatapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
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

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private final String TAG = "In LoginActivity:";
    FirebaseUser firebaseUser;
    private EditText phoneNumber;
    private Button btn_signIn;
    private FirebaseAuth auth;
    private PinView pinView;
    private TextView textU;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private ConstraintLayout second;
    private String codeBySystem;
    private Button registerBtn;

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

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        phoneNumber = findViewById(R.id.phoneTextBox);
        pinView = findViewById(R.id.pinView);
        textU = findViewById(R.id.textView_noti);
        second = findViewById(R.id.constraintLayout1);
        btn_signIn = findViewById(R.id.signBT);
        registerBtn = findViewById(R.id.registerBT);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        auth = FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NotNull PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);
                Toast.makeText(LoginActivity.this, "onVerificationCompleted:" + credential, Toast.LENGTH_LONG).show();
                pinView.setText(credential.getSmsCode());
                PhoneAuthCredential credential1 = PhoneAuthProvider.getCredential(codeBySystem, Objects.requireNonNull(credential.getSmsCode()));
                signInWithPhoneAuthCredential(credential1);
            }

            @Override
            public void onVerificationFailed(@NotNull FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);
                Toast.makeText(LoginActivity.this, "OTP Sent", Toast.LENGTH_LONG).show();
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
                        pinView.setLineColor(Color.GREEN);
                        textU.setText("OTP Verified");
                        textU.setTextColor(Color.GREEN);
                        startActivity(new Intent(this, MessageActivity.class));
                        this.finish();
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


    @SuppressLint("SetTextI18n")
    public void login(View view) {
        String pno = phoneNumber.getText().toString();
        if (TextUtils.isEmpty(pno)) {
            Toast.makeText(LoginActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
        } else if (btn_signIn.getText().equals("Verify")) {
            String OTP = Objects.requireNonNull(pinView.getText()).toString();
            textU.setText("Verifying");
            PhoneAuthCredential credential1 = PhoneAuthProvider.getCredential(codeBySystem, OTP);
            signInWithPhoneAuthCredential(credential1);
        } else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean flag = false;
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        User user = snap.getValue(User.class);
                        assert user != null;
                        if (pno.equals(user.getPhoneNo())) {
                            flag = true;
                            break;
                        }
                    }
                    if (flag) {
                        try {
                            PhoneAuthOptions options =
                                    PhoneAuthOptions.newBuilder(auth)
                                            .setPhoneNumber(pno)       // Phone number to verify
                                            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                            .setActivity(LoginActivity.this)                 // Activity (for callback binding)
                                            .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                                            .build();
//                            startActivity(new Intent(LoginActivity.this, MessageActivity.class));
                            PhoneAuthProvider.verifyPhoneNumber(options);
                            btn_signIn.setText("Verify");
                            registerBtn.setVisibility(View.GONE);
                            phoneNumber.setVisibility(View.GONE);
                            second.setVisibility(View.VISIBLE);
                            reference.removeEventListener(this);
                        } catch (Exception e) {
                            Log.d(TAG, e.toString());
                        }
                    } else
                        Toast.makeText(LoginActivity.this, "This Phone number is not registered with us,Please Register.", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public void register(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}