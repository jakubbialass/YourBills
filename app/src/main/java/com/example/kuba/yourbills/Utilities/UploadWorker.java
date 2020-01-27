package com.example.kuba.yourbills.Utilities;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.kuba.yourbills.R;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class UploadWorker extends Worker {

    public static String NOTIFICATION_ID = "notification_id";
    public static String NOTIFICATION = "notification";

    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters params){
        super(context, params);
    }


    @NonNull
    @Override
    public Result doWork() {
        triggerNotification();
        return Result.success();
    }



    private void triggerNotification(){
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());



        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "channel_id")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("My notificatiop")
                .setContentText("Hello")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Notification notification = builder.build();



        final int id = getInputData().getInt(NOTIFICATION_ID, 0);
        Log.v("notification_jakieid ", Integer.toString(id));
        notificationManager.notify(id, notification);
    }
}
