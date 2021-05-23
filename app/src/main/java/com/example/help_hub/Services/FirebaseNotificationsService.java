package com.example.help_hub.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.help_hub.Activities.ChatActivity;
import com.example.help_hub.Activities.MainActivity;
import com.example.help_hub.OtherClasses.MyApplication;
import com.example.help_hub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FirebaseNotificationsService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull @NotNull RemoteMessage remoteMessage) {


        if(remoteMessage.getData().size() > 0){
            Map<String, String> map = remoteMessage.getData();
            String title = map.get("name");
            String message = map.get("message");
            String hisId = map.get("hisId");
            String chatId = map.get("chatId");
            String chatType = map.get("chatType");
            String id = map.get("id");
            String chatTitle = map.get("title");
            String otherUserName = map.get("otherUserName");

            createNormalNotification(title, message, hisId, chatId, chatType, id, chatTitle, otherUserName);
        }

        if (MyApplication.getInstance().getCurrentActivity() != null && MyApplication.getInstance().getCurrentActivity() instanceof MainActivity) {
            ((MainActivity) MyApplication.getInstance().getCurrentActivity()).calculateUnreadConversations();

        }

        super.onMessageReceived(remoteMessage);
    }

    @Override
    public void onNewToken(@NonNull @NotNull String s) {
        updateToken(s);
        super.onNewToken(s);
    }

    private void updateToken(String token){
        if(FirebaseAuth.getInstance().getUid() == null)
            return;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getUid());
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        databaseReference.updateChildren(map);
    }

    private void createNormalNotification(String title, String message, String hisId, String chatId, String chatType, String id, String chatTitle, String otherUserName){

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // create channel in new versions of android
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel("chatMessages", "messages", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            manager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "chatMessages");
        builder.setContentTitle(title).
                setContentText(message).
                setAutoCancel(true).
                setSmallIcon(R.drawable.ic_baseline_missing_image_24).
                setPriority(NotificationCompat.PRIORITY_HIGH);

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatidextra", chatId);
        intent.putExtra("useridextra", hisId);
        intent.putExtra("chattypeextra", chatType);
        intent.putExtra("needhelpidextra", id);
        intent.putExtra("titleextra", chatTitle);
        intent.putExtra("usernameextra", otherUserName);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        builder.setContentIntent(pendingIntent);


        manager.notify(new Random().nextInt(85-65), builder.build());

    }


}
