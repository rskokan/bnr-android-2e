package com.example.radek.criminalintent;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by radek on 10/01/16.
 */
public class DatePickerFragment extends DialogFragment {

    public static final String TAG = "DialogDate";
    public static final String EXTRA_DATE = "criminalintent.date";
    private static final String ARG_DATE = "date";

    public static DatePickerFragment newInstance(Date date) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.setArguments(args);
        return datePickerFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Date date = (Date) getArguments().getSerializable(ARG_DATE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_date, null);
        final DatePicker datePicker = (DatePicker) v.findViewById(R.id.dialog_date_date_picker);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        Log.d(TAG, "Initializing datePicker: year=" + year + ", month=" + month + ", day=" + day);
        datePicker.init(year, month, day, null);
        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.date_picker_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int year = datePicker.getYear();
                        int month = datePicker.getMonth();
                        int day = datePicker.getDayOfMonth();
                        Log.d(TAG, "Selected date: year=" + year + ", month=" + month + ", day=" + day);
                        Calendar selectedCal = GregorianCalendar.getInstance();
                        selectedCal.set(year, month, day);
                        sendResult(Activity.RESULT_OK, selectedCal.getTime());
                    }
                })
                .create();
    }

    private void sendResult(int resultCode, Date date) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent i = new Intent();
        i.putExtra(EXTRA_DATE, date);
        Log.d(TAG, "sendResult() date=" + date.toString());
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
    }
}
