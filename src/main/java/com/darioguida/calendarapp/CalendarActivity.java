package com.darioguida.calendarapp;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CalendarView;



public class CalendarActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_view);

        CalendarView calendar = (CalendarView) findViewById(R.id.calendarView);
        //--Button newEvent=(Button)findViewById(R.id.newEvent);
        if (calendar != null) {
            calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                    @Override
                    public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                        Log.d("Test-------", "----Test-----");
                        System.out.println(view.getDate());
                    }
                });
            }


    }



}
