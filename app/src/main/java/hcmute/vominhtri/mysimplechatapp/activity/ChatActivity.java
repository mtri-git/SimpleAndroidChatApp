package hcmute.vominhtri.mysimplechatapp.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import hcmute.vominhtri.mysimplechatapp.adapters.ChatMessageAdapter;
import hcmute.vominhtri.mysimplechatapp.databinding.ActivityChatBinding;
import hcmute.vominhtri.mysimplechatapp.models.ChatMessage;
import hcmute.vominhtri.mysimplechatapp.models.User;
import hcmute.vominhtri.mysimplechatapp.utilities.Constants;
import hcmute.vominhtri.mysimplechatapp.utilities.PreferenceManager;

public class  ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    private User receiverUser = new User();
    private List<ChatMessage> chatMessages;
    private ChatMessageAdapter chatMessageAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversionId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadReceiverDetail();
        setListener();
        init();
        listenMessages();
        Log.e("AAA", receiverUser.id);
    }

    private void sendMessage()
    {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constants.KEY_MESSAGE, binding.tvInputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if(conversionId != null)
        {
            updateConversion(binding.tvInputMessage.getText().toString());
        }
        else
        {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE, binding.tvInputMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP, new Date());
            addConversion(conversion);
        }
            binding.tvInputMessage.setText(null);
    }

    private Bitmap getBitmapFromEncodedString(String encodedImage)
    {
        byte[] bytes;
        try {
            bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        }
        catch (Exception e)
        {
            bytes = Base64.decode("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    ", Base64.DEFAULT);
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void addConversion(HashMap<String, Object> conversion)
    {
        database.collection(Constants.KEY_COLLECTION_CONVERSIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    private void updateConversion(String message)
    {
        DocumentReference documentReference
                = database.collection(Constants.KEY_COLLECTION_CONVERSIONS).document(conversionId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIMESTAMP, new Date()
        );
    }

    private void listenMessages()
    {
        // Self
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        // Other
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);

    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null)
            return;
        if(value != null)
        {
            int count = chatMessages.size();
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED)
                {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.datetime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.datetimeObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.datetimeObject.compareTo(obj2.datetimeObject));
            if (count == 0 ){
                chatMessageAdapter.notifyDataSetChanged();
            }
            else
            {
                chatMessageAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.rvChat.smoothScrollToPosition(chatMessages.size()-1);
            }
            binding.rvChat.setVisibility(View.VISIBLE);
        }
        binding.prBar.setVisibility(View.GONE);
        if(conversionId == null){
            checkForConversion();
        }
    };

    private String getReadableDateTime(Date date)
    {
        return new SimpleDateFormat("MMMM dd, yyyy -hh:mm a", Locale.getDefault()).format(date);
    }

    private void loadReceiverDetail(){
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.tvName.setText(receiverUser. name);
//        Log.e("AAA", receiverUser.id);
    }

    private void init()
    {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatMessageAdapter = new ChatMessageAdapter(
                chatMessages,
                getBitmapFromEncodedString(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.rvChat.setAdapter(chatMessageAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void setListener()
    {
        binding.imgBack.setOnClickListener(view -> onBackPressed());
        binding.layoutSend.setOnClickListener(view -> sendMessage());
    }

    private void checkForConversion(){
        if(chatMessages.size() != 0){
            checkForConversionRemotely(
                    preferenceManager.getString(Constants.KEY_SENDER_ID),
                    receiverUser.id
            );
            checkForConversionRemotely(
                    receiverUser.id,
                    preferenceManager.getString(Constants.KEY_SENDER_ID)
            );
        }
    }

    private void checkForConversionRemotely(String senderId, String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
      if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0)
        {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                conversionId = documentSnapshot.getId();
        }

    };
}