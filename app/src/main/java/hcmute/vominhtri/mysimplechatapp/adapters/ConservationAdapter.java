package hcmute.vominhtri.mysimplechatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.vominhtri.mysimplechatapp.databinding.ItemContainerUserRecentConversionBinding;
import hcmute.vominhtri.mysimplechatapp.listeners.ConversionListener;
import hcmute.vominhtri.mysimplechatapp.models.ChatMessage;
import hcmute.vominhtri.mysimplechatapp.models.User;

public class ConservationAdapter extends RecyclerView.Adapter<ConservationAdapter.ConversionViewHolder>{

    private final List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;

    public ConservationAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener) {
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
        Log.e("AAA", chatMessages.get(position).toString());
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }


    // View holder
    class ConversionViewHolder extends RecyclerView.ViewHolder{
        ItemContainerUserRecentConversionBinding binding;

        ConversionViewHolder(ItemContainerUserRecentConversionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }


        void setData(ChatMessage chatMessage)
        {
            try {
                binding.imgAvatarRecent.setImageBitmap(getConversionImage(chatMessage.conversionImage));
            }
            catch (Exception e)
            {
                Log.e("AAA", "Không có ảnh để load");
            };

            binding.tvNameItem.setText(chatMessage.conversionName);
            binding.tvRecentMessage.setText(chatMessage.message);
            //add
            binding.getRoot().setOnClickListener(view -> {
                User user = new User();
                user.id = chatMessage.receiverId;
                Log.e("AAA", chatMessage.conversionName);
                user.name = chatMessage.conversionName;
                user.image = chatMessage.conversionImage;
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
