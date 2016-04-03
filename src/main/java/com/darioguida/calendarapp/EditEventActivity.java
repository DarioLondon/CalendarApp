package com.darioguida.calendarapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

/**
 * Created by Dario on 31/03/2016.
 */
public class EditEventActivity extends Activity {

    public final static String _ID = "id";
    public final static String _TITLE = "title";
    public final static String _DATE = "date";
    public final static String _TIME = "time";
    public final static String _DESCRIPTION = "description";
    int minute;
    int hour;
    private String _id;
    private EditText title;
    private EditText description;
    private TimePicker timePicker;
    private String eventDate;
    private int _day;
    private int _month;
    private int _year;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_event_layout);

        _id = getIntent().getStringExtra(_ID);
        String eventTitle = getIntent().getStringExtra(_TITLE);
        eventDate = getIntent().getStringExtra(_DATE);
        String eventDescription = getIntent().getStringExtra(_DESCRIPTION);
        String eventTime = getIntent().getStringExtra(_TIME);
        String[] time = eventTime.split(":");
        hour = Integer.parseInt(time[0]);
        minute = Integer.parseInt(time[1]);


        title = (EditText) findViewById(R.id.edit_field_title);
        description = (EditText) findViewById(R.id.edit_field_desc);
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        final Button save = (Button) findViewById(R.id.edit_save);
        final CheckBox checkBoxDate = (CheckBox) findViewById(R.id.checkBoxDate);
        checkBoxDate.setChecked(false);
        checkBoxDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBoxDate.setChecked(true);
                System.out.println("checked");
                showCalendar(v);
            }
        });
        timePicker.setHour(hour);
        timePicker.setMinute(minute);
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int _minute) {
                hour = hourOfDay;
                minute = _minute;
            }
        });
        title.setText(eventTitle);
        description.setText(eventDescription);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new UpdateEvent(title.getText().toString(), description.getText().toString()).execute();

            }
        });
    }

    public void showCalendar(View view) {

        View layoutAlertBox = View.inflate(this, R.layout.calendar_layout, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(layoutAlertBox);
        alertDialogBuilder.setTitle("Move Date ");
        alertDialogBuilder.setPositiveButton("Save", null);
        alertDialogBuilder.setNegativeButton("Back", null);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        final CalendarView calendar = (CalendarView) layoutAlertBox.findViewById(R.id.calendarView2);
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {


            @Override
            public void onShow(DialogInterface dialog) {

                Button move = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button back = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                move.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(EditEventActivity.this, "The event has been Moved", Toast.LENGTH_LONG).show();
                        alertDialog.dismiss();
                    }
                });
                back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();

                    }
                });


            }


        });
        setCalendarDate(calendar);
        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                eventDate = dayOfMonth + "/" + (month + 1) + "/" + year;

            }
        });
        alertDialog.show();
    }

    private void setCalendarDate(CalendarView cal) {
        String date = eventDate;
        System.out.println(eventDate);
        String parts[] = date.split("/");
        _day = Integer.parseInt(parts[0]);
        _month = Integer.parseInt(parts[1]) - 1;
        _year = Integer.parseInt(parts[2]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, _year);
        calendar.set(Calendar.MONTH, _month);
        calendar.set(Calendar.DAY_OF_MONTH, _day);

        long milliTime = calendar.getTimeInMillis();
        System.out.println(milliTime);
        cal.setDate(milliTime);
    }

    public class UpdateEvent extends AsyncTask<Object, Objects, Boolean> {

        String new_time;
        String new_title;
        String new_description;
        String new_date;
        String id;


        UpdateEvent(String title, String description) {
            this.id = _id;
            this.new_title = title;
            this.new_description = description;
            this.new_date = eventDate;
            this.new_time = hour + ":" + minute;

        }

        @Override
        protected Boolean doInBackground(Object... params) {

            EventsDbHelper db = new EventsDbHelper(getApplicationContext());
            Boolean result;
            // boolean checkTitle = db.checkDuplicateTitle(this.new_title, this.new_date);

           /* if (checkTitle) {
                System.out.println(checkTitle);
                result = true;

            } else {*/


            HashMap<String, String> data = new HashMap<>();
            data.put("id", id);
            System.out.println(id);
            data.put("title", new_title);
            System.out.println(new_title);
            data.put("time", new_time);
            System.out.println(new_time);
            data.put("date", new_date);
            System.out.println(new_date);
            data.put("content", new_description);
            System.out.println(new_description);
            db.updateAppoinment(data);

            result = false;

            //}
            db.close();
            return result;
        }

        protected void onPostExecute(Boolean res) {


            if (res) {
                System.out.println("second --->" + res);
                Toast.makeText(EditEventActivity.this, "The title has been already used", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(EditEventActivity.this, "Event Updated", Toast.LENGTH_LONG).show();

                EditEventActivity.this.finish();
            }


        }
    }
}