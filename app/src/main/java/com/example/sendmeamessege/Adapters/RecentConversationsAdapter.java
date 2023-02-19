package com.example.sendmeamessege.Adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sendmeamessege.Listeners.ConversationListener;
import com.example.sendmeamessege.Listeners.UserListener;
import com.example.sendmeamessege.Models.ChatMessage;
import com.example.sendmeamessege.Models.User;
import com.example.sendmeamessege.R;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final ArrayList<ChatMessage> chatMessages;
    private final ConversationListener conversationListener;

    private View view;

    public RecentConversationsAdapter(ArrayList<ChatMessage> chatMessages,ConversationListener conversationListener) {
        this.chatMessages = chatMessages;
        this.conversationListener=conversationListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConversationHolder conversationHolder;
        View tempView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_recent_conversation, parent, false);
        view=tempView;
        conversationHolder = new ConversationHolder(tempView);
        return conversationHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        final ConversationHolder holder = (ConversationHolder) viewHolder;
        ChatMessage chatMessage = chatMessages.get(position);
        holder.textName.setText(chatMessage.getConversationName().toUpperCase().charAt(0)+chatMessage.getConversationName().substring(1));
        holder.textRecentMessage.setText(chatMessage.getMessage());
        holder.accountUser.setText(chatMessage.getConversationName().toUpperCase().charAt(0)+"");
        view.setOnClickListener(v->{
            User user=new User();
            user.setId(chatMessage.getConversationId());
            user.setName(chatMessage.getConversationName());
            conversationListener.onConversationClicked(user);
        });

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class ConversationHolder extends RecyclerView.ViewHolder{
        private final MaterialTextView textName;
        private final MaterialTextView textRecentMessage;
        private final MaterialTextView accountUser;



        public ConversationHolder(@NonNull View itemView) {
            super(itemView);
            textName=itemView.findViewById(R.id.text_name);
            accountUser=itemView.findViewById(R.id.account_user);
            textRecentMessage=itemView.findViewById(R.id.text_recent_message);
        }
    }
}
