package com.example.brightstart.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.brightstart.R;
import com.example.brightstart.data.ChildProfile;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ChildViewHolder> {

    private List<ChildProfile> childList;
    private FirebaseFirestore firestore;

    public ChildAdapter(List<ChildProfile> childList) {
        this.childList = childList;
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_child_profile, parent, false);
        return new ChildViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        ChildProfile child = childList.get(position);

        holder.textViewChildName.setText(child.getChildName() != null ? child.getChildName() : "Name Not Available");
        holder.textViewAge.setText(child.getAge() > 0 ? "Age: " + child.getAge() : "Age: N/A");

        String status = child.getAttendanceStatus() != null ? child.getAttendanceStatus() : "Absent";
        holder.textViewAttendanceStatus.setText("Status: " + status);

        updateButtonColors(holder, "Present".equalsIgnoreCase(status));

        holder.buttonPresent.setOnClickListener(v -> updateAttendanceStatus(child, "Present", holder));
        holder.buttonAbsent.setOnClickListener(v -> updateAttendanceStatus(child, "Absent", holder));
    }

    private void updateAttendanceStatus(ChildProfile child, String status, ChildViewHolder holder) {
        firestore.collection("childrenProfiles")
                .document(child.getId())
                .update("attendanceStatus", status)
                .addOnSuccessListener(aVoid -> {
                    child.setAttendanceStatus(status);
                    holder.textViewAttendanceStatus.setText("Status: " + status);
                    updateButtonColors(holder, "Present".equalsIgnoreCase(status));
                    Toast.makeText(holder.itemView.getContext(), "Marked as " + status, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(holder.itemView.getContext(), "Failed to update status", Toast.LENGTH_SHORT).show();
                    Log.e("ChildAdapter", "Error updating attendance status", e);
                });
    }

    private void updateButtonColors(ChildViewHolder holder, boolean isPresent) {
        if (isPresent) {
            holder.buttonPresent.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.button_present));
            holder.buttonAbsent.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.button_disabled));
            holder.buttonPresent.setEnabled(false);
            holder.buttonAbsent.setEnabled(true);
        } else {
            holder.buttonPresent.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.button_disabled));
            holder.buttonAbsent.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.button_absent));
            holder.buttonPresent.setEnabled(true);
            holder.buttonAbsent.setEnabled(false);
        }
    }

    @Override
    public int getItemCount() {
        return childList.size();
    }

    static class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView textViewChildName, textViewAge, textViewAttendanceStatus;
        Button buttonPresent, buttonAbsent;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewChildName = itemView.findViewById(R.id.textViewChildName);
            textViewAge = itemView.findViewById(R.id.textViewAge);
            textViewAttendanceStatus = itemView.findViewById(R.id.textViewAttendanceStatus);
            buttonPresent = itemView.findViewById(R.id.buttonPresent);
            buttonAbsent = itemView.findViewById(R.id.buttonAbsent);
        }
    }
}
