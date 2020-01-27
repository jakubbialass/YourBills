package com.example.kuba.yourbills.Fragments;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.example.kuba.yourbills.Utilities.DBHelper;
import com.example.kuba.yourbills.R;
import com.example.kuba.yourbills.Models.Bill;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;

public class FragmentNewBill extends Fragment {

    private Button buttonAddNewBill, buttonCancel;
    private EditText description;//, amount;

    private TextInputEditText amountEditText, billTitleEditText;

    private TextView dateTextView;
    private Date date;
    private ConstraintLayout newBillLayout;
    public static int FRAGMENT_CODE = 1;
    public static String FRAGMENT_TAG = "New bill";
    private View v;

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
        buttonAddNewBill = view.findViewById(R.id.button_add_new_bill);
        buttonCancel = view.findViewById(R.id.button_cancel_new_bill);
        billTitleEditText = view.findViewById(R.id.billTitleEditText);
        description = view.findViewById(R.id.billDescription);
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

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeKeyboard();
                getFragmentManager().popBackStack();

            }
        });

        buttonAddNewBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float amount;
                if(amountEditText.getEditableText().toString().equals(""))
                    amount=0f;
                else
                    amount = Float.valueOf(amountEditText.getEditableText().toString());
                Bill newBill = new Bill(billTitleEditText.getEditableText().toString(), description.getEditableText().toString(),
                        amount, date, false);
                myDb.insertBill(newBill);
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(),
                            0,
                            dateSetListener(view),
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH));
                    datePickerDialog.getDatePicker().setFirstDayOfWeek(2);
                    datePickerDialog.show();
                }
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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, dd.MM.yyyy", Locale.getDefault());
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

    //resets hour to calculate bill's deadline properly
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


}
