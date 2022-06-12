package hcmute.vominhtri.mysimplechatapp.adapters;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.vominhtri.mysimplechatapp.databinding.ItemContainerReceivedMessageBinding;
import hcmute.vominhtri.mysimplechatapp.databinding.ItemContainerSentMessageBinding;
import hcmute.vominhtri.mysimplechatapp.models.ChatMessage;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final Bitmap receiverAvatar;
    private final List<ChatMessage> chatMessageList;
    private final String senderId;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public ChatMessageAdapter(List<ChatMessage> chatMessageList, Bitmap receiverAvatar, String senderId) {
        this.receiverAvatar = receiverAvatar;
        this.chatMessageList = chatMessageList;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SENT)
        {
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }else
        {
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if(getItemViewType(position) == VIEW_TYPE_SENT)
            {
                ((SentMessageViewHolder) holder).setData(chatMessageList.get(position));
            }
            else {
                ((ReceivedMessageViewHolder) holder).setData(chatMessageList.get(position), receiverAvatar);
            }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessageList.get(position).senderId.equals(senderId))
        {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSentMessageBinding binding;


        SentMessageViewHolder(ItemContainerSentMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(ChatMessage chatMessage)
        {
            binding.tvMessage.setText(chatMessage.message);
            binding.tvDateMessage.setText(chatMessage.datetime);

        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerReceivedMessageBinding binding;

        public ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(ChatMessage chatMessage, Bitmap avatar)
        {
            binding.tvMessageReceived.setText(chatMessage.message);
            binding.tvDateMessage.setText(chatMessage.message);
            try {
                binding.imgProfile.setImageBitmap(avatar);
            }
            catch (Exception ex)
            {
                Log.e("AAA", "Lỗi hình ảnh ở ChatAdaoter");
            }

        }
    }
}
