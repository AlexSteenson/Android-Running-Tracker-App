package com.example.alexsteenson.runningtracker;

/**
 * Class to store workout data, needed for the custom listView adapter
 */
public class Workout {

    private int _id;
    private String type;
    private float distance;
    private long time;
    private String startDate;
    private String startTime;
    private String name;

    // Constructor
    public Workout(int _id, String type, float distance, long time, String startTime, String startDate, String name){
        this._id = _id;
        this.distance = distance;
        this.time = time;
        this.type = type;
        this.startTime = startTime;
        this.startDate = startDate;
        this.name = name;
    }

    // Getters for each bit of a workout
    public String getType() {
        return type;
    }

    public float getDistance() {
        return distance;
    }

    public long getTime() {
        return time;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getName() {
        return name;
    }

    public int get_id() {
        return _id;
    }

    public String getTimeAsString(){
        int seconds = (int) (time / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
