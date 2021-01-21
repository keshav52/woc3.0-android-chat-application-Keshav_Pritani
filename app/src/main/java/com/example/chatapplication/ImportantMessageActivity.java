package com.example.chatapplication;

import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.Adapter.ChatAdapter;
import com.example.chatapplication.Adapter.DBHelper;
import com.example.chatapplication.Model.Chat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class ImportantMessageActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_important_message);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Important Messages");

        loadMessages();
    }

    private void loadMessages() {
        DBHelper DB = new DBHelper(this);
        Cursor res = DB.getData();
        if (res.getCount() != 0) {
            ArrayList<Chat> mchats = new ArrayList<>();
            while (res.moveToNext()) {
                Chat c = new Chat(
                        res.getString(res.getColumnIndex("sender")),
                        res.getString(res.getColumnIndex("receiver")),
                        res.getString(res.getColumnIndex("message")),
                        new Date(res.getString(res.getColumnIndex("d"))),
                        res.getString(res.getColumnIndex("type"))
                );
                mchats.add(c);
            }
            ChatAdapter messageAdapter = new ChatAdapter(this, mchats, "imp");
            recyclerView.setAdapter(messageAdapter);
        }
    }
}