package com.example.chatapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText phoneNumber,password;
    private ImageView btn_signIn;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        phoneNumber = findViewById(R.id.phoneTextBox);
        password = findViewById(R.id.nameTextBox);
        btn_signIn = findViewById(R.id.loginBtn);

        auth = FirebaseAuth.getInstance();
        btn_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_username = phoneNumber.getText().toString();
                String txt_password = password.getText().toString();

                if (TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_password)){
                    Toast.makeText(MainActivity.this, "All fileds are required", Toast.LENGTH_SHORT).show();
                } else if (txt_password.length() < 6 ){
                    Toast.makeText(MainActivity.this, "password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                } else {
//                    register(txt_username, txt_password);
                }
            }
        });
    }

    public void login(View view) {
        Intent intent = new Intent(this,MessageScreen.class);
        startActivity(intent);
        this.finish();
    }

    public void register(View view)
    {
        Intent intent = new Intent(this,Register.class);
        startActivity(intent);
    }

    public void forgotPassword(View view)
    {
        Intent intent = new Intent(this,MessageScreen.class);
        startActivity(intent);
    }
}