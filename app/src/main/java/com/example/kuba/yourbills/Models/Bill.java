package com.example.kuba.yourbills.Models;

import android.content.Context;
import android.util.Log;

import com.example.kuba.yourbills.R;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class Bill implements Serializable {

    private String billTitle;
    private String billDescription;
    private float billAmount;
    private boolean paid;
    private Date billDate;
    private int daysLeft;
    private String month;
    private int id;
    //private String notificationTimeBefore;
    private int notificationHour;
    private int notificationMinute;
    private int countToRemind;
    private String remindEvery;
    private int childId; // 0 = no child

    public enum SortCategory {TITLE, AMOUNT, DEADLINE};




    public Bill(String billTitle, String billDescription, float billAmount, Date date, boolean paid,
                int notificationHour, int notificationMinute, int countToRemind, String remindEvery,
                int id, int childId){
        this.billTitle = billTitle;
        this.billDescription = billDescription;
        this.billAmount = billAmount;
        this.id = id;
        //this.paid = false;
        //Calendar calendar = Calendar.getInstance();
        //this.date = calendar.getTime();
        this.billDate = date;
        this.paid = paid;
        this.daysLeft = setDaysLeft(this);
        //this.notificationTimeBefore = notificationTimeBefore;
        this.notificationHour = notificationHour;
        this.notificationMinute = notificationMinute;
        this.countToRemind = countToRemind;
        this.remindEvery = remindEvery;
        this.childId = childId;
        setMonth();
    }

    /*public Bill(String billTitle, String billDescription, float billAmount, boolean paid){
        this.billTitle = billTitle;
        this.billDescription = billDescription;
        this.billAmount = billAmount;
        this.paid = paid;
        //this.date = date;
    }*/



    public static Comparator<Bill> billDeadlineComparator = new Comparator<Bill>() {

        public int compare(Bill b1, Bill b2) {
            int billDaysLeft1 = b1.getDaysLeft();
            int billDaysLeft2 = b2.getDaysLeft();

            //ascending order
            return billDaysLeft1 - billDaysLeft2;
            //descending order
            //return billDaysLeft2 - BillDaysLeft1;
        }};

    public static Comparator<Bill> billTitleComparator = new Comparator<Bill>() {

        public int compare(Bill b1, Bill b2) {
            String billTitle1 = b1.getBillTitle().toLowerCase();
            String billTitle2 = b2.getBillTitle().toLowerCase();

            //ascending order
            return billTitle1.compareTo(billTitle2);
            //descending order
            //return billDaysLeft2.compareTo(billDaysLeft1);
        }};

    public static Comparator<Bill> billAmountComparator = new Comparator<Bill>() {

        public int compare(Bill b1, Bill b2) {
            float billAmount1 = b1.getBillAmount();
            float billAmount2 = b2.getBillAmount();

            //ascending order
            return (int)(billAmount1 - billAmount2);
            //descending order
            //return billDaysLeft2 - BillDaysLeft1;
        }};





    private int setDaysLeft(Bill bill){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd.MM.yyyy", Locale.getDefault());
        Calendar calendar = simpleDateFormat.getCalendar();
        simpleDateFormat.format(new Date()); // totalnie nie wiem po chuj trzeba to wywolac, ale bez tego ucina 10 dni, wtf
        //Log.v("ktorydzisiaj ", todaysDate);
        Date today = calendar.getTime();
        //Log.v("datex_", bill.getBillTitle() + " " + getBillDate().toString());
        //Log.v("datex_", "TODAY " + today.toString());
        long diff = bill.getBillDate().getTime() - today.getTime();
        //Log.v("datex_", "diff " + diff);
        long daysInLong = diff/1000/60/60/24;
        int days = (int)daysInLong;
        //Log.v("datex_", "DAYS " + days);
        return days;
    }

    public int getDaysLeft(){
        return this.daysLeft;
    }


    public String getBillTitle() {
        return billTitle;
    }

    public void setBillTitle(String billTitle) {
        this.billTitle = billTitle;
    }

    public String getBillDescription() {
        return billDescription;
    }

    public void setBillDescription(String billDescription) {
        this.billDescription = billDescription;
    }

    public float getBillAmount() {
        return billAmount;
    }

    public void setBillAmount(float billAmount) {
        this.billAmount = billAmount;
    }

    public boolean isPaid(){
        return this.paid;
    }

    public void setPaid(boolean paid){
        this.paid = paid;
    }

    public int getId(){
        return id;
    }


    public Date getBillDate() {
        return billDate;
    }

    public void setBillDate(Date date) {
        this.billDate = date;
    }

    public String getMonth(){
        return this.month;
    }


    public int getNotificationHour() {
        return notificationHour;
    }

    public void setNotificationHour(int notificationHour) {
        this.notificationHour = notificationHour;
    }

    public int getNotificationMinute() {
        return notificationMinute;
    }

    public void setNotificationMinute(int notificationMinute) {
        this.notificationMinute = notificationMinute;
    }

    public int getCountToRemind() {
        return countToRemind;
    }
    public String getRemindEvery() {
        return remindEvery;
    }

    private void setMonth(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM yyyy");
        Calendar calendar = simpleDateFormat.getCalendar();
        calendar.setTime(billDate);
        month = simpleDateFormat.format(calendar.getTime());
        //Log.v("Naszmiesiacto ", month);
    }

    public int getChildId() {
        return childId;
    }

    public void setChildId(int childId) {
        this.childId = childId;
    }
}
