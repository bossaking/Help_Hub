package com.example.help_hub.Fragments;

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

import com.example.help_hub.Activities.ChatActivity;
import com.example.help_hub.Activities.WantToHelpDetails;
import com.example.help_hub.OtherClasses.Chat;
import com.example.help_hub.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
//import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MessageBoxFragment extends Fragment {

    private RecyclerView recyclerView;

    DatabaseReference databaseReference;

    private Activity myActivity;
    private Context myContext;

    private List<Chat> chatListMain;

    private ChatAdapter adapter;

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

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        Chat chat = new Chat();
                        chat.setChatId(npsnapshot.getKey());
                        chatListMain.add(chat);

                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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
                holder.offerTitle.setText(chat.getChatId());
            }
        }

        @Override
        public int getItemCount() {
            return chatList.size();
        }

        public class ChatHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView offerTitle, userName;

            public ChatHolder(@NonNull View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);

                offerTitle = itemView.findViewById(R.id.item_message_box_title);
                userName = itemView.findViewById(R.id.item_message_box_user_name);
            }

            @Override
            public void onClick(View v) {

                String offerId = offerTitle.getText().toString().substring(0, 20);

                Intent intent = new Intent(myContext, ChatActivity.class);
                intent.putExtra(ChatActivity.NEED_HELP_ID_EXTRA, offerId);
                intent.putExtra(ChatActivity.TITLE_EXTRA, offerTitle.getText());
                //intent.putExtra(ChatActivity.THIS_USER_ID_EXTRA, firebaseAuth.getUid());
                intent.putExtra(ChatActivity.OTHER_USER_NAME_EXTRA, userName.getText());
                myActivity.startActivity(intent);
            }
        }
    }
}