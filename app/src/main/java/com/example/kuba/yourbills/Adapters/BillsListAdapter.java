package com.example.kuba.yourbills.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kuba.yourbills.Utilities.DBHelper;
import com.example.kuba.yourbills.R;
import com.example.kuba.yourbills.Models.Bill;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BillsListAdapter extends RecyclerView.Adapter<BillsListAdapter.BillsViewHolder> {

    private DBHelper mydb;
    private ArrayList<Bill> billsList;

    public BillsListAdapter(ArrayList<Bill> billsList){
        this.billsList = billsList;
    }

    public static class BillsViewHolder extends RecyclerView.ViewHolder{
        private TextView billTitle;
        private TextView billDescription;
        private TextView billAmount;
        private TextView billDaysLeft;
        private final Context mContext;



        private BillsViewHolder(View itemView) {
            super(itemView);
            billTitle = itemView.findViewById(R.id.bill_title);
            billDescription = itemView.findViewById(R.id.bill_description);
            billAmount = itemView.findViewById(R.id.bill_amount);
            billDaysLeft = itemView.findViewById(R.id.bill_days_left);
            mContext = itemView.getContext();
        }
    }

    @NonNull
    @Override
    public BillsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate existing view
        View billsListViewItem = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_bills, parent, false);
        mydb = new DBHelper(parent.getContext()); // czy to tak????
        return new BillsViewHolder(billsListViewItem);
    }

    @Override
    public void onBindViewHolder(@NonNull BillsViewHolder holder, int position) {
        Bill bill = billsList.get(position);
        holder.billTitle.setText(bill.getBillTitle());
        holder.billAmount.setText(String.valueOf(bill.getBillAmount()));
       // holder.billDaysLeft.setText(String.valueOf(getDaysLeft(bill)));
        holder.billDaysLeft.setText(deadlineToString(holder, bill));
        holder.billDescription.setText(bill.getBillDescription());
    }

    @Override
    public int getItemCount() {
        return billsList.size();
    }


    /*private int getDaysLeft(Bill bill){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd.MM.yyyy", Locale.getDefault());
        Calendar calendar = simpleDateFormat.getCalendar();

        simpleDateFormat.format(new Date()); // totalnie nie wiem po chuj trzeba to wywolac, ale bez tego ucina 10 dni, wtf
        //Log.v("ktorydzisiaj ", todaysDate);

        Date today = calendar.getTime();
        long diff = bill.getBillDate().getTime() - today.getTime();
        int days = (int)diff/1000/60/60/24;


        //Log.v("ktoryddni ", String.valueOf(days));
        //return (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        return days;
    }*/

    private String deadlineToString(BillsViewHolder holder, Bill bill){
        String info;
        int days = bill.getDaysLeft();//getDaysLeft(bill);
        Log.v("datex_", bill.getBillTitle() + " " + bill.getBillDate().toString());
        Log.v("datex_", "DAYS " + days);
        if(bill.isPaid()) {
            info = holder.mContext.getResources().getString(R.string.bill_paid);
            holder.billDaysLeft.setTextColor(holder.mContext.getResources().getColor(R.color.paidTextColor));
        }
        else if(days == 1){
            info = "One day left";
            holder.billDaysLeft.setTextColor(Color.RED);
        }
        else if(days == 0){
            info = "Deadline today";
            holder.billDaysLeft.setTextColor(Color.RED);
        }
        else if(days < 0){
            info = "Overdued by " + bill.getDaysLeft();
            holder.billDaysLeft.setTextColor(Color.parseColor("#b71c1c"));
        }
        else if(bill.getDaysLeft()>30 && bill.getDaysLeft()<365){
            int monthsLeft = (int)Math.floor((double)(bill.getDaysLeft()/30));
            info = monthsLeft + " months left";
            holder.billDaysLeft.setTextColor(Color.parseColor("#000000"));
        }
        else {
            info = bill.getDaysLeft() + " days left";
            holder.billDaysLeft.setTextColor(Color.parseColor("#000000"));
        }
        return info;
    }

    public void deleteBill(int position){
        billsList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    private Calendar getTodayCalendar(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        simpleDateFormat.format(new Date());
        Calendar calendar = simpleDateFormat.getCalendar();
        return calendar;
    }

}
