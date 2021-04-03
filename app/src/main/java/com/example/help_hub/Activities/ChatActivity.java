package com.example.help_hub.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.help_hub.Adapters.MessagesAdapter;
import com.example.help_hub.AlertDialogues.LoadingDialog;
import com.example.help_hub.AlertDialogues.RatingDialog;
import com.example.help_hub.OtherClasses.Category;
import com.example.help_hub.OtherClasses.ChatMessage;
import com.example.help_hub.OtherClasses.NeedHelp;
import com.example.help_hub.OtherClasses.User;
import com.example.help_hub.R;
import com.example.help_hub.Singletones.UserDatabase;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.firestore.*;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChatActivity extends AppCompatActivity implements MessagesAdapter.onMessageClickListener, RatingDialog.ratingChangedListener {

    private ImageView avatarImage;
    private TextView nameText, titleText;
    private RecyclerView messageList;
    MessagesAdapter messageAdapter;
    private EditText messageEdit;
    private Button sendButton;
    private FirebaseAuth firebaseAuth;
    public static final String NEED_HELP_ID_EXTRA = "needhelpidextra", TITLE_EXTRA = "titleextra", THIS_USER_ID_EXTRA = "useridextra",
            OTHER_USER_NAME_EXTRA = "usernameextra", CHAT_ID_EXTRA = "chatidextra", CHAT_TYPE_EXTRA = "chattypeextra";
    private User loggedUser;
    private String Id, Title, userId, userName, otherUserId;

    List<ChatMessage> messages;

    DatabaseReference reference;
    ValueEventListener messagesEventListener;
    SimpleDateFormat format;
    LoadingDialog loadingDialog;

    String chatId;

    //New functionality
    private LinearLayout performerActionsLinearLayout;
    private Button performerActionsButton;
    private TextView performerActionsTextView;
    private NeedHelp needHelp;
    private String chatType;

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


        //New functionality
        performerActionsLinearLayout = findViewById(R.id.performer_actions_linear_layout);
        performerActionsButton = findViewById(R.id.performer_actions_button);
        performerActionsTextView = findViewById(R.id.performer_actions_text_view);
        chatType = getIntent().getStringExtra(CHAT_TYPE_EXTRA);
        if(chatType.equals("NH")) {
            needHelp = new NeedHelp();
            getNeedHelp();
        }


        titleText.setText(Title);
        nameText.setText(userName);

        chatId = getIntent().getStringExtra(CHAT_ID_EXTRA);
        if (chatId == null) {
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

        if (messages.size() == 0) {
            DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(userId)
                    .collection("chats").document(chatId);
            HashMap<String, Object> chatMap = new HashMap<>();
            chatMap.put("offer id", Id);
            chatMap.put("other user id", otherUserId);
            chatMap.put("chat type", chatType);
            docRef.set(chatMap);

            docRef = FirebaseFirestore.getInstance().collection("users").document(otherUserId)
                    .collection("chats").document(chatId);
            chatMap.clear();
            chatMap.put("offer id", Id);
            chatMap.put("other user id", userId);
            chatMap.put("chat type", chatType);
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


    //New functionality
    private void getNeedHelp() {
        FirebaseFirestore.getInstance().collection("announcement").document(Id).addSnapshotListener((documentSnapshot, e) -> {
            needHelp = documentSnapshot.toObject(NeedHelp.class);
            checkData();
        });
    }

    private void checkData() {

        performerActionsLinearLayout.setVisibility(View.VISIBLE);

        if (needHelp.getUserId().equals(userId)) {


            performerActionsButton.setVisibility(View.VISIBLE);

            if (needHelp.getPerformerId() == null || needHelp.getPerformerId().isEmpty()) {
                performerActionsTextView.setText(R.string.assign_as_a_performer);
                performerActionsButton.setText(R.string.assign);
                performerActionsButton.setOnClickListener(v -> {
                    assignPerformer();
                });
            } else if (needHelp.getPerformerId().equals(otherUserId)) {

                switch (needHelp.getStatus()) {
                    case "In progress":
                        performerActionsTextView.setText(R.string.this_user_assigned_as_the_performer);
                        performerActionsButton.setVisibility(View.GONE);
                        break;
                    case "Done":
                        performerActionsTextView.setText(R.string.work_is_done);
                        performerActionsButton.setText(R.string.accept);
                        performerActionsButton.setOnClickListener(v -> {
                            acceptWork();
                        });
                        break;
                    case "Closed":
                        performerActionsTextView.setText(R.string.announcement_closed);
                        performerActionsButton.setVisibility(View.GONE);
                        break;
                }


            } else {
                performerActionsTextView.setText(R.string.another_performer);
                performerActionsButton.setVisibility(View.GONE);
            }

        } else {
            if (needHelp.getPerformerId() != null && needHelp.getPerformerId().equals(userId)) {

                performerActionsLinearLayout.setVisibility(View.VISIBLE);

                if(needHelp.getStatus() != null){

                    switch (needHelp.getStatus()){

                        case "In progress":
                            performerActionsTextView.setText(R.string.you_are_assigned_as_the_performer);
                            performerActionsButton.setVisibility(View.VISIBLE);
                            performerActionsButton.setText(R.string.done);
                            performerActionsButton.setOnClickListener(v -> {
                                workIsDone();
                            });
                            break;
                        case "Done":
                            performerActionsTextView.setText(R.string.wait_while_work_confirmed);
                            performerActionsButton.setVisibility(View.GONE);
                            break;
                        case "Closed":
                            performerActionsTextView.setText(R.string.announcement_closed);
                            performerActionsButton.setVisibility(View.GONE);
                            break;

                    }
                }
            } else {

                performerActionsButton.setVisibility(View.GONE);

                if(needHelp.getStatus() != null){

                    switch (needHelp.getStatus()){

                        case "In progress":
                        case "Done":
                            performerActionsTextView.setText(R.string.performer_selected);
                            break;
                        case "Closed":
                            performerActionsTextView.setText(R.string.announcement_closed);
                            break;

                    }
                }else{
                    performerActionsLinearLayout.setVisibility(View.GONE);
                }
            }
        }
    }

    private void assignPerformer() {

        Map<String, Object> needHelpMap = new HashMap<>();
        needHelpMap.put("PerformerId", otherUserId);
        needHelpMap.put("Status", "In progress");

        FirebaseFirestore.getInstance().collection("announcement").document(Id).update(needHelpMap)
                .addOnSuccessListener(unused -> {
                    needHelp.setPerformerId(otherUserId);
                    needHelp.setStatus("In progress");
                    Toast.makeText(getApplicationContext(), getString(R.string.performer_assigned), Toast.LENGTH_SHORT).show();
                    checkData();
                }).addOnFailureListener(e -> {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void workIsDone() {
        FirebaseFirestore.getInstance().collection("announcement").document(Id).update("Status", "Done")
                .addOnSuccessListener(unused -> {
                    needHelp.setStatus("Done");
                    Toast.makeText(getApplicationContext(), getString(R.string.wait_while_work_confirmed), Toast.LENGTH_SHORT).show();
                    checkData();
                }).addOnFailureListener(e -> {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void acceptWork() {
        RatingDialog ratingDialog = new RatingDialog(otherUserId, userId, this);
        ratingDialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void ratingChanged() {
        FirebaseFirestore.getInstance().collection("announcement").document(Id).update("Status", "Closed");
        performerActionsLinearLayout.setVisibility(View.GONE);
    }
}
