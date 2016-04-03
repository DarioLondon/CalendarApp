package com.darioguida.calendarapp;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;


/**
 * Created by Dario on 02/04/2016.
 */
public class DeleteActivity extends AppCompatActivity {

    public static final String DATE_ = "date";
    ArrayList<String> ids = new ArrayList<>();
    private ListView list;
    private ListAdapter mAdapter;
    private Cursor data;
    private String date;
    private Button deleteAll;
    private EditText eventNumber;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.cancel_event_layout);
        date = getIntent().getStringExtra(DATE_);


        showList();
        final Button deleteAllButton = (Button) findViewById(R.id.deleteAll);
        final Button deleteButton = (Button) findViewById(R.id.buttonSingle);
        final EditText numberOfItem = (EditText) findViewById(R.id.eventToDelete);
        /*if (numberOfItem != null) {
            final int val=Integer.parseInt(numberOfItem.getText().toString());
        }*/

        if (deleteAllButton != null) {
            deleteAllButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAlertConfirmation();


                }
            });
        }
        if (deleteButton != null) {
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAlertConfirmationSingle();

                }
            });
        }

    }

    public void showList() {

        list = (ListView) findViewById(R.id.listView);
        final Button deleteItem = (Button) findViewById(R.id.deleteSingleButton);
        if (deleteItem != null) {
            deleteItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteSingle(1);
                    System.out.println("test");
                }
            });
        }
        data = new EventsDbHelper(getApplicationContext()).get(date);
        ArrayList<String> l = new ArrayList<>();
        if (data.moveToFirst()) {
            if (data != null && data.getCount() > 0)
                do {

                    l.add(data.getString(data.getColumnIndex(EventDatabase.Events.COLUMN_NAME_TIME)) + " | " + data.getString(data.getColumnIndex(EventDatabase.Events.COLUMN_NAME_TITLE)) + "\n" + data.getString(data.getColumnIndex(EventDatabase.Events.COLUMN_NAME_DESCRIPTION)));
                    ids.add(data.getString(data.getColumnIndex(EventDatabase.Events.COLUMN_NAME_ENTRY_ID)));
                } while (data.moveToNext());


        }
        String[] from = {EventDatabase.Events.COLUMN_NAME_TITLE, EventDatabase.Events.COLUMN_NAME_DESCRIPTION, EventDatabase.Events.COLUMN_NAME_TIME};

        int[] to = {R.id.titleLabel, R.id.descLabel, R.id.timeLabel};
        mAdapter = new SimpleCursorAdapter(DeleteActivity.this,
                R.layout.row_layout, data, from, to, 0);

        list.setAdapter(mAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                deleteSingle(position);
                System.out.println("test6666");
            }
        });


    }


    public void deleteSingle(int n) {
        String val = ids.get(n);
        new EventsDbHelper(getApplicationContext()).deleteSingle(val);
    }

    public void deleteAll(String date) {

        new EventsDbHelper(getApplicationContext()).deleteAll(date);
    }

    public void showAlertConfirmation() {
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(DeleteActivity.this);
        builderSingle.setCancelable(true);
        builderSingle.setMessage("Are you sure you want to delete all events ?");
        builderSingle.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAll(date);
                dialog.dismiss();
                showList();
            }
        });
        builderSingle.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

            }
        });
        final AlertDialog alert = builderSingle.create();
        alert.show();
    }

    public void showAlertConfirmationSingle() {
    }

}
