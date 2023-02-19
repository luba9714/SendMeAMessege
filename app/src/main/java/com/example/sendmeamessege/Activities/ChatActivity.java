package com.example.sendmeamessege.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.sendmeamessege.Adapters.ChatAdapter;
import com.example.sendmeamessege.Models.FinalConstants;
import com.example.sendmeamessege.Firebase.PreferenceManager;
import com.example.sendmeamessege.Models.ChatMessage;
import com.example.sendmeamessege.R;
import com.example.sendmeamessege.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ChatActivity extends AppCompatActivity {
    private User receiverUser;
    private MaterialTextView other_user_name;
    private ArrayList<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    private EditText chat_message;
    private ImageView send_image;
    private RecyclerView chat_list;
    private ProgressBar progress_bar;
    private String conversationId=null;

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener= (value, error) -> {
        if(error !=null){
            return;
        }
        if(value!= null){
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType()== DocumentChange.Type.ADDED){
                    ChatMessage chatMessage =new ChatMessage();
                    chatMessage.setSenderId(documentChange.getDocument().getString(FinalConstants.KEY_SENDER_ID));
                    chatMessage.setReceiverId(documentChange.getDocument().getString(FinalConstants.KEY_RECEIVER_ID));
                    String key=chatMessage.getSenderId().substring(0,8)+chatMessage.getReceiverId().substring(0,8);
                    Log.d("pttt",key);
                    String decryptedMessage= null;
                    try {
                        decryptedMessage = decryptString(documentChange.getDocument().getString(FinalConstants.KEY_MESSAGE),key);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    chatMessage.setMessage(decryptedMessage);
                    chatMessage.setDateTime(getReadableDateTime(documentChange.getDocument().getDate(FinalConstants.KEY_TIMESTAMP)));
                    chatMessage.setDate(documentChange.getDocument().getDate(FinalConstants.KEY_TIMESTAMP));
                    chatMessages.add(chatMessage);
                }
            }
            int count=chatMessages.size();
            Collections.sort(chatMessages, (obj1,obj2)->obj2.getDate().compareTo(obj1.getDate()));
            if(count==0){
                chatAdapter.notifyDataSetChanged();;
            }else{
                chatAdapter.notifyItemRangeInserted(chatMessages.size(),chatMessages.size());
                chat_list.smoothScrollToPosition(chatMessages.size()-1);
                LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
                mLayoutManager.setReverseLayout(true);
                chat_list.setLayoutManager(mLayoutManager);
                chat_list.setAdapter(chatAdapter);
            }
            chat_list.setVisibility(View.VISIBLE);
        }
        progress_bar.setVisibility(View.GONE);
        if(conversationId==null){
            checkForConversation();
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        findViews();
        initFirst();
        loadReceiverDetails();
        send_image.setOnClickListener(v-> {
            try {
                sendMessage();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        listenMessages();

    }
    @SuppressLint("SetTextI18n")
    public void loadReceiverDetails(){
        receiverUser= (User)getIntent().getSerializableExtra(FinalConstants.KEY_USER);
        other_user_name.setText(receiverUser.getName().toUpperCase().charAt(0)+receiverUser.getName().substring(1));
        chatAdapter=new ChatAdapter(chatMessages,preferenceManager.getString(FinalConstants.KEY_USER_ID), other_user_name.getText().toString());
    }

    public void sendMessage() throws Exception {
        if(conversationId!=null){
            Log.d("pttt","update");
            updateConversation(chat_message.getText().toString());
        }else{
            HashMap<String,Object> conversation=new HashMap<>();
            conversation.put(FinalConstants.KEY_SENDER_ID,preferenceManager.getString(FinalConstants.KEY_USER_ID));
            conversation.put(FinalConstants.KEY_SENDER_NAME,preferenceManager.getString(FinalConstants.KEY_NAME));
            conversation.put(FinalConstants.KEY_RECEIVER_ID,receiverUser.getId());
            conversation.put(FinalConstants.KET_RECEIVER_NAME,receiverUser.getName());
            String key=preferenceManager.getString(FinalConstants.KEY_USER_ID).substring(0,8) + receiverUser.getId().substring(0,8);
            Log.d("pttt",key);
            String encryptMessage=encryptString(chat_message.getText().toString(),key);
            conversation.put(FinalConstants.KEY_LAST_MESSAGE,encryptMessage);
            conversation.put(FinalConstants.KEY_TIMESTAMP,new Date());
            addConversation(conversation);
        }

        HashMap<String,Object> message=new HashMap<>();
        message.put(FinalConstants.KEY_SENDER_ID,preferenceManager.getString(FinalConstants.KEY_USER_ID));
        message.put(FinalConstants.KEY_RECEIVER_ID,receiverUser.getId());
        String key=preferenceManager.getString(FinalConstants.KEY_USER_ID).substring(0,8) + receiverUser.getId().substring(0,8);
        String encryptMessage=encryptString(chat_message.getText().toString(),key);
        message.put(FinalConstants.KEY_MESSAGE,encryptMessage);
        message.put(FinalConstants.KEY_TIMESTAMP,new Date());
        database.collection(FinalConstants.KEY_COLLECTION_CHATS).add(message);
        chat_message.setText(null);
    }


    public String encryptString(String str, String key) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance(FinalConstants.CYPHER_INSTANCE);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(FinalConstants.INITIALIZATION_VECTOR.getBytes()));
        byte[] encrypted = cipher.doFinal(str.getBytes());
        return Base64.encodeToString(encrypted, Base64.DEFAULT);

    }

    public String decryptString(String str, String key) throws Exception{
        byte[] encryted_bytes = Base64.decode(str, Base64.DEFAULT);
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance(FinalConstants.CYPHER_INSTANCE);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(FinalConstants.INITIALIZATION_VECTOR.getBytes()));
        byte[] decrypted = cipher.doFinal(encryted_bytes);
        return new String(decrypted, "UTF-8");

    }
    public String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);

    }

    private void initFirst() {
        database=FirebaseFirestore.getInstance();
        chatMessages=new ArrayList<>();
        preferenceManager=new PreferenceManager(getApplicationContext());

    }

    public void findViews() {
        other_user_name=findViewById(R.id.other_user_name);
        chat_message=findViewById(R.id.chat_message);
        send_image=findViewById(R.id.send_image);
        chat_list=findViewById(R.id.chat_list);
        progress_bar=findViewById(R.id.progress_bar);
    }

    public void listenMessages(){

        database.collection(FinalConstants.KEY_COLLECTION_CHATS)
                .whereEqualTo(FinalConstants.KEY_SENDER_ID, preferenceManager.getString(FinalConstants.KEY_USER_ID))
                .whereEqualTo(FinalConstants.KEY_RECEIVER_ID, receiverUser.getId())
                .addSnapshotListener(eventListener);
        database.collection(FinalConstants.KEY_COLLECTION_CHATS)
                .whereEqualTo(FinalConstants.KEY_SENDER_ID, receiverUser.getId())
                .whereEqualTo(FinalConstants.KEY_RECEIVER_ID, preferenceManager.getString(FinalConstants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }



    public void addConversation(HashMap<String,Object> conversation){
        database.collection(FinalConstants.KET_COLLECTION_CONVERSATION)
                .add(conversation)
                .addOnSuccessListener(documentReference -> conversationId=documentReference.getId());
    }
    public void updateConversation(String message) throws Exception {
        DocumentReference documentReference=database.collection(FinalConstants.KET_COLLECTION_CONVERSATION)
                .document(conversationId);

        String key=preferenceManager.getString(FinalConstants.KEY_USER_ID).substring(0,8) + receiverUser.getId().substring(0,8);
        Log.d("pttt",key);
        String encryptMessage=encryptString(message,key);
        documentReference.update(FinalConstants.KEY_LAST_MESSAGE,encryptMessage,FinalConstants.KEY_TIMESTAMP,new Date(),
                FinalConstants.KEY_SENDER_ID,preferenceManager.getString(FinalConstants.KEY_USER_ID),
                FinalConstants.KEY_RECEIVER_ID,receiverUser.getId(),
                FinalConstants.KEY_SENDER_NAME,preferenceManager.getString(FinalConstants.KEY_NAME),
                FinalConstants.KET_RECEIVER_NAME,receiverUser.getName());
    }
    public void checkForConversation(){
        if(chatMessages.size()!=0){
            checkForConversationRemotely(preferenceManager.getString(FinalConstants.KEY_USER_ID),receiverUser.getId());
            checkForConversationRemotely(receiverUser.getId(),preferenceManager.getString(FinalConstants.KEY_USER_ID));
        }
    }
    public void checkForConversationRemotely(String sendId,String receiverId){
        database.collection(FinalConstants.KET_COLLECTION_CONVERSATION)
                .whereEqualTo(FinalConstants.KEY_SENDER_ID,sendId)
                .whereEqualTo(FinalConstants.KEY_RECEIVER_ID,receiverId)
                .get()
                .addOnCompleteListener(conversationListener);
    }


     final OnCompleteListener<QuerySnapshot> conversationListener=task->{
        if(task.isSuccessful() && task.getResult()!=null &&task.getResult().getDocuments().size()>0){
            DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
            conversationId=documentSnapshot.getId();
        }
    };

}