package com.example.chatapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.chatapplication.Adapter.MessageActivityAdapter;
import com.example.chatapplication.Model.User;
import com.gigamole.navigationtabstrip.NavigationTabStrip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {


    FirebaseUser firebaseUser;
    DatabaseReference reference;

    CircleImageView profile_image;
    TextView username;

    Toolbar toolbar;

    SharedPreferences.Editor editor;
    private SharedPreferences prefs;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        final NavigationTabStrip navigationTabStrip = (NavigationTabStrip) findViewById(R.id.nts1);
        navigationTabStrip.setTabIndex(0, true);
        ViewPager mViewPager = findViewById(R.id.viewPage);
        MessageActivityAdapter adapterPager = new MessageActivityAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapterPager);
        navigationTabStrip.setViewPager(mViewPager);
        navigationTabStrip.setTabIndex(0, true);


        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                username.setText(user.getName());
                if (user.getImageURL().equals("default")) {
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {

                    //change this
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefs = getSharedPreferences("PREFS", MODE_PRIVATE);
        editor = prefs.edit();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_profile:
                Intent intent = new Intent(MessageActivity.this, UserProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MessageActivity.this, LoginActivity.class));
                finish();
                break;
            case R.id.modeSwitch:
                if (item.getTitle().equals("Switch to Light Theme")) {
                    item.setTitle("Switch to Dark Theme");
                    editor.putString("modeState", "light");
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else {
                    item.setTitle("Switch to Light Theme");
                    editor.putString("modeState", "dark");
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                editor.apply();
                startActivity(new Intent(this,MessageActivity.class));
                this.finish();
                break;
        }
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem item = menu.getItem(0);

        if(prefs.getString("modeState", "dark").equals("dark"))
        {
            item.setTitle("Switch to Light Theme");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else
        {
            item.setTitle("Switch to Dark Theme");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        return true;
    }

    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("lastSeen", status);

        reference.updateChildren(hashMap);
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