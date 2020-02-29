package com.example.kuba.yourbills.Fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.kuba.yourbills.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;

public class FragmentDoublePicker extends Fragment {


    private NumberPicker valuePicker, typePicker;
    private TextView textView1a, textView1b, textView2a, textView2b;
    private String[] values, types;
    private RelativeLayout secondRelative;
    private LinearLayout pickers;
    private Date repeatEndDate;
    private int countToRepeat;
    private String repeatEvery;
    public static int FRAGMENT_CODE = 23485;

    //common
    private int pickerType;

    //reminder
    public static int FRAGMENT_REMIND = 23486;
    private int notificationHour;
    private int notificationMinute;
    private int countToRemind;
    private String remindEvery;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_double_picker, container, false);

        //final int minVal = getArguments().getInt("minVal", 0);

        getArgs();


        valuePicker = view.findViewById(R.id.value_picker);
        typePicker = view.findViewById(R.id.type_picker);
        textView1a = view.findViewById(R.id.text_view_1a);
        textView1b = view.findViewById(R.id.text_view_1b);
        textView2a = view.findViewById(R.id.text_view_2a);
        textView2b = view.findViewById(R.id.text_view_2b);



        if(pickerType==1)
            initRepeater(view);
        else
            initReminder(view);







        /*valuePicker.setMinValue(minVal);
        valuePicker.setMaxValue(60);
        valuePicker.setWrapSelectorWheel(false);
        valuePicker.setEnabled(false);*/




        initBackButton(view);

        return view;
    }


    public void initReminder(View view){
        textView1a.setText(getResources().getString(R.string.remind_label));
        textView2a.setText(getResources().getString(R.string.time_label));
        String text1b = countToRemind + " " + remindEvery + " " + getResources().getString(R.string.before);
        textView1b.setText(text1b);
        String zero = "";
        if(notificationMinute<10)
            zero="0";
        String text2b = notificationHour + ":" + zero + notificationMinute;
        textView2b.setText(text2b);


        pickers = view.findViewById(R.id.pickers);
        secondRelative = view.findViewById(R.id.second_relative);
        secondRelative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getActivity().getSupportFragmentManager(), TimePickerFragment.FRAGMENT_TAG);
                newFragment.setTargetFragment(FragmentDoublePicker.this, TimePickerFragment.FRAGMENT_CODE);
            }
        });


        setNumbers(valuePicker, 1, 7);
        valuePicker.setValue(countToRemind);

        if(countToRemind<=1)
            types = new String[]{"Day", "Week", "Month", "Year"};
        else
            types = new String[]{"Days", "Weeks", "Months", "Years"};

        valuePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                if (valuePicker.getValue() > 1) {
                    types = new String[]{"Days", "Weeks", "Months", "Years"};
                    typePicker.setDisplayedValues(types);
                    countToRemind = Integer.valueOf(values[valuePicker.getValue() - 1]);
                    remindEvery = types[typePicker.getValue() - 1];
                } else {
                    types = new String[]{"Day", "Week", "Month", "Year"};
                    typePicker.setDisplayedValues(types);
                    countToRemind = Integer.valueOf(values[valuePicker.getValue() - 1]);
                    remindEvery = types[typePicker.getValue() - 1];
                }
                textView1b.setText(countToRemind + " " + remindEvery + " " + getResources().getString(R.string.before));
            }
        });

        typePicker.setMinValue(1);
        typePicker.setMaxValue(4);
        typePicker.setWrapSelectorWheel(false);
        typePicker.setDisplayedValues(types);
        for (int i = 0; i < types.length; i++) {
            if (types[i].toUpperCase().equals(remindEvery.toUpperCase())) {
                typePicker.setValue(i + 1);
                break;
            }
        }


        typePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                    remindEvery = types[typePicker.getValue()-1];
                    textView1b.setText(countToRemind + " " + remindEvery + " " + getResources().getString(R.string.before));
            }
        });
    }

    public void initRepeater(View view){
        textView2b.setText(getDateToString(repeatEndDate));
        if(countToRepeat==0)
            textView1b.setText(repeatEvery);
        else
            textView1b.setText(countToRepeat + " " + repeatEvery);


        pickers = view.findViewById(R.id.pickers);
        secondRelative = view.findViewById(R.id.second_relative);
        secondRelative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(view, textView2b);
            }
        });


        if(countToRepeat<=1)
            types = new String[]{"No repeat", "Day", "Week", "Month", "Year"};
        else
            types = new String[]{"No repeat", "Days", "Weeks", "Months", "Years"};

        valuePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                if(values.length!=1){
                    if(numberPicker.getValue()>1){
                        types = new String[]{"No repeat", "Days", "Weeks", "Months", "Years"};
                        typePicker.setDisplayedValues(types);
                        countToRepeat = Integer.valueOf(values[valuePicker.getValue()-1]);
                        repeatEvery = types[typePicker.getValue()-1];
                    }
                    else{
                        types = new String[]{"No repeat", "Day", "Week", "Month", "Year"};
                        typePicker.setDisplayedValues(types);
                        countToRepeat = Integer.valueOf(values[valuePicker.getValue()-1]);
                        repeatEvery = types[typePicker.getValue()-1];
                    }
                    textView1b.setText(values[numberPicker.getValue()-1] + " " + types[typePicker.getValue()-1]);
                }
            }
        });

        typePicker.setMinValue(1);
        typePicker.setMaxValue(5);
        typePicker.setWrapSelectorWheel(false);
        typePicker.setDisplayedValues(types);


        if(countToRepeat==0) {
            valuePicker.setMinValue(1);
            valuePicker.setMaxValue(1);
            valuePicker.setWrapSelectorWheel(false);
            valuePicker.setDisplayedValues(new String[]{" "});
        }
        else{
            setNumbers(valuePicker, 1, 60);
            //countToRepeat = Integer.valueOf(values[valuePicker.getValue()-1]);
            valuePicker.setValue(countToRepeat);
            for (int i=0; i<types.length; i++) {
                Log.v("strzelam", types[i] + " " + repeatEvery);
                if (types[i].equals(repeatEvery)) {
                    typePicker.setValue(i+1);
                    break;
                }

            }
        }

        typePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                if(typePicker.getValue()==1){
                    //valuePicker.setEnabled(false);
                    setEmpty(valuePicker);
                    countToRepeat = 0;
                    repeatEvery = types[typePicker.getValue()-1];
                    textView1b.setText("No repeat");
                }
                else{
                    setNumbers(valuePicker, 1, 60);
                    countToRepeat = Integer.valueOf(values[valuePicker.getValue()-1]);
                    repeatEvery = types[typePicker.getValue()-1];
                    textView1b.setText(countToRepeat + " " + repeatEvery);

                }
            }
        });
    }


    public void setArgs(int countToRepeat, String repeatEvery, long repeatEndDateInMillis){
        Bundle args = new Bundle();
        args.putInt("countToRepeat", countToRepeat);
        args.putString("repeatEvery", repeatEvery);
        args.putLong("repeatEndDateInMillis", repeatEndDateInMillis);
        args.putInt("pickerType", 1);
        this.setArguments(args);
    }
    public void setArgs(int notificationHour, int notificationMinute, int countToRemind, String remindEvery){
        Bundle args = new Bundle();
        args.putInt("notificationHour", notificationHour);
        args.putInt("notificationMinute", notificationMinute);
        args.putInt("countToRemind", countToRemind);
        args.putString("remindEvery", remindEvery);
        args.putInt("pickerType", 2);
        this.setArguments(args);
    }

    private void getArgs(){
        pickerType = getArguments().getInt("pickerType", 0);
        if (pickerType==1) {
            repeatEndDate = new Date();
            repeatEndDate.setTime(getArguments().getLong("repeatEndDateInMillis", 0));
            countToRepeat = getArguments().getInt("countToRepeat", 0);
            repeatEvery = getArguments().getString("repeatEvery", "No repeat");
        }
        else if (pickerType==2){
            notificationHour = getArguments().getInt("notificationHour", 10);
            notificationMinute = getArguments().getInt("notificationMinute", 0);
            countToRemind = getArguments().getInt("countToRemind", 1);
            remindEvery = getArguments().getString("remindEvery", "Day");

        }
    }

    private void showDatePickerDialog(View view, final TextView textView) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(repeatEndDate);

        DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(),
                0,
                dateSetListener(textView),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            datePickerDialog.getDatePicker().setFirstDayOfWeek(2);
        }
        datePickerDialog.show();
    }

    private DatePickerDialog.OnDateSetListener dateSetListener(final TextView textView){
        DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                repeatEndDate = getDate(year, month, day);
                textView.setText(getDateToString(getDate(year, month, day)));
            }
        };
        return onDateSetListener;
    }

    private Date getDate(int year, int month, int day){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        resetTime(calendar);
        return calendar.getTime();
    }

    private void resetTime(Calendar calendar){
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private String getDateToString(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String dateString = simpleDateFormat.format(date);
        return dateString;
    }

    private void setEmpty(NumberPicker numberPicker){
        String[] strings = new String[]{" "};
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(1);
        numberPicker.setDisplayedValues(strings);
        numberPicker.setWrapSelectorWheel(false);
    }

    private void setNumbers(NumberPicker numberPicker, int minVal, int maxVal){
        values = new String[maxVal-minVal+1];
        int firstVal = minVal;
        for(int i=0; i<values.length; i++){
            values[i] = Integer.toString(firstVal);
            firstVal++;
        }
        numberPicker.setDisplayedValues(values);
        numberPicker.setMinValue(minVal);
        numberPicker.setMaxValue(maxVal);
        numberPicker.setWrapSelectorWheel(false);
        //numberPicker.setDisplayedValues(values);
    }

    private void sendParams(){
        Intent intent = new Intent(getContext(), FragmentDoublePicker.class);
        if(pickerType==1) {
            intent.putExtra("countToRepeat", countToRepeat);
            intent.putExtra("repeatEvery", repeatEvery);
            intent.putExtra("repeatEndDate", repeatEndDate);
        }
        else if(pickerType==2){
            intent.putExtra("notificationHour", notificationHour);
            intent.putExtra("notificationMinute", notificationMinute);
            intent.putExtra("countToRemind", countToRemind);
            intent.putExtra("remindEvery", remindEvery);
        }
        getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_OK, intent);
        getFragmentManager().popBackStack();
    }


    private void initBackButton(View view){
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener( new View.OnKeyListener()
        {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event )
            {
                if( keyCode == KeyEvent.KEYCODE_BACK )
                {
                    sendParams();
                    return true;
                }
                return false;
            }
        } );
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == TimePickerFragment.FRAGMENT_CODE) {
                notificationHour = (int) data.getSerializableExtra("hour");
                notificationMinute = (int) data.getSerializableExtra("minute");
                String zero = "";
                if (notificationMinute < 10)
                    zero = "0";

                this.textView2b.setText(notificationHour + ":" + zero + notificationMinute);
            }
        }
    }


}
