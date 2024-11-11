package com.example.brightstart;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    private List<Student> studentList;
    private Context context;

    // Constructor for StudentAdapter
    public StudentAdapter(List<Student> studentList, Context context) {
        this.studentList = studentList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each student item
        View view = LayoutInflater.from(context).inflate(R.layout.student_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the current student
        Student student = studentList.get(position);

        // Set the name and standard in the TextViews
        holder.nameTextView.setText(student.getName());
        holder.standardTextView.setText(student.getStandard());

        // Set an onClickListener to navigate to BillingDetailsActivity
        holder.itemView.setOnClickListener(v -> {
            // Create an Intent to start BillingDetailsActivity
            Intent intent = new Intent(context, BillingDetailsActivity.class);
            intent.putExtra("documentId", student.getDocumentId());  // Pass the Firestore document ID
            intent.putExtra("childName", student.getName());  // Pass the student's name
            context.startActivity(intent);  // Start the BillingDetailsActivity
        });
    }

    @Override
    public int getItemCount() {
        return studentList.size();  // Return the total number of students
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, standardTextView;  // UI components

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.student_name);  // Find the student name TextView
            standardTextView = itemView.findViewById(R.id.student_standard);  // Find the student standard TextView
        }
    }
}


