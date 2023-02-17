package com.example.sendmeamessege.Adapters;


import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sendmeamessege.Listeners.UserListener;
import com.example.sendmeamessege.R;
import com.example.sendmeamessege.Models.User;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private ArrayList<User> users = new ArrayList<>();
    private View view;
    private UserListener userListener;


    public UsersAdapter(ArrayList<User> users, UserListener userListener){

        this.users = users;
        this.userListener=userListener;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewTemp = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_builder, parent, false);
        view=viewTemp;
        UserHolder userHolder = new UserHolder(view);
        return userHolder;


    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder,int position) {
        final UserHolder holder = (UserHolder) viewHolder;
        User user = users.get(position);
        holder.text_name.setText(user.getName().toUpperCase().charAt(0)+user.getName().substring(1));
        holder.text_email.setText(user.getEmail());
        holder.account_user.setText(user.getName().toUpperCase().charAt(0)+"");
        view.setOnClickListener(v->userListener.onUserListener(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserHolder extends RecyclerView.ViewHolder  {

        private MaterialTextView text_name;
        private MaterialTextView text_email;
        private MaterialTextView account_user;

        public UserHolder(View itemView) {
            super(itemView);
            text_name = itemView.findViewById(R.id.text_name);
            text_email = itemView.findViewById(R.id.text_email);
            account_user=itemView.findViewById(R.id.account_user);
        }



    }

}
