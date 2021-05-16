package com.example.help_hub.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.help_hub.Adapters.MessagesAdapter;
import com.example.help_hub.AlertDialogues.LoadingDialog;
import com.example.help_hub.AlertDialogues.RatingDialog;
import com.example.help_hub.OtherClasses.*;
import com.example.help_hub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ChatActivity extends AppCompatActivity implements MessagesAdapter.onMessageClickListener, RatingDialog.ratingChangedListener {

    public static final String NEED_HELP_ID_EXTRA = "needhelpidextra",
            TITLE_EXTRA = "titleextra",
            THIS_USER_ID_EXTRA = "useridextra",
            OTHER_USER_NAME_EXTRA = "usernameextra",
            CHAT_ID_EXTRA = "chatidextra",
            CHAT_TYPE_EXTRA = "chattypeextra";

    private ImageView avatarImage;
    private TextView nameText, titleText;
    private EditText messageEdit;
    private Button sendButton;
    private ImageButton attachButton;

    private String chatId, Id, Title, userId, userName, otherUserId;
    private List<ChatMessage> messages;

    private FirebaseAuth firebaseAuth;

    private RecyclerView messageList;
    private MessagesAdapter messageAdapter;

    private DatabaseReference reference;
    private ValueEventListener messagesEventListener;
    private SimpleDateFormat format;
    private LoadingDialog loadingDialog;

    private Context context;

    //New functionality
    private LinearLayout performerActionsLinearLayout;
    private Button performerActionsButton, performerActionsSecondButton;
    private TextView performerActionsTextView;
    private NeedHelp needHelp;
    private WantToHelp wantToHelp;
    private String chatType;

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        context = getApplicationContext();

        loadingDialog = new LoadingDialog(this);
        loadingDialog.StartLoadingDialog();

        firebaseAuth = FirebaseAuth.getInstance();
        userId = firebaseAuth.getUid();

        format = new SimpleDateFormat("HH:mm");

        Id = getIntent().getStringExtra(NEED_HELP_ID_EXTRA);
        Title = getIntent().getStringExtra(TITLE_EXTRA);
        otherUserId = getIntent().getStringExtra(THIS_USER_ID_EXTRA);
        userName = getIntent().getStringExtra(OTHER_USER_NAME_EXTRA);

        avatarImage = findViewById(R.id.avatar_image_view);
        nameText = findViewById(R.id.name_text_view);
        titleText = findViewById(R.id.title_text_view);
        messageList = findViewById(R.id.messages_list);
        messageList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setStackFromEnd(true);

        messageList.setLayoutManager(linearLayoutManager);
        messageEdit = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);
        attachButton = findViewById(R.id.attach_button);
        attachButton.setOnClickListener(view -> CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON).setCropShape(CropImageView.CropShape.RECTANGLE)
                .start(this));

        //New functionality
        performerActionsLinearLayout = findViewById(R.id.performer_actions_linear_layout);
        performerActionsButton = findViewById(R.id.performer_actions_button);
        performerActionsSecondButton = findViewById(R.id.performer_actions_second_button);
        performerActionsTextView = findViewById(R.id.performer_actions_text_view);

        chatId = getIntent().getStringExtra(CHAT_ID_EXTRA);
        chatType = getIntent().getStringExtra(CHAT_TYPE_EXTRA);

        if (chatType.equals("NH")) {
            needHelp = new NeedHelp();
            getNeedHelp();
        } else if (chatType.equals("WTH")) {
            wantToHelp = new WantToHelp();
            getWantToHelp();
        }

        titleText.setText(Title);
        nameText.setText(userName);

        if (chatId == null)
            chatId = FirebaseDatabase.getInstance().getReference("chat").push().getKey();

        messages = new ArrayList<>();
        messageAdapter = new MessagesAdapter(this, messages, userId, this);
        messageList.setAdapter(messageAdapter);

        sendButton.setOnClickListener(v -> {
            if (messageEdit.getText().toString().trim().equals("") || messageEdit.getText() == null)
                return;
            sendMessage(userId, messageEdit.getText().toString().trim(), "Text");
        });

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference imgRef = storageReference.child("users/" + otherUserId + "/profile.jpg");
        imgRef.getDownloadUrl()
                .addOnSuccessListener(v -> Glide.with(context).load(v).placeholder(R.drawable.image_with_progress).error(R.drawable.broken_image_24).into(avatarImage))
                .addOnFailureListener(v -> avatarImage.setImageResource(R.drawable.default_user_image));

        readMessages();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                sendMessage(userId, resultUri.toString(), "Image");
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
                Toast.makeText(this, R.string.error + result.getError().getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void sendMessage(String sender, String message, String type) {
        if (messages.size() == 0) {
            DocumentReference docRef = FirebaseFirestore.getInstance().collection("users")
                    .document(userId).collection("chats").document(chatId);

            HashMap<String, Object> chatMap = new HashMap<>();
            chatMap.put("offer id", Id);
            chatMap.put("other user id", otherUserId);
            chatMap.put("chat type", chatType);

            docRef.set(chatMap);
            docRef = FirebaseFirestore.getInstance().collection("users")
                    .document(otherUserId).collection("chats").document(chatId);

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
        messageMap.put("type", type);

        if (type.equals("Image")) {
            StorageReference storage = FirebaseStorage.getInstance().getReference().child("chats/" + chatId + "/" + Uri.parse(message).getLastPathSegment());

            storage.putFile(Uri.parse(message))
                    .addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show();
                        reference.child("Chats").child(chatId).push().setValue(messageMap);
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, R.string.error + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
        } else reference.child("Chats").child(chatId).push().setValue(messageMap);

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

                    if (message.getType().equals("Image")) {
                        StorageReference storage = FirebaseStorage.getInstance().getReference()
                                .child("chats/" + chatId + "/" + Uri.parse(message.getMessage()).getLastPathSegment());

                        storage.getDownloadUrl().addOnSuccessListener(uri -> {
                            message.setMessage(uri.toString());
                            messageAdapter.notifyDataSetChanged();
                        });
                    } else messageAdapter.notifyDataSetChanged();
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
        if (messages.get(position).getType().equals("Image")) {
            Intent intent = new Intent(this, FullscreenImageActivity.class);
            intent.putExtra("image", messages.get(position).getMessage());
            startActivity(intent);
        } else {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                intent.setData(Uri.parse(messages.get(position).getMessage()));
                startActivity(intent);
            } catch (Exception ignored) {
            }
        }
    }

    //New functionality
    private void getNeedHelp() {
        FirebaseFirestore.getInstance().collection("announcement").document(Id)
                .addSnapshotListener((documentSnapshot, e) -> {
                    needHelp = documentSnapshot.toObject(NeedHelp.class);
                    checkDataNeedHelp();
                });
    }

    private void getWantToHelp() {
        FirebaseFirestore.getInstance().collection("offers").document(Id)
                .addSnapshotListener((documentSnapshot, e) -> {
                    wantToHelp = documentSnapshot.toObject(WantToHelp.class);
                    getConfirmedUsers();
                });
    }

    @SuppressLint("SetTextI18n")
    private void checkDataNeedHelp() {
        performerActionsLinearLayout.setVisibility(View.VISIBLE);
        performerActionsButton.setBackgroundColor(getColor(R.color.greenButtonColor));
        performerActionsSecondButton.setVisibility(View.GONE);

        if (needHelp.getUserId().equals(userId)) {
            performerActionsButton.setVisibility(View.VISIBLE);

            if (needHelp.getPerformerId() == null || needHelp.getPerformerId().isEmpty()) {
                performerActionsTextView.setText(R.string.assign_as_a_performer);
                performerActionsButton.setText(R.string.assign);
                performerActionsButton.setOnClickListener(v -> assignPerformer());
            } else if (needHelp.getPerformerId().equals(otherUserId)) {
                switch (needHelp.getStatus()) {
                    case "In progress":
                        performerActionsTextView.setText(R.string.this_user_assigned_as_the_performer);
                        performerActionsButton.setText(R.string.cancel);
                        performerActionsButton.setBackgroundColor(getColor(R.color.redColor));
                        performerActionsButton.setOnClickListener(v -> cancelPerformer());
                        break;

                    case "Done":
                        performerActionsTextView.setText(R.string.work_is_done);
                        performerActionsButton.setText(R.string.accept);
                        performerActionsButton.setOnClickListener(v -> acceptWork());
                        break;

                    case "Canceled":
                        performerActionsTextView.setText(R.string.work_canceled_by_performer);
                        performerActionsButton.setText(R.string.accept);
                        performerActionsButton.setOnClickListener(v -> acceptCanceledWork());
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

                if (needHelp.getStatus() != null) {
                    switch (needHelp.getStatus()) {
                        case "In progress":
                            performerActionsTextView.setText(R.string.you_are_assigned_as_the_performer);
                            performerActionsButton.setVisibility(View.VISIBLE);
                            performerActionsButton.setText(R.string.done);
                            performerActionsButton.setOnClickListener(v -> workIsDone());
                            performerActionsSecondButton.setVisibility(View.VISIBLE);
                            performerActionsSecondButton.setOnClickListener(v -> cancelWork());
                            break;

                        case "Done":
                        case "Canceled":
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

                if (needHelp.getStatus() != null) {
                    switch (needHelp.getStatus()) {
                        case "Available":
                            performerActionsLinearLayout.setVisibility(View.GONE);
                            break;

                        case "In progress":
                        case "Canceled":
                        case "Done":
                            performerActionsTextView.setText(R.string.performer_selected);
                            break;

                        case "Closed":
                            performerActionsTextView.setText(R.string.announcement_closed);
                            break;
                    }
                } else performerActionsLinearLayout.setVisibility(View.GONE);
            }
        }
    }

    private void getConfirmedUsers() {
        FirebaseFirestore.getInstance().collection("offers").document(Id).collection("confirmedUsers")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    List<ConfirmedUser> confirmedUsers = new ArrayList<>();

                    for (DocumentSnapshot ds : queryDocumentSnapshots) {
                        confirmedUsers.add(ds.toObject(ConfirmedUser.class));
                    }

                    checkDataWantToHelp(confirmedUsers);
                });
    }

    private void checkDataWantToHelp(List<ConfirmedUser> confirmedUsers) {
        if (wantToHelp.getUserId().equals(userId)) {
            performerActionsLinearLayout.setVisibility(View.VISIBLE);

            List<ConfirmedUser> confirmedUserList = confirmedUsers.stream().filter(u -> u.getUserId().equals(otherUserId)).collect(Collectors.toList());

            if (confirmedUserList.size() == 0) {
                performerActionsTextView.setText(R.string.help_this_person_question);
                performerActionsButton.setText(R.string.help);
                performerActionsButton.setVisibility(View.VISIBLE);
                performerActionsButton.setOnClickListener(v -> helpThisPerson());
            } else {
                performerActionsTextView.setText(R.string.already_agree_to_help);
                performerActionsButton.setVisibility(View.GONE);
            }
        } else {
            List<ConfirmedUser> confirmedUserList = confirmedUsers.stream().filter(u -> u.getUserId().equals(userId)).collect(Collectors.toList());

            if (confirmedUserList.size() > 0) {
                ConfirmedUser confirmedUser = confirmedUserList.get(0);

                performerActionsLinearLayout.setVisibility(View.VISIBLE);

                if (confirmedUser.isOpinionSended()) {
                    performerActionsTextView.setText(R.string.already_rate_performer);
                    performerActionsButton.setVisibility(View.GONE);
                } else {
                    performerActionsTextView.setText(R.string.rate_help_level);
                    performerActionsButton.setText(R.string.rate);
                    performerActionsButton.setOnClickListener(v -> rateHelpLevel(confirmedUser));
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
                    Toast.makeText(context, getString(R.string.performer_assigned), Toast.LENGTH_SHORT).show();
                    checkDataNeedHelp();
                })
                .addOnFailureListener(e -> Toast.makeText(context, R.string.error + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
    }

    private void cancelPerformer() {
        Map<String, Object> needHelpMap = new HashMap<>();
        needHelpMap.put("PerformerId", "");
        needHelpMap.put("Status", "Available");

        FirebaseFirestore.getInstance().collection("announcement").document(Id).update(needHelpMap)
                .addOnSuccessListener(unused -> {
                    needHelp.setPerformerId(null);
                    needHelp.setStatus("Available");
                    Toast.makeText(context, getString(R.string.remove_performer), Toast.LENGTH_SHORT).show();
                    checkDataNeedHelp();
                })
                .addOnFailureListener(e -> Toast.makeText(context, R.string.error + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
    }

    private void cancelWork() {
        Map<String, Object> needHelpMap = new HashMap<>();
        needHelpMap.put("Status", "Canceled");

        FirebaseFirestore.getInstance().collection("announcement").document(Id).update(needHelpMap)
                .addOnSuccessListener(unused -> {
                    needHelp.setStatus("Canceled");
                    Toast.makeText(context, getString(R.string.cancel_work), Toast.LENGTH_SHORT).show();
                    checkDataNeedHelp();
                })
                .addOnFailureListener(e -> Toast.makeText(context, R.string.error + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
    }

    private void workIsDone() {
        FirebaseFirestore.getInstance().collection("announcement").document(Id).update("Status", "Done")
                .addOnSuccessListener(unused -> {
                    needHelp.setStatus("Done");
                    Toast.makeText(context, getString(R.string.wait_while_work_confirmed), Toast.LENGTH_SHORT).show();
                    checkDataNeedHelp();
                })
                .addOnFailureListener(e -> Toast.makeText(context, R.string.error + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
    }

    private void acceptWork() {
        RatingDialog ratingDialog = new RatingDialog(otherUserId, userId, this);
        ratingDialog.setCancelable(false);
        ratingDialog.show(getSupportFragmentManager(), null);
    }

    private void acceptCanceledWork() {
        RatingDialog ratingDialog = new RatingDialog(otherUserId, userId, this);
        ratingDialog.setCancelable(false);
        ratingDialog.show(getSupportFragmentManager(), null);
    }

    private void helpThisPerson() {
        DocumentReference doc = FirebaseFirestore.getInstance().collection("offers").document(Id)
                .collection("confirmedUsers").document();

        ConfirmedUser confirmedUser = new ConfirmedUser();
        confirmedUser.setUserId(otherUserId);
        confirmedUser.setOpinionSended(false);
        confirmedUser.setId(doc.getId());

        FirebaseFirestore.getInstance().collection("offers").document(Id).collection("confirmedUsers").document(doc.getId()).set(confirmedUser)
                .addOnCompleteListener(task -> Toast.makeText(context, getString(R.string.thank_for_help), Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, R.string.error + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
    }

    private void rateHelpLevel(ConfirmedUser confirmedUser) {
        confirmedUser.setOpinionSended(true);

        FirebaseFirestore.getInstance().collection("offers").document(Id).collection("confirmedUsers").document(confirmedUser.getId()).set(confirmedUser)
                .addOnCompleteListener(task -> {
                    Toast.makeText(context, getString(R.string.thank_for_help), Toast.LENGTH_SHORT).show();
                    acceptWork();
                })
                .addOnFailureListener(e -> Toast.makeText(context, R.string.error + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
    }

    @Override
    public void ratingChanged() {
        if (chatType.equals("NH")) {
            if (needHelp.getStatus().equals("Canceled")) {
                needHelp.setStatus("Available");
                needHelp.setPerformerId(null);

                Map<String, Object> needHelpMap = new HashMap<>();
                needHelpMap.put("PerformerId", "");
                needHelpMap.put("Status", "Available");

                FirebaseFirestore.getInstance().collection("announcement").document(Id).update(needHelpMap);
            } else {
                FirebaseFirestore.getInstance().collection("announcement").document(Id).update("Status", "Closed");
                //performerActionsLinearLayout.setVisibility(View.GONE);
            }

            checkDataNeedHelp();
        } else if (chatType.equals("WTH")) performerActionsLinearLayout.setVisibility(View.GONE);
    }
}
