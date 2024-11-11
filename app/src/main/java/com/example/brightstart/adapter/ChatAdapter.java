package com.example.brightstart.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.brightstart.R;
import com.example.brightstart.data.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> chatMessages;
    private SimpleDateFormat dateFormat;
    private String currentUserEmail;

    public ChatAdapter(List<ChatMessage> chatMessages, String currentUserEmail) {
        this.chatMessages = chatMessages;
        this.dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        this.currentUserEmail = currentUserEmail;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        holder.textViewMessage.setText(chatMessage.getMessage());
        holder.textViewSender.setText(chatMessage.getSender());

        // Format timestamp to readable time
        String time = dateFormat.format(new Date(chatMessage.getTimestamp()));
        holder.textViewTimestamp.setText(time);

        // Differentiate between sent and received messages
        if (chatMessage.getSender().equals(currentUserEmail)) {
            // Sent message
//            holder.itemView.setBackgroundResource(R.drawable.sent_message_background);
            holder.textViewSender.setText("You");
        } else {
            // Received message
//            holder.itemView.setBackgroundResource(R.drawable.received_message_background);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage, textViewSender, textViewTimestamp;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewSender = itemView.findViewById(R.id.textViewSender);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
        }
    }
}
