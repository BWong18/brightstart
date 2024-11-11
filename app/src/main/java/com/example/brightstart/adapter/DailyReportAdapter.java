package com.example.brightstart.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.brightstart.R;
import com.example.brightstart.data.DailyReport;

import java.util.List;

public class DailyReportAdapter extends RecyclerView.Adapter<DailyReportAdapter.DailyReportViewHolder> {

    private List<DailyReport> dailyReports;

    public DailyReportAdapter(List<DailyReport> dailyReports) {
        this.dailyReports = dailyReports;
    }

    @NonNull
    @Override
    public DailyReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_daily_report_card, parent, false);
        return new DailyReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DailyReportViewHolder holder, int position) {
        DailyReport report = dailyReports.get(position);

        // Set text data
        holder.tvChildName.setText("Child Name: " + report.getChildName());
        holder.tvDate.setText("Date: " + report.getDate());
        holder.tvHomework.setText("Homework: " + report.getHomework());
        holder.tvMeals.setText("Meals: " + report.getMeals());
        holder.tvMood.setText("Mood: " + report.getMood());
        holder.tvPerformance.setText("Performance: " + report.getPerformance());
        holder.tvTuition.setText("Tuition: " + report.getTuition());

        // Load images using Glide
// URL of the placeholder image on the internet
        String placeholderUrl = "https://developers.elementor.com/docs/assets/img/elementor-placeholder-image.png";
        String errorUrl = "https://e7.pngegg.com/pngimages/10/205/png-clipart-computer-icons-error-information-error-angle-triangle-thumbnail.png";

// Load images using Glide with an online placeholder
        Glide.with(holder.itemView.getContext()).load(report.getImageUrl0()).thumbnail(Glide.with(holder.itemView.getContext()).load(placeholderUrl))  // Load URL as placeholder
                .error(Glide.with(holder.itemView.getContext()).load(errorUrl))  // Load URL as error image
                .into(holder.imageView0);


    }

    @Override
    public int getItemCount() {
        return dailyReports.size();
    }

    static class DailyReportViewHolder extends RecyclerView.ViewHolder {

        TextView tvChildName, tvDate, tvHomework, tvMeals, tvMood, tvPerformance, tvTuition;
        ImageView imageView0;

        public DailyReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChildName = itemView.findViewById(R.id.tvChildName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvHomework = itemView.findViewById(R.id.tvHomework);
            tvMeals = itemView.findViewById(R.id.tvMeals);
            tvMood = itemView.findViewById(R.id.tvMood);
            tvPerformance = itemView.findViewById(R.id.tvPerformance);
            tvTuition = itemView.findViewById(R.id.tvTuition);
            imageView0 = itemView.findViewById(R.id.imageView0);
        }
    }
}
