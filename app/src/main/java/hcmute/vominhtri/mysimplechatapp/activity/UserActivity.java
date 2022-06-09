package hcmute.vominhtri.mysimplechatapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import hcmute.vominhtri.mysimplechatapp.adapters.UserAdapter;
import hcmute.vominhtri.mysimplechatapp.databinding.ActivityUserBinding;
import hcmute.vominhtri.mysimplechatapp.listeners.UserListener;
import hcmute.vominhtri.mysimplechatapp.models.User;
import hcmute.vominhtri.mysimplechatapp.utilities.Constants;
import hcmute.vominhtri.mysimplechatapp.utilities.PreferenceManager;

public class UserActivity extends AppCompatActivity implements UserListener {

    private ActivityUserBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListener();
        getUser();
    }

    private void setListener()
    {
        binding.imgBack.setOnClickListener(view -> onBackPressed());
    }

    private void getUser()
    {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task ->
                {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() != null)
                    {
                        List<User> users = new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                            if(currentUserId.equals(queryDocumentSnapshot.getId()))
                                continue;
                            User user = new User();
                            user.setName(queryDocumentSnapshot.getString(Constants.KEY_NAME));
                            user.setEmail(queryDocumentSnapshot.getString(Constants.KEY_EMAIL));
                            user.setImage(queryDocumentSnapshot.getString(Constants.KEY_IMAGE));
                            user.setToken(queryDocumentSnapshot.getString(Constants.KEY_FCM));
                            user.setId(queryDocumentSnapshot.getId());
                            users.add(user);
                        }
                        if(users.size() > 0)
                        {
                            UserAdapter userAdapter = new UserAdapter(users, this);
                            binding.rvUser.setAdapter(userAdapter);
                            binding.rvUser.setVisibility(View.VISIBLE);
                        }
                        else {
                            showErrorMessage();
                        }

                    }
                });

    }

    private void showErrorMessage()
    {
        binding.tvError.setText(String.format("%s", "Không có người dùng nào có sẵn!"));
        binding.tvError.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading)
    {
        if(isLoading)
        {
            binding.prBarUserlist.setVisibility(View.VISIBLE);
        }
        else
        {
            binding.prBarUserlist.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClick(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}