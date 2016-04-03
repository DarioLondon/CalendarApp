package com.darioguida.calendarapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Dario on 03/04/2016.
 */
public class SearchActivity extends Activity {

    @Override
    public void onCreate(Bundle savedIstanceStatus) {
        super.onCreate(savedIstanceStatus);
        setContentView(R.layout.search_layout);
        final ListView listView = (ListView) findViewById(R.id.listView2);
        final ImageButton search = (ImageButton) findViewById(R.id.searchButton);
        final EditText searchBox = (EditText) findViewById(R.id.searchBox);
        if (search != null && listView != null) {
            search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    reset(listView);
                    String search = searchBox.getText().toString().toLowerCase();
                    new Search(search, listView).execute();
                }
            });
        }

    }

    public boolean match(String text, String wordToFind) {

        String searchMe = text;
        String findMe = wordToFind;
        int searchMeLength = searchMe.length();
        int findMeLength = findMe.length();
        boolean foundIt = false;
        for (int i = 0; i <= (searchMeLength - findMeLength); i++) {
            if (searchMe.regionMatches(i, findMe, 0, findMeLength)) {
                foundIt = true;
                break;
            }
        }
        if (!foundIt) {
            foundIt = false;
        }

        return foundIt;
    }

    public void reset(ListView listView) {
        ArrayList<String> values = new ArrayList<String>();
        //put anything you want in values as start
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this,
                android.R.layout.select_dialog_singlechoice, values);
        listView.setAdapter(adapter);
    }

    public class Search extends AsyncTask<Object, Objects, ArrayAdapter<String>> {
        final ArrayList<String[]> res = new ArrayList<>();
        String text;
        ListView listView;

        Search(String textToSearch, ListView _listView) {
            this.text = textToSearch.toLowerCase();
            this.listView = _listView;
        }

        @Override
        protected ArrayAdapter doInBackground(Object... params) {
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(SearchActivity.this,
                    android.R.layout.select_dialog_singlechoice);
            ListView listView = null;
            EventsDbHelper db = new EventsDbHelper(getApplicationContext());
            ArrayList<String> list = new ArrayList<>();

            Cursor data = db.getAll();

            if (data.moveToFirst()) {
                if (data != null && data.getCount() > 0)
                    do {

                        list.add(data.getString(data.getColumnIndex(EventDatabase.Events.COLUMN_NAME_ID)) + "_" + data.getString(data.getColumnIndex(EventDatabase.Events.COLUMN_NAME_ENTRY_ID)) + "_" + data.getString(data.getColumnIndex(EventDatabase.Events.COLUMN_NAME_TITLE)) + "_" + data.getString(data.getColumnIndex(EventDatabase.Events.COLUMN_NAME_TIME)) + "_" + data.getString(data.getColumnIndex(EventDatabase.Events.COLUMN_NAME_DATE)) + "_" + data.getString(data.getColumnIndex(EventDatabase.Events.COLUMN_NAME_DESCRIPTION)) + "_" + " ");

                    } while (data.moveToNext());


                for (String l : list) {
                    String[] s = l.split("_");

                    res.add(s);
                }
                String reg = ".*" + text + ".*";
                for (String[] s : res) {

                    Pattern pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);

                    Matcher matcher1 = pattern.matcher(s[2]);
                    Matcher matcher2 = pattern.matcher(s[4]);
                    if (matcher1.matches() || matcher2.matches()) {
                        arrayAdapter.add(s[2] + "\n" + s[3] + "\n" + s[4]);
                    }
                }


                data.close();

            }


            return arrayAdapter;
        }

        protected void onPostExecute(ArrayAdapter<String> arrayAdapter) {


            listView.setAdapter(arrayAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                    String[] data = res.get(position);
                    Intent i = new Intent(SearchActivity.this, ViewEvent.class);
                    i.putExtra(ViewEvent.DATE, data[4]);
                    i.putExtra(ViewEvent.TITLE, data[2]);
                    i.putExtra(ViewEvent.TIME, data[3]);
                    i.putExtra(ViewEvent.DESCRIPTION, data[5]);
                    SearchActivity.this.startActivity(i);

                }

            });
            arrayAdapter.notifyDataSetChanged();


        }

    }
}
