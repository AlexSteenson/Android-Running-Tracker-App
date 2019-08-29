package com.example.alexsteenson.runningtracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * This activity allows the user to select a run they have done before, create a new one or start
 * a new run without naming it
 */
public class NameWorkout extends AppCompatActivity {

    private String plan;
    public boolean started = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_run);

        // Get bundle data
        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
            plan = bundle.getString("plan");

        // Set title
        TextView title = findViewById(R.id.txtBeforeTitle);
        title.setText("Have you done this " + plan + " before?");
    }

    /**
     * If returning to this activity a run has probably taken place so we can finish this one to
     * return to the home page
     */
    @Override
    protected void onResume(){
        super.onResume();

        if(started){
            finish();
        }
    }

    /**
     * If the user has done their workout route before they can select it by name
     * @param v
     */
    public void onClickYes(View v){
        started = true;
        // Create intent to go to an activity to select the workout
        Intent intent = new Intent(NameWorkout.this, SelectExistingWorkout.class);
        Bundle bundle = new Bundle();
        // Add the plan and activity that went to there as another activity also uses SelectExistingWorkout
        bundle.putString("plan", plan);
        bundle.putString("parent", "NameWorkout");
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * If the user has not done the route before and wants to save it they can name it in a pop up
     * EditText field
     * @param v
     */
    public void onClickNoSave(View v){
        // Create new alert
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        // Set title for alert
        alert.setTitle(plan + " name");
        alert.setMessage("Name your new " + plan + ":");

        // Create new EditText
        final EditText input = new EditText(this);
        alert.setView(input);

        // Create button to accept the name
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Start the next activity with the new name
                startNewActivity(input.getText().toString());
            }
        });

        // Cancel button that does nothing
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show(); // Show the alert
    }

    /**
     * If the user hasn't done the route before and doesn't want to name it
     * @param v
     */
    public void onClickNoDontSave(View v){
        startNewActivity("No name");
    }

    /**
     * Start the LogWorkout activity and pass through the name and plan type
     * @param name
     */
    public void startNewActivity(String name){
        started = true;
        // Create intent
        Intent intent = new Intent(NameWorkout.this, LogWorkout.class);
        Bundle bundle = new Bundle();
        // Add bundle data
        bundle.putString("plan", plan);
        bundle.putString("name", name);
        intent.putExtras(bundle);
        // Start intent
        startActivity(intent);
    }
}
