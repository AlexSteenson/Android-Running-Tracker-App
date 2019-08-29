package com.example.alexsteenson.runningtracker;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * This class starts the distanceService when the user starts their workout to log their movement
 * It uses a local broadcast to update the time, distance, speed and motivation TextFields
 */
public class LogWorkout extends AppCompatActivity {

    private String plan = "";
    private String name = "";
    private boolean started = false;
    DistanceService.DistanceBinder controller = null;
    DistanceBroadcastReceiver receiver;

    TextView time;
    TextView txtSpeed;
    TextView txtMotiv;
    TextView txtDistance;
    String currentTime;
    String currentDate;
    private float distance;

    DBHelper dbHelper;
    SQLiteDatabase db;

    private ServiceConnection serviceConnection = new ServiceConnection(){

        /**
         * On binding with the service obtain the binder token to interact with the service
         * @param name
         * @param service
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            controller = (DistanceService.DistanceBinder) service;
            Log.d("is bounded", "Yes");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            controller = null; // Remove binder token
        }
    };

    /**
     * Create new intent, start the service and bind to the service with BindService
     */
    public void createBinder(){
        Intent serviceIntent = new Intent(this, DistanceService.class);
        startService(serviceIntent);
        this.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * When this activity is created obtain the workout type and name from the bundle and initialise the database
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        time = findViewById(R.id.txtTime);

        // Obtain bundle data
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){ // If there is bundle data
            // Set the plan and name
            plan = bundle.getString("plan");
            name = bundle.getString("name");

            // Set title according to workout type
            setTitleAndButtonText();
        }else{ // If there is no bundle data, the workout has begun
            // Obtain data from shared preferences
            SharedPreferences prefs = this.getSharedPreferences(
                    "com.example.alexsteenson.runningtracker", Context.MODE_PRIVATE);
            started = prefs.getBoolean("started", false);
            currentTime = prefs.getString("time", "");
            currentDate = prefs.getString("date", "");
            plan = prefs.getString("plan", "");
            name = prefs.getString("name", "");

            if(started){
                // Reset the destroyed boolean
                prefs.edit().putBoolean("destroyed", false).apply();

                // Bind to service
                Intent serviceIntent = new Intent(this, DistanceService.class);
                this.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

                // Get TextViews for broadcast receiver
                txtDistance = findViewById(R.id.txtCurrentDistance);
                txtSpeed = findViewById(R.id.txtSpeed);
                txtMotiv = findViewById(R.id.txtMotiv);
                receiver = new DistanceBroadcastReceiver(txtDistance, time, txtSpeed, txtMotiv, plan);
                // Register receiver
                IntentFilter filter = new IntentFilter("DISTANCE_BROADCAST");
                LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

                // Change title and button text to display to finish
                setTitleAndButtonText();
            }
        }
        // init db
        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();
    }

    /**
     * Stop the service, stop broadcasting and close the database to ensure all the
     * life cycles are complete and end
     */
    @Override
    protected void onDestroy(){
        super.onDestroy();

        // If the workout had started
        if(started){
            SharedPreferences prefs = this.getSharedPreferences(
                    "com.example.alexsteenson.runningtracker", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("started", started).apply();
            prefs.edit().putBoolean("destroyed", true).apply();
            prefs.edit().putString("time", currentTime).apply();
            prefs.edit().putString("date", currentDate).apply();
            prefs.edit().putString("plan", plan).apply();
            prefs.edit().putString("name", name).apply();

            // Unbind
            unbindService(serviceConnection);
            // Stop broadcasting
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
            db.close();
        }
    }

    /**
     * Stop broadcasting when the app isn't user facing
     */
    @Override
    protected void onPause(){
        super.onPause();

        // If the workout has started
        if(started){
            // Stop broadcasting
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        }
    }

    /**
     * When the app returns to be user facing start broadcasting again
     */
    @Override
    protected void onResume(){
        super.onResume();

        // If the workout has started
        if(started){
            SharedPreferences prefs = this.getSharedPreferences(
                    "com.example.alexsteenson.runningtracker", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("destroyed", false).apply();
            // Start receiving broadcasts again
            receiver = new DistanceBroadcastReceiver(txtDistance, time, txtSpeed, txtMotiv, plan);
            IntentFilter filter = new IntentFilter("DISTANCE_BROADCAST");
            LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
        }
    }

    /**
     * Toggle between starting the workout and stopping it
     * @param v
     */
    public void onClickEvent(View v){
        // If the workout hasn't yet started
        if (!started) { // Begin timing and tracking
            // Check permissions
            if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
                // Start the workout
                started = !started;
                // Change title and button text to display to finish
                setTitleAndButtonText();

                // Get the start time and date
                getTimeDate();

                // Create a connection to the distance service
                createBinder();
                txtDistance = findViewById(R.id.txtCurrentDistance);
                txtSpeed = findViewById(R.id.txtSpeed);
                txtMotiv = findViewById(R.id.txtMotiv);
                // Start receiving broadcasts to update the TextViews
                receiver = new DistanceBroadcastReceiver(txtDistance, time, txtSpeed, txtMotiv, plan);
                IntentFilter filter = new IntentFilter("DISTANCE_BROADCAST");
                LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
            }else{ // The user has not allowed location permissions
                // Display a toast message to allow permissions
                Toast.makeText(this, "Permissions Required", Toast.LENGTH_LONG).show();
            }

        } else { // End and save
            started = !started;
            setTitleAndButtonText();

            distance = controller.getDistanceTravelled();
            long elapsedTime = controller.getElapsedTime();
            // Stop the service
            controller.stopWorkout();
            // Unbind
            unbindService(serviceConnection);
            // Stop receiving broadcasts
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
            // Add the workout to the db
            addDBEntry(distance, elapsedTime);
        }

    }

    /**
     * Set the title and button text depending on if the user has started their run or not
     */
    public void setTitleAndButtonText(){
        TextView title = findViewById(R.id.txtTitle);
        Button event = findViewById(R.id.btnStart);

        if (!started){ // If the workout hasn't begun
            title.setText("Press start to begin your " + plan);
            event.setText("Start your " + plan);
            TextView time = findViewById(R.id.txtTime);
            time.setText("0:00");
            TextView distance = findViewById(R.id.txtCurrentDistance);
            distance.setText("0.00 km");
            TextView speed = findViewById(R.id.txtSpeed);
            speed.setText("0.00 km/h");
        } else{ // If the workout has started
            title.setText("Press finish to end your " + plan);
            event.setText("Finish your " + plan);
        }
    }

    /**
     * Obtain the current start time and date
     */
    public void getTimeDate(){
        Date calDate = Calendar.getInstance().getTime();
        // Formatting for the time
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        // Formatting for the date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        currentDate = dateFormat.format(calDate);
        currentTime = timeFormat.format(calDate);
    }

    /**
     * Insert a new entry into the db
     * @param distance
     * @param elapsedTime
     */
    public void addDBEntry(float distance, long elapsedTime){
        // New entry values
        ContentValues newValues = new ContentValues();
        newValues.put(DBProviderContract.TYPE, plan);
        newValues.put(DBProviderContract.TIME, elapsedTime);
        newValues.put(DBProviderContract.DISTANCE, distance);
        newValues.put(DBProviderContract.TIMEDATE, currentTime);
        newValues.put(DBProviderContract.DATE, currentDate);
        newValues.put(DBProviderContract.NAME, name);

        // Add the new entry
        getContentResolver().insert(DBProviderContract.WORKOUT_URI, newValues);
        // Close the db connection
        db.close();
        // Finish the activity
        finish();
    }
}