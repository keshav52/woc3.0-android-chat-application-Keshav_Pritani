package com.example.chatapplication.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.chatapplication.MessageActivityUI.ChatFragment;
import com.example.chatapplication.MessageActivityUI.ContactsFragment;
import com.example.chatapplication.MessageActivityUI.FavouriteFragment;

public class MessageActivityAdapter extends FragmentStatePagerAdapter {
    public static final int count = 3;

    public MessageActivityAdapter(FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0:
                return ChatFragment.getInstance();
            case 1:
                return ContactsFragment.getInstance();
            case 2:
                return FavouriteFragment.getInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        return count;
    }
}
