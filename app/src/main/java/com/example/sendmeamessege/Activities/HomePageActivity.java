package com.example.sendmeamessege.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.sendmeamessege.Adapters.RecentConversationsAdapter;
import com.example.sendmeamessege.Models.FinalConstants;
import com.example.sendmeamessege.Firebase.PreferenceManager;
import com.example.sendmeamessege.Listeners.ConversationListener;
import com.example.sendmeamessege.Models.ChatMessage;
import com.example.sendmeamessege.Models.User;
import com.example.sendmeamessege.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class HomePageActivity extends AppCompatActivity implements ConversationListener {
    private PreferenceManager preferenceManager;
    private MaterialTextView name_txt;
    private FloatingActionButton add_button;
    private RecyclerView recyclerView;
    private ArrayList<ChatMessage> conversations;
    private RecentConversationsAdapter conversationsAdapter;
    private ProgressBar progressBar;
    private FirebaseFirestore database;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        preferenceManager=new PreferenceManager(getApplicationContext());
        findViews();
        initFirst();
        updateName();
        getToken();
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),UserActivity.class));
            }
        });
        listenConversation();
    }

    public void listenConversation(){
        database.collection(FinalConstants.KET_COLLECTION_CONVERSATION)
                .whereEqualTo(FinalConstants.KEY_SENDER_ID,preferenceManager.getString(FinalConstants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(FinalConstants.KET_COLLECTION_CONVERSATION)
                .whereEqualTo(FinalConstants.KEY_RECEIVER_ID,preferenceManager.getString(FinalConstants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    public String decryptString(String str, String key) throws Exception{
        byte[] encryted_bytes = Base64.decode(str, Base64.DEFAULT);
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance(FinalConstants.CYPHER_INSTANCE);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(FinalConstants.INITIALIZATION_VECTOR.getBytes()));
        byte[] decrypted = cipher.doFinal(encryted_bytes);
        return new String(decrypted, "UTF-8");

    }
    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener=((value, error) -> {
        if(error!=null){
            return;
        }
        if(value!=null){
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId=documentChange.getDocument().getString(FinalConstants.KEY_SENDER_ID);
                    String receiverId=documentChange.getDocument().getString(FinalConstants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage=new ChatMessage();
                    chatMessage.setSenderId(senderId);
                    chatMessage.setReceiverId(receiverId);
                    if(preferenceManager.getString(FinalConstants.KEY_USER_ID).equals(senderId)){
                        chatMessage.setConversationName(documentChange.getDocument().getString(FinalConstants.KET_RECEIVER_NAME));
                        chatMessage.setConversationId(documentChange.getDocument().getString(FinalConstants.KEY_RECEIVER_ID));
                    }else{
                        chatMessage.setConversationName(documentChange.getDocument().getString(FinalConstants.KEY_SENDER_NAME));
                        chatMessage.setConversationId(documentChange.getDocument().getString(FinalConstants.KEY_SENDER_ID));
                    }
                    String key="";
                    if(preferenceManager.getString(FinalConstants.KEY_NAME)
                            .equals(senderId)){
                        key=receiverId.substring(0,8)+senderId.substring(0,8);

                    }else{
                        key=senderId.substring(0,8)+receiverId.substring(0,8);
                    }
                    String decrypt= null;
                    try {
                        decrypt = decryptString(documentChange.getDocument().getString(FinalConstants.KEY_LAST_MESSAGE),key);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    chatMessage.setMessage(decrypt);

                    chatMessage.setDate(documentChange.getDocument().getDate(FinalConstants.KEY_TIMESTAMP));
                    conversations.add(chatMessage);
                }else if(documentChange.getType()==DocumentChange.Type.MODIFIED) {
                    for(int i = 0; i< conversations.size(); i++){
                        String senderId= documentChange.getDocument().getString(FinalConstants.KEY_SENDER_ID);
                        String receiverId =documentChange.getDocument().getString(FinalConstants.KEY_RECEIVER_ID);
                        if (conversations.get(i).getSenderId().equals(senderId) && conversations.get(i).getReceiverId().equals(receiverId)) {
                            conversations.get(i).setMessage(documentChange.getDocument().getString(FinalConstants.KEY_LAST_MESSAGE));
                            conversations.get(i).setDate(documentChange.getDocument().getDate(FinalConstants.KEY_TIMESTAMP));
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations, (obj1, obj2)->obj2.getDate().compareTo(obj1.getDate()));


            conversationsAdapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(0);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
            mLayoutManager.setReverseLayout(true);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setAdapter(conversationsAdapter);
        }
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

    });
    private void initFirst() {
        conversations =new ArrayList<>();
        conversationsAdapter=new RecentConversationsAdapter(conversations,this);
        database=FirebaseFirestore.getInstance();
    }

    @SuppressLint("SetTextI18n")
    private void updateName() {
        String name=preferenceManager.getString(FinalConstants.KEY_NAME);
        name_txt.setText(name.toUpperCase().charAt(0)+ name.substring(1)+"s");
    }

    private void findViews() {
        name_txt=findViewById(R.id.name_txt);
        add_button=findViewById(R.id.add_button);
        recyclerView=findViewById(R.id.conversation_recyclerview);
        progressBar=findViewById(R.id.progress_bar);

    }


    public void updateToken(String token)
    {
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        DocumentReference documentReference=database.collection(FinalConstants.KEY_COLLECTION_USERS).document(preferenceManager.getString(FinalConstants.KEY_USER_ID));
        documentReference.update(FinalConstants.KER_FCM_TOKEN,token)
                .addOnSuccessListener(unused -> Toast.makeText(getApplicationContext(),"Token updated successfully",Toast.LENGTH_SHORT))
                .addOnFailureListener(exception-> Toast.makeText(getApplicationContext(),"Unable to update Token",Toast.LENGTH_SHORT));
    }
    public void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.log_out) {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            DocumentReference documentReference = database.collection(FinalConstants.KEY_COLLECTION_USERS).document(preferenceManager.getString(FinalConstants.KEY_USER_ID));
            HashMap<String, Object> updates = new HashMap<>();
            updates.put(FinalConstants.KER_FCM_TOKEN, FieldValue.delete());
            documentReference.update(updates).addOnSuccessListener(unused -> {
                preferenceManager.clear();
                startActivity(new Intent(this, SighInActivity.class));
            }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Unable to sign in", Toast.LENGTH_SHORT));
            return true;
        }
        return super.onOptionsItemSelected(item);
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


    @Override
    public void onConversationClicked(User user) {
        Intent intent=new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(FinalConstants.KEY_USER,user);
        startActivity(intent);
    }
}