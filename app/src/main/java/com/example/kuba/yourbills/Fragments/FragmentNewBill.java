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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.example.kuba.yourbills.Utilities.DBHelper;
import com.example.kuba.yourbills.R;
import com.example.kuba.yourbills.Models.Bill;
import com.example.kuba.yourbills.Utilities.NotificationScheduler;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;

public class FragmentNewBill extends Fragment {

    private TextView exitTextView, saveTextView;

    private EditText description;//, amount;

    private TextInputEditText amountEditText;//, billTitleEditText;
    private EditText billTitleEditText;

    private TextView dateTextView;
    private Date date;
    private ConstraintLayout newBillLayout;
    public static int FRAGMENT_CODE = 1;
    public static String FRAGMENT_TAG = "New bill";
    private View v;
    private AppCompatSpinner remindSpinner;
    private TextView hourTextView;
    private int notificationHour, notificationMinute;

    private DBHelper myDb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_new_bill, container, false);

        init(view);



        v=view;

        return view;
    }






    private void init(View view){
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
                closeKeyboard();
                getFragmentManager().popBackStack();

            }
        });

        saveTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float amount;
                if(amountEditText.getEditableText().toString().equals(""))
                    amount=0f;
                else
                    amount = Float.valueOf(amountEditText.getEditableText().toString());
                Bill newBill = new Bill(billTitleEditText.getEditableText().toString(), description.getEditableText().toString(),
                        amount, date, false,
                        remindSpinner.getSelectedItem().toString(), notificationHour, notificationMinute,
                        myDb.getMaxIdFromBills()+1);
                myDb.insertBill(newBill);
                Log.v("daysBefore: ", remindSpinner.getSelectedItem().toString());
                setNotification(newBill);
                closeKeyboard();
                sendBillToPreviousFragment(newBill);
                Log.v("Dodalem", "Billa");
            }
        });

        dateTextView = view.findViewById(R.id.date);
        dateTextView.setText(getTodaysDate());
        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            @TargetApi(24)
            public void onClick(View view) {
                closeKeyboard();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext());
                    datePickerDialog.getDatePicker().setFirstDayOfWeek(2);
                    datePickerDialog.show();
                    datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                            setSelectedDate(year, month, day);
                            dateTextView.setText(getSelectedDateString());
                        }
                    });
                }
                else{
                    Log.v("entering", "<N");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(),
                            0,
                            dateSetListener(view),
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        datePickerDialog.getDatePicker().setFirstDayOfWeek(2);
                    }
                    datePickerDialog.show();
                }
            }
        });

        remindSpinner = view.findViewById(R.id.remind_spinner);
        String[] remindSpinnerItems = {"1 day before", "2 days before", "3 days before", "1 week before"};
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


    }


    private DatePickerDialog.OnDateSetListener dateSetListener(View view){
        DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                setSelectedDate(year, month, day);
                dateTextView.setText(getSelectedDateString());
            }
        };
        return onDateSetListener;
    }

    private String getTodaysDate(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String todaysDate = simpleDateFormat.format(new Date());
        Calendar calendar = simpleDateFormat.getCalendar();
        //resetTime(calendar);
        date = calendar.getTime();
        return todaysDate;
    }

    private String getSelectedDateString(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, dd.MM.yyyy");
        return simpleDateFormat.format(date);
    }

    private void setSelectedDate(int year, int month, int day){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        resetTime(calendar);
        this.date = calendar.getTime();
    }

    //resets hourTextView to calculate bill's deadline properly
    private void resetTime(Calendar calendar){
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);
    }


    private void sendBillToPreviousFragment(Bill bill){
       /* Bundle bundle = new Bundle();
        bundle.putSerializable("newBill", bill);
        Fragment fragment = getActivity().getSupportFragmentManager().getFragment(null, "Bills");
        fragment.setArguments(bundle);*/
        Intent intent = new Intent(getContext(), FragmentNewBill.class);
        intent.putExtra("bill", bill);
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
        }
    }






}
