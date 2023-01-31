package com.example.secret;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.secret.databinding.FragmentSinglePostBinding;
import com.example.secret.model.Comment;
import com.example.secret.model.CommentsModel;
import com.example.secret.model.PostsModel;
import com.example.secret.model.User;
import com.example.secret.viewmodel.PostsViewModel;
import com.example.secret.viewmodel.UsersViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;

public class SinglePostFragment extends Fragment {

    FragmentSinglePostBinding binding;
    CommentRecyclerAdapter adapter;
    CommentsListFragmentViewModel viewModel;

    BottomNavigationView navigationView;

    User currentUser;

    String postId;

    public static final String POST_ID_PARAM_NAME = "POST_ID";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSinglePostBinding.inflate(inflater, container, false);
        navigationView = getActivity().findViewById(R.id.main_bottomNavigationView);
        navigationView.setVisibility(View.VISIBLE);
        currentUser = UsersViewModel.instance().getCurrentUser();

        Bundle arguments = getArguments();
        if (arguments != null) {
            this.postId = arguments.getString(POST_ID_PARAM_NAME);
        }

        this.postId = SinglePostFragmentArgs.fromBundle(getArguments()).getPostId();
        View view = binding.getRoot();
        initializeComponentsWithPostData();

        binding.commentsRecyclerView.setHasFixedSize(true);
        binding.commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CommentRecyclerAdapter(getLayoutInflater(), viewModel.getPostComments(postId).getValue());
        binding.commentsRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(pos -> {
            Log.d("TAG", "Row was clicked " + pos);
        });

        binding.singlePostBtnEditPost.setOnClickListener(v -> {
            SinglePostFragmentDirections.ActionSinglePostFragmentToEditPostFragment action =
                    SinglePostFragmentDirections.actionSinglePostFragmentToEditPostFragment(
                            postId
                    );
            Navigation.findNavController(view).navigate(action);
        });

        binding.singlePostCommentBtn.setOnClickListener(v -> {
            Comment comment = new Comment(
                    UUID.randomUUID().toString(),
                    binding.singlePostCommentEt.getText().toString(),
                    currentUser.id,
                    postId
            );
            adapter.addComment(comment, unused -> {
                binding.singlePostCommentEt.setText("");
                SinglePostFragment.this.reloadData();
            }, fail -> Toast.makeText(getActivity(), "Can't add comment", Toast.LENGTH_SHORT));
        });

        viewModel.getPostComments(postId).observe(getViewLifecycleOwner(), commentsList ->
                adapter.setComments(commentsList));

        CommentsModel.instance().eventCommentsListLoadingState.observe(getViewLifecycleOwner(), status ->
                binding.commentsSwipeRefresh.setRefreshing(status == CommentsModel.LoadingState.LOADING));

        binding.commentsSwipeRefresh.setOnRefreshListener(this::reloadData);
        return view;
    }

    private void initializeComponentsWithPostData() {
        if (postId == null) {
            onRetrievePostFailed();
            return;
        }
        PostsViewModel.instance().getPost(postId,
                post -> {
                    binding.singlePostContentTv.setText(post.getContent());
                    if (post.getBackgroundUrl() != null && post.getBackgroundUrl().length() > 5) {
                        Picasso.get().load(post.getBackgroundUrl()).placeholder(R.drawable.sharing_secret_image).into(binding.singlePostBackgroundImg);
                    } else {
                        binding.singlePostBackgroundImg.setImageResource(R.drawable.sharing_secret_image);
                    }
                    if (Objects.equals(post.userId, currentUser.id)) {
                        binding.singlePostBtnEditPost.setVisibility(View.VISIBLE);
                    }
                },
                fail -> onRetrievePostFailed());
    }

    private void onRetrievePostFailed() {
        Toast.makeText(getActivity(), "Post was deleted.", Toast.LENGTH_SHORT).show();
        Executors.newSingleThreadExecutor().execute(() -> {
            PostsModel.instance().deletePost(postId);
        });
        Navigation.findNavController(binding.getRoot()).popBackStack();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(this).get(CommentsListFragmentViewModel.class);
    }

    void reloadData() {
        CommentsModel.instance().refreshLatestComments();
    }
}
