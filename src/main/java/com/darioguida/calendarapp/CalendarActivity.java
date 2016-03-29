package com.darioguida.calendarapp;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.Toast;

import java.util.Calendar;


public class CalendarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_view);

        CalendarView calendar=(CalendarView)findViewById(R.id.calendarView);

      /*  calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                Log.d("Test-------","----Test-----");
                System.out.println(view.getDate());
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.about_game_dialogBox_title);
                builder.setMessage(R.string.rule_of_the_game);
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.about_game_dialogBox_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {

                    }
                });
                gameRulesDialogBox = builder.show();
            }
        });
            }
        });
*/
    }



}
