package com.example.kuba.yourbills.Fragments;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.example.kuba.yourbills.Utilities.DBHelper;
import com.example.kuba.yourbills.R;
import com.example.kuba.yourbills.Models.Bill;
import com.example.kuba.yourbills.Utilities.NotificationScheduler;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import static android.app.Activity.RESULT_OK;

public class FragmentNewBill extends Fragment {

    private TextView exitTextView, saveTextView;

    private EditText description;//, amount;

    private TextInputEditText amountEditText;//, billTitleEditText;
    private EditText billTitleEditText;

    private TextView dateTextView, noRepeat;
    private ConstraintLayout newBillLayout;
    public static int FRAGMENT_CODE = 1;
    public static String FRAGMENT_TAG = "New bill";
    private View v;
    private AppCompatSpinner remindSpinner;
    private TextView hourTextView;
    private DBHelper myDb;
    private String repeatEvery = "No repeat";
    private int countToRepeat = 0;
    private Date repeatEndDate;
    private TextView remindInfoTextView;


    //bill parameters to save
    private Date billDate;
    private int notificationHour, notificationMinute;
    private ArrayList<Bill> newBillsList;
    private int countToRemind;
    private String remindEvery;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_new_bill, container, false);

        init(view);



        v=view;

        return view;
    }






    private void init(View view){
        newBillsList = new ArrayList<>();
        exitTextView = view.findViewById(R.id.exit_new_bill);
        saveTextView = view.findViewById(R.id.save_bill);
        billTitleEditText = view.findViewById(R.id.billTitleEditText);

        billTitleEditText.setRawInputType(InputType.TYPE_CLASS_TEXT);
        billTitleEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.v("klawiatura ", "ogarnalem");
                    billTitleEditText.clearFocus();
                    closeKeyboard();
                    handled = true;
                }
                return handled;

            }
        });
        description = view.findViewById(R.id.billDescription);
        description.setRawInputType(InputType.TYPE_CLASS_TEXT);
        description.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.v("klawiatura ", "ogarnalem");
                    description.clearFocus();
                    closeKeyboard();
                    handled = true;
                }
                return handled;

            }
        });
        amountEditText = view.findViewById(R.id.billAmount);

        newBillLayout = view.findViewById(R.id.newBillLayout);
        newBillLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearEditTextsFocus();
                closeKeyboard();
            }
        });

        myDb = new DBHelper(view.getContext());

        exitTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.v("datexRepeat", repeatEndDate.toString());
                closeKeyboard();
                getFragmentManager().popBackStack();

            }
        });

        initSave(view);

        dateTextView = view.findViewById(R.id.date);
        dateTextView.setTag("billDate");
        billDate = getTodayDate();
        dateTextView.setText(getTodaysDate());
        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            @TargetApi(24)
            public void onClick(View view) {
                closeKeyboard();
                showDatePickerDialog(view, dateTextView);
            }
        });

        remindSpinner = view.findViewById(R.id.remind_spinner);
        String[] remindSpinnerItems = {getResources().getString(R.string.one_day_before),
                getResources().getString(R.string.two_days_before),
                getResources().getString(R.string.three_days_before),
                getResources().getString(R.string.one_week_before),};
        ArrayAdapter<String> remindSpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, remindSpinnerItems);
        remindSpinner.setAdapter(remindSpinnerAdapter);

        hourTextView = view.findViewById(R.id.hour);
        hourTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getActivity().getSupportFragmentManager(), TimePickerFragment.FRAGMENT_TAG);
                newFragment.setTargetFragment(FragmentNewBill.this, TimePickerFragment.FRAGMENT_CODE);
            }
        });



        initRepeat(view);

        initRemind(view);



    }

    private void initSave(View view){

        saveTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float amount = 0f;
                if(amountEditText.getEditableText().toString().length()!=0)
                    amount = Float.valueOf(amountEditText.getEditableText().toString());


                Bill newBill = new Bill(billTitleEditText.getEditableText().toString(), description.getEditableText().toString(),
                        amount, billDate, false,
                        remindSpinner.getSelectedItem().toString(), notificationHour, notificationMinute,
                        myDb.getMaxIdFromBills()+1, 0);

                newBillsList.add(newBill);

                if(countToRepeat!=0 && countToRepeat>0){
                    //countToRepeat = Integer.valueOf(repeatCountEditText.getText().toString());

                    addBillChild(newBill, countToRepeat, repeatEvery);



                }

                for(Bill bill : newBillsList) {
                    myDb.insertBill(bill);
                    setNotification(bill);
                }

                closeKeyboard();
                sendBillToPreviousFragment(newBill);
                Log.v("Dodalem", "Billa");
            }
        });
    }

    private void initRepeat(View view){
        repeatEndDate = getTodayDate();

        noRepeat = view.findViewById(R.id.no_repeat);
        noRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                //fragmentManager.popBackStack();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_right_to_left, R.anim.slide_out_to_left, R.anim.slide_in_top_right, R.anim.slide_out_top_right);

                FragmentDoublePicker fragmentDoublePicker = new FragmentDoublePicker();
                fragmentDoublePicker.setTargetFragment(FragmentNewBill.this, FragmentDoublePicker.FRAGMENT_CODE);
                fragmentDoublePicker.setArgs(countToRepeat, repeatEvery, repeatEndDate.getTime());
                fragmentTransaction.add(R.id.fragment_placeholder, fragmentDoublePicker, "doublePicker");
                fragmentTransaction.addToBackStack(FRAGMENT_TAG);
                fragmentTransaction.commit();
            }
        });
    }


    private void initRemind(View view){
        notificationHour = 10;
        notificationMinute = 0;
        countToRemind = 1;
        remindEvery = getResources().getString(R.string.day);



        remindInfoTextView = view.findViewById(R.id.remind_info);
        remindInfoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                //fragmentManager.popBackStack();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_right_to_left, R.anim.slide_out_to_left, R.anim.slide_in_top_right, R.anim.slide_out_top_right);

                FragmentDoublePicker fragmentDoublePicker = new FragmentDoublePicker();
                fragmentDoublePicker.setTargetFragment(FragmentNewBill.this, FragmentDoublePicker.FRAGMENT_REMIND);
                fragmentDoublePicker.setArgs(notificationHour, notificationMinute, countToRemind, remindEvery);
                fragmentTransaction.add(R.id.fragment_placeholder, fragmentDoublePicker, "doublePicker");
                fragmentTransaction.addToBackStack(FRAGMENT_TAG);
                fragmentTransaction.commit();
            }
        });
    }



    private void addBillChild(Bill parent, int countToRepeat, String repeatEvery){

        Date parentDate = parent.getBillDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parentDate);
        addDays(calendar, countToRepeat, repeatEvery);



        Log.v("timex_repeatEnd", repeatEndDate.toString());
        Log.v("timex_child", calendar.getTime().toString());

        if(repeatEndDate.getTime()>=calendar.getTime().getTime()){
            parent.setChildId(parent.getId()+1);

            Bill childBill = new Bill(parent.getBillTitle(),
                    parent.getBillDescription(),
                    parent.getBillAmount(),
                    calendar.getTime(),
                    false,
                    parent.getNotificationTimeBefore(),
                    parent.getNotificationHour(),
                    parent.getNotificationMinute(),
                    parent.getId()+1,
                    0);

            newBillsList.add(childBill);

            addBillChild(childBill, countToRepeat, repeatEvery);
        }

    }

    private void addDays(Calendar calendar, int countToRepeat, String repeatEvery){

        if(repeatEvery.toUpperCase().equals(getResources().getString(R.string.day).toUpperCase()) || repeatEvery.toUpperCase().equals(getResources().getString(R.string.days).toUpperCase()))
            calendar.add(Calendar.DAY_OF_MONTH, countToRepeat);
        else if(repeatEvery.toUpperCase().equals(getResources().getString(R.string.week).toUpperCase()) || repeatEvery.toUpperCase().equals(getResources().getString(R.string.weeks).toUpperCase()))
            calendar.add(Calendar.DAY_OF_MONTH, countToRepeat*7);
        else if(repeatEvery.toUpperCase().equals(getResources().getString(R.string.month).toUpperCase()) || repeatEvery.toUpperCase().equals(getResources().getString(R.string.months).toUpperCase()))
            calendar.add(Calendar.MONTH, countToRepeat);
        else if(repeatEvery.toUpperCase().equals(getResources().getString(R.string.year).toUpperCase()) || repeatEvery.toUpperCase().equals(getResources().getString(R.string.years).toUpperCase()))
            calendar.add(Calendar.YEAR, countToRepeat);
    }

    private void showDatePickerDialog(View view, final TextView textView) {
        Calendar calendar = Calendar.getInstance();
        if (textView.getTag().equals("billDate"))
            calendar.setTime(billDate);
        DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(),
                0,
                dateSetListener(view, textView),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            datePickerDialog.getDatePicker().setFirstDayOfWeek(2);
        }
        datePickerDialog.show();
    }


    private DatePickerDialog.OnDateSetListener dateSetListener(View view, final TextView textView){
        DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                //setSelectedDate(year, month, day);
                if(textView.getTag().equals("billDate"))
                    billDate = getSelectedDate(year, month, day);
                textView.setText(getDateToString(getSelectedDate(year, month, day)));
            }
        };
        return onDateSetListener;
    }

    private String getTodaysDate(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String todaysDate = simpleDateFormat.format(new Date());
        Calendar calendar = simpleDateFormat.getCalendar();
        //resetTime(calendar);
        billDate = calendar.getTime();
        return todaysDate;
    }
    private Date getTodayDate(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        simpleDateFormat.format(new Date());
        Calendar calendar = simpleDateFormat.getCalendar();
        return calendar.getTime();
    }

    private String getDateToString(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        return simpleDateFormat.format(date);
    }


    private void setSelectedDate(int year, int month, int day){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        resetTime(calendar);
        this.billDate = calendar.getTime();
    }

    private Date getSelectedDate(int year, int month, int day){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        resetTime(calendar);
        return calendar.getTime();
    }

    //resets time in date to calculate bill's deadline properly
    private void resetTime(Calendar calendar){
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);
    }


    private void sendBillToPreviousFragment(Bill bill){
        Intent intent = new Intent(getContext(), FragmentNewBill.class);
        intent.putExtra("newBillsList", newBillsList);
        getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_OK, intent);
        getFragmentManager().popBackStack();
    }

    private void closeKeyboard(){
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void clearEditTextsFocus(){
        amountEditText.clearFocus();
        billTitleEditText.clearFocus();
        description.clearFocus();
    }


    private void setNotification(Bill bill){
        NotificationScheduler notificationScheduler = new NotificationScheduler(getContext());
        notificationScheduler.scheduleNotificationWorker(bill);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode== TimePickerFragment.FRAGMENT_CODE){
                int hour = notificationHour = (int)data.getSerializableExtra("hour");
                int minute = notificationMinute = (int)data.getSerializableExtra("minute");
                String zero = "";
                if(minute<10)
                    zero="0";

                this.hourTextView.setText(hour + ":" + zero + minute);
            }
            if (requestCode== FragmentDoublePicker.FRAGMENT_CODE){
                countToRepeat = (int)data.getSerializableExtra("countToRepeat");
                repeatEvery = (String)data.getSerializableExtra("repeatEvery");
                repeatEndDate = (Date)data.getSerializableExtra("repeatEndDate");

                if(countToRepeat==0)
                    noRepeat.setText(repeatEvery);
                else {
                    String repeatInfo = "Every " + countToRepeat + " " + repeatEvery
                            + "\n to " + getDateToString(repeatEndDate);
                    noRepeat.setText(repeatInfo);
                }
            }
        }
    }




}
