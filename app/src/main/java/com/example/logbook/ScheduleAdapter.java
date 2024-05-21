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
    private String dayOfWeek;

    public ScheduleAdapter(ArrayList<String> scheduleList, String dayOfWeek) {
        this.scheduleList = scheduleList;
        this.dayOfWeek = dayOfWeek;
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
        holder.textViewDayOfWeek.setText(dayOfWeek);
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSubject;
        TextView textViewDayOfWeek;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSubject = itemView.findViewById(R.id.textViewSubject);
            textViewDayOfWeek = itemView.findViewById(R.id.textViewDayOfWeek);
        }
    }
}
