package com.example.alexsteenson.runningtracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * This class creates the custom view for the listView. It allows for four column to display all the data needed
 */
public class CustomListViewAdapter extends ArrayAdapter<Workout> implements View.OnClickListener{

    private Context context;

    /**
     * This class stores all the TextViews needed in the adapter
     */
    private static class ViewHolder {
        TextView txtType;
        TextView txtTime;
        TextView txtDistance;
        TextView txtStartTime;
        TextView txtStartDate;
    }

    /**
     * Constructor
     * @param data
     * @param context
     */
    public CustomListViewAdapter(ArrayList<Workout> data, Context context) {
        super(context, R.layout.custom_list_view, data);
        this.context = context;
    }

    @Override
    public void onClick(View v) {}

    /**
     * Collects all the data needed to be displayed from a Workout object and displays it in the TextViews
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Workout workoutObject = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.custom_list_view, parent, false);
            // Set the TextViews in the View to the TextViews in the viewHolder class
            viewHolder.txtType = convertView.findViewById(R.id.txtType);
            viewHolder.txtTime = convertView.findViewById(R.id.txtTime);
            viewHolder.txtDistance = convertView.findViewById(R.id.txtDistance);
            viewHolder.txtStartTime = convertView.findViewById(R.id.txtStartTime);
            viewHolder.txtStartDate = convertView.findViewById(R.id.txtStartDateTime);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Set the text for each TextView with the relevant data
        viewHolder.txtTime.setText(workoutObject.getTimeAsString());
        String distance = String.format("%.2f", workoutObject.getDistance());
        viewHolder.txtDistance.setText("" + distance);
        viewHolder.txtStartTime.setText(workoutObject.getStartTime());
        viewHolder.txtStartDate.setText(workoutObject.getStartDate());

        // If the workout doesn't have a name display the workout type instead
        if(workoutObject.getName().equals("No name")){
            viewHolder.txtType.setText(workoutObject.getType()); // Workout type
        }else{
            viewHolder.txtType.setText(workoutObject.getName()); // Workout name
        }

        return convertView;
    }
}

