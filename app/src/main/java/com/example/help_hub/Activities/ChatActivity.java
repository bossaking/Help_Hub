package com.example.help_hub.Activities;

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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.help_hub.OtherClasses.ChatMessage;
import com.example.help_hub.OtherClasses.NeedHelp;
import com.example.help_hub.OtherClasses.User;
import com.example.help_hub.R;
import com.example.help_hub.Singletones.UserDatabase;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ChatActivity extends AppCompatActivity {

    private ImageView avatarImage;
    private TextView nameText, titleText;
    private ListView messageList;
    private FirebaseListAdapter<ChatMessage> adapter;
    private EditText messageEdit;
    private Button sendButton;
    public static final String NEED_HELP_ID_EXTRA = "needhelpidextra", TITLE_EXTRA = "titleextra", OTHER_USER_ID_EXTRA = "useridextra", OTHER_USER_NAME_EXTRA = "usernameextra" ;
    private User loggedUser;
    private String Id, Title, userId, userName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Id = getIntent().getStringExtra(NEED_HELP_ID_EXTRA);
        Title = getIntent().getStringExtra(TITLE_EXTRA);
        userId = getIntent().getStringExtra(OTHER_USER_ID_EXTRA);
        userName = getIntent().getStringExtra(OTHER_USER_NAME_EXTRA);

        UserDatabase userDatabase = UserDatabase.getInstance(this, FirebaseAuth.getInstance().getUid());
        loggedUser = userDatabase.getUser();

        avatarImage = findViewById(R.id.avatar_image_view);
        nameText = findViewById(R.id.name_text_view);
        titleText = findViewById(R.id.title_text_view);
        messageList = findViewById(R.id.messages_list);
        messageEdit = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);

        titleText.setText(Title);
        nameText.setText(userName);

        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class, R.layout.item_message, FirebaseDatabase.getInstance().getReference(Id + userId)) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                TextView userNameText = v.findViewById(R.id.user_name_text_view);
                TextView messageText = v.findViewById(R.id.message_text_text_view);

                if (model.getUserId().equals(loggedUser.getId())) {
                    ((LinearLayout)v.findViewById(R.id.message_item_linear_layout)).setGravity(Gravity.RIGHT);
                    userNameText.setText(loggedUser.getName());
                } else {
                    userNameText.setText(userName);
                }

                messageText.setText(model.getMessage());
            }
        };

        messageList.setAdapter(adapter);

        sendButton.setOnClickListener(v -> {
            if (messageEdit.getText().equals("") || messageEdit.getText().equals(null))
                return;
            FirebaseDatabase.getInstance().getReference(Id + userId).push().setValue(new ChatMessage(messageEdit.getText().toString(), loggedUser.getId()));
            messageEdit.setText("");
        });

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference imgRef = storageReference.child("users/" + userId + "/profile.jpg");
        imgRef.getDownloadUrl().addOnSuccessListener(v -> {
            Glide.with(getApplicationContext()).load(v).placeholder(R.drawable.image_with_progress).error(R.drawable.broken_image_24).into(avatarImage);
        }).addOnFailureListener(v -> {
            avatarImage.setImageResource(R.drawable.default_user_image);
        });
    }
}