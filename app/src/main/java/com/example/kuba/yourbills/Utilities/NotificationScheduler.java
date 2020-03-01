package com.example.kuba.yourbills.Utilities;

import android.content.Context;
import android.util.Log;

import com.example.kuba.yourbills.Models.Bill;
import com.example.kuba.yourbills.R;

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
    private int countToRemind;
    private String remindEvery;

    public NotificationScheduler(Context context, int countToRemind, String remindEvery){
        this.context = context;
        this.countToRemind = countToRemind;
        this.remindEvery = remindEvery;
    }



    public void scheduleNotificationWorker(Bill bill){

        Log.v("id_przy_tworzeniu_not", Integer.toString(bill.getId()));
/*
        int daysBefore = 1;




        if(bill.getNotificationTimeBefore().equals(context.getResources().getString(R.string.two_days_before)))
            daysBefore = 2;
        else if (bill.getNotificationTimeBefore().equals(context.getResources().getString(R.string.three_days_before)))
            daysBefore = 3;
        else if (bill.getNotificationTimeBefore().equals(context.getResources().getString(R.string.one_week_before)))
            daysBefore = 7;
*/
        if (calculateNotificationDelay(bill, countToRemind, remindEvery) > 0) {
            Data inputData = new Data.Builder().putInt(UploadWorker.NOTIFICATION_ID, bill.getId()).build();
            OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(UploadWorker.class)
                    .setInitialDelay(calculateNotificationDelay(bill, countToRemind, remindEvery), TimeUnit.MILLISECONDS)
                    //.setInitialDelay(5000, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .addTag("notification")
                    .build();
            WorkManager.getInstance(context).enqueue(notificationWork);
            Log.v("Kurwa", "1");
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


    private long calculateNotificationDelay(Bill bill, int notificationDaysBeforePayment){
        long oneDayInMillis = 24*60*60*1000;

        Date billDate = bill.getBillDate();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd.MM.yyyy", Locale.getDefault());
        Calendar calendar = simpleDateFormat.getCalendar();
        simpleDateFormat.format(new Date());
        calendar.setTime(billDate);
        setNotificationTime(calendar, bill.getNotificationHour(), bill.getNotificationMinute());

        long notificationTimeInMillis = calendar.getTimeInMillis() - (notificationDaysBeforePayment * oneDayInMillis);
        long delay = notificationTimeInMillis-getCurrentTimeInMillis();

        //Log.v("today_billDeadline ", Integer.toString(bill.getDaysLeft()));
        Log.v("today_delays ", Long.toString(delay));
        //Log.v("today_milis ", Long.toString(getCurrentTimeInMillis()));
        //Log.v("today_bill ", Long.toString(bill.getBillDate().getTime()));

        //return delay;
        return delay;
    }

    private long calculateNotificationDelay(Bill bill, int countToRemind, String remindEvery){
        long oneDayInMillis = 24*60*60*1000;

        Date billDate = bill.getBillDate();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd.MM.yyyy", Locale.getDefault());
        Calendar calendar = simpleDateFormat.getCalendar();
        simpleDateFormat.format(new Date());
        calendar.setTime(billDate);
        setNotificationTime(calendar, bill.getNotificationHour(), bill.getNotificationMinute());

        int daysBefore = 1;
        if(remindEvery.toUpperCase().equals(context.getResources().getString(R.string.days).toUpperCase()))
            daysBefore = countToRemind;
        else if(remindEvery.toUpperCase().equals(context.getResources().getString(R.string.week).toUpperCase()) || remindEvery.toUpperCase().equals(context.getResources().getString(R.string.weeks).toUpperCase()))
            daysBefore = countToRemind*7;
        else if(remindEvery.toUpperCase().equals(context.getResources().getString(R.string.month).toUpperCase()) || remindEvery.toUpperCase().equals(context.getResources().getString(R.string.months).toUpperCase()))
            daysBefore = countToRemind*30;
        else if(remindEvery.toUpperCase().equals(context.getResources().getString(R.string.year).toUpperCase()) || remindEvery.toUpperCase().equals(context.getResources().getString(R.string.years).toUpperCase()))
            daysBefore = countToRemind*365;

        long notificationTimeInMillis = calendar.getTimeInMillis() - (daysBefore * oneDayInMillis);
        long delay = notificationTimeInMillis-getCurrentTimeInMillis();

        Log.v("today_daysbefore", Integer.toString(daysBefore));
        Log.v("today_billDeadline ", Integer.toString(bill.getDaysLeft()));
        Log.v("today_delays ", Long.toString(delay));
        Log.v("today_notimilis ", Long.toString(notificationTimeInMillis));
        Log.v("today_milis ", Long.toString(getCurrentTimeInMillis()));
        Log.v("today_calendar ", calendar.getTime().toString());
        //Log.v("today_bill ", Long.toString(bill.getBillDate().getTime()));

        //return delay;
        return delay;
    }

    private void setNotificationTime(Calendar calendar, int notificationHour, int notificationMinute){
        calendar.set(Calendar.HOUR_OF_DAY, notificationHour);
        calendar.set(Calendar.MINUTE, notificationMinute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }



}
