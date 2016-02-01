package com.flywolf.familytree;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by flywolf on 3/4/15.
 */
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {
    private DbWorker.Relative openInDialog;
    private View dialogView;
    private TextView leafBirthday;

    public DatePickerFragment(DbWorker.Relative openInDialog, View dialogView, TextView leafBirthday) {
        this.openInDialog = openInDialog;
        this.dialogView = dialogView;
        this.leafBirthday = leafBirthday;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = new GregorianCalendar();
        if (openInDialog.getBirthday() != null)
            c.setTime(openInDialog.getBirthday());
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        GregorianCalendar gk = new GregorianCalendar(year, month,
                day);
        // Log.d(LOG_TAG, "goToDate = " + gk.getTime());
        gk.add(Calendar.DATE, 1);
        openInDialog.setBirthday(gk.getTime());
        FamilyTree.dbWorker.saveRelative(openInDialog);
        TextView text = (TextView) dialogView
                .findViewById(R.id.big_photo_text);
        text.setText(openInDialog.getName());
        text = (TextView) dialogView.findViewById(R.id.birthday);
        text.setText(LeafFrame.dateFormat(openInDialog.getBirthday()));

        int id = getResources()
                .getIdentifier(
                        FamilyTree.PACKAGE_NAME + ":id/leaf"
                                + openInDialog.getLeafId() + "Birthday",
                        null, null);
        // text = (TextView) findViewById(id);
        leafBirthday.setVisibility(android.view.View.VISIBLE);
        leafBirthday.setText(LeafFrame.dateFormatShort(openInDialog.getBirthday()));
    }
}

