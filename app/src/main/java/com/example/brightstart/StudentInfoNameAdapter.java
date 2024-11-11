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

public class StudentInfoNameAdapter extends RecyclerView.Adapter<StudentInfoNameAdapter.ViewHolder> {

    private List<Student> studentList;
    private Context context;

    // Constructor for AdminStudentAdapter
    public StudentInfoNameAdapter(List<Student> studentList, Context context) {
        this.studentList = studentList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each student item for admin report
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


        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(context, AdminStudentInfo.class);
            intent.putExtra("documentId", student.getDocumentId());  // Pass the Firestore document ID
            intent.putExtra("childName", student.getName());  // Pass the student's name
            context.startActivity(intent);  // Start the AdminReportActivity
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

