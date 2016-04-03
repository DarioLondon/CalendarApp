package com.darioguida.calendarapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Dario on 03/04/2016.
 */
public class ViewEvent extends Activity {
    public final static String DATE = "date";
    public final static String TITLE = "title";
    public final static String TIME = "time";
    public final static String DESCRIPTION = "description";

    @Override
    public void onCreate(Bundle savedIstanceState) {
        super.onCreate(savedIstanceState);
        setContentView(R.layout.view_layout);

        final String date = getIntent().getStringExtra(DATE);
        final String title = getIntent().getStringExtra(TITLE);
        final String time = getIntent().getStringExtra(TIME);
        final String desc = getIntent().getStringExtra(DESCRIPTION);


        final Button close = (Button) findViewById(R.id.closeButton);
        if (close != null) {
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(ViewEvent.this, CalendarActivity.class);
                    ViewEvent.this.startActivity(i);
                }
            });
        }
        final TextView titleLabel = (TextView) findViewById(R.id.titleText);
        final TextView timeLabel = (TextView) findViewById(R.id.timeText);
        final TextView dateLabel = (TextView) findViewById(R.id.dateText);
        final TextView descLabel = (TextView) findViewById(R.id.desText);

        titleLabel.setText(title);
        descLabel.setText(desc);
        timeLabel.setText(time);
        dateLabel.setText(date);

    }


}
// TODO my key  gB5tplzjJee641pjQDmL