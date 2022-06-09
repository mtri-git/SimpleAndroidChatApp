package hcmute.vominhtri.mysimplechatapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import hcmute.vominhtri.mysimplechatapp.databinding.ActivityTestBinding;

public class TestActivity extends AppCompatActivity {
    ActivityTestBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}