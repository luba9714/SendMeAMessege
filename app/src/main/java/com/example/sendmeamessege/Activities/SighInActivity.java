package com.example.sendmeamessege.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.sendmeamessege.Models.FinalConstants;
import com.example.sendmeamessege.Firebase.PreferenceManager;
import com.example.sendmeamessege.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SighInActivity extends AppCompatActivity {
    private MaterialTextView sign_up_txt;
    private MaterialButton sigh_in_btn;
    private ProgressBar progress_bar;
    private EditText input_password, input_email;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sigh_in);
        preferenceManager= new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(FinalConstants.KEY_IS_SIGNED_IN)){
            Intent intent=new Intent(getApplicationContext(),HomePageActivity.class);
            startActivity(intent);
            finish();
        }
        findViews();
        ifSighUp();
        sigh_in_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkIfValid()){
                    sighIn();
                }
            }
        });


    }

    public void ifSighUp() {
        ForegroundColorSpan color = new ForegroundColorSpan(getResources().getColor(R.color.textSighUp));
        SpannableString ss = new SpannableString(sign_up_txt.getText());
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                startActivity(new Intent(SighInActivity.this, SighUpActivity.class));
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan, 23, sign_up_txt.getText().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(color, 23, sign_up_txt.getText().length(), SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
        ss.setSpan(new UnderlineSpan(), 23, sign_up_txt.getText().length(), SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
        sign_up_txt.setText(ss);
        sign_up_txt.setMovementMethod(LinkMovementMethod.getInstance());
        sign_up_txt.setHighlightColor(Color.TRANSPARENT);
    }

    public void findViews() {
        sign_up_txt = findViewById(R.id.sign_up);
        sigh_in_btn = findViewById(R.id.sign_in);
        input_email =findViewById(R.id.input_email_sign_in);
        input_password =findViewById(R.id.input_password_sign_in);
        progress_bar=findViewById(R.id.progress_bar);
    }

    public void sighIn() {
        loading(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(FinalConstants.KEY_COLLECTION_USERS)
                .whereEqualTo(FinalConstants.KEY_EMAIL,input_email.getText().toString().toLowerCase())
                .whereEqualTo(FinalConstants.KEY_PASSWORD,input_password.getText().toString())
                .get()
                .addOnCompleteListener(task->{
                    if(task.isSuccessful()&&task.getResult()!=null &&
                            task.getResult().getDocuments().size()>0){
                        DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(FinalConstants.KEY_IS_SIGNED_IN,true);
                        preferenceManager.putString(FinalConstants.KEY_USER_ID,documentSnapshot.getId());
                        preferenceManager.putString(FinalConstants.KEY_NAME,documentSnapshot.getString(FinalConstants.KEY_NAME));
                        Intent intent=new Intent(getApplicationContext(),HomePageActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }else{
                        loading(false);
                        Toast.makeText(getApplicationContext(),"Unable to sigh in",Toast.LENGTH_LONG).show();
                    }
                });


    }


    public boolean checkIfValid() {
        boolean isValid=true;
        if (input_email.getText().toString().isEmpty()) {
            input_email.setError("Enter Email");
            isValid=false;
        }else{
            input_email.setError(null);
        }
        if (input_password.getText().toString().isEmpty()) {
            input_password.setError("Enter Password");
            isValid=false;
        }else{
            input_password.setError(null);
        }
        return isValid;
    }

    private void loading(boolean isLoading) {
        if(isLoading){
            sigh_in_btn.setVisibility(View.INVISIBLE);
            progress_bar.setVisibility(View.VISIBLE);
        }else {
            progress_bar.setVisibility(View.INVISIBLE);
            sigh_in_btn.setVisibility(View.VISIBLE);
        }
    }
}