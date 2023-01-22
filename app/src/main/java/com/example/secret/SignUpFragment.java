package com.example.secret;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.secret.databinding.FragmentSignUpBinding;
import com.example.secret.interfaces.Listener;
import com.example.secret.model.User;
import com.example.secret.model.UsersModel;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class SignUpFragment extends Fragment {

    FragmentSignUpBinding binding;
    ActivityResultLauncher<Void> cameraLauncher;
    ActivityResultLauncher<String> galleryLauncher;

    Boolean isAvatarSelected = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), result -> {
            if (result != null) {
                binding.avatarImg.setImageBitmap(result);
                isAvatarSelected = true;
            }
        });
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {
                binding.avatarImg.setImageURI(result);
                isAvatarSelected = true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        binding.registerProgressBar.setVisibility(View.INVISIBLE);

        binding.registerBtn.setOnClickListener(this::performRegister);

        binding.cameraButton.setOnClickListener(view1 -> {
            cameraLauncher.launch(null);
        });

        binding.galleryButton.setOnClickListener(view1 -> {
            galleryLauncher.launch("image/*");
        });

        binding.cancelBtn.setOnClickListener(view -> {
            Navigation.findNavController(view).popBackStack();
        });

        return binding.getRoot();
    }

    private void performRegister(View view) {
        binding.registerProgressBar.setVisibility(View.VISIBLE);
        binding.registerProgressBar.requestFocus();
        String email = binding.emailEt.getText().toString();
        String password = binding.passwordEt.getText().toString();
        String nickname = binding.nicknameEt.getText().toString();
        User user = new User("", nickname, null, email);
        Optional<String> validationError = validateUser(user, password);
        if(validationError.isPresent()){
            Toast.makeText(getActivity(), validationError.get(), Toast.LENGTH_SHORT).show();
            return;
        }

        Listener<Void> createUserSuccessListener = unused -> {
            Toast.makeText(getActivity(), "Registered successfully", Toast.LENGTH_SHORT).show();
            binding.registerProgressBar.setVisibility(View.INVISIBLE);
            navigateToFeed(view);
        };

        Listener<Void> createUserFailListener = unused -> {
            binding.registerProgressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(getActivity(), "Register failed", Toast.LENGTH_SHORT).show();
        };

        if (isAvatarSelected) {
            Bitmap bitmap = ((BitmapDrawable) binding.avatarImg.getDrawable()).getBitmap();
            UsersModel.instance().uploadImage(UUID.randomUUID().toString(), bitmap, url -> {
                if (url == null) {
                    Log.w("REGISTER", "Could not save image. User will be created without one");
                }
                user.setAvatarUrl(url);
                UsersModel.instance().registerUser(user, password, createUserSuccessListener, createUserFailListener);
            });
        } else {
            UsersModel.instance().registerUser(user, password, createUserSuccessListener, createUserFailListener);
        }
    }

    private Optional<String> validateUser(User user, String password){
        String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        String passwordAtLeast8WithOneCharOneNum = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";
        boolean isEmailValid = Pattern.compile(regexPattern).matcher(user.email).matches();
        boolean isPasswordValid = Pattern.compile(passwordAtLeast8WithOneCharOneNum).matcher(password).matches();

        if (!isEmailValid){
            return Optional.of("Email is invalid");
        }
        if (!isPasswordValid){
            return Optional.of("Password must contain 8 characters, with one letter and one number");
        }
        return Optional.empty();
    }

    private void navigateToFeed(View view) {
        Navigation.findNavController(view).navigate(SignUpFragmentDirections.actionSignUpFragmentToUserProfileFragment());
    }
}