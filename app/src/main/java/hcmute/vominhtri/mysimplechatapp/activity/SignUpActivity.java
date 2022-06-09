package hcmute.vominhtri.mysimplechatapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import hcmute.vominhtri.mysimplechatapp.databinding.ActivitySignUpBinding;
import hcmute.vominhtri.mysimplechatapp.utilities.Constants;
import hcmute.vominhtri.mysimplechatapp.utilities.PreferenceManager;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    PreferenceManager preferenceManager;
    private String encodedImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    private void setListeners()
    {
        binding.tvSignIn.setOnClickListener(view ->
                onBackPressed());

        binding.btnSignUp.setOnClickListener(view ->
        {
            if(isValidateSignUp())
                signUp();
        });

        binding.flImageSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void signUp()
    {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, binding.edtName.getText().toString().trim());
        user.put(Constants.KEY_EMAIL, binding.edtEmailSignUp.getText().toString().trim());
        user.put(Constants.KEY_PASSWORD, binding.edtPasswordSignUp.getText().toString().trim());
        user.put(Constants.KEY_IMAGE, encodedImage);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference ->
                {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGN_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME, (String) user.get(Constants.KEY_NAME));
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception ->
                {
                    loading(false);
                    showToast(exception.getMessage());
                });
    }

    public String encodedImage(Bitmap image) {
        int previewWidth = 150;
        int previewHeight = image.getHeight() * previewWidth/image.getWidth();
        Bitmap previewImage = Bitmap.createScaledBitmap(image, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewImage.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return android.util.Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK)
                {
                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imgAvatarSignUp.setImageBitmap(bitmap);
                            binding.tvAddImage.setVisibility(View.GONE);
                            encodedImage = encodedImage(bitmap);
                        }catch (FileNotFoundException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private void loading(Boolean isLoading)
    {
        if(isLoading){
            binding.btnSignUp.setVisibility(View.INVISIBLE);
            binding.prBarSignUp.setVisibility(View.VISIBLE);
        }
        else{
            binding.btnSignUp.setVisibility(View.VISIBLE);
            binding.prBarSignUp.setVisibility(View.INVISIBLE);
        }
    }
    private Boolean isValidateSignUp()
    {
        if(encodedImage == null){
            showToast("Thêm ảnh đại diện");
            return false;
        }
        else if( binding.edtName.getText().toString().trim().isEmpty()) {
            showToast("Thêm tên vào");
            return false;
        }
        else if(binding.edtEmailSignUp.getText().toString().trim().isEmpty()){
            showToast("Thêm Email");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.edtEmailSignUp.getText().toString()).matches()){
            showToast("Email không đúng định dạng");
            return false;

        }
        else if(binding.edtPasswordSignUp.getText().toString().trim().isEmpty()){
            showToast("Nhập mật khẩu!");
            return false;
        }
        else if(binding.edtPasswordConfirmSignUp.getText().toString().trim().isEmpty()){
            showToast("Xác nhận mật khẩu");
            return false;
        }
        else if(!binding.edtPasswordSignUp.getText().toString().equals(
                binding.edtPasswordConfirmSignUp.getText().toString()))
        {
            showToast("Mật khẩu và xác nhận mật khẩu không giống nhau");
            Log.e("AAA" ,binding.edtPasswordSignUp.getText().toString() + " " +  binding.edtPasswordConfirmSignUp.getText().toString());
            return false;
        }
        else {
        return true;
        }

    }
}