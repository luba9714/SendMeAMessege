package com.example.sendmeamessege.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.sendmeamessege.Adapters.UsersAdapter;
import com.example.sendmeamessege.Models.FinalConstants;
import com.example.sendmeamessege.Firebase.PreferenceManager;
import com.example.sendmeamessege.Listeners.UserListener;
import com.example.sendmeamessege.Models.User;
import com.example.sendmeamessege.R;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class UserActivity extends AppCompatActivity implements UserListener {
    private PreferenceManager preferenceManager;
    private MaterialTextView error_message;
    private ProgressBar progress_bar;
    private RecyclerView users_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        preferenceManager=new PreferenceManager(getApplicationContext());
        findViews();
        getUsers();

    }


    public void getUsers(){
        loading(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(FinalConstants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task->{
                    loading(false);
                    String userId=preferenceManager.getString(FinalConstants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult()!=null){
                        ArrayList<User> users=new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                            if(userId.equalsIgnoreCase(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user=new User();
                            user.setName(queryDocumentSnapshot.getString(FinalConstants.KEY_NAME));
                            user.setEmail(queryDocumentSnapshot.getString(FinalConstants.KEY_EMAIL));
                            user.setToken(queryDocumentSnapshot.getString(FinalConstants.KER_FCM_TOKEN));
                            user.setId(queryDocumentSnapshot.getId());
                            users.add(user);
                        }
                        if(users.size()>0){
                            UsersAdapter usersAdapter=new UsersAdapter(users,this);
                            users_list.setLayoutManager(new LinearLayoutManager(this));
                            users_list.setHasFixedSize(true);
                            users_list.setAdapter(usersAdapter);
                            users_list.setVisibility(View.VISIBLE);
                        }else {
                            showErrorMessage();
                        }
                    }else{
                        showErrorMessage();
                    }
                });
    }

    public void showErrorMessage(){
        error_message.setVisibility(View.VISIBLE);
    }




    private void findViews() {
        users_list=findViewById(R.id.users_list);
        progress_bar =findViewById(R.id.progress_bar);
        error_message=findViewById(R.id.error_message);
    }

    public void loading(boolean isLoading){
        if(isLoading){
            users_list.setVisibility(View.INVISIBLE);
            progress_bar.setVisibility(View.VISIBLE);
        }else {
            progress_bar.setVisibility(View.INVISIBLE);
            users_list.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onUserListener(User user) {
        Intent intent=new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(FinalConstants.KEY_USER,user);
        startActivity(intent);
    }
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_out:
                FirebaseFirestore database=FirebaseFirestore.getInstance();
                DocumentReference documentReference=database.collection(FinalConstants.KEY_COLLECTION_USERS).document(preferenceManager.getString(FinalConstants.KEY_USER_ID));
                HashMap<String,Object> updates= new HashMap<>();
                updates.put(FinalConstants.KER_FCM_TOKEN, FieldValue.delete());
                documentReference.update(updates).addOnSuccessListener(unused -> {preferenceManager.clear();
                    startActivity(new Intent(this, SighInActivity.class));
                }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(),"Unable to sign in",Toast.LENGTH_SHORT));
                //Toast.makeText(this, "update_user selected", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);

        }
        return true;
    }

}