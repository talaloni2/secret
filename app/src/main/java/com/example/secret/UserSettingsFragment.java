package com.example.secret;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.secret.databinding.FragmentUserSettingsBinding;
import com.example.secret.interfaces.Listener;
import com.example.secret.model.ImageModel;
import com.example.secret.model.User;
import com.example.secret.model.UsersModel;
import com.example.secret.utls.BitmapConverter;
import com.example.secret.utls.UserValidator;
import com.example.secret.viewmodel.UsersViewModel;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class UserSettingsFragment extends Fragment {

    FragmentUserSettingsBinding binding;
    User currentUser;

    ActivityResultLauncher<Void> cameraLauncher;
    ActivityResultLauncher<String> galleryLauncher;

    static final String TAG = "UpdateProfile";

    static final int MAX_DAYS_BACK_MAX = 100;
    static final int MAX_DAYS_BACK_MIN = 1;

    private boolean isAvatarChanged = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().findViewById(R.id.main_bottomNavigationView).setVisibility(View.VISIBLE);
        currentUser = UsersViewModel.instance().getCurrentUser();

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), result -> {
            if (result != null) {
                binding.avatarImg.setImageBitmap(result);
                isAvatarChanged = true;
            }
        });
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {
                binding.avatarImg.setImageURI(result);
                isAvatarChanged = true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUserSettingsBinding.inflate(inflater, container, false);
        instantiateViews();

        binding.updateProfileBtn.setOnClickListener(this::onUpdateProfileClicked);

        return binding.getRoot();
    }

    private void setMaxDaysBackPostsPicker(int currentMaxDaysBackPosts) {
        binding.maxDaysBackPicker.setMaxValue(MAX_DAYS_BACK_MAX);
        binding.maxDaysBackPicker.setMinValue(MAX_DAYS_BACK_MIN);
        binding.maxDaysBackPicker.setValue(currentMaxDaysBackPosts);
    }

    private void onUpdateProfileClicked(View view) {
        makeProgressBarVisible();
        currentUser.setBio(binding.bioEt.getText().toString());
        currentUser.setMaxDaysBackPosts(binding.maxDaysBackPicker.getValue());
        currentUser.setNickname(binding.nicknameEt.getText().toString());

        UserValidator.validateNickname(currentUser.getNickname(), currentUser.getId(),
                this::onUpdateProfileUserValidated,
                validationError -> {
                    makeProgressBarInVisible();
                    Toast.makeText(view.getContext(), validationError, Toast.LENGTH_SHORT).show();
                }
        );

    }

    private void instantiateViews(){
        binding.nicknameEt.setText(currentUser.nickname);
        binding.userEmailTV.setText(currentUser.email);
        binding.bioEt.setText(currentUser.bio);
        binding.maxDaysBackPicker.setValue(currentUser.maxDaysBackPosts);
        if (currentUser.getAvatarUrl() != null && currentUser.getAvatarUrl().length() > 5) {
            ImageModel.instance().getImage(currentUser.getAvatarUrl(), R.drawable.avatar, bitmap -> {
                binding.avatarImg.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
                makeProgressBarInVisible();
            }, failReason -> {
                Toast.makeText(getActivity(), "Cannot load profile image", Toast.LENGTH_SHORT).show();
                makeProgressBarInVisible();
            });
        } else {
            binding.avatarImg.setImageResource(R.drawable.avatar);
            makeProgressBarInVisible();
        }
        setMaxDaysBackPostsPicker(currentUser.maxDaysBackPosts);
        binding.cameraButton.setOnClickListener(view1 -> cameraLauncher.launch(null));

        binding.galleryButton.setOnClickListener(view1 -> galleryLauncher.launch("image/*"));
    }

    private void makeProgressBarVisible() {
        binding.updateProfileProgressBar.setVisibility(View.VISIBLE);
    }

    private void makeProgressBarInVisible() {
        binding.updateProfileProgressBar.setVisibility(View.INVISIBLE);
    }

    private void onUpdateProfileUserValidated(Void unused) {
        Listener<Void> updateUserSuccessListener = success -> {
            UsersViewModel.instance().setUser(
                    _success -> {
                        makeProgressBarInVisible();
                        Toast.makeText(getActivity(), "Updated profile successfully", Toast.LENGTH_SHORT).show();
                    },
                    this::onUpdateUserFailed
            );
        };

        Listener<Void> updateUserFailListener = this::onUpdateUserFailed;

        performUpdateUserWithAvatar(updateUserSuccessListener, updateUserFailListener);
    }

    private void onUpdateUserFailed(Void unused) {
        makeProgressBarInVisible();
        Toast.makeText(getActivity(), "Could not update profile", Toast.LENGTH_SHORT).show();
    }

    private void validateUser(User user, Listener<Void> valid, Listener<String> invalid) {

    }

    private void performUpdateUserWithAvatar(Listener<Void> createUserSuccessListener, Listener<Void> createUserFailListener) {
        if (isAvatarChanged) {
            Bitmap bitmap = BitmapConverter.fromDrawable(binding.avatarImg.getDrawable());
            UsersModel.instance().uploadImage(UUID.randomUUID().toString(), bitmap, url -> {
                if (url == null) {
                    Log.w(TAG, "Could not save image. The older image will remain");
                }
                else {
                    currentUser.setAvatarUrl(url);
                }
                UsersModel.instance().updateUser(currentUser, createUserSuccessListener, createUserFailListener);
            });
        } else {
            UsersModel.instance().updateUser(currentUser, createUserSuccessListener, createUserFailListener);
        }
    }
}