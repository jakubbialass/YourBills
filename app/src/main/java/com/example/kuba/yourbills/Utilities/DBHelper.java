package com.example.kuba.yourbills.Utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.kuba.yourbills.Models.Bill;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {


    public static final String DATABASE_NAME = "YourBillsDatabase13.db";
    public static final String BILLS_TABLE_NAME = "bills";


    public DBHelper(Context context){
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table bills " +
                        "(id integer primary key, title TEXT,description TEXT,billAmount REAL,billDate INTEGER," +
                        " notificationHour INTEGER, notificationMinute INTEGER, countToRemind INTEGER, remindEvery TEXT, paid INTEGER, childId INTEGER)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS bills");
        onCreate(db);
    }


    public boolean insertBill(Bill bill){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", bill.getBillTitle());
        cv.put("description", bill.getBillDescription());
        cv.put("billAmount", bill.getBillAmount());
        cv.put("billDate", bill.getBillDate().getTime());
        cv.put("notificationHour", bill.getNotificationHour());
        cv.put("notificationMinute", bill.getNotificationMinute());
        cv.put("countToRemind", bill.getCountToRemind());
        cv.put("remindEvery", bill.getRemindEvery());
        cv.put("paid", bill.isPaid()?1:0); //1 if true 0 if false
        cv.put("childId", bill.getChildId());
        db.insert("bills", null, cv);
        return true;
    }

    public boolean removeBill(Bill bill){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM bills WHERE id = " + bill.getId());
        return true;
    }


    public int getMaxIdFromBills(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "select * from bills WHERE id=(select MAX(id) from bills)", null );
        int id = 0;
        if(res!=null && res.getCount()>0) {
            res.moveToFirst();
            id = res.getInt(res.getColumnIndex("id"));
        }
        if (!res.isClosed())  {
            res.close();
        }
        return id;
    }


    public ArrayList<Bill> getBillsList(){
        ArrayList<Bill> tempBillsList = new ArrayList<>();
        Log.v("billowy1 " , Integer.toString(numberOfRows()));
        int id = 1;
        for(int i=1; i<=numberOfRows(); i++){
            Cursor rs = getData(id);
            while(rs.getCount()==0){
                id++;
                rs = getData(id);
            }
            //id_To_Update = Value;
            Log.v("billowy8 " , Integer.toString(rs.getCount()));
            Bill bill = null;
            if(rs!=null && rs.getCount()>0) {
                bill = getBillWithArgs(rs);

                tempBillsList.add(bill);
                id++;
                Log.v("billowy " + bill.getBillTitle(), " id " + bill.getId());
            }
            if (!rs.isClosed())  {
                rs.close();
            }
            //Log.v("podaje dane", title);
        }
        return tempBillsList;
    }

    public void updatePaidData(Bill bill){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("paid", bill.isPaid()?1:0);
        db.update(BILLS_TABLE_NAME, cv, "id="+bill.getId(), null);
    }


    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, BILLS_TABLE_NAME);
        return numRows;
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "select * from bills where id=" + id + "", null );
        return res;
    }



    private void resetTime(Calendar calendar){
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);
    }


    public Bill getBillById(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor rs = db.rawQuery( "select * from bills WHERE id= " + id + "", null );
        Bill bill = null;
        if(rs!=null && rs.getCount()>0) {
            bill = getBillWithArgs(rs);
        }
        if (!rs.isClosed()) {
            rs.close();
        }
        return bill;
    }

    public void removeChildId(int parentId){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("childId", 0);
        db.update(BILLS_TABLE_NAME, cv, "id="+parentId, null);
    }


    private Bill getBillWithArgs(Cursor rs){
        rs.moveToFirst();
        int id = rs.getInt(rs.getColumnIndex("id"));
        String title = rs.getString(rs.getColumnIndex("title"));
        String description = rs.getString(rs.getColumnIndex("description"));
        float billAmount = rs.getFloat(rs.getColumnIndex("billAmount"));
        long dateInMillis = rs.getLong(rs.getColumnIndex("billDate"));
        int notificationHour = rs.getInt(rs.getColumnIndex("notificationHour"));
        int notificationMinute = rs.getInt(rs.getColumnIndex("notificationMinute"));
        int countToRemind = rs.getInt(rs.getColumnIndex("countToRemind"));
        String remindEvery = rs.getString(rs.getColumnIndex("remindEvery"));
        int childId = rs.getInt(rs.getColumnIndex("childId"));

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateInMillis);
        resetTime(calendar);
        Date billDate = calendar.getTime();
        boolean paid = (rs.getInt(rs.getColumnIndex("paid"))) == 1;

        return new Bill(title, description, billAmount, billDate, paid,
                notificationHour, notificationMinute, countToRemind, remindEvery,
                id, childId);
    }



}
