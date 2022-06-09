package hcmute.vominhtri.mysimplechatapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import hcmute.vominhtri.mysimplechatapp.adapters.RecentConservationAdapter;
import hcmute.vominhtri.mysimplechatapp.databinding.ActivityMainBinding;
import hcmute.vominhtri.mysimplechatapp.listeners.ConversionListener;
import hcmute.vominhtri.mysimplechatapp.models.ChatMessage;
import hcmute.vominhtri.mysimplechatapp.models.User;
import hcmute.vominhtri.mysimplechatapp.utilities.Constants;
import hcmute.vominhtri.mysimplechatapp.utilities.PreferenceManager;

public class MainActivity extends AppCompatActivity implements ConversionListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> recentConversions;
    private RecentConservationAdapter recentConservationAdapter;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        loadUserData();
        getToken();
        setListener();
        listenConversions();
    }


    private void init()
    {
        recentConversions = new ArrayList<>();
        recentConservationAdapter = new RecentConservationAdapter(recentConversions, this);
        binding.rvRecentConversion.setAdapter(recentConservationAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void loadUserData(){
        binding.tvName.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imgAvatar.setImageBitmap(bitmap);
    }

    private void setListener()
    {
        binding.btnLogOut.setOnClickListener(view -> signOut());
        binding.fabNewchat.setOnClickListener(view ->
            startActivity(new Intent(MainActivity.this, UserActivity.class)));
    }


    private void getToken()
    {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
//        Log.e("FCM", )
    }

    private void updateToken(String token)
    {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM, token)
//                .addOnSuccessListener(unused -> showToast("Cập nhật token thành công"))
                .addOnFailureListener(e -> showToast("Không cập nhật được"));
    }

    private void signOut()
    {
        showToast("Đăng xuất");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused ->
                {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("Không thể đăng xuất được!"));
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
    }

    private void listenConversions(){

        //as a sender
        database.collection(Constants.KEY_COLLECTION_CONVERSIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        //as a receiver
        database.collection(Constants.KEY_COLLECTION_CONVERSIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null)
            return;
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if(documentChange.getType() == DocumentChange.Type.ADDED)
                {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setReceiverId(receiverId);
                    chatMessage.setSenderId(senderId);
                    if(preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId))
                    {
                        chatMessage.setConversionImage(documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE));
                        chatMessage.setConversionName(documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME));
                        chatMessage.setReceiverId(documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID));
                    }
                    else {
                        chatMessage.setConversionImage(documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE));
                        chatMessage.setConversionName(documentChange.getDocument().getString(Constants.KEY_SENDER_NAME));
                        chatMessage.setReceiverId(documentChange.getDocument().getString(Constants.KEY_SENDER_ID));
                    }
                    chatMessage.setMessage(documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE));
                    chatMessage.setDatetimeObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    recentConversions.add(chatMessage);
                }
                else if(documentChange.getType() == DocumentChange.Type.MODIFIED)
                {
                    for(int i =0; i < recentConversions.size(); i++)
                    {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if(recentConversions.get(i).getSenderId().equals(senderId) &&
                                recentConversions.get(i).getReceiverId().equals(receiverId))
                        {
                            recentConversions.get(i).setMessage(documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE));
                            recentConversions.get(i).setDatetimeObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                            break;
                        }
                    }
                }
            }
            Collections.sort(recentConversions, (obj1, obj2) -> obj1.getDatetimeObject().compareTo(obj2.getDatetimeObject()));
            recentConservationAdapter.notifyDataSetChanged();
            binding.rvRecentConversion.smoothScrollToPosition(0);
            binding.rvRecentConversion.setVisibility(View.VISIBLE);
            binding.prBar.setVisibility(View.GONE);
        }
    };

    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }
}