package com.example.logbook;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private ArrayList<String> scheduleList;
    private String[] scheduleTimes;

    public ScheduleAdapter(ArrayList<String> scheduleList, String[] scheduleTimes) {
        this.scheduleList = scheduleList;
        this.scheduleTimes = scheduleTimes;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_list_item, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        String subject = scheduleList.get(position);
        holder.textViewSubject.setText(subject);
        if (position < scheduleTimes.length) {
            holder.textViewScheduleTime.setText(scheduleTimes[position]);
        } else {
            holder.textViewScheduleTime.setText("Time not available");
        }
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSubject;
        TextView textViewScheduleTime;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSubject = itemView.findViewById(R.id.textViewSubject);
            textViewScheduleTime = itemView.findViewById(R.id.textViewDayOfWeek);
        }
    }
}
