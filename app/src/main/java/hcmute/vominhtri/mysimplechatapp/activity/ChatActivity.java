package hcmute.vominhtri.mysimplechatapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
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

public class ChatActivity extends AppCompatActivity {

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
    }

    private void sendMessage()
    {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.getId());
        message.put(Constants.KEY_MESSAGE, binding.tvInputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if(conversionId != null)
        {
            updateConversion(binding.tvInputMessage.getText().toString());
        }
        else
        {
            HashMap<String, Object> consversion = new HashMap<>();
            consversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            consversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            consversion.put(Constants.KEY_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            consversion.put(Constants.KEY_RECEIVER_ID, receiverUser.getId());
            consversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.getName());
            consversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.getImage());
            consversion.put(Constants.KEY_LAST_MESSAGE, binding.tvInputMessage.getText().toString());
            consversion.put(Constants.KEY_TIMESTAMP, new Date());
            addConversion(consversion);
        }
            binding.tvInputMessage.setText(null);
    }

    private Bitmap getBitmapFromEncodedString(String encodedImage)
    {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
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
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.getId())
                .addSnapshotListener(eventListener);
        // Other
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.getId())
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
                    chatMessage.setSenderId(documentChange.getDocument().getString(Constants.KEY_SENDER_ID));
                    chatMessage.setReceiverId(documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID));
                    chatMessage.setMessage(documentChange.getDocument().getString(Constants.KEY_MESSAGE));
                    chatMessage.setDatetime(getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP)));
                    chatMessage.setDatetimeObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.getDatetimeObject().compareTo(obj2.getDatetimeObject()));
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
        binding.tvName.setText(receiverUser.getName());
    }

    private void init()
    {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatMessageAdapter = new ChatMessageAdapter(
                chatMessages,
                getBitmapFromEncodedString(receiverUser.getImage()),
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
                    receiverUser.getId()
            );
            checkForConversionRemotely(
                    receiverUser.getId(),
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