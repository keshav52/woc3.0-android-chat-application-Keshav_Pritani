package com.example.chatapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.chatapplication.Adapter.MessageActivityAdapter;
import com.example.chatapplication.Model.User;
import com.gigamole.navigationtabstrip.NavigationTabStrip;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    FirebaseUser firebaseUser;
    DatabaseReference reference;

    CircleImageView profile_image;
    TextView username;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

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

        /*reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
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
        });*/
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        toolbar.bringToFront();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,R.string.nav_app_bar_open_drawer_description,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_bus);

//
//        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle mToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.string.nav_app_bar_open_drawer_description,R.string.navigation_drawer_close);
//
//        mDrawerLayout.addDrawerListener(mToggle);
//        mToggle.syncState();

    }

    @SuppressLint("WrongConstant")
    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(Gravity.START))
            drawerLayout.closeDrawer(Gravity.START);
        else
            super.onBackPressed();
    }

    public void chat(View view) {
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.nav_home:
                Intent intent = new Intent(MessageActivity.this,ThemeActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_share:
                Toast.makeText(this,"Keshav",Toast.LENGTH_LONG).show();
                break;
        }
        return true;
    }

    public void clickMe(View view)
    {
        Toast.makeText(this,"keshav",Toast.LENGTH_LONG).show();
    }
}