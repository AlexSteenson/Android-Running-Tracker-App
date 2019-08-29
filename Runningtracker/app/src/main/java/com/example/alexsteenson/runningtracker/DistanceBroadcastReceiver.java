package com.example.alexsteenson.runningtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * This class listens for a broadcast made locally to display the time elapsed, distance travelled and the current speed
 * of the user
 */
public class DistanceBroadcastReceiver extends BroadcastReceiver {

    // TextViews declaration
    private TextView txtDistance;
    private TextView txtTime;
    private TextView txtSpeed;
    private TextView txtMotiv;
    private String plan;

    public DistanceBroadcastReceiver(){}

    /**
     * Constructor used when creating the BroadcastReceiver. Assigns the relevant TextViews and the
     * workout type
     * @param distance
     * @param time
     * @param speed
     * @param motiv
     * @param plan
     */
    public DistanceBroadcastReceiver(TextView distance, TextView time, TextView speed, TextView motiv, String plan){
        this.txtDistance = distance;
        this.txtTime = time;
        this.txtSpeed = speed;
        this.txtMotiv = motiv;
        this.plan = plan;
    }

    /**
     * When a broadcast is received this function is called
     * This function displays the users current time, distance travelled and speed
     * It also tells the user if they need to speed up or slow down according to average
     * walk, jog and run speeds found on google
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        // Obtain the time, distance and speed of the user
        float currentDistance = bundle.getFloat("distance");
        String distance = String.format("%.2f", currentDistance / 1000); // Format the distance
        long time = bundle.getLong("time");
        Double speed = bundle.getDouble("speed");

        // Display the distance, time and speed
        txtDistance.setText(distance + " km");
        txtTime.setText(displayTime(time));
        String avgSpeed = String.format("%.2f", speed); // Format the speed
        txtSpeed.setText(avgSpeed + " km/h");

        // Display speed recommendations with a max and min speed for each workout type
        if(plan.equals("walk")){
            checkSpeed(5, 2.5, speed);
        }else if(plan.equals("jog")){
            checkSpeed(7, 5, speed);
        }else if(plan.equals("run")){
            checkSpeed(999, 7, speed); // No max speed for running
        }
        Log.d("Received", "True");
    }

    /**
     * Convert the millisecond time sored into minutes and seconds
     * @param millis
     * @return
     */
    public String displayTime(long millis){
        int seconds = (int) (millis / 1000); // Convert to seconds
        int minutes = seconds / 60; // Get the mins rounded down
        seconds = seconds % 60; // Obtain the "left over" seconds
        return String.format("%d:%02d", minutes, seconds); // Format the time
    }

    /**
     * Display to the user if they are travelling too fast, slow or just right according to averages
     */
    public void checkSpeed(double max, double min, double speed){
        if(speed >= max){ // Too fast
            txtMotiv.setText("You're going fast for a " + plan + "\n don't forget to pace yourself");
        }else if(speed <= min){ // Too slow
            txtMotiv.setText("You're going a little slow for a " + plan + "\n remember to push yourself");
        }else{ // Just right
            txtMotiv.setText("You're going at the perfect speed, keep it up!");
        }
    }
}
