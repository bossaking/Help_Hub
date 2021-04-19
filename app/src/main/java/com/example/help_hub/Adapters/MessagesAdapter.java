package com.example.help_hub.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.help_hub.OtherClasses.ChatMessage;
import com.example.help_hub.R;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public static final int MSG_TYPE_LEFT_WITH_IMAGE = 2;
    public static final int MSG_TYPE_RIGHT_WITH_IMAGE = 3;

    private final Context mContext;
    private final List<ChatMessage> messages;
    private final String userId;

    private final onMessageClickListener onMessageClickListener;

    public MessagesAdapter(Context mContext, List<ChatMessage> messages, String userId, onMessageClickListener onMessageClickListener) {
        this.mContext = mContext;
        this.messages = messages;
        this.userId = userId;

        this.onMessageClickListener = onMessageClickListener;
    }

    @NonNull
    @Override
    public MessagesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = null;

        switch (viewType) {

            case MSG_TYPE_RIGHT:
                view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
                break;
            case MSG_TYPE_LEFT:
                view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
                break;
        }

        return new ViewHolder(view, onMessageClickListener);

    }

    @Override
    public void onBindViewHolder(@NonNull MessagesAdapter.ViewHolder holder, int position) {

        ChatMessage message = messages.get(position);

        if(message.getType().equals("Text")) {

            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.messageImageView.setVisibility(View.GONE);
            holder.messageTextView.setText(message.getMessage());

        }else if(message.getType().equals("Image")){
            holder.messageTextView.setVisibility(View.GONE);
            holder.messageImageView.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(Uri.parse(message.getMessage())).placeholder(R.drawable.image_with_progress)
                    .error(R.drawable.broken_image_24).into(holder.messageImageView);
        }

        holder.messageTimeTextView.setText(message.getTime());
    }

    @Override
    public int getItemCount() {
        return messages == null ? 0 : messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView messageTextView, messageTimeTextView;
        public ImageView messageImageView;

        public onMessageClickListener onMessageClickListener;

        public ViewHolder(@NonNull View itemView, onMessageClickListener onMessageClickListener) {
            super(itemView);

            messageTextView = itemView.findViewById(R.id.message_text_view);
            messageTimeTextView = itemView.findViewById(R.id.message_time_text_view);
            messageImageView = itemView.findViewById(R.id.message_image_view);

            this.onMessageClickListener = onMessageClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onMessageClickListener.onMessageClick(getAdapterPosition());
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (messages.get(position).getUserId().equals(userId)) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }

    }

    public interface onMessageClickListener {
        void onMessageClick(int position);
    }
}
