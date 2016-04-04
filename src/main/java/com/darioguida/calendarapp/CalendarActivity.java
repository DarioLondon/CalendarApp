package com.darioguida.calendarapp;


import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;


public class CalendarActivity extends AppCompatActivity {
    public String today = todayDate();
    public String time = timeNow();
    public TextView titleView;
    public TextView descView;
    public TimePicker timePicker;
    private Cursor data;
    private EditText searchBox;
    private ImageButton searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_view);

        CalendarView calendar = (CalendarView) findViewById(R.id.calendarView);


        Button newEvent = (Button) findViewById(R.id.newEvent);
        Button viewList = (Button) findViewById(R.id.viewEvent);
        Button deleteEvent = (Button) findViewById(R.id.deleteButton);

        ImageButton search = (ImageButton) findViewById(R.id.searchButton);

        if (search != null) {
            search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent i = new Intent(getApplication(), SearchActivity.class);
                    CalendarActivity.this.startActivity(i);

                }
            });
        }

        if (deleteEvent != null) {

            deleteEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CalendarActivity.this, DeleteActivity.class);
                    intent.putExtra(DeleteActivity.DATE_, today);
                    CalendarActivity.this.startActivity(intent);
                }
            });

        }

        if (calendar != null) {
            calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                @Override
                public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {

                    today = dayOfMonth + "/" + (month + 1) + "/" + year;
                    System.out.println(today);
                }
            });
        }
        if (newEvent != null) {
            newEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(CalendarActivity.this, SuggestionActivity.class);
                    i.putExtra(SuggestionActivity.DATE, today);
                    CalendarActivity.this.startActivity(i);
                    //open(v);

                }
            });
        }

        if (viewList != null) {
            viewList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new GetData(today).execute();

                }
            });
        }


    }


    //When the user click on add this function will create a AlertDialog with a form to insert data
    public void open(View view) {

        View layoutAlertBox = View.inflate(this, R.layout.new_appointment_layout, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);


        titleView = (TextView) layoutAlertBox.findViewById(R.id.edit_title);
        descView = (TextView) layoutAlertBox.findViewById(R.id.description);
        timePicker = (TimePicker) layoutAlertBox.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                time = hourOfDay + ":" + minute;
            }
        });
        alertDialogBuilder.setView(layoutAlertBox);
        alertDialogBuilder.setTitle("Add Event :");
        alertDialogBuilder.setPositiveButton("Save", null);
        alertDialogBuilder.setNegativeButton("Back", null);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {


                Button positiveB = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeB = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                positiveB.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {


                        String title = titleView.getText().toString();
                        String description = descView.getText().toString();
                        String date = today;
                        String id = title + "@" + date;
                        String _time = time;
                        HashMap<String, String> data;
                        data = new HashMap<>();
                        data.put("id", id);
                        data.put("title", title);
                        data.put("time", _time);
                        data.put("date", date);
                        data.put("content", description);
                        new WriteData(data, alertDialog).execute();


                    }

                });


                negativeB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
            }
        });
        alertDialog.show();
    }

    // get today Date
    public String todayDate() {

        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("d/M/yyyy");
        String formattedDate = df.format(c.getTime());
        System.out.println(formattedDate);
        return formattedDate;
    }

    //Thread to get data from the DataBase

    //get timeNow
    public String timeNow() {
        Date date = new Date();

        String time = date.getHours() + ":" + date.getMinutes();
        return time;
    }


    //Thread to write data in the DataBase


    public class WriteData extends AsyncTask<Object, Object, Boolean> {


        HashMap<String, String> data;
        AlertDialog alertDialog;

        WriteData(HashMap<String, String> _data, AlertDialog _alertDialog) {
            data = _data;
            alertDialog = _alertDialog;

        }

        @Override
        protected Boolean doInBackground(Object... params) {
            EventsDbHelper db = new EventsDbHelper(CalendarActivity.this.getApplicationContext());
            Boolean result = false;


            if (db.checkDuplicateTitle(data.get("title"), data.get("date"))) {

                result = true;
                db.close();

            } else {

                db.insert(data);
                result = false;

            }
            db.close();
            return result;
        }

        @Override
        protected void onPostExecute(Boolean r) {


            String result;
            if (!r) {
                result = "Event Saved !";
                //Toast.makeText(CalendarActivity.this, result, Toast.LENGTH_LONG).show();
                alertDialog.dismiss();

            } else {
                result = "The title you typed has been already chosen ";
                //Toast.makeText(CalendarActivity.this, result, Toast.LENGTH_LONG).show();
            }
        }
    }

    public class GetData extends AsyncTask<Object, Objects, ArrayList<String[]>> {


        String day;

        GetData(String date) {
            this.day = date;

        }

        @Override
        protected ArrayList<String[]> doInBackground(Object... params) {
            EventsDbHelper db = new EventsDbHelper(getApplicationContext());
            ArrayList<String> list = new ArrayList<>();
            ArrayList<String[]> res = new ArrayList<>();
            data = db.get(day);

            if (data.moveToFirst()) {
                if (data != null && data.getCount() > 0)
                    do {
                        list.add(data.getString(data.getColumnIndex(EventDatabase.Events.COLUMN_NAME_ENTRY_ID)) + "_" + data.getString(data.getColumnIndex(EventDatabase.Events.COLUMN_NAME_TITLE)) + "_" + data.getString(data.getColumnIndex(EventDatabase.Events.COLUMN_NAME_TIME)) + "_" + data.getString(data.getColumnIndex(EventDatabase.Events.COLUMN_NAME_DATE)) + "_" + data.getString(data.getColumnIndex(EventDatabase.Events.COLUMN_NAME_DESCRIPTION)) + "_" + " ");

                    } while (data.moveToNext());


                for (String l : list) {
                    String[] s = l.split("_");
                    System.out.println(s[0]);
                    System.out.println(s[1]);
                    res.add(s);

                }

                data.close();
            }
            return res;
        }

        protected void onPostExecute(ArrayList<String[]> _res) {
            final AlertDialog.Builder builderSingle = new AlertDialog.Builder(CalendarActivity.this);
            builderSingle.setCancelable(true);
            final ArrayList<String[]> res = _res;
            builderSingle.setTitle("Select Event");

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    CalendarActivity.this,
                    android.R.layout.select_dialog_singlechoice);
            int counter = 1;
            for (String[] el : res) {
                arrayAdapter.add(counter + ")" + el[2] + "  " + el[1]);
                System.out.println(el[1]);
                counter++;
            }


            builderSingle.setPositiveButton(
                    "BACK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //dialog.dismiss();
                        }
                    });


            builderSingle.setAdapter(
                    arrayAdapter,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {


                            String[] data = res.get(which);

                            Intent intent = new Intent(CalendarActivity.this, EditEventActivity.class);
                            intent.putExtra(EditEventActivity._ID, data[0]);
                            intent.putExtra(EditEventActivity._TITLE, data[1]);
                            intent.putExtra(EditEventActivity._TIME, data[2]);
                            intent.putExtra(EditEventActivity._DATE, data[3]);
                            intent.putExtra(EditEventActivity._DESCRIPTION, data[4]);
                            CalendarActivity.this.startActivity(intent);

                        }
                    });

            final AlertDialog alert = builderSingle.create();
            alert.show();
            // System.out.println(today);
            //ShowAlertDialogWithListview(res);


        }

    }


}

