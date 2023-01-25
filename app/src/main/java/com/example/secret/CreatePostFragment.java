package com.example.secret;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.secret.databinding.FragmentCreatePostBinding;
import com.example.secret.model.Post;
import com.example.secret.model.PostsModel;
import com.example.secret.model.User;
import com.example.secret.viewmodel.UsersViewModel;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

public class CreatePostFragment extends Fragment {
    FragmentCreatePostBinding binding;
    User currentUser;

    ActivityResultLauncher<Void> cameraLauncher;
    ActivityResultLauncher<String> galleryLauncher;

    boolean isBackgroundSelected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().findViewById(R.id.main_bottomNavigationView).setVisibility(View.VISIBLE);
        currentUser = UsersViewModel.instance().getCurrentUser();

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), result -> {
            if (result != null) {
                BitmapDrawable background = new BitmapDrawable(getResources(), result);
                binding.postContentEt.setBackground(background);
                isBackgroundSelected = true;
            }
        });
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {
                try {
                    InputStream is = getContext().getContentResolver().openInputStream(result);
                    binding.postContentEt.setBackground(Drawable.createFromStream(is, result.toString()));
                    isBackgroundSelected = true;
                } catch (FileNotFoundException e) {
                    Toast.makeText(getActivity(), "Could not select image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCreatePostBinding.inflate(inflater, container, false);
        makeProgressBarInvisible();

        binding.cameraButton.setOnClickListener(view1 -> cameraLauncher.launch(null));

        binding.galleryButton.setOnClickListener(view1 -> galleryLauncher.launch("image/*"));

        binding.publishButton.setOnClickListener(this::onPublishClick);

        return binding.getRoot();
    }

    private void makeProgressBarInvisible() {
        binding.publishPostProgressBar.setVisibility(View.INVISIBLE);
    }

    private void onPublishClick(View view){
        String postContent = binding.postContentEt.getText().toString();
        boolean isAnonymous = binding.anonymousCbx.isChecked();

        if(isBackgroundSelected){
            PostsModel.instance().uploadBackground(
                    UUID.randomUUID().toString(),
                    ((BitmapDrawable)binding.postContentEt.getBackground()).getBitmap(),
                    url -> {
                        if (url == null){
                            onCreatePostFailed(null);
                            return;
                        }
                        Post p = new Post(UUID.randomUUID().toString(), postContent, isAnonymous, url);
                        PostsModel.instance().uploadPost(
                                p,
                                success -> {
                                    makeProgressBarInvisible();
                                    Toast.makeText(getActivity(), "Created post successfully", Toast.LENGTH_SHORT).show();
                                    Navigation.findNavController(view).navigate(CreatePostFragmentDirections.actionCreatePostFragmentToUserSettingsFragment());
                                },
                                this::onCreatePostFailed);
                    }
                    );
        }
    }

    private void onCreatePostFailed(Void unused) {
        makeProgressBarInvisible();
        Toast.makeText(getActivity(), "Please try again", Toast.LENGTH_SHORT).show();
    }
}