package com.example.chatapplication.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.chatapplication.MessageActivityUI.ChatFragment;
import com.example.chatapplication.MessageActivityUI.UserFragment;
import com.example.chatapplication.MessageActivityUI.FavouriteFragment;
import com.example.chatapplication.MessageActivityUI.OnlineUserFragment;

public class MessageActivityAdapter extends FragmentStatePagerAdapter {
    public static final int count = 4;

    public MessageActivityAdapter(FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return ChatFragment.getInstance();
            case 1:
                return UserFragment.getInstance();
            case 2:
                return FavouriteFragment.getInstance();
            case 3:
                return OnlineUserFragment.getInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        return count;
    }
}
