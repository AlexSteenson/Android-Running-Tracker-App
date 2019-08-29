package com.example.alexsteenson.runningtracker;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class WorkoutOverview extends AppCompatActivity {

    // Array indexing constants
    private final int TOTAL = 0;
    private final int DISTANCE = 1;
    private final int TIME = 2;

    private final int WALK = 0;
    private final int JOG = 1;
    private final int RUN = 2;

    String tense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_overview);

        getOverviewData(getCursor());
    }

    public Cursor getCursor(){
        // Get the all columns
        String[] projection = new String[] {"type", "time", "distance", "date"};
        return getContentResolver().query(DBProviderContract.WORKOUT_URI, projection, null, null, null);
    }

    public void getOverviewData(Cursor c){
        // Create new arrays containing overview data for the dat, week and total
        double todayOverviewData[][] = new double[3][3];
        double weekOverviewData[][] = new double[3][3];
        double totalOverviewData[][] = new double[3][3];

        // If the query returned results
        if (c != null && c.getCount() > 0){
            // Get column indexing
            int typeIndex = c.getColumnIndex(DBProviderContract.TYPE);
            int distanceIndex = c.getColumnIndex(DBProviderContract.DISTANCE);
            int timeIndex = c.getColumnIndex(DBProviderContract.TIME);
            int dateIndex = c.getColumnIndex(DBProviderContract.DATE);

            // Get today's date in date and string format
            Date today = Calendar.getInstance().getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
            String sToday = dateFormat.format(today);

            // Iterate over each workout
            while(c.moveToNext()) {
                // Get the plan type and date
                String sPlan = c.getString(typeIndex);
                int planIndex = getIndex(sPlan);
                String sDate = c.getString(dateIndex);
                try{
                    Date workoutDate = new SimpleDateFormat("dd-MMM-yyyy").parse(sDate); // Convert workout date to date object
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(workoutDate);
                    calendar.add(Calendar.DATE, 7); // Add a week to the workout date
                    Date workoutDateAddWeek = calendar.getTime(); // Get the date object of the workout add a week
                    if(!workoutDateAddWeek.before(today)){ // It's within a week
                        weekOverviewData[planIndex][TOTAL]++; // Increase total workouts that week
                        weekOverviewData[planIndex][DISTANCE] += c.getDouble(distanceIndex); // Increase distance
                        weekOverviewData[planIndex][TIME] += c.getLong(timeIndex); // Increase Time
                    }
                }catch (Exception e){}

                // If the workout happened today
                if(sDate.equals(sToday)){
                    todayOverviewData[planIndex][TOTAL]++; // Increase total workouts that day
                    todayOverviewData[planIndex][DISTANCE] += c.getDouble(distanceIndex); // Increase distance
                    todayOverviewData[planIndex][TIME] += c.getLong(timeIndex); // Increase Time
                }

                // Increase data for total workouts
                totalOverviewData[planIndex][TOTAL]++;
                totalOverviewData[planIndex][DISTANCE] += c.getDouble(distanceIndex);
                totalOverviewData[planIndex][TIME] += c.getLong(timeIndex);

            }
        }

        // Get the daily TextFields
        TextView txtDailyWalk = findViewById(R.id.txtDailyWalk);
        TextView txtDailyJog = findViewById(R.id.txtDailyJog);
        TextView txtDailyRun = findViewById(R.id.txtDailyRun);
        TextView txtDailyTotal = findViewById(R.id.txtDailyTotal);

        // Print the daily data
        printPlan("walk", txtDailyWalk, todayOverviewData);
        printPlan("jog", txtDailyJog,todayOverviewData);
        printPlan("run", txtDailyRun,todayOverviewData);
        printTotal(txtDailyTotal,todayOverviewData);

        // Get the weekly TextFields
        TextView txtWeeklyWalk = findViewById(R.id.txtWeeklyWalk);
        TextView txtWeeklyJog = findViewById(R.id.txtWeeklyJog);
        TextView txtWeeklyRun = findViewById(R.id.txtWeeklyRun);
        TextView txtWeeklyTotal = findViewById(R.id.txtWeeklyTotal);

        // Print the weekly data
        printPlan("walk", txtWeeklyWalk,weekOverviewData);
        printPlan("jog", txtWeeklyJog, weekOverviewData);
        printPlan("run", txtWeeklyRun, weekOverviewData);
        printTotal(txtWeeklyTotal, weekOverviewData);

        // Get the total TextFields
        TextView txtTotalWalk = findViewById(R.id.txtTotalWalk);
        TextView txtTotalJog = findViewById(R.id.txtTotalJog);
        TextView txtTotalRun = findViewById(R.id.txtTotalRun);
        TextView txtTotalTotal = findViewById(R.id.txtTotalTotal);

        // Print the total data
        printPlan("walk", txtTotalWalk,totalOverviewData);
        printPlan("jog", txtTotalJog, totalOverviewData);
        printPlan("run", txtTotalRun, totalOverviewData);
        printTotal(txtTotalTotal, totalOverviewData);
    }

    // Get the array index depending on the plan type
    public int getIndex(String plan){
        switch (plan){
            case "walk":
                tense = "walked";
                return WALK;
            case "jog":
                tense = "jogged";
                return JOG;
            case "run":
                tense = "ran";
                return RUN;
        }
        return -1;
    }

    public void printPlan(String plan, TextView view, double data[][]){
        int index = getIndex(plan);
        // Get distance and time and format it
        String totalDistance = String.format("%.2f", data[index][DISTANCE]);
        String time = convertTime(data[index][TIME]);

        // Set the TextView to the data
        view.setText("You have " + tense + " " + (int)data[index][TOTAL] +
                " times for " + totalDistance +
                " km in " + time + "\n");
    }

    public void printTotal(TextView view, double data[][]){
        double totalWorkout = 0;
        double totalDistance = 0;
        double totalTime = 0;
        // Get the total data for the array
        for(int i = 0; i < 3; i++){
            totalWorkout += data[i][TOTAL];
            totalDistance += data[i][DISTANCE];
            totalTime += data[i][TIME];
        }
        // Get distance and time and format it
        String sTotalDistance = String.format("%.2f", totalDistance);
        String sTime = convertTime(totalTime);

        // Set the TextView to the data
        view.setText("You have worked out a total of " + (int)totalWorkout +
                " times for " + sTotalDistance +
                " km in " + sTime + "\n");
    }

    public String convertTime(double time){
        // Get the time in seconds
        int seconds = (int) (time / 1000);
        // Calculate the minutes
        int minutes = seconds / 60;
        // Get the "remaining" seconds
        seconds = seconds % 60;

        // Format the time
        return String.format("%d:%02d", minutes, seconds);
    }
}
