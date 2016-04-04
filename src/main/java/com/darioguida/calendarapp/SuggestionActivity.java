package com.darioguida.calendarapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by Dario on 03/04/2016.
 */
public class SuggestionActivity extends Activity {
    public final static String DATE = "date";
    public String time = timeNow();
    public TextView titleView;
    public TextView descView;
    public TimePicker timePicker;
    AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            String replace = (String) parent.getItemAtPosition(position);
            int startSelection = titleView.getSelectionStart();
            int endSelection = titleView.getSelectionEnd();
            int startSelection1 = descView.getSelectionStart();
            int endSelection1 = descView.getSelectionEnd();
            String selectedText = titleView.getText().toString().substring(startSelection, endSelection);
            String selectedText1 = descView.getText().toString().substring(startSelection1, endSelection1);
            if (selectedText.length() == 0 && selectedText1.length() > 0) {
                descView.setText(replace);
            }
            if (selectedText.length() > 0 && selectedText1.length() == 0) {
                titleView.setText(replace);
            }

        }
    };
    private EditText origText;
    private ListView suggList;
    private Button save;
    private Button searchButton;
    private Handler guiThread;
    private ExecutorService suggThread;
    private Runnable updateTask;
    private Future<?> suggPending;
    private List<String> items;
    private ArrayAdapter<String> adapter;
    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            initThreading();
            queueUpdate(1000);
        }
    };
    private String today;
    private Button search;
    private String timestamp = timeInt();
    private android.app.AlertDialog box;
    View.OnClickListener searchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (findWord().length() == 0) {
                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SuggestionActivity.this);
                builder.setTitle("");
                builder.setMessage("No text selected");
                builder.setCancelable(false);
                builder.setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        box.dismiss();
                    }
                });
                box = builder.show();
            } else {
                initThreading1(findWord());
                queueUpdate(1000);
            }


        }
    };
    private View.OnClickListener saveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            titleView = (EditText) findViewById(R.id.edit_title);
            descView = (EditText) findViewById(R.id.description);
            String title = titleView.getText().toString().trim();
            String description = descView.getText().toString().trim();
            String date = today;
            String id = title + "@" + date;
            String _time = time;
            String _timestamp = timestamp;
            HashMap<String, String> data;
            data = new HashMap<>();
            data.put("id", id);
            data.put("title", title);
            data.put("time", _time);
            data.put("date", date);
            data.put("content", description);
            data.put("timestamp", timestamp);

            new WriteData(data).execute();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.final_main);

        findViews();
        setAdapters();
        today = getIntent().getStringExtra(DATE);

        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                time = hourOfDay + ":" + minute;
                timestamp = hourOfDay + "" + minute;
            }
        });
        searchButton.setOnClickListener(listener);
        save.setOnClickListener(saveListener);
        search.setOnClickListener(searchListener);
        suggList.setOnItemClickListener(clickListener);
    }

    /** @Override
    protected void onDestroy() {
        // Terminate extra threads here
        suggThread.shutdownNow();
        super.onDestroy();
    } */

    /**
     * Get a handle to all user interface elements
     */
    private void findViews() {
        origText = (EditText) findViewById(R.id.original_text);
        suggList = (ListView) findViewById(R.id.result_list);
        searchButton = (Button) findViewById(R.id.buttonSearch);
        titleView = (TextView) findViewById(R.id.edit_title);
        descView = (TextView) findViewById(R.id.description);
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        save = (Button) findViewById(R.id.save);
        search = (Button) findViewById(R.id.button);


    }

    /**
     * Setup user interface event handlers
     */

    /**
     * Set up adapter for list view.
     */
    private void setAdapters() {
        items = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, items);
        suggList.setAdapter(adapter);
    }

    /**
     * Initialize multi-threading. There are two threads: 1) The main
     * graphical user interface thread already started by Android,
     * and 2) The suggest thread, which we start using an executor.
     */
    private void initThreading() {
        guiThread = new Handler();
        suggThread = Executors.newSingleThreadExecutor();

        // This task gets suggestions and updates the screen
        updateTask = new Runnable() {
            public void run() {
                // Get text to suggest
                String original = origText.getText().toString().trim();

                // Cancel previous suggestion if there was one
                if (suggPending != null)
                    suggPending.cancel(true);

                // Check to make sure there is text to work on
                if (original.length() != 0) {
                    // Let user know we're doing something
                    setText(R.string.working);

                    // Begin suggestion now but don't wait for it
                    try {
                        SuggestTask suggestTask = new SuggestTask(
                                SuggestionActivity.this, // reference to activity
                                original // original text
                        );
                        System.out.println("works");
                        suggPending = suggThread.submit(suggestTask);
                    } catch (RejectedExecutionException e) {
                        // Unable to start new task
                        setText(R.string.error);
                    }
                }
            }
        };
    }


    private void initThreading1(final String _word) {
        guiThread = new Handler();
        suggThread = Executors.newSingleThreadExecutor();

        // This task gets suggestions and updates the screen
        updateTask = new Runnable() {
            public void run() {
                // Get text to suggest
                String original = _word;

                // Cancel previous suggestion if there was one
                if (suggPending != null)
                    suggPending.cancel(true);

                // Check to make sure there is text to work on
                if (original.length() != 0) {
                    // Let user know we're doing something
                    setText(R.string.working);

                    // Begin suggestion now but don't wait for it
                    try {
                        SuggestTask suggestTask = new SuggestTask(
                                SuggestionActivity.this, // reference to activity
                                original // original text
                        );

                        suggPending = suggThread.submit(suggestTask);
                    } catch (RejectedExecutionException e) {
                        // Unable to start new task
                        setText(R.string.error);
                    }
                }
            }
        };
    }


    /**
     * Request an update to start after a short delay
     */
    private void queueUpdate(long delayMillis) {
        // Cancel previous update if it hasn't started yet
        guiThread.removeCallbacks(updateTask);
        // Start an update if nothing happens after a few milliseconds
        guiThread.postDelayed(updateTask, delayMillis);
    }

    /**
     * Modify list on the screen (called from another thread)
     */
    public void setSuggestions(List<String> suggestions) {
        guiSetList(suggList, suggestions);
    }

    /**
     * All changes to the GUI must be done in the GUI thread
     */
    private void guiSetList(final ListView view,
                            final List<String> list) {
        guiThread.post(new Runnable() {
            public void run() {
                setList(list);
            }

        });
    }

    /**
     * Display a message
     */
    private void setText(int id) {
        adapter.clear();
        adapter.add(getResources().getString(id));
    }

    /**
     * Display a list
     */
    private void setList(List<String> list) {
        adapter.clear();
        adapter.addAll(list);
    }

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

    public String timeInt() {
        Date date = new Date();
        return date.getHours() + "" + date.getMinutes();
    }

    public String findWord() {
        String value = "";
        if (titleView != null && descView != null) {
            int startSelection = titleView.getSelectionStart();
            int endSelection = titleView.getSelectionEnd();
            int startSelection1 = descView.getSelectionStart();
            int endSelection1 = descView.getSelectionEnd();
            String selectedText = titleView.getText().toString().substring(startSelection, endSelection);
            String selectedText1 = descView.getText().toString().substring(startSelection1, endSelection1);
            if (selectedText.length() == 0 && selectedText1.length() > 0) {
                value = selectedText1;
            }
            if (selectedText.length() > 0 && selectedText1.length() == 0) {
                value = selectedText;
            }
        }
        return value;
    }

    //Thread to write data in the DataBase


    public class WriteData extends AsyncTask<Object, Object, Boolean> {


        HashMap<String, String> data;

        WriteData(HashMap<String, String> _data) {
            this.data = _data;


        }

        @Override
        protected Boolean doInBackground(Object... params) {
            EventsDbHelper db = new EventsDbHelper(SuggestionActivity.this.getApplicationContext());
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
                Toast.makeText(SuggestionActivity.this, result, Toast.LENGTH_LONG).show();


            } else {
                result = "The title you typed has been already chosen ";
                Toast.makeText(SuggestionActivity.this, result, Toast.LENGTH_LONG).show();
            }
        }
    }
}

