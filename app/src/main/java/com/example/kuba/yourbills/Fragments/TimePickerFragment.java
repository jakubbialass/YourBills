package com.example.kuba.yourbills.Fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;

import com.example.kuba.yourbills.R;

import java.util.Calendar;

import androidx.fragment.app.DialogFragment;

import static android.app.Activity.RESULT_OK;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {
    public static int FRAGMENT_CODE = 2;
    public static String FRAGMENT_TAG = "Hour picker";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it

        return new TimePickerDialog(getActivity(),this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        Intent intent = new Intent(getContext(), FragmentNewBill.class);
        intent.putExtra("hour", hourOfDay);
        intent.putExtra("minute", minute);
        Log.v("requestcode ", Integer.toString(getTargetRequestCode()));
        getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_OK, intent );
    }
}
