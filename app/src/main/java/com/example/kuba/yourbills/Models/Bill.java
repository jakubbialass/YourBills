package com.example.kuba.yourbills.Models;

import android.content.Context;

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
    private String notificationTimeBefore;

    public enum SortCategory {TITLE, AMOUNT, DEADLINE};


    public Bill(String billTitle, String billDescription, float billAmount, Date date, boolean paid){
        this.billTitle = billTitle;
        this.billDescription = billDescription;
        this.billAmount = billAmount;
        //this.paid = false;
        //Calendar calendar = Calendar.getInstance();
        //this.date = calendar.getTime();
        this.billDate = date;
        this.paid = paid;
        this.daysLeft = setDaysLeft(this);
        setMonth();
    }

    public Bill(String billTitle, String billDescription, float billAmount, Date date, boolean paid, String notificationTimeBefore, int id){
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
        this.notificationTimeBefore = notificationTimeBefore;
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
        long diff = bill.getBillDate().getTime() - today.getTime();
        int days = (int)diff/1000/60/60/24;
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

    public int getDatabaseId(){
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

    public String getNotificationTimeBefore(){
        return this.notificationTimeBefore;
    }

    private void setMonth(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM yyyy");
        Calendar calendar = simpleDateFormat.getCalendar();
        calendar.setTime(billDate);
        month = simpleDateFormat.format(calendar.getTime());
        //Log.v("Naszmiesiacto ", month);
    }











}
