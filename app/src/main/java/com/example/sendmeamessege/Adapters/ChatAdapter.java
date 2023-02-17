package com.example.sendmeamessege.Adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sendmeamessege.Models.ChatMessage;
import com.example.sendmeamessege.R;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private ArrayList<ChatMessage> chatMessages;
    //private View view;

    private static final int VIEW_TYPE_SENT=1;
    private static final int VIEW_TYPE_RECEIVED=2;
    private final String senderId;
    private final String receiverName;


    public ChatAdapter(ArrayList<ChatMessage> chatMessages, String senderId, String name) {
        this.chatMessages = chatMessages;
        this.senderId=senderId;
        this.receiverName=name;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if( viewType==VIEW_TYPE_SENT){
            ChatAdapter.SendMessageViewHolder sendMessageViewHolder;
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_sent_message, parent, false);
            //view=view;
            sendMessageViewHolder = new ChatAdapter.SendMessageViewHolder(view);
            return sendMessageViewHolder;
        }else{
            ChatAdapter.ReceiveMessageViewHolder receiveMessageViewHolder;
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_received_message, parent, false);
            //view=view;
            receiveMessageViewHolder = new ChatAdapter.ReceiveMessageViewHolder(view);
            return receiveMessageViewHolder;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        if(getItemViewType(position)==VIEW_TYPE_SENT){
            final ChatAdapter.SendMessageViewHolder holder = (ChatAdapter.SendMessageViewHolder) viewHolder;
            ChatMessage chatMessage = chatMessages.get(position);
            holder.text_message_sent.setText(chatMessage.getMessage());
            holder.date_time_sent.setText(chatMessage.getDateTime());



            //view.setOnClickListener(v->userListener.onUserListener(user));
        } else {
            final ChatAdapter.ReceiveMessageViewHolder holder = (ChatAdapter.ReceiveMessageViewHolder) viewHolder;
            ChatMessage chatMessage = chatMessages.get(position);
            holder.text_message_rec.setText(chatMessage.getMessage());
            holder.date_time_rec.setText(chatMessage.getDateTime());
            holder.user_account.setText(receiverName.toUpperCase().charAt(0)+"");
        }

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).getSenderId().equalsIgnoreCase(senderId)) {
            return VIEW_TYPE_SENT;
        }else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    class SendMessageViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView text_message_sent;
        private MaterialTextView date_time_sent;


        public SendMessageViewHolder(View itemView) {
            super(itemView);
            text_message_sent = itemView.findViewById(R.id.text_message_sent);
            date_time_sent = itemView.findViewById(R.id.date_time_sent);

        }



    }

    class ReceiveMessageViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView text_message_rec;
        private MaterialTextView date_time_rec;
        private MaterialTextView user_account;

        public ReceiveMessageViewHolder(View itemView) {
            super(itemView);
            text_message_rec = itemView.findViewById(R.id.text_message_rec);
            date_time_rec = itemView.findViewById(R.id.date_time_rec);
            user_account=itemView.findViewById(R.id.user_account);
        }
    }

}
