package hcmute.vominhtri.mysimplechatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.vominhtri.mysimplechatapp.databinding.ItemContainerUserRecentConversionBinding;
import hcmute.vominhtri.mysimplechatapp.listeners.ConversionListener;
import hcmute.vominhtri.mysimplechatapp.models.ChatMessage;
import hcmute.vominhtri.mysimplechatapp.models.User;

public class RecentConservationAdapter extends RecyclerView.Adapter<RecentConservationAdapter.ConversionViewHolder>{

    private final List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;

    public RecentConservationAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener) {
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerUserRecentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder{
        ItemContainerUserRecentConversionBinding binding;

        ConversionViewHolder(ItemContainerUserRecentConversionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(ChatMessage chatMessage)
        {
            binding.imgAvatarItem.setImageBitmap(getConversionImage(chatMessage.getConversionImage()));
            binding.tvNameItem.setText(chatMessage.getConversionName());
            binding.tvRecentMessage.setText(chatMessage.getMessage());
            //add
            binding.getRoot().setOnClickListener(view -> {
                User user = new User();
                user.setId(chatMessage.getConversionId());
                user.setName(chatMessage.getConversionName());
                user.setImage(chatMessage.getConversionImage());
                conversionListener.onConversionClicked(user);
            });
        }
    }


    private Bitmap getConversionImage(String encodedImage)
    {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
