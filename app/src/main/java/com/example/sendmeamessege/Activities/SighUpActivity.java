package com.example.sendmeamessege.Activities;



import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.sendmeamessege.Models.FinalConstants;
import com.example.sendmeamessege.Firebase.PreferenceManager;
import com.example.sendmeamessege.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SighUpActivity extends AppCompatActivity {
    private EditText input_name,input_email,input_password,confirm_password;
    private MaterialButton sign_up_btn;
    private ProgressBar progress_bar;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sigh_up);
        findViews();
        preferenceManager=new PreferenceManager(getApplicationContext());
        sign_up_btn.setOnClickListener(v->{
            if(checkIfValid()){
                sighUp();
            }
        });

    }

    private void findViews() {
        input_name=findViewById(R.id.input_name);
        input_email=findViewById(R.id.input_email_sigh_up);
        input_password=findViewById(R.id.input_password_sign_up);
        confirm_password=findViewById(R.id.confirm_password);
        sign_up_btn=findViewById(R.id.sign_up_btn);
        progress_bar=findViewById(R.id.progress_bar);

    }

    private void loading(boolean isLoading) {
        if(isLoading){
            sign_up_btn.setVisibility(View.INVISIBLE);
            progress_bar.setVisibility(View.VISIBLE);
        }else {
            progress_bar.setVisibility(View.INVISIBLE);
            sign_up_btn.setVisibility(View.VISIBLE);
        }
    }

    public void sighUp(){
        loading(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        HashMap<String,Object> user=new HashMap<>();
        user.put(FinalConstants.KEY_NAME,input_name.getText().toString());
        user.put(FinalConstants.KEY_EMAIL,input_email.getText().toString().toLowerCase());
        user.put(FinalConstants.KEY_PASSWORD,input_password.getText().toString());
        database.collection(FinalConstants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(FinalConstants.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(FinalConstants.KEY_USER_ID,documentReference.getId());
                    preferenceManager.putString(FinalConstants.KEY_NAME,input_name.getText().toString());
                    Intent intent=new Intent(this,HomePageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(exception->{
                    loading(false);
                    Toast.makeText(getApplicationContext(),exception.getMessage(),Toast.LENGTH_SHORT).show();
                });
    }

    public boolean checkIfValid() {
        boolean isValid=true;
        if (input_name.getText().toString().isEmpty()) {
            input_name.setError("Enter Name");
            isValid=false;
        }else{
            input_name.setError(null);
        }
        if (input_email.getText().toString().isEmpty()) {
            input_email.setError("Enter Email");
            isValid=false;
        }else {
            input_email.setError(null);
        }
        if (input_password.getText().toString().isEmpty()) {
            input_password.setError("Enter Password");
            isValid=false;
        }else {
            input_password.setError(null);
        }
        if (confirm_password.getText().toString().isEmpty()) {
            confirm_password.setError("Confirm Password");
            isValid=false;
        } else{
            confirm_password.setError(null);
        }

        if (!input_password.getText().toString().equalsIgnoreCase(confirm_password.getText().toString())){
            Toast.makeText(getApplicationContext(),"Password and confirm Password are not the same",Toast.LENGTH_SHORT).show();
            isValid=false;
        }
        return isValid;
    }
}