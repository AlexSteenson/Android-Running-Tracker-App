package com.example.alexsteenson.runningtracker;

import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import java.util.ArrayList;

public class ViewAllWorkouts extends AppCompatActivity {

    private static CustomListViewAdapter customAdapter;
    private String option = "all";
    private String name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_runs);

        Bundle bundle = getIntent().getExtras();
        // Get the selected option type
        if(bundle != null)
            option = bundle.getString("option");

        // If the option is name get the run name
        if(option.equals("name")){
            name = bundle.getString("name");
        }

        // Register content observer
        Handler h = new Handler();
        getContentResolver().registerContentObserver(DBProviderContract.WORKOUT_URI, true, new DBContentObserver(h));

        // Fill the ListView
        fillList(getCursor("none"));
    }

    public void fillList(Cursor c){
        // Create ListView
        ListView listView = findViewById(R.id.lvRuns);
        // Create ArrayList of workouts
        ArrayList<Workout> everyWorkout = new ArrayList<>();

        // If the query returns anything
        if (c != null && c.getCount() > 0){
            // Set indexes
            int idIndex = c.getColumnIndex(DBProviderContract._ID);
            int typeIndex = c.getColumnIndex(DBProviderContract.TYPE);
            int distanceIndex = c.getColumnIndex(DBProviderContract.DISTANCE);
            int timeIndex = c.getColumnIndex(DBProviderContract.TIME);
            int startTimeIndex = c.getColumnIndex(DBProviderContract.TIMEDATE);
            int startDateIndex = c.getColumnIndex(DBProviderContract.DATE);
            int nameIndex = c.getColumnIndex(DBProviderContract.NAME);

            // Iterate over each Workout
            while(c.moveToNext()) {
                // Get data about the workout frm the db
                int id = Integer.parseInt(c.getString(idIndex));
                String type = c.getString(typeIndex);
                float distance = Float.parseFloat(c.getString(distanceIndex));
                long time = Long.parseLong(c.getString(timeIndex));
                String startTime = c.getString(startTimeIndex);
                String startDate = c.getString(startDateIndex);
                String name = c.getString(nameIndex);
                // Create new Workout object with the data and add it to the ArrayList
                Workout workout = new Workout(id, type, distance, time, startTime, startDate, name);
                everyWorkout.add(workout);
            }
        }

        // Set the ListView adapter to the custom adapter
        customAdapter = new CustomListViewAdapter(everyWorkout,getApplicationContext());
        listView.setAdapter(customAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                // Get run at selected location
                Workout selectedFromList = (Workout) (listView.getItemAtPosition(myItemInt));
                Intent intent = new Intent(ViewAllWorkouts.this, ViewSelectedWorkout.class);
                // Create new bundle with the ID of the run
                Bundle bundle = new Bundle();
                bundle.putInt("_id", selectedFromList.get_id());
                intent.putExtras(bundle);
                // Start activity
                startActivity(intent);
            }
        });
    }

    public Cursor getCursor(String sort){
        // Get the all columns
        String[] projection = new String[] {"_id", "type", "time", "distance", "timeDate", "date", "name"};
        String selection = DBProviderContract.TYPE + " = ?";
        String[] selectionArgs = {""};
        selectionArgs[0] = option;
        String sortOrder;
        Cursor c;

        // Query depending on the option selected
        if (option.equals("all")) {
            selection = null;
            selectionArgs = null;
        }else if(option.equals("name")){
            selection = DBProviderContract.NAME + " = ?";
            selectionArgs[0] = name;
        }

        // Sort by sorting choice
        switch (sort){
            case "date":
                sortOrder = DBProviderContract.DATE + " DESC";
                break;
            case "distance":
                sortOrder = DBProviderContract.DISTANCE + " DESC";
                break;
            case "time":
                sortOrder = DBProviderContract.TIME + " ASC";
                break;
            default:
                sortOrder = null;
        }

        // Query the db
        return getContentResolver().query(DBProviderContract.WORKOUT_URI, projection, selection, selectionArgs, sortOrder);
    }

    // Sort by distance
    public void onClickSortDistance(View v){
        fillList(getCursor("distance"));
    }

    // Sort by time
    public void onClickSortTime(View v){
        fillList(getCursor("time"));
    }

    // Sort by date
    public void onClickSortDate(View v){
        fillList(getCursor("date"));
    }

    /**
     * Get notified when the database changes
     */
    class DBContentObserver extends ContentObserver {

        public DBContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            // Fill the ListView
            fillList(getCursor("none"));
        }
    }
}
