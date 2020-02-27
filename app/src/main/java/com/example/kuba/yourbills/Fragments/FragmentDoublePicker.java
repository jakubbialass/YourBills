package com.example.kuba.yourbills.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.kuba.yourbills.R;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;

public class FragmentDoublePicker extends Fragment {


    private NumberPicker valuePicker, typePicker;
    private TextView textView1a, textView1b, textView2a, textView2b;
    private String[] values, types;
    private RelativeLayout relativeLayoutRepeatEndDate;
    private LinearLayout pickers;
    private Date repeatEndDate;
    private int countToRepeat;
    private String repeatEvery;
    public static int FRAGMENT_CODE = 23485;

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

        if(countToRepeat==0)
            textView1b.setText(repeatEvery);
        else
            textView1b.setText(countToRepeat + " " + repeatEvery);


        pickers = view.findViewById(R.id.pickers);
        relativeLayoutRepeatEndDate = view.findViewById(R.id.repeat_end_date);
        relativeLayoutRepeatEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickers.setVisibility(View.GONE);

                pickers.setVisibility(View.VISIBLE);
            }
        });



        /*valuePicker.setMinValue(minVal);
        valuePicker.setMaxValue(60);
        valuePicker.setWrapSelectorWheel(false);
        valuePicker.setEnabled(false);*/

        Log.v("strzelamsetutaj", Integer.toString(countToRepeat));
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
                    setEmpty(valuePicker, values);
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


        initBackButton(view);

        return view;
    }


    public void setArgs(int countToRepeat, String repeatEvery, long repeatEndDateInMillis){
        Bundle args = new Bundle();
        args.putInt("countToRepeat", countToRepeat);
        args.putString("repeatEvery", repeatEvery);
        args.putLong("repeatEndDateInMillis", repeatEndDateInMillis);
        this.setArguments(args);
    }

    private void getArgs(){
        repeatEndDate = new Date();
        repeatEndDate.setTime(getArguments().getLong("repeatEndDateInMillis", 0));
        countToRepeat = getArguments().getInt("countToRepeat", 0);
        repeatEvery = getArguments().getString("repeatEvery", "No repeat");
    }

    private void setEmpty(NumberPicker numberPicker, String[] strings){
        strings = new String[]{" "};
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
        intent.putExtra("countToRepeat", countToRepeat);
        intent.putExtra("repeatEvery", repeatEvery);
        intent.putExtra("repeatEndDate", repeatEndDate);
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


}
