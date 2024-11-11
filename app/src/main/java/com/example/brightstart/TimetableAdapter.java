package com.example.brightstart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> {

    private List<TimetableItem> timetableItems;
    private Context context;

    public TimetableAdapter(List<TimetableItem> timetableItems, Context context) {
        this.timetableItems = timetableItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.timetable_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimetableItem item = timetableItems.get(position);

        // Set event text
        holder.eventTextView.setText(item.getEvent());

        // Show time only if it's the first occurrence of the time
        if (position == 0 || !item.getTime().equals(timetableItems.get(position - 1).getTime())) {
            holder.timeTextView.setVisibility(View.VISIBLE);
            holder.timeTextView.setText(item.getTime());
        } else {
            holder.timeTextView.setVisibility(View.GONE); // Hide time if it's the same as the previous one
        }
    }

    @Override
    public int getItemCount() {
        return timetableItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView, eventTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.timetable_time);
            eventTextView = itemView.findViewById(R.id.timetable_event);
        }
    }
}
