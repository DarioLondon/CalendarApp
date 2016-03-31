package com.darioguida.calendarapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

/**
 * Created by Dario on 31/03/2016.
 */
public class EditEventActivity extends Activity {

    public final static String _ID = "id";
    public final static String _TITLE = "title";
    public final static String _DATE = "date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_event_layout);

        String t = getIntent().getStringExtra(_TITLE);

        EditText title = (EditText) findViewById(R.id.edit_field_title);
        EditText description = (EditText) findViewById(R.id.edit_field_desc);
        title.setText(t);
        description.setText(_DATE);
    }


}
