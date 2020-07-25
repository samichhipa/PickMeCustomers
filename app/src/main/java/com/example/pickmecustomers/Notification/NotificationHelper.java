package com.example.pickmecustomers.Notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.pickmecustomers.R;


public class NotificationHelper extends ContextWrapper {

    private static final String CHANNEL_ID="com.examplepickmecustomer";
    private static final String CHANNEL_NAME="PickMe";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){

            createChannel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {

        NotificationChannel channel=new NotificationChannel(CHANNEL_ID,CHANNEL_NAME
        , NotificationManager.IMPORTANCE_HIGH);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);


        getManager().createNotificationChannel(channel);


    }

    public NotificationManager getManager()
    {

        if (manager==null)

            manager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            return manager;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public NotificationCompat.Builder getEatItChannelNotification(String title, String body, PendingIntent pendingIntent, Uri soundUri)
    {

        return new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(soundUri)
                .setAutoCancel(false);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getEatItChannelNotification(String title, String body, Uri soundUri)
    {

        return new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(soundUri)
                .setAutoCancel(false);

    }
}
