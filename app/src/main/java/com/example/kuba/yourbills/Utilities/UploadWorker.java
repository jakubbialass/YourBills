package com.example.kuba.yourbills.Utilities;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.kuba.yourbills.Models.Bill;
import com.example.kuba.yourbills.R;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class UploadWorker extends Worker {

    public static String NOTIFICATION_ID = "notification_id";
    public static String NOTIFICATION = "notification";
    private DBHelper myDb;

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

        final int id = getInputData().getInt(NOTIFICATION_ID, 0);
        myDb = new DBHelper(getApplicationContext());
        Bill bill = myDb.getBillById(id);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "channel_id")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(bill.getBillTitle())
                .setContentText(getNotificationDescription(bill))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Notification notification = builder.build();


        Log.v("notification_jakieid ", Integer.toString(id));
        notificationManager.notify(id, notification);
    }


    private String getNotificationTimeLeft(Bill bill){
        String timeLeft = getApplicationContext().getResources().getString(R.string.one_day_left);
        String timeBefore = bill.getNotificationTimeBefore();
        if(timeBefore.equals(getApplicationContext().getResources().getString(R.string.two_days_before)))
            timeLeft = getApplicationContext().getResources().getString(R.string.two_days_left);
        else if (timeBefore.equals(getApplicationContext().getResources().getString(R.string.three_days_before)))
            timeLeft = getApplicationContext().getResources().getString(R.string.three_days_left);
        else if (timeBefore.equals(getApplicationContext().getResources().getString(R.string.one_week_before)))
            timeLeft = getApplicationContext().getResources().getString(R.string.one_week_left);
        return timeLeft;
    }

    private String getNotificationDescription(Bill bill){
        return getNotificationTimeLeft(bill) + " " + getApplicationContext().getResources().getString(R.string.to_pay_bill);
    }

}
