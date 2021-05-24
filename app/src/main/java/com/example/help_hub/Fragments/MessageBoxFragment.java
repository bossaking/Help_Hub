package com.example.help_hub.Fragments;

import android.annotation.SuppressLint;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.help_hub.Activities.ChatActivity;
import com.example.help_hub.Activities.MainActivity;
import com.example.help_hub.Activities.RegistrationActivity;

import com.example.help_hub.AlertDialogues.LoadingDialog;
import com.example.help_hub.OtherClasses.Chat;
import com.example.help_hub.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
//import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MessageBoxFragment extends Fragment {

    private String userId;
    private List<Chat> chatListMain;

    private RecyclerView recyclerView;
    private ChatAdapter adapter;

    private DatabaseReference databaseReference;

    private LoadingDialog dataLoadingDialog;

    private Activity myActivity;
    private Context myContext;

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

        CollectionReference chatsRef = FirebaseFirestore.getInstance().collection("users").document(userId).collection("chats");

        dataLoadingDialog = new LoadingDialog(getActivity());
        dataLoadingDialog.StartLoadingDialog();
        chatsRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (queryDocumentSnapshots.getDocumentChanges().size() == 0 || queryDocumentSnapshots.isEmpty())
                dataLoadingDialog.DismissDialog();
            else {
                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    switch (dc.getType()) {
                        case ADDED:
                            QueryDocumentSnapshot doc = dc.getDocument();
                            DocumentReference documentReference;
                            String type = doc.getString("chat type");
                            String id = doc.getString("offer id");

                            switch (type){

                                case "NH":

                                    documentReference = FirebaseFirestore.getInstance().collection("announcement").document(id);
                                    documentReference.get().addOnSuccessListener(documentSnapshot -> {
                                        if(documentSnapshot.exists()){
                                            addNewChatItem(doc);
                                        }else{
                                            chatsRef.document(doc.getId()).delete();
                                        }
                                    });

                                    break;
                                case "WTH":
                                    documentReference = FirebaseFirestore.getInstance().collection("offers").document(id);
                                    documentReference.get().addOnSuccessListener(documentSnapshot -> {
                                        if(documentSnapshot.exists()){
                                            addNewChatItem(doc);
                                        }else{
                                            chatsRef.document(doc.getId()).delete();
                                        }
                                    });
                                    break;
                            }
                            break;
                        case MODIFIED:
                            doc = dc.getDocument();
                            if(doc.getBoolean("has unread messages") && chatListMain.size() > 0) {
                                chatListMain.get(dc.getOldIndex()).setHasUnreadMessages(true);
                            }else if(chatListMain.size() > 0){
                                chatListMain.get(dc.getOldIndex()).setHasUnreadMessages(false);
                                ((MaterialCardView)recyclerView.getChildAt(dc.getOldIndex()))
                                        .setStrokeColor(ContextCompat.getColor(requireContext(), R.color.fui_transparent));
                            }
                            adapter.notifyDataSetChanged();
                            break;
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
                DocumentReference offerRef = FirebaseFirestore.getInstance()
                        .collection("offers").document(chat.getOfferId());
                offerRef.get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task1.getResult();

                        if (documentSnapshot.getString("Title") != null) {
                            String Title = documentSnapshot.getString("Title");
                            if (Title.length() > 20) Title = Title.substring(0, 20) + "...";
                            holder.offerTitle.setText(Title);
                            getOtherUserData(holder, position);
                        } else {
                            DocumentReference announcementRef = FirebaseFirestore.getInstance()
                                    .collection("announcement").document(chat.getOfferId());
                            announcementRef.get().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot ds = task.getResult();
                                    String Title = ds.getString("Title");
                                    if (Title.length() > 20) Title = Title.substring(0, 20) + "...";
                                    holder.offerTitle.setText(Title);
                                    getOtherUserData(holder, position);
                                }
                            });
                        }
                    }
                });
            }
        }

        @SuppressLint("ResourceAsColor")
        private void getOtherUserData(ChatHolder holder, int position) {
            Chat chat = chatListMain.get(position);

            //Pobieranie danych innego użytkownika
            DocumentReference userRef = FirebaseFirestore.getInstance()
                    .collection("users").document(chat.getOtherUserId());
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot docSnap = task.getResult();
                    holder.userName.setText(docSnap.getString("Name"));

                    StorageReference avatarRef = FirebaseStorage.getInstance()
                            .getReference().child("users/" + chat.getOtherUserId() + "/profile.jpg");
                    avatarRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> Glide.with(getActivity())
                                    .load(uri).placeholder(R.drawable.image_with_progress)
                                    .error(R.drawable.broken_image_24).into(holder.avatar));

                    if(chat.isHasUnreadMessages()){
                        holder.cardView.setStrokeColor(ContextCompat.getColor(getContext(), R.color.yellowSecondColor));
                    }

                    dataLoadingDialog.DismissDialog();
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
            MaterialCardView cardView;

            @SuppressLint("ResourceAsColor")
            public ChatHolder(@NonNull View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);

                cardView = (MaterialCardView)itemView;
                offerTitle = itemView.findViewById(R.id.item_message_box_title);
                userName = itemView.findViewById(R.id.item_message_box_user_name);
                avatar = itemView.findViewById(R.id.item_message_box_avatar);
            }

            @Override
            public void onClick(View v) {
                String offerId = chatListMain.get(getAdapterPosition()).getOfferId();
                String type = chatListMain.get(getAdapterPosition()).getChatType();

                DocumentReference documentReference;

                switch (type){
                    case "NH":

                        documentReference = FirebaseFirestore.getInstance().collection("announcement").document(offerId);
                        documentReference.get().addOnSuccessListener(documentSnapshot -> {
                            if(!documentSnapshot.exists()){
                                chatList.remove(getAdapterPosition());
                                adapter.notifyDataSetChanged();
                                Toast.makeText(myContext, "This announcement not exists", Toast.LENGTH_LONG).show();
                            }else{
                                openChat(v);
                            }
                        });

                        break;
                    case "WTH":
                        documentReference = FirebaseFirestore.getInstance().collection("offers").document(offerId);
                        documentReference.get().addOnSuccessListener(documentSnapshot -> {
                            if(!documentSnapshot.exists()){
                                chatList.remove(getAdapterPosition());
                                adapter.notifyDataSetChanged();
                                Toast.makeText(myContext, "This offer not exists", Toast.LENGTH_LONG).show();
                            }else{
                                openChat(v);
                            }
                        });
                        break;
                }


            }

            private void openChat(View v){



                Intent intent = new Intent(myContext, ChatActivity.class);
                intent.putExtra(ChatActivity.NEED_HELP_ID_EXTRA, chatListMain.get(getAdapterPosition()).getOfferId());
                intent.putExtra(ChatActivity.TITLE_EXTRA, offerTitle.getText());
                intent.putExtra(ChatActivity.THIS_USER_ID_EXTRA, chatListMain.get(getAdapterPosition()).getOtherUserId());
                intent.putExtra(ChatActivity.OTHER_USER_NAME_EXTRA, userName.getText());
                intent.putExtra(ChatActivity.CHAT_ID_EXTRA, chatListMain.get(getAdapterPosition()).getChatId());
                intent.putExtra(ChatActivity.CHAT_TYPE_EXTRA, chatListMain.get(getAdapterPosition()).getChatType());

                chatListMain.get(getAdapterPosition()).setHasUnreadMessages(false);
                ((MaterialCardView)v).setStrokeColor(ContextCompat.getColor(getContext(), R.color.fui_transparent));
                adapter.notifyDataSetChanged();

                myActivity.startActivityForResult(intent, 001);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {

        if(requestCode == 001){
            ((MainActivity)myActivity).updateUnreadConversationsNumber();
        }


        super.onActivityResult(requestCode, resultCode, data);


    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) myActivity).getSupportActionBar().hide();
        ((MainActivity)myActivity).calculateUnreadConversations();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((AppCompatActivity) myActivity).getSupportActionBar().show();
    }

    private void addNewChatItem(QueryDocumentSnapshot doc){
        Chat chat = new Chat();
        chat.setChatId(doc.getId());
        chat.setOtherUserId(doc.getString("other user id"));
        chat.setOfferId(doc.getString("offer id"));
        chat.setChatType(doc.getString("chat type"));
        if(doc.contains("has unread messages")) {
            chat.setHasUnreadMessages(doc.getBoolean("has unread messages"));
        }else{
            chat.setHasUnreadMessages(false);
        }
        chatListMain.add(chat);
        adapter.notifyDataSetChanged();
    }
}