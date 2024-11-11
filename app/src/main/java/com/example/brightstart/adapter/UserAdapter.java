package com.example.brightstart.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.brightstart.R;

import java.util.List;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(String userEmail);
    }

    private List<String> userList;
    private Map<String, Boolean> unreadMessageStatus; // Map to track unread messages per user
    private OnUserClickListener listener;

    public UserAdapter(List<String> userList, Map<String, Boolean> unreadMessageStatus, OnUserClickListener listener) {
        this.userList = userList;
        this.unreadMessageStatus = unreadMessageStatus;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        String userEmail = userList.get(position);
        holder.textViewUserEmail.setText(userEmail);

        // Show or hide the green dot based on the unreadMessageStatus map
        Boolean hasUnreadMessages = unreadMessageStatus.get(userEmail);
        holder.newMessageIndicator.setVisibility(Boolean.TRUE.equals(hasUnreadMessages) ? View.VISIBLE : View.GONE);

        // Set click listener to notify when a user is clicked and mark as read
        holder.itemView.setOnClickListener(v -> {
            listener.onUserClick(userEmail);
            unreadMessageStatus.put(userEmail, false); // Mark as read in the adapter
            notifyItemChanged(holder.getAdapterPosition()); // Update UI immediately
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUserEmail;
        ImageView newMessageIndicator; // Green dot for new message indicator

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUserEmail = itemView.findViewById(R.id.textViewUserEmail);
            newMessageIndicator = itemView.findViewById(R.id.newMessageIndicator); // Reference to green dot
        }
    }
}
