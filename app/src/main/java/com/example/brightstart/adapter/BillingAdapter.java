package com.example.brightstart.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.brightstart.BillingDetail;
import com.example.brightstart.R;

import java.util.List;

public class BillingAdapter extends RecyclerView.Adapter<BillingAdapter.BillingViewHolder> {

    private List<BillingDetail> billingDetails;

    public BillingAdapter(List<BillingDetail> billingDetails) {
        this.billingDetails = billingDetails;
    }

    @NonNull
    @Override
    public BillingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_billing_card, parent, false);
        return new BillingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillingViewHolder holder, int position) {
        BillingDetail billingDetail = billingDetails.get(position);

        holder.tvChildName.setText("Child Name: " + billingDetail.getChildName());
        holder.tvMonthYear.setText("Month/Year: " + billingDetail.getMonthYear());
        holder.tvPaymentStatus.setText("Payment Status: " + billingDetail.getPaymentStatus());
        holder.tvMeals.setText("Meals: " + billingDetail.getMeals());
        holder.tvResourceFees.setText("Resource Fees: " + billingDetail.getResourceFees());
        holder.tvTransportation.setText("Transportation: " + billingDetail.getTransportation());
        holder.tvTuitionFees.setText("Tuition Fees: " + billingDetail.getTuitionFees());
    }

    @Override
    public int getItemCount() {
        return billingDetails.size();
    }

    static class BillingViewHolder extends RecyclerView.ViewHolder {

        TextView tvChildName, tvMonthYear, tvPaymentStatus, tvMeals, tvResourceFees, tvTransportation, tvTuitionFees;

        public BillingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChildName = itemView.findViewById(R.id.tvChildName);
            tvMonthYear = itemView.findViewById(R.id.tvMonthYear);
            tvPaymentStatus = itemView.findViewById(R.id.tvPaymentStatus);
            tvMeals = itemView.findViewById(R.id.tvMeals);
            tvResourceFees = itemView.findViewById(R.id.tvResourceFees);
            tvTransportation = itemView.findViewById(R.id.tvTransportation);
            tvTuitionFees = itemView.findViewById(R.id.tvTuitionFees);
        }
    }
}
