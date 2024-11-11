package com.example.brightstart.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.brightstart.R;
import com.example.brightstart.data.CalendarEvent;

import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private List<CalendarEvent> calendarEvents;

    public CalendarAdapter(List<CalendarEvent> calendarEvents) {
        this.calendarEvents = calendarEvents;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_event_card, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        CalendarEvent event = calendarEvents.get(position);
        holder.tvTitle.setText(event.getTitle());
        holder.tvDate.setText("Date: " + event.getDate());
        holder.tvDescription.setText(event.getDescription());
    }

    @Override
    public int getItemCount() {
        return calendarEvents.size();
    }

    static class CalendarViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvDate, tvDescription;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}
