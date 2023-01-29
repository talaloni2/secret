package com.example.secret;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.secret.databinding.FragmentSinglePostBinding;
import com.example.secret.model.CommentsModel;
import com.example.secret.model.User;
import com.example.secret.viewmodel.PostsViewModel;
import com.example.secret.viewmodel.UsersViewModel;
import com.squareup.picasso.Picasso;

public class SinglePostFragment extends Fragment {

    FragmentSinglePostBinding binding;
    User currentUser;

    String postId;

    public static final String POST_ID_PARAM_NAME = "POST_ID";

    public static SinglePostFragment newInstance(String postId) {
        SinglePostFragment frag = new SinglePostFragment();
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
        binding = FragmentSinglePostBinding.inflate(inflater, container, false);
        this.postId = SinglePostFragmentArgs.fromBundle(getArguments()).getPostId();
        initializeComponentsWithPostData();

        return binding.getRoot();
    }

    private void initializeComponentsWithPostData() {
        if (postId == null) {
            onRetrievePostFailed();
            return;
        }
        PostsViewModel.instance().getPost(postId,
                post -> {
                    binding.singlePostContentTv.setText(post.getContent());
                    CommentsModel.instance().getCommentsByPostIdLimited(postId).observe(getViewLifecycleOwner(), comments -> {
                        binding.singlePostComment1.setText(comments.size() > 0 ? comments.get(0).content : "");
                        binding.singlePostComment2.setText(comments.size() > 1 ? comments.get(1).content : "");
                    });
                    Picasso.get().load(post.getBackgroundUrl()).into(binding.singlePostBackgroundImg);
                },
                fail -> onRetrievePostFailed());
    }

    private void onRetrievePostFailed() {
        Toast.makeText(getActivity(), "Cannot get post.", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(binding.getRoot()).navigate(EditPostFragmentDirections.actionEditPostFragmentToUserSettingsFragment());
    }
}
