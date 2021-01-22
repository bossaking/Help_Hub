package com.example.help_hub.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.help_hub.Adapters.MessagesAdapter;
import com.example.help_hub.AlertDialogues.LoadingDialog;
import com.example.help_hub.OtherClasses.ChatMessage;
import com.example.help_hub.OtherClasses.NeedHelp;
import com.example.help_hub.OtherClasses.User;
import com.example.help_hub.R;
import com.example.help_hub.Singletones.UserDatabase;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChatActivity extends AppCompatActivity implements MessagesAdapter.onMessageClickListener {

    private ImageView avatarImage;
    private TextView nameText, titleText;
    private RecyclerView messageList;
    MessagesAdapter messageAdapter;
    private EditText messageEdit;
    private Button sendButton;
    private FirebaseAuth firebaseAuth;
    public static final String NEED_HELP_ID_EXTRA = "needhelpidextra", TITLE_EXTRA = "titleextra", THIS_USER_ID_EXTRA = "useridextra",
            OTHER_USER_NAME_EXTRA = "usernameextra", CHAT_ID_EXTRA = "chatidextra" ;
    private User loggedUser;
    private String Id, Title, userId, userName, otherUserId;

    List<ChatMessage> messages;

    DatabaseReference reference;
    ValueEventListener messagesEventListener;
    SimpleDateFormat format;
    LoadingDialog loadingDialog;

    String chatId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        loadingDialog = new LoadingDialog(this);
        loadingDialog.StartLoadingDialog();

        firebaseAuth = FirebaseAuth.getInstance();
        format = new SimpleDateFormat("HH:mm");
        Id = getIntent().getStringExtra(NEED_HELP_ID_EXTRA);
        Title = getIntent().getStringExtra(TITLE_EXTRA);
        otherUserId = getIntent().getStringExtra(THIS_USER_ID_EXTRA);
        userId = firebaseAuth.getUid();
        userName = getIntent().getStringExtra(OTHER_USER_NAME_EXTRA);

        UserDatabase userDatabase = UserDatabase.getInstance(this, firebaseAuth.getUid());
        loggedUser = userDatabase.getUser();

        avatarImage = findViewById(R.id.avatar_image_view);
        nameText = findViewById(R.id.name_text_view);
        titleText = findViewById(R.id.title_text_view);
        messageList = findViewById(R.id.messages_list);
        messageList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        messageList.setLayoutManager(linearLayoutManager);
        messageEdit = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);

        titleText.setText(Title);
        nameText.setText(userName);

        chatId = getIntent().getStringExtra(CHAT_ID_EXTRA);
        if(chatId == null){
            chatId = FirebaseDatabase.getInstance().getReference("chat").push().getKey();
        }

        messages = new ArrayList<>();

        messageAdapter = new MessagesAdapter(this, messages, userId, this);

        messageList.setAdapter(messageAdapter);

        sendButton.setOnClickListener(v -> {
            if (messageEdit.getText().toString().trim().equals("") || messageEdit.getText().equals(null))
                return;
            sendMessage(userId, messageEdit.getText().toString().trim());
        });

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference imgRef = storageReference.child("users/" + otherUserId + "/profile.jpg");
        imgRef.getDownloadUrl().addOnSuccessListener(v -> {
            Glide.with(getApplicationContext()).load(v).placeholder(R.drawable.image_with_progress).error(R.drawable.broken_image_24).into(avatarImage);
        }).addOnFailureListener(v -> {
            avatarImage.setImageResource(R.drawable.default_user_image);
        });

        readMessages();
    }

    private void sendMessage(String sender, String message) {

        if(messages.size() == 0) {
            DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(userId)
                    .collection("chats").document(chatId);
            HashMap<String, Object> chatMap = new HashMap<>();
            chatMap.put("offer id", Id);
            chatMap.put("other user id", otherUserId);
            docRef.set(chatMap);

            docRef = FirebaseFirestore.getInstance().collection("users").document(otherUserId)
                    .collection("chats").document(chatId);
            chatMap.clear();
            chatMap.put("offer id", Id);
            chatMap.put("other user id", userId);
            docRef.set(chatMap);
        }

        reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> messageMap = new HashMap<>();
        messageMap.put("userId", sender);
        messageMap.put("message", message);
        messageMap.put("time", localToUTC("HH:mm", format.format(new Date())));

        reference.child("Chats").child(chatId).push().setValue(messageMap);

        messageEdit.setText("");
    }

    private void readMessages() {

        reference = FirebaseDatabase.getInstance().getReference("Chats").child(chatId);
        messagesEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                messages.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {

                    ChatMessage message = snap.getValue(ChatMessage.class);
                    Objects.requireNonNull(message).setTime(uTCToLocal("HH:mm", "HH:mm", message.getTime()));
                    messages.add(message);
                    messageAdapter.notifyDataSetChanged();
                }
                loadingDialog.DismissDialog();

                messageList.scrollToPosition(messages.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        };
        reference.addValueEventListener(messagesEventListener);
    }

    //Methods for time convertation
    public String localToUTC(String dateFormat, String datesToConvert) {


        String dateToReturn = datesToConvert;

        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setTimeZone(TimeZone.getDefault());
        Date gmt;

        SimpleDateFormat sdfOutPutToSend = new SimpleDateFormat(dateFormat);
        sdfOutPutToSend.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {

            gmt = sdf.parse(datesToConvert);
            dateToReturn = sdfOutPutToSend.format(Objects.requireNonNull(gmt));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateToReturn;
    }

    public static String uTCToLocal(String dateFormatInPut, String dateFormatOutPut, String datesToConvert) {


        String dateToReturn = datesToConvert;

        SimpleDateFormat sdf = new SimpleDateFormat(dateFormatInPut);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date gmt;

        SimpleDateFormat sdfOutPutToSend = new SimpleDateFormat(dateFormatOutPut);
        sdfOutPutToSend.setTimeZone(TimeZone.getDefault());

        try {

            gmt = sdf.parse(datesToConvert);
            dateToReturn = sdfOutPutToSend.format(Objects.requireNonNull(gmt));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateToReturn;
    }

    @Override
    public void onMessageClick(int position) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.setData(Uri.parse(messages.get(position).getMessage()));
            startActivity(intent);
        } catch (Exception ignored) {

        }
    }
}
