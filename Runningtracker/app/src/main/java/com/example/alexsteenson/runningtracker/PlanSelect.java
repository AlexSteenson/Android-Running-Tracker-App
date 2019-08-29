package com.example.alexsteenson.runningtracker;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

/**
 * This class lets the user select their workout type (walk, jog or run)
 * It also allows them to select an option to view their past runs
 * They can also navigate to an activity to see an overview of all their runs
 */
public class PlanSelect extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_select);

        // If the workout is taking place
        if(isServiceRunning(this, DistanceService.class)){
            // Get the destroyed boolean to see if the activity was destroyed, or if the notification was pressed when paused
            SharedPreferences prefs = this.getSharedPreferences(
                    "com.example.alexsteenson.runningtracker", Context.MODE_PRIVATE);
            boolean destroyed = prefs.getBoolean("destroyed", false);

            if(destroyed){ // If the app was destroyed
                // Go back to LogWorkout with NO bundle data
                Intent intent = new Intent(PlanSelect.this, LogWorkout.class);
                startActivity(intent);
            }else{
                // The activity to return back to LogActivity
                finish();
            }
        }
        // Spinner element
        Spinner spinner = findViewById(R.id.spnOptions);

        // Spinner Drop down elements
        List<String> options = new ArrayList<String>();
        options.add("all");
        options.add("run");
        options.add("jog");
        options.add("walk");
        options.add("name");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, options);

        // Drop down layout style
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        // If the user doesn't have location permissions request them
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
    }

    public void onClickWalk(View v){
        // User selects to walk
        returnActivity("plan", "walk");
    }

    public void onClickJog(View v){
        // User selects to jog
        returnActivity("plan", "jog");
    }

    public void onClickRun(View v){
        // User selects to run
        returnActivity("plan", "run");
    }

    public void onClickView(View v){
        // Get spinner option
        Spinner spinner = findViewById(R.id.spnOptions);
        String selectedOption = spinner.getSelectedItem().toString();

        // If its to select runs by name take them to an activity to select a named run
        if(selectedOption.equals("name")){
            Intent intent = new Intent(PlanSelect.this, SelectExistingWorkout.class);
            Bundle bundle = new Bundle();
            // Put parent activity
            bundle.putString("parent", "PlanSelect");
            intent.putExtras(bundle);
            // Start intent
            startActivity(intent);
        }else{ // any other option
            Intent intent = new Intent(PlanSelect.this, ViewAllWorkouts.class);
            Bundle bundle = new Bundle();
            // Pass option though in bundle and start activity
            bundle.putString("option", selectedOption);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    public void onClickOverview(View v){
        Intent intent = new Intent(PlanSelect.this, WorkoutOverview.class);
        startActivity(intent);
    }

    public void returnActivity(String bundleKey, String bundleValue){
        // Go to select workout name activity
        Intent intent = new Intent(PlanSelect.this, NameWorkout.class);
        Bundle bundle = new Bundle();
        // Add workout type
        bundle.putString(bundleKey, bundleValue);
        intent.putExtras(bundle);
        // Start activity
        startActivity(intent);
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }

            }
        }
        return false;
    }
}
