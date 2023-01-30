package com.example.secret;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import com.example.secret.utls.CameraActivityResultCallback;
import com.example.secret.utls.GalleryActivityResultCallback;
import com.example.secret.utls.PostPublisher;
import com.example.secret.utls.RandomBackgroundClickedListener;
import com.example.secret.viewmodel.UsersViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.UUID;

public class CreatePostFragment extends Fragment {
    FragmentCreatePostBinding binding;
    User currentUser;

    ActivityResultLauncher<Void> cameraLauncher;
    ActivityResultLauncher<String> galleryLauncher;

    BottomNavigationView navigationView;

    boolean isBackgroundSelected;

    public void setBackgroundSelected(boolean backgroundSelected) {
        isBackgroundSelected = backgroundSelected;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navigationView = getActivity().findViewById(R.id.main_bottomNavigationView);
        navigationView.setVisibility(View.VISIBLE);
        currentUser = UsersViewModel.instance().getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCreatePostBinding.inflate(inflater, container, false);

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                new CameraActivityResultCallback(this::setBackgroundSelected, binding.postImage, getResources())
        );
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new GalleryActivityResultCallback(getActivity(), binding.postImage, this::setBackgroundSelected)
        );

        makeProgressBarInvisible();

        binding.cameraButton.setOnClickListener(view1 -> cameraLauncher.launch(null));

        binding.galleryButton.setOnClickListener(view1 -> galleryLauncher.launch("image/*"));

        binding.randomBackgroundButton.setOnClickListener(
                new RandomBackgroundClickedListener(
                        getActivity(), binding.publishPostProgressBar,
                        binding.postImage, this::setBackgroundSelected
                ));

        binding.publishButton.setOnClickListener(this::onPublishClick);

        return binding.getRoot();
    }

    private void makeProgressBarInvisible() {
        binding.publishPostProgressBar.setVisibility(View.INVISIBLE);
    }

    private void makeProgressBarVisible() {
        binding.publishPostProgressBar.setVisibility(View.VISIBLE);
    }

    private void onPublishClick(View view) {
        makeProgressBarVisible();
        String postContent = binding.postContentEt.getText().toString();
        boolean isAnonymous = binding.anonymousCbx.isChecked();
        Bitmap image = null;
        if(isBackgroundSelected){
            image = ((BitmapDrawable) binding.postImage.getDrawable()).getBitmap();
        }
        new PostPublisher(
                success -> {
                    makeProgressBarInvisible();
                    Toast.makeText(getActivity(), "Created post successfully", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).popBackStack();
                },
                this::onCreatePostFailed
        ).publish(new Post(UUID.randomUUID().toString(), postContent, isAnonymous, null, currentUser.getId()), image);
    }

    private void onCreatePostFailed(String cause) {
        makeProgressBarInvisible();
        Toast.makeText(getActivity(), cause, Toast.LENGTH_SHORT).show();
    }
}