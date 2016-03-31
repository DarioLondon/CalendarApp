package com.darioguida.calendarapp;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;


public class CalendarActivity extends AppCompatActivity {

    String today = todayDate();
    String time = timeNow();
    private TextView titleView;
    private TextView descView;
    private TimePicker timePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_view);

        CalendarView calendar = (CalendarView) findViewById(R.id.calendarView);


        Button newEvent = (Button) findViewById(R.id.newEvent);
        Button viewList = (Button) findViewById(R.id.viewEvent);
        Button moveEvent = (Button) findViewById(R.id.moveEvent);



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

                    open(v);

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
                        String id = title + "_" + date;
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

        SimpleDateFormat df = new SimpleDateFormat("dd/M/yyyy");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    //Thread to get data from the DataBase

    //get timeNow
    public String timeNow() {
        Date date = new Date();

        String time = date.getHours() + ":" + date.getMinutes();
        return time;
    }

    public void ShowAlertDialogWithListview(ArrayList<String[]> listItems) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(CalendarActivity.this);
        ArrayList<String[]> res = listItems;
        builderSingle.setTitle("Select Event");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                CalendarActivity.this,
                android.R.layout.select_dialog_singlechoice);
        if (res.size() > 0) {
            for (String[] el : res) {
                arrayAdapter.add(el[0]);

            }
        } else {
            builderSingle.setMessage("You have not Appointments today");
        }
        builderSingle.setNegativeButton(
                "Back",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);
                        Intent intent = new Intent(CalendarActivity.this, EditEventActivity.class);
                        intent.putExtra(EditEventActivity._ID, String.valueOf(which + 1));
                        intent.putExtra(EditEventActivity._TITLE, strName);
                        intent.putExtra(EditEventActivity._DATE, today);
                        CalendarActivity.this.startActivity(intent);

                    }
                });
        builderSingle.show();
    }

    //Thread to write data in the DataBase
    private class WriteData extends AsyncTask<Object, Object, Boolean> {
        HashMap<String, String> data;
        AlertDialog alertDialog;

        WriteData(HashMap<String, String> _data, AlertDialog _alertDialog) {
            data = _data;
            alertDialog = _alertDialog;
        }

        @Override
        protected Boolean doInBackground(Object... params) {

            Boolean result = false;

            boolean titleIsEqual = new EventsDbHelper(getApplicationContext()).checkDuplicateTitle(data.get("title"), data.get("date"));
            if (titleIsEqual) {
                result = true;
            } else {

                new EventsDbHelper(getApplicationContext()).insert(data);
                result = false;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean r) {
            String result;
            if (!r) {
                result = "Event Saved !";
                Toast.makeText(CalendarActivity.this, result, Toast.LENGTH_LONG).show();
                alertDialog.dismiss();
            } else {
                result = "The title you typed has been already chosen ";
                Toast.makeText(CalendarActivity.this, result, Toast.LENGTH_LONG).show();
            }
        }
    }

    private class GetData extends AsyncTask<Object, Objects, ArrayList<String[]>> {
        String day;

        GetData(String date) {
            this.day = date;
        }

        @Override
        protected ArrayList<String[]> doInBackground(Object... params) {

            ArrayList<String> data = new EventsDbHelper(getApplicationContext()).get(day);
            ArrayList<String[]> list = new ArrayList<>();

            for (String l : data) {
                String[] s = l.split("_");
                list.add(s);

            }
            return list;
        }

        protected void onPostExecute(ArrayList<String[]> res) {
            ShowAlertDialogWithListview(res);
            System.out.println(today);
        }
    }


}