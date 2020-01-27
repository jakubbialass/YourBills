package com.example.kuba.yourbills.Utilities;

import android.content.Context;
import android.util.Log;

import com.example.kuba.yourbills.Models.Bill;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class NotificationScheduler {

    private Context context;

    public NotificationScheduler(Context context){
        this.context = context;
    }



    public void scheduleNotificationWorker(Bill bill){
        if (calculateNotificationDelay(bill, 14, 1) > 0) {
            Data inputData = new Data.Builder().putInt(UploadWorker.NOTIFICATION_ID, 1).build();
            OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(UploadWorker.class)
                    .setInitialDelay(calculateNotificationDelay(bill, 14, 2), TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .addTag("notification")
                    .build();
            WorkManager.getInstance(context).enqueue(notificationWork);
        }
        else
            Log.e("notification_error ", "Could't create notification because delay is negative. Probably bill is overdued.");
    }

    private long getCurrentTimeInMillis(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd.MM.yyyy", Locale.getDefault());
        Calendar calendar = simpleDateFormat.getCalendar();
        simpleDateFormat.format(new Date());
        Date date = calendar.getTime();
        return date.getTime();
    }


    private long calculateNotificationDelay(Bill bill, int notificationHour, int notificationDaysBeforePayment){
        long oneDayInMillis = 24*60*60*1000;

        Date billDate = bill.getBillDate();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd.MM.yyyy", Locale.getDefault());
        Calendar calendar = simpleDateFormat.getCalendar();
        simpleDateFormat.format(new Date());
        calendar.setTime(billDate);
        setNotificationHour(calendar, notificationHour);

        long notificationTimeInMillis = calendar.getTimeInMillis() - (notificationDaysBeforePayment * oneDayInMillis);
        long delay = notificationTimeInMillis-getCurrentTimeInMillis();

        //Log.v("today_billDeadline ", Integer.toString(bill.getDaysLeft()));
        Log.v("today_delays ", Long.toString(delay));
        //Log.v("today_milis ", Long.toString(getCurrentTimeInMillis()));
        //Log.v("today_bill ", Long.toString(bill.getBillDate().getTime()));

        //return delay;
        return 5000;
    }

    private void setNotificationHour(Calendar calendar, int notificationHour){
        calendar.set(Calendar.HOUR_OF_DAY, notificationHour);
        calendar.set(Calendar.MINUTE, 6);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }



}
