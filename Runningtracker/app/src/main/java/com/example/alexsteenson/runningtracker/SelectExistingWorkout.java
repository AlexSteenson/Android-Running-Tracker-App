package com.example.alexsteenson.runningtracker;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * This activity selects all the named workouts and lists them in a ListView
 */
public class SelectExistingWorkout extends AppCompatActivity {

    private boolean started = false;
    private String plan = "";
    private String parent = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_existing_run);

        //Get bundle data
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            plan = bundle.getString("plan");
            parent = bundle.getString("parent");
        }

        // Fill list
        fillList(getCursor());
    }

    @Override
    protected void onResume(){
        super.onResume();

        // Return to parent activity
        if(started){
            finish();
        }
    }

    public void fillList(Cursor c){
        // Keep an ArrayList of every workout name
        ArrayList<String> everyWorkoutName = new ArrayList<>();

        // If the query returned a result
        if (c != null && c.getCount() > 0){
            // Get name
            int nameIndex = c.getColumnIndex(DBProviderContract.NAME);

            while(c.moveToNext()) {
                String name = c.getString(nameIndex);
                // If the workout was named and isn't already in the list add it
                if(!name.equals("No name") && !everyWorkoutName.contains(name))
                    everyWorkoutName.add(name);
            }
        }

        // Create the ListView and set the adapter
        ListView listView = (ListView) findViewById(R.id.lvSelectPrevRun);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, everyWorkoutName));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                // Get run name at selected location
                String selectedFromList = (String) (listView.getItemAtPosition(myItemInt));

                if(parent.equals("NameWorkout")){
                    // If the parent activity is NameWorkout then go to Log workout with the name
                    startWorkout(selectedFromList);
                }else{
                    // Select the workout to be displayed
                    selectNameForDisplay(selectedFromList);
                }
            }
        });
    }

    public Cursor getCursor(){
        // Get the name column
        String[] projection = new String[] {"name"};

        // Return the query
        return getContentResolver().query(DBProviderContract.WORKOUT_URI, projection, null, null, null);
    }

    public void startWorkout(String name){
        started = true;
        // Create intent to goto the LogWorkout activity
        Intent intent = new Intent(SelectExistingWorkout.this, LogWorkout.class);
        // Create new bundle with the name of the run
        Bundle bundle = new Bundle();
        bundle.putString("plan", plan);
        bundle.putString("name", name);
        intent.putExtras(bundle);
        // Start activity
        startActivity(intent);
    }

    public void selectNameForDisplay(String name){
        started = true;
        // Create intent to goto the ViewAllWorkouts intent
        Intent intent = new Intent(SelectExistingWorkout.this, ViewAllWorkouts.class);
        // Create new bundle with the name of the run
        Bundle bundle = new Bundle();
        bundle.putString("option", "name");
        bundle.putString("name", name);
        intent.putExtras(bundle);
        // Start activity
        startActivity(intent);
    }
}
