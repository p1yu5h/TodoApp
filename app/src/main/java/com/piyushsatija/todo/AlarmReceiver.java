package com.piyushsatija.todo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;
import java.util.Random;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(context, MainActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingI = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default",
                    "Default Channel", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Daily Notification");
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
        NotificationCompat.Builder b = new NotificationCompat.Builder(context, "default");
        b.setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Reminder")
                .setContentText(intent.getStringExtra("title"))
                .setContentInfo("INFO")
                .setContentIntent(pendingI);

        if (nm != null) {
            Random r = new Random();
            nm.notify(r.nextInt(10), b.build());
            Calendar nextNotifyTime = Calendar.getInstance();
            nextNotifyTime.add(Calendar.DATE, 1);
        }
    }
}
