package com.example.alexsteenson.runningtracker;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ViewSelectedWorkout extends AppCompatActivity {

    private int id = -1;
    private TextView txtType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_selected_run);

        // Set id to the run id
        Bundle bundle = getIntent().getExtras();
        id = bundle.getInt("_id");

        txtType = findViewById(R.id.txtPlan);

        // Get cursor for the run entry
        displayRunData(getCursorQuery(id));
    }

    // Queries the database with run ID to get entry data
    public Cursor getCursorQuery(int id){
        String[] projection = new String[] { "_id", "type", "distance", "time", "timeDate", "date", "name" };
        String selection = DBProviderContract._ID + " = ?";
        String[] selectionArgs = {""};
        selectionArgs[0] = Integer.toString(id);

        return getContentResolver().query(DBProviderContract.WORKOUT_URI, projection, selection, selectionArgs, null);
    }

    // Displays run data in text fields
    public void displayRunData(Cursor c){

        if (c != null && c.getCount() > 0){
            int typeIndex = c.getColumnIndex(DBProviderContract.TYPE);
            int distanceIndex = c.getColumnIndex(DBProviderContract.DISTANCE);
            int timeIndex = c.getColumnIndex(DBProviderContract.TIME);
            int timeDateIndex = c.getColumnIndex(DBProviderContract.TIMEDATE);
            int dateIndex = c.getColumnIndex(DBProviderContract.DATE);
            int nameIndex = c.getColumnIndex(DBProviderContract.NAME);

            while(c.moveToNext()) {
                String plan = c.getString(typeIndex);
                // Set title depending on the workout type
                TextView txtTitle = findViewById(R.id.txtTitle);
                txtTitle.setText("Your selected " + plan);

                String name = c.getString(nameIndex);
                // If the workout wasn't named then set the type TextView to type else the name
                if(name.equals("No name")){
                    txtType.setText(plan);
                }else{
                    txtType.setText(name);
                }

                // Get the distance and format it
                TextView txtDistance = findViewById(R.id.txtDistanceRan);
                float distance = Float.parseFloat(c.getString(distanceIndex));
                String totalDistance = String.format("%.2f", distance);
                txtDistance.setText(totalDistance + " km");

                // Get the time taken
                TextView txtTimeDate = findViewById(R.id.txtTimeDate);
                txtTimeDate.setText(c.getString(timeDateIndex));

                // Get the date
                TextView txtDate = findViewById(R.id.txtDate);
                txtDate.setText(c.getString(dateIndex));

                // Calculate the time
                int elapsedTime = Integer.parseInt(c.getString(timeIndex));
                int seconds = (int) (elapsedTime / 1000);
                int minutes = seconds / 60;

                // Calculate the speed
                double speed = distance * 1000 / seconds; // speed = distance / time. m/s
                speed = speed * 3.6; // Convert to km/h

                // Get the remaining seconds
                seconds = seconds % 60;

                // Set the time and format it
                TextView txtTime = findViewById(R.id.txtTimeTaken);
                txtTime.setText(String.format("%d:%02d", minutes, seconds));

                // Set the speed and format it
                TextView txtSpeed = findViewById(R.id.txtAvgSpeed);
                String avgSpeed = String.format("%.2f", speed);
                txtSpeed.setText(avgSpeed + " km/h");
            }
        }
    }

    // Deletes the run when delete button is pressed
    public void onClickDelete(View v){
        // Create sql command
        String command = DBProviderContract._ID + " LIKE ?";
        String[] commandArgs = {""};
        // For the run id
        commandArgs[0] = Integer.toString(id);

        // Delete the entry
        getContentResolver().delete(DBProviderContract.WORKOUT_URI, command, commandArgs);

        // Return to main activity
        finish();
    }

    // Allows the user to change the name of their run
    public void onClickUpdate(View v){
        // Create new alert
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        // Set title for alert
        alert.setTitle("Rename");
        alert.setMessage("Name your workout");

        // Create new EditText
        final EditText input = new EditText(this);
        alert.setView(input);

        // Create button to accept the name
        alert.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                ContentValues newValues = new ContentValues();

                // Get new name
                String newName = input.getText().toString();
                newValues.put(DBProviderContract.NAME, newName);

                // Get sql commands
                String selection = DBProviderContract._ID + " = ?";
                String[] selectionArgs = {"" + id};

                //Update entry
                getContentResolver().update(DBProviderContract.WORKOUT_URI, newValues, selection, selectionArgs);

                txtType.setText(newName);
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
}
