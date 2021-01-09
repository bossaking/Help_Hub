package com.example.help_hub.Fragments;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.help_hub.Activities.ChatActivity;
import com.example.help_hub.Activities.WantToHelpDetails;
import com.example.help_hub.OtherClasses.Chat;
import com.example.help_hub.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
//import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import de.hdodenhof.circleimageview.CircleImageView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MessageBoxFragment extends Fragment {

    private RecyclerView recyclerView;

    DatabaseReference databaseReference;

    private Activity myActivity;
    private Context myContext;

    private List<Chat> chatListMain;

    private ChatAdapter adapter;

    private String userId;

    //private FirebaseRecyclerOptions<Chat> options;
    //private FirebaseRecyclerAdapter<Chat, ChatHolder> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myActivity = getActivity();
        myContext = myActivity.getApplicationContext();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference("chat");

        recyclerView = view.findViewById(R.id.messages_recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(myContext));

        chatListMain = new ArrayList<>();

        adapter = new ChatAdapter(chatListMain);
        recyclerView.setAdapter(adapter);

        userId = FirebaseAuth.getInstance().getUid();

        CollectionReference chatsRef = FirebaseFirestore.getInstance().collection("users").document(userId)
                .collection("chats");

        chatsRef.get().addOnCompleteListener(task -> {
           if(task.isSuccessful()){
               for(QueryDocumentSnapshot doc : Objects.requireNonNull(task.getResult())){
                    Chat chat = new Chat();
                    chat.setChatId(doc.getId());
                    chat.setOtherUserId(doc.getString("other user id"));
                    chat.setOfferId(doc.getString("offer id"));

                    chatListMain.add(chat);
                    adapter.notifyDataSetChanged();
               }

           }
        });

        return view;
    }


    public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatHolder> {

        private List<Chat> chatList;

        public ChatAdapter(List<Chat> chatList) {
            this.chatList = chatList;
        }

        @NonNull
        @Override
        public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_box, parent, false);
            return new ChatHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatHolder holder, int position) {
            if (chatList != null) {
                Chat chat = chatList.get(position);

                //Pobieranie tytułu oferty
                DocumentReference offerRef = FirebaseFirestore.getInstance().collection("offers")
                        .document(chat.getOfferId());
                offerRef.get().addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task1.getResult();
                        if(documentSnapshot.getString("Title") != null) {
                            holder.offerTitle.setText(documentSnapshot.getString("Title"));
                            getOtherUserData(holder, position);
                        }else{
                            DocumentReference announcementRef = FirebaseFirestore.getInstance().collection("announcement")
                                    .document(chat.getOfferId());
                            announcementRef.get().addOnCompleteListener(task -> {
                                if(task.isSuccessful()){
                                    DocumentSnapshot ds = task.getResult();
                                    holder.offerTitle.setText(ds.getString("Title"));
                                    getOtherUserData(holder, position);
                                }
                            });
                        }
                    }
                });


            }
        }

        private void getOtherUserData(ChatHolder holder, int position){

            Chat chat = chatListMain.get(position);

            //Pobieranie danych innego użytkownika
            DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(chat.getOtherUserId());
            userRef.get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    DocumentSnapshot docSnap = task.getResult();
                    holder.userName.setText(docSnap.getString("Name"));

                    StorageReference avatarRef = FirebaseStorage.getInstance().getReference().child("users/" + chat.getOtherUserId() + "/profile.jpg");
                    avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Glide.with(getActivity()).load(uri).placeholder(R.drawable.image_with_progress).error(R.drawable.broken_image_24)
                                .into(holder.avatar);
                    });
                    if(position == chatListMain.size() - 1){
                        //TODO CLOSE LOADING DIALOG
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return chatList.size();
        }

        public class ChatHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView offerTitle, userName;
            CircleImageView avatar;

            public ChatHolder(@NonNull View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);

                offerTitle = itemView.findViewById(R.id.item_message_box_title);
                userName = itemView.findViewById(R.id.item_message_box_user_name);
                avatar = itemView.findViewById(R.id.item_message_box_avatar);
            }

            @Override
            public void onClick(View v) {

                String offerId = chatListMain.get(getAdapterPosition()).getOfferId();

                Intent intent = new Intent(myContext, ChatActivity.class);
                intent.putExtra(ChatActivity.NEED_HELP_ID_EXTRA, offerId);
                intent.putExtra(ChatActivity.TITLE_EXTRA, offerTitle.getText());
                intent.putExtra(ChatActivity.THIS_USER_ID_EXTRA, chatListMain.get(getAdapterPosition()).getOtherUserId());
                intent.putExtra(ChatActivity.OTHER_USER_NAME_EXTRA, userName.getText());
                intent.putExtra(ChatActivity.CHAT_ID_EXTRA, chatListMain.get(getAdapterPosition()).getChatId());
                myActivity.startActivity(intent);
            }
        }
    }
}