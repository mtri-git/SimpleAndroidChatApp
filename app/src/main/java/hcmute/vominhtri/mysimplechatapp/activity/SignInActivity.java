package hcmute.vominhtri.mysimplechatapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

import java.util.HashMap;

import hcmute.vominhtri.mysimplechatapp.databinding.ActivitySignInBinding;
import hcmute.vominhtri.mysimplechatapp.utilities.Constants;
import hcmute.vominhtri.mysimplechatapp.utilities.PreferenceManager;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGN_IN)) // Nếu đã đăng nhập
        {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListener();

    }
    private void setListener()
    {
        binding.tvCreateAccount.setOnClickListener(view ->
                startActivity(new Intent(this, SignUpActivity.class)));
        binding.btnSignIn.setOnClickListener(view -> {
            if(isValidSignIn())
                    signIn();
                });
    }

    private void signIn()
    {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.edtEmail.getText().toString().trim())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.edtPassword.getText().toString())
                .get()
                .addOnCompleteListener(task ->{
                    if(task.isSuccessful() && task.getResult() != null
                            && task.getResult().getDocuments().size() > 0 )
                    {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGN_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        preferenceManager.putString(Constants.KEY_SENDER_ID, documentSnapshot.getId());
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    else {
                        loading(false);
                        showToast("Không thể đăng nhập được");
                    }
                });
    }

    private void loading(Boolean isLoading)
    {
        if(isLoading){
            binding.btnSignIn.setVisibility(View.INVISIBLE);
            binding.prBarSignIn.setVisibility(View.VISIBLE);
        }
        else{
            binding.btnSignIn.setVisibility(View.VISIBLE);
            binding.prBarSignIn.setVisibility(View.INVISIBLE);
        }
    }

//    private void adDataToFireStore(){
//        FirebaseFirestore database = FirebaseFirestore.getInstance();
//        HashMap<String, Object> data = new HashMap<>();
//        data.put("first_name", "Trí");
//        data.put("last_name", "Võ");
//        database.collection("users")
//                .add(data)
//                .addOnSuccessListener(documentReference ->{
//                        Toast.makeText(getApplicationContext(),"Data Inserted", Toast.LENGTH_SHORT).show();})
//                .addOnFailureListener(exception -> {
//                    Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }

    private Boolean isValidSignIn()
    {
        if(binding.edtEmail.getText().toString().isEmpty())
        {
            showToast("Nhập Email");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.edtEmail.getText().toString()).matches())
        {
            showToast("Email sai định dạng");
            return false;
        }
        else if (binding.edtPassword.getText().toString().isEmpty()){
            showToast("Nhập mật khẩu");
            return false;
        }
        else
            return true;
    }

    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}