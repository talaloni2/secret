package com.example.secret;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.secret.databinding.FragmentCreatePostBinding;
import com.example.secret.model.ExternalBackgroundModel;
import com.example.secret.model.Post;
import com.example.secret.model.PostsModel;
import com.example.secret.model.User;
import com.example.secret.viewmodel.UsersViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.base.Strings;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

import javax.annotation.Nullable;

public class CreatePostFragment extends Fragment {
    FragmentCreatePostBinding binding;
    User currentUser;

    ActivityResultLauncher<Void> cameraLauncher;
    ActivityResultLauncher<String> galleryLauncher;

    BottomNavigationView navigationView;

    boolean isBackgroundSelected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navigationView = getActivity().findViewById(R.id.main_bottomNavigationView);
        navigationView.setVisibility(View.VISIBLE);
        currentUser = UsersViewModel.instance().getCurrentUser();

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), result -> {
            if (result != null) {
                BitmapDrawable background = new BitmapDrawable(getResources(), result);
                binding.postContentLayout.setBackground(background);
                isBackgroundSelected = true;
            }
        });
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {
                try {
                    InputStream is = getContext().getContentResolver().openInputStream(result);
                    binding.postContentLayout.setBackground(Drawable.createFromStream(is, result.toString()));
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
        binding.postContentEt.setTextColor(Color.WHITE);
        makeProgressBarInvisible();

        binding.cameraButton.setOnClickListener(view1 -> cameraLauncher.launch(null));

        binding.galleryButton.setOnClickListener(view1 -> galleryLauncher.launch("image/*"));

        binding.randomBackgroundButton.setOnClickListener(this::onRandomBackgroundRequested);

        binding.publishButton.setOnClickListener(this::onPublishClick);
        binding.editRandomPostButton.setOnClickListener(view -> {
            PostsModel.instance().getRandomPost(
                    UsersViewModel.instance().getCurrentUser().getId(),
                    post ->
                            Navigation.findNavController(view).navigate(
                                    CreatePostFragmentDirections.actionCreatePostFragmentToEditPostFragment(
                                            post.getId()
                                    )
                            ),
                    fail -> Toast.makeText(getActivity(), "Cannot load random post", Toast.LENGTH_SHORT).show());
        });

        return binding.getRoot();
    }

    private void onRandomBackgroundRequested(View view) {
        makeProgressBarVisible();
        ExternalBackgroundModel.getInstance().getRandomBackground(
                backgroundMeta -> {
                    Picasso.get().load(backgroundMeta.getUrls().getThumb()).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            binding.postContentLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
                            isBackgroundSelected = true;
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            Toast.makeText(getActivity(), "Try again later", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            Log.d("GetRandomPostBackground", "Preparing load");
                        }
                    });
                    makeProgressBarInvisible();
                },
                fail -> {
                    makeProgressBarInvisible();
                    Toast.makeText(getActivity(), "Try again later", Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void makeProgressBarInvisible() {
        binding.publishPostProgressBar.setVisibility(View.INVISIBLE);
    }

    private void makeProgressBarVisible() {
        binding.publishPostProgressBar.setVisibility(View.VISIBLE);
    }

    private void onPublishClick(View view) {
        String postContent = binding.postContentEt.getText().toString();
        if (Strings.isNullOrEmpty(postContent) || postContent.length() < 5){
            Toast.makeText(getActivity(), "Post content too short", Toast.LENGTH_SHORT).show();
            return;
        }
        makeProgressBarVisible();
        boolean isAnonymous = binding.anonymousCbx.isChecked();


        if (isBackgroundSelected) {
            PostsModel.instance().uploadBackground(
                    UUID.randomUUID().toString(),
                    ((BitmapDrawable) binding.postContentLayout.getBackground()).getBitmap(),
                    url -> {
                        if (url == null) {
                            onCreatePostFailed(null);
                            return;
                        }
                        createPostWithNullableAvatar(view, postContent, isAnonymous, url);
                    }
            );
        }
        else {
            createPostWithNullableAvatar(view, postContent, isAnonymous, null);
        }
    }

    private void createPostWithNullableAvatar(View view, String postContent, boolean isAnonymous, @Nullable String url) {
        Post p = new Post(UUID.randomUUID().toString(), postContent, isAnonymous, url, currentUser.getId());
        PostsModel.instance().uploadPost(
                p,
                success -> {
                    makeProgressBarInvisible();
                    Toast.makeText(getActivity(), "Created post successfully", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).popBackStack();
                },
                this::onCreatePostFailed);
    }

    private void onCreatePostFailed(Void unused) {
        makeProgressBarInvisible();
        Toast.makeText(getActivity(), "Please try again", Toast.LENGTH_SHORT).show();
    }
}