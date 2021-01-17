package com.example.chatapplication.Notifications;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;

import com.example.chatapplication.ChatActivity;
import com.example.chatapplication.GroupChatActivity;
import com.example.chatapplication.LoginActivity;
import com.example.chatapplication.MessageActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NotNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String sented = remoteMessage.getData().get("sented");
        String user = remoteMessage.getData().get("user");

        SharedPreferences preferences = getSharedPreferences("PREFS", MODE_PRIVATE);
        String currentUser = preferences.getString("currentuser", "none");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            assert sented != null;
            if (sented.equals(firebaseUser.getUid())) {
                if (!currentUser.equals(user)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        sendOreoNotification(remoteMessage);
                    } else {
                        sendNotification(remoteMessage);
                    }
                }
            }
        }
    }

    private void sendOreoNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String type = remoteMessage.getData().get("type");

        remoteMessage.getNotification();
        assert user != null;
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Bundle bundle = new Bundle();
        Intent intent;
        Class<ChatActivity> c = ChatActivity.class;
        assert type != null;
        if (type.equals("chat")) {
            intent = new Intent(this, c);
            bundle.putString("userid", user);
        } else if (type.equals("group")) {
            intent = new Intent(this, GroupChatActivity.class);
            bundle.putString("groupid", user);
        } else {
            intent = new Intent(this, MessageActivity.class);
            bundle.putString("tabIndex", user);
        }
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent,
                defaultSound, icon);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        oreoNotification.getManager().notify(i, builder.build());

    }

    private void sendNotification(RemoteMessage remoteMessage) {

        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String type = remoteMessage.getData().get("type");

        remoteMessage.getNotification();
        assert user != null;
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Bundle bundle = new Bundle();
        Intent intent;
        Class<ChatActivity> c = ChatActivity.class;
        assert type != null;
        if (type.equals("chat")) {
            intent = new Intent(this, c);
            bundle.putString("userid", user);
        } else if (type.equals("group")) {
            intent = new Intent(this, GroupChatActivity.class);
            bundle.putString("groupid", user);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        assert icon != null;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);
        NotificationManager noti = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        noti.notify(i, builder.build());
    }
}
