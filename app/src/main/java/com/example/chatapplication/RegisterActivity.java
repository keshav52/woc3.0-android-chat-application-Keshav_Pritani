package com.example.chatapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.chaos.view.PinView;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private PinView pinView;
    private Button next;
    private TextView topText, textU;
    private EditText name, email, status;
    private ConstraintLayout first, second;
    private FirebaseAuth auth;
    private DatabaseReference reference;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private final String TAG = "Keshav Pritani";
    private String codeBySystem;

    FirebaseUser firebaseUser;

    @Override
    protected void onStart() {
        super.onStart();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //check if user is null
        if (firebaseUser != null){
            Intent intent = new Intent(this, MessageActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*requestWindowFeature(1);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(Color.BLUE);*/
        setContentView(R.layout.activity_register);

        topText = findViewById(R.id.topText);
        pinView = findViewById(R.id.pinView);
        next = findViewById(R.id.button);
        name = findViewById(R.id.nameTextBox);
        email = findViewById(R.id.phoneTextBox);
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
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);
                Toast.makeText(RegisterActivity.this, "onVerificationCompleted:" + credential, Toast.LENGTH_LONG).show();
                pinView.setText(credential.getSmsCode());
                PhoneAuthCredential credential1 = PhoneAuthProvider.getCredential(codeBySystem, credential.getSmsCode());
                signInWithPhoneAuthCredential(credential1);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);

                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();

//                if (e instanceof FirebaseAuthInvalidCredentialsException) {
//                    // Invalid request
//                    // ...
//                } else if (e instanceof FirebaseTooManyRequestsException) {
//                    // The SMS quota for the project has been exceeded
//                    // ...
//                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);
                Toast.makeText(RegisterActivity.this, "onCodeSent:" + verificationId, Toast.LENGTH_LONG).show();
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
                        hashMap.put("email", email.getText().toString());
                        hashMap.put("imageURL", "default");
                        hashMap.put("status",status.getText().toString());
                        hashMap.put("active", "false");

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
            String email1 = email.getText().toString();
            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email1)) {
                next.setText("Verify");
                first.setVisibility(View.GONE);
                second.setVisibility(View.VISIBLE);
                topText.setText("I Still don't trust you.\nTell me something that only two of us know.");
                try {

                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(auth)
                                    .setPhoneNumber(email1)       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(RegisterActivity.this)                 // Activity (for callback binding)
                                    .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
//                    PhoneAuthProvider.getInstance().verifyPhoneNumber(email1,60,TimeUnit.SECONDS, RegisterActivity.this,mCallbacks);
                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }

            } else {
                Toast.makeText(RegisterActivity.this, "Please enter the details", Toast.LENGTH_LONG).show();
            }
        } else if (next.getText().equals("Verify")) {
            String OTP = pinView.getText().toString();
            PhoneAuthCredential credential1 = PhoneAuthProvider.getCredential(codeBySystem, OTP);

            signInWithPhoneAuthCredential(credential1);
        } else if (next.getText().equals("Next")) {
            RegisterActivity.this.finish();
        }
    }

    private void register(final String username, String email, String password) {

//        email = "keshav@gmail.com";
//        username='keshav';
        /*auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        assert firebaseUser != null;
                        String userid = firebaseUser.getUid();

                        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("id", userid);
                        hashMap.put("name", username);
                        hashMap.put("email", email);
                        hashMap.put("imageURL", "default");
                        hashMap.put("status", "offline");

                        reference.setValue(hashMap).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        });
                    } else {
                        Toast.makeText(RegisterActivity.this, "You can't register with this email or password", Toast.LENGTH_SHORT).show();
                    }
                });*/

    }
}