package com.example.pickmecustomers.Notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.example.pickmecustomers.Activity.HomeActivity;
import com.example.pickmecustomers.Activity.RatingActivity;
import com.example.pickmecustomers.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

public class MyFirebaseMessaging extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(@NonNull final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);


        //this is outside the main thread, so if you want to run Toast,you need to create handler to do that//


        if (remoteMessage.getData().get("title").equals("Cancel")) {

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(MyFirebaseMessaging.this, "" + remoteMessage.getData().get("body"), Toast.LENGTH_SHORT).show();
                }
            });

            LocalBroadcastManager.getInstance(MyFirebaseMessaging.this)
                    .sendBroadcast(new Intent("CANCEL_PICKUP"));

        } else if (remoteMessage.getData().get("title").equals("Arrived")) {


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                OreoAndAboveNotificationDriverArrived(remoteMessage.getData().get("body"));

            } else {

                showDriverArrivedNotification(remoteMessage.getData().get("body"));

            }

        } else if (remoteMessage.getData().get("title").equals("DropOff")) {


            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("DROPOFF"));
            Intent intent = new Intent(this, RatingActivity.class);
            // intent.putExtra("driver_id",remoteMessage.getData().get("body").toString());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);



          /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                OreoAndAboveRatingNotification(remoteMessage.getData().get("body"));

            } else {

                showRatingNotification(remoteMessage.getData().get("body"));

            }

           */


        }


    }

    private void showRatingNotification(String body) {

        Intent intent = new Intent(this, RatingActivity.class);
        intent.putExtra("driver_id", body);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void OreoAndAboveRatingNotification(String body) {

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_ONE_SHOT);


        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(this);

        NotificationCompat.Builder builder = null;


        builder = notificationHelper.getEatItChannelNotification("Arrived", body, pendingIntent, defaultSoundUri);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void OreoAndAboveNotificationDriverArrived(String body) {


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_ONE_SHOT);


        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(this);

        NotificationCompat.Builder builder = null;


        builder = notificationHelper.getEatItChannelNotification("Arrived", body, pendingIntent, defaultSoundUri);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());


    }

    private void showDriverArrivedNotification(String body) {


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_ONE_SHOT);


        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = null;

        builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Arrived")
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());

        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("ARRIVED"));


    }
}
