package com.example.brightstart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {

    private List<Student> studentList;
    private Context context;

    // Constructor to initialize adapter with student list and context
    public PaymentAdapter(List<Student> studentList, Context context) {
        this.studentList = studentList;
        this.context = context;
    }

    // Method to update the list of students in the adapter
    public void updateList(List<Student> updatedStudentList) {
        this.studentList.clear(); // Clear the existing list
        this.studentList.addAll(updatedStudentList); // Add new items to the list
        notifyDataSetChanged(); // Notify the adapter to refresh the RecyclerView
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the RecyclerView
        View view = LayoutInflater.from(context).inflate(R.layout.payment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind data to each item in the RecyclerView
        Student student = studentList.get(position);

        // Check if student data is not null before setting the text
        if (student != null) {
            // Safeguard against null or missing names
            if (student.getName() != null) {
                holder.nameTextView.setText(student.getName());
            } else {
                holder.nameTextView.setText("No Name");  // Default text for missing name
            }

            // Safeguard against null or missing standard
            if (student.getStandard() != null) {
                holder.standardTextView.setText(student.getStandard());
            } else {
                holder.standardTextView.setText("No Standard");  // Default text for missing standard
            }

            // Set payment status label and color
            if (student.isPaid()) {
                holder.paymentStatus.setText("Paid");
                holder.paymentStatus.setBackgroundResource(R.drawable.paid_status_background);  // green background
            } else {
                holder.paymentStatus.setText("Pending");
                holder.paymentStatus.setBackgroundResource(R.drawable.pending_status_background);  // red background
            }

            // Set onClickListener to navigate to AdminConfirmPaymentActivity with documentId
            // Inside onBindViewHolder or where you navigate to AdminConfirmPaymentActivity
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, AdminConfirmPaymentActivity.class);
                intent.putExtra("studentDocumentId", student.getDocumentId());  // Pass the document ID to the next activity
                intent.putExtra("studentName", student.getName());  // Pass student name as well
                ((PaymentConfirmationActivity) context).startActivityForResult(intent, 1);  // Use startActivityForResult
            });


        }
    }

    @Override
    public int getItemCount() {
        // Return the total number of items in the list
        return studentList.size();
    }

    // ViewHolder class to hold the views for each item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, standardTextView, paymentStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ensure that you are referencing the correct IDs in the layout
            nameTextView = itemView.findViewById(R.id.student_name); // Reference to student name TextView
            standardTextView = itemView.findViewById(R.id.student_standard); // Reference to student standard TextView
            paymentStatus = itemView.findViewById(R.id.payment_status); // Reference to payment status TextView
        }
    }
}

