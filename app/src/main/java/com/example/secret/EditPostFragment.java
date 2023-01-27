package com.example.secret;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.room.util.StringUtil;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.secret.databinding.FragmentEditPostBinding;
import com.example.secret.model.Post;
import com.example.secret.model.PostsModel;
import com.example.secret.model.User;
import com.example.secret.viewmodel.PostsViewModel;
import com.example.secret.viewmodel.UsersViewModel;
import com.google.common.base.Strings;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

import javax.annotation.Nullable;

public class EditPostFragment extends Fragment {

    FragmentEditPostBinding binding;
    User currentUser;

    ActivityResultLauncher<Void> cameraLauncher;
    ActivityResultLauncher<String> galleryLauncher;

    boolean isBackgroundSelected;

    String postId;

    public static final String POST_ID_PARAM_NAME = "POST_ID";

    public static EditPostFragment newInstance(String postId) {
        EditPostFragment frag = new EditPostFragment();
        Bundle bundle = new Bundle();
        bundle.putString(POST_ID_PARAM_NAME, postId);
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().findViewById(R.id.main_bottomNavigationView).setVisibility(View.VISIBLE);
        currentUser = UsersViewModel.instance().getCurrentUser();

        Bundle arguments = getArguments();
        if (arguments != null) {
            this.postId = arguments.getString(POST_ID_PARAM_NAME);
        }

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
        binding = FragmentEditPostBinding.inflate(inflater, container, false);
        this.postId = EditPostFragmentArgs.fromBundle(getArguments()).getPostId();
        makeProgressBarInvisible();
        initializeComponentsWithPostData();

        binding.cameraButton.setOnClickListener(view1 -> cameraLauncher.launch(null));

        binding.galleryButton.setOnClickListener(view1 -> galleryLauncher.launch("image/*"));

        binding.publishButton.setOnClickListener(this::onPublishClick);

        return binding.getRoot();
    }

    private void initializeComponentsWithPostData() {
        Resources resources = getResources();
        makeProgressBarVisible();
        if (postId == null) {
            onRetrievePostFailed();
            return;
        }
        PostsViewModel.instance().getPost(postId,
                post -> {
                    binding.anonymousCbx.setChecked(post.getAnonymous());
                    binding.postContentEt.setText(post.getContent());

                    Picasso.get().load(currentUser.getAvatarUrl()).placeholder(R.drawable.avatar).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            binding.postContentLayout.setBackground(new BitmapDrawable(resources, bitmap));
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            Log.e("GetPostBackground", "Could not load image", e);
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            Log.d("GetPostBackground", "Preparing load");
                        }
                    });

                    makeProgressBarInvisible();
                },
                fail -> onRetrievePostFailed());

    }

    private void onRetrievePostFailed() {
        Toast.makeText(getActivity(), "Cannot get post.", Toast.LENGTH_SHORT).show();
        makeProgressBarInvisible();
        Navigation.findNavController(binding.getRoot()).navigate(EditPostFragmentDirections.actionEditPostFragmentToUserSettingsFragment());
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
        Post currentlyEditedPost = PostsViewModel.instance().getCurrentPost();
        currentlyEditedPost.setContent(postContent);
        currentlyEditedPost.setAnonymous(binding.anonymousCbx.isChecked());

        if (isBackgroundSelected) {
            PostsModel.instance().uploadBackground(
                    UUID.randomUUID().toString(),
                    ((BitmapDrawable) binding.postContentLayout.getBackground()).getBitmap(),
                    url -> {
                        if (url == null) {
                            onEditPostFailed(null);
                            return;
                        }
                        uploadPostWithBackground(view, currentlyEditedPost, url);
                    }
            );
        }
        else {
            uploadPostWithBackground(view, currentlyEditedPost, null);
        }
    }

    private void uploadPostWithBackground(View view, Post currentlyEditedPost, @Nullable String backgroundUrl) {
        if (!Strings.isNullOrEmpty(backgroundUrl)) {
            currentlyEditedPost.setBackgroundUrl(backgroundUrl);
        }

        PostsModel.instance().uploadPost(
                currentlyEditedPost,
                success -> {
                    makeProgressBarInvisible();
                    Toast.makeText(getActivity(), "Updated post successfully", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).popBackStack(R.id.createPostFragment, true);
                },
                this::onEditPostFailed);
    }

    private void onEditPostFailed(Void unused) {
        makeProgressBarInvisible();
        Toast.makeText(getActivity(), "Please try again", Toast.LENGTH_SHORT).show();
    }
}