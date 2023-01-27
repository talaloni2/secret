package com.example.secret;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.Navigation;

import com.example.secret.databinding.FragmentSignUpBinding;
import com.example.secret.interfaces.Listener;
import com.example.secret.model.User;
import com.example.secret.model.UsersModel;
import com.example.secret.viewmodel.UsersViewModel;

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
        getActivity().findViewById(R.id.main_bottomNavigationView).setVisibility(View.INVISIBLE);

        FragmentActivity parentActivity = getActivity();
        parentActivity.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.removeItem(R.id.signInFragment);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        },this, Lifecycle.State.RESUMED);

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
        setButtonsClickable(false);
        binding.registerProgressBar.setVisibility(View.VISIBLE);
        User user = composeUser();
        String password = binding.passwordEt.getText().toString();

        validateUser(user, password,
                valid -> onUserValid(view, user, password),
                validationError -> {
                    Toast.makeText(getActivity(), validationError, Toast.LENGTH_SHORT).show();
                    setButtonsClickable(true);
                    binding.registerProgressBar.setVisibility(View.INVISIBLE);
                }
        );
    }

    private void setButtonsClickable(boolean isClickable){
        binding.cancelBtn.setClickable(isClickable);
        binding.registerBtn.setClickable(isClickable);
    }

    private void onUserValid(View view, User user, String password) {
        Listener<Void> createUserSuccessListener = unused -> {
            UsersViewModel.instance().setUser(
                    success -> {
                        binding.registerProgressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getActivity(), "Registered successfully", Toast.LENGTH_SHORT).show();
                        setButtonsClickable(true);
                        navigateToFeed(view);
                    },
                    fail -> {
                        binding.registerProgressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getActivity(), "Sign in with your new credentials", Toast.LENGTH_SHORT).show();
                        setButtonsClickable(true);
                    }
            );
        };

        Listener<Void> createUserFailListener = unused -> {
            binding.registerProgressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(getActivity(), "Register failed", Toast.LENGTH_SHORT).show();
        };

        performRegisterWithAvatar(user, password, createUserSuccessListener, createUserFailListener);
    }

    private void performRegisterWithAvatar(User user, String password, Listener<Void> createUserSuccessListener, Listener<Void> createUserFailListener) {
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

    @NonNull
    private User composeUser() {
        String email = binding.emailEt.getText().toString();
        String nickname = binding.nicknameEt.getText().toString();
        return new User("", nickname, null, email, "", 10);
    }

    private void validateUser(User user, String password, Listener<Void> valid, Listener<String> invalid) {
        String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        String passwordAtLeast8WithOneCharOneNum = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";

        boolean isEmailValid = Pattern.compile(regexPattern).matcher(user.email).matches();
        boolean isPasswordValid = Pattern.compile(passwordAtLeast8WithOneCharOneNum).matcher(password).matches();
        boolean isNicknameValid = user.nickname != null && user.nickname.length() > 5; //TODO: add validation for unique nickname

        if (!isEmailValid) {
            invalid.onComplete("Email is invalid");
            return;
        }
        if (!isPasswordValid) {
            invalid.onComplete("Password must contain 8 characters, with one letter and one number");
            return;
        }
        if (!isNicknameValid) {
            invalid.onComplete("Nickname is too short");
            return;
        }

        UsersModel.instance().checkForNicknameExistence(user.getNickname(), isExists->{
            if (isExists){
                invalid.onComplete("Nickname already exists");
                return;
            }
            valid.onComplete(null);
        }, err->{
            Log.e("REGISTER", "Error while checking for nickname uniqueness", err);
            invalid.onComplete("An error occurred, try again later");
        });
    }

    private void navigateToFeed(View view) {
        Navigation.findNavController(view).navigate(SignUpFragmentDirections.actionSignUpFragmentToUserSettingsFragment());
    }
}