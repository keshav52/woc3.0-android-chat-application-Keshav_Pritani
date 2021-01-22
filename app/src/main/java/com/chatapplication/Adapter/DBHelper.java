package com.chatapplication.Adapter;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context) {
        super(context, "IMPData.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase DB) {
        DB.execSQL("create Table IMP(message TEXT PRIMARY KEY,sender TEXT,receiver TEXT, type varchar, d TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int i, int i1) {
        DB.execSQL("drop Table if exists IMP");
    }

    public Boolean insertData(String sender, String receiver, String message, String type, String time) {
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("message", message);
        contentValues.put("sender", sender);
        contentValues.put("receiver", receiver);
        contentValues.put("type", type);
        contentValues.put("d", time);
        long result = DB.insert("IMP", null, contentValues);
        return result != -1;
    }


    public Boolean deleteData(String message) {
        SQLiteDatabase DB = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = DB.rawQuery("Select * from IMP where message = ?", new String[]{message});
        if (cursor.getCount() > 0) {
            long result = DB.delete("IMP", "message=?", new String[]{message});
            return result != -1;
        } else {
            return false;
        }

    }

    public Cursor getData() {
        SQLiteDatabase DB = this.getWritableDatabase();
        return DB.rawQuery("Select * from IMP", null);

    }
}

