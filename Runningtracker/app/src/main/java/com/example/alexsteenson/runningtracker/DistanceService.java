package com.example.alexsteenson.runningtracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * This service uses a location manager and listener to calculate how far the user has traveled
 * It also keeps track of the time the service has been active and broadcasts the distance and time
 * The service is only created when the user starts their workout
 * The service stops when it is destroyed, or the workout ends, and stops broadcasting when the app isn't user facing
 * This is a foreground service so must display a notification
 */
public class DistanceService extends Service {

    public static final String CHANNEL_ID = "1000";
    public static final int NOTIFICATION_ID = 1;
    private Location currentLocation;
    private float distanceTravelled = 0;
    private IBinder distanceBinder = new DistanceBinder();
    private boolean isBound = false;
    private Context context = this;
    private long startTime;
    private long elapsedTime;

    LocationListener locationListener;
    LocationManager locationManager;

    public DistanceService() {
    }

    /**
     * Set up the Location listener and manager to start detecting location changes when the service is created
     */
    @Override
    public void onCreate(){
        // Create listener
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(isBound){ // If the workout has isBound
                    if(currentLocation == null){ // If there is no current location set it to where the user currently is
                        currentLocation = location;
                    }
                    distanceTravelled += location.distanceTo(currentLocation); // Calculate the distance since last location and log it
                    currentLocation = location; // Update position
                    Log.d("g53mdp", location.getLatitude() + " " + location.getLongitude());
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { // information about the signal, i.e. number of satellites
                Log.d("g53mdp", "onStatusChanged: " + provider + " " + status);
            }

            @Override
            public void onProviderEnabled(String provider) {
                // the user enabled (for example) the GPS
                Log.d("g53mdp", "onProviderEnabled: " + provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                // the user disabled (for example) the GPS
                Log.d("g53mdp", "onProviderDisabled: " + provider);
            }
        };

        // Create location manager
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        try {
            // Assign listener
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5, // minimum time interval between updates
                    5, // minimum distance between updates, in metres
                    locationListener);
        } catch(SecurityException e) { // If the user hasn't allowed location position
            Log.d("g53mdp", e.toString());
        }
    }

    /**
     * In a thread a broadcast is sent every second so the time TextView updated every second
     * The time, distance and speed are all broadcast
     */
    public void sendBroadcast(){
        new Thread(() -> {
            while(isBound){
                try{
                    // Calculate time
                    elapsedTime = System.currentTimeMillis() - startTime;
                    // Create the intent to broadcast
                    Intent distanceIntent = new Intent("DISTANCE_BROADCAST");
                    Bundle bundle = new Bundle();
                    // Send the time, distance and speed
                    bundle.putLong("time", elapsedTime);
                    bundle.putFloat("distance", distanceTravelled);
                    bundle.putDouble("speed", calculateSpeed());
                    distanceIntent.putExtras(bundle);
                    // Send the broadcast
                    LocalBroadcastManager.getInstance(context).sendBroadcast(distanceIntent);
                    // Wait a second
                    Thread.sleep(1000);
                    Log.d("Sent", "true");
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     * Calculates the users speed
     * @return
     */
    public double calculateSpeed(){
        double speed = distanceTravelled / (elapsedTime / 1000); // speed = distance / time. m/s
        return speed * 3.6; // Convert to km/h
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }


    /**
     * On binding log the start time, set the isBound boolean to true, start broadcasting,
     * create the notification, start the service and send the binder token
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        // Get the starting time
        startTime = System.currentTimeMillis();
        // Start the service
        startForeground(NOTIFICATION_ID, getNotification());
        isBound = true;
        // Start sending broadcasts
        sendBroadcast();
        // Send the binder token
        return distanceBinder;
    }

    @Override
    public void onRebind(Intent intent){
        isBound = true;
        // Start sending broadcasts
        sendBroadcast();
    }


    /**
     * Create and return a notification
     * @return
     */
    public Notification getNotification(){

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel name";
            String description = "channel description";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name,
                    importance);
            channel.setDescription(description);

            notificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(this, PlanSelect.class);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Runner is currently open")
                .setContentText("Click to return to app or select an action")
                .setContentIntent(intent)
                .setOngoing(true);

        return mBuilder.build();
    }

    /**
     * On unbinding stop listening for location changes and stop the service
     * @param intent
     * @return
     */
    @Override
    public boolean onUnbind(Intent intent){
        isBound = false;
        return true;
    }

    public void stopService(){
        locationManager.removeUpdates(locationListener);
        stopSelf();
    }

    /**
     * This class works as the binder token to allow the ui thread to interact with the service
     */
    public class DistanceBinder extends Binder {

        // Get the distance ran
        public float getDistanceTravelled(){
            return distanceTravelled / 1000;
        }

        // Get the elapsed time
        public long getElapsedTime(){
            return elapsedTime;
        }

        // Stop run and destroy the service
        public void stopWorkout(){
            stopService();
        }
    }
}