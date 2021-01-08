package com.example.chatapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;

public class Register extends AppCompatActivity implements View.OnClickListener  {

    private PinView pinView;
    private Button next;
    private TextView topText,textU;
    private EditText name, userPhone, password;
    private ConstraintLayout first, second;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*requestWindowFeature(1);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(Color.BLUE);
        setContentView(R.layout.activity_register);*/

        topText = findViewById(R.id.topText);
        pinView = findViewById(R.id.pinView);
        next = findViewById(R.id.button);
        name = findViewById(R.id.nameTextBox);
        userPhone = findViewById(R.id.phoneTextBox);
        password = findViewById(R.id.passwordTextBox);
        first = findViewById(R.id.constraintLayout);
        second = findViewById(R.id.constraintLayout1);
        textU = findViewById(R.id.textView_noti);
        second.setVisibility(View.INVISIBLE);

        next.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (next.getText().equals("Let's go!")) {
            String name = this.name.getText().toString();
            String phone = userPhone.getText().toString();
            String pass = password.getText().toString();
            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(phone)) {
                next.setText("Verify");
                first.setVisibility(View.GONE);
                second.setVisibility(View.VISIBLE);
                topText.setText("I Still don't trust you.\nTell me something that only two of us know.");
            } else {
                Toast.makeText(Register.this, "Please enter the details", Toast.LENGTH_SHORT).show();
            }
        } else if (next.getText().equals("Verify")) {
            String OTP = pinView.getText().toString();
            if (OTP.equals("0000")) {
                pinView.setLineColor(Color.GREEN);
                textU.setText("OTP Verified");
                textU.setTextColor(Color.GREEN);
                next.setText("Next");
            } else {
                pinView.setLineColor(Color.RED);
                textU.setText("X Incorrect OTP");
                textU.setTextColor(Color.RED);
            }
        }else if (next.getText().equals("Next")) {
            Register.this.finish();
        }

    }
}