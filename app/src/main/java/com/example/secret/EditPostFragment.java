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

import com.example.secret.databinding.FragmentEditPostBinding;
import com.example.secret.model.ImageModel;
import com.example.secret.model.Post;
import com.example.secret.model.User;
import com.example.secret.utls.BitmapConverter;
import com.example.secret.utls.CameraActivityResultCallback;
import com.example.secret.utls.GalleryActivityResultCallback;
import com.example.secret.utls.PostPublisher;
import com.example.secret.utls.RandomBackgroundClickedListener;
import com.example.secret.viewmodel.PostsViewModel;
import com.example.secret.viewmodel.UsersViewModel;

public class EditPostFragment extends Fragment {

    FragmentEditPostBinding binding;
    User currentUser;

    ActivityResultLauncher<Void> cameraLauncher;
    ActivityResultLauncher<String> galleryLauncher;

    boolean isBackgroundSelected;

    String postId;

    public static final String POST_ID_PARAM_NAME = "POST_ID";

    public void setBackgroundSelected(boolean backgroundSelected) {
        isBackgroundSelected = backgroundSelected;
    }

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEditPostBinding.inflate(inflater, container, false);
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                new CameraActivityResultCallback(this::setBackgroundSelected, binding.postImage, getResources())
        );
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new GalleryActivityResultCallback(getActivity(), binding.postImage, this::setBackgroundSelected)
        );
        this.postId = EditPostFragmentArgs.fromBundle(getArguments()).getPostId();
        makeProgressBarInvisible();
        initializeComponentsWithPostData();

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

    private void initializeComponentsWithPostData() {
        makeProgressBarVisible();
        if (postId == null) {
            onRetrievePostFailed("Post is not defined");
            return;
        }
        PostsViewModel.instance().getPost(postId,
                post -> {
                    binding.anonymousCbx.setChecked(post.getAnonymous());
                    binding.postContentEt.setText(post.getContent());
                    ImageModel.instance().getImage(post.getBackgroundUrl(), bitmap -> {
                        binding.postImage.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
                    }, this::onRetrievePostFailed);
                    makeProgressBarInvisible();
                },
                fail -> onRetrievePostFailed("Failed getting post"));

    }

    private void onRetrievePostFailed(String reason) {
        Toast.makeText(getActivity(), reason, Toast.LENGTH_SHORT).show();
        makeProgressBarInvisible();
        Navigation.findNavController(binding.getRoot()).popBackStack();
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
        Post currentlyEditedPost = PostsViewModel.instance().getCurrentPost();
        currentlyEditedPost.setContent(postContent);
        currentlyEditedPost.setAnonymous(binding.anonymousCbx.isChecked());
        Bitmap newImage = null;
        if (isBackgroundSelected) {
            newImage = BitmapConverter.fromDrawable(binding.postImage.getDrawable());
        }

        new PostPublisher(
                success -> {
                    makeProgressBarInvisible();
                    Toast.makeText(getActivity(), "Updated post successfully", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).popBackStack(R.id.createPostFragment, true);
                },
                this::onEditPostFailed
        ).publish(currentlyEditedPost, newImage);
    }

    private void onEditPostFailed(String cause) {
        makeProgressBarInvisible();
        Toast.makeText(getActivity(), cause, Toast.LENGTH_SHORT).show();
    }
}