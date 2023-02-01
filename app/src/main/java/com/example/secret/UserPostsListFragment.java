package com.example.secret;

import static com.example.secret.PostsListViewUtils.onPostClicked;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.secret.databinding.FragmentPostsListBinding;
import com.example.secret.databinding.FragmentUserPostsListBinding;
import com.example.secret.model.Comment;
import com.example.secret.model.CommentsModel;
import com.example.secret.model.Post;
import com.example.secret.model.PostsModel;
import com.example.secret.model.User;
import com.example.secret.viewmodel.UsersViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserPostsListFragment extends Fragment {
    FragmentUserPostsListBinding binding;
    PostRecyclerAdapter adapter;
    PostsListFragmentViewModel viewModel;

    BottomNavigationView navigationView;

    User currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentUserPostsListBinding.inflate(inflater, container, false);
        navigationView = getActivity().findViewById(R.id.main_bottomNavigationView);
        navigationView.setVisibility(View.VISIBLE);
        currentUser = UsersViewModel.instance().getCurrentUser();
        View view = binding.getRoot();

        initPostsList(view);
        binding.userPostsListBtnLoadMore.setOnClickListener(v -> {
            PostsModel.instance().loadMoreUserPosts(currentUser.id).observe(getViewLifecycleOwner(), this::onPostsListChanged);
        });

        viewModel.getUserPosts(currentUser.id).observe(getViewLifecycleOwner(), this::onPostsListChanged);
        return view;
    }

    private void initPostsList(View view) {
        binding.userPostsListRecyclerView.setHasFixedSize(true);
        binding.userPostsListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PostRecyclerAdapter(getLayoutInflater(), viewModel.getPosts().getValue());
        binding.userPostsListRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(pos -> onPostClicked(adapter, view, pos));

        viewModel.getPosts().observe(getViewLifecycleOwner(), this::onPostsListChanged);

        PostsModel.instance().EventPostsListLoadingState.observe(getViewLifecycleOwner(), status ->
                binding.userPostsListSwipeRefresh.setRefreshing(status == PostsModel.LoadingState.LOADING));
        binding.userPostsListSwipeRefresh.setOnRefreshListener(PostsListViewUtils::reloadData);
    }

    private void onPostsListChanged(List<Post> postsList) {
        adapter.setPosts(postsList);
        for (Post post : postsList) {
            viewModel.getPostLatestComments(post.id).observe(getViewLifecycleOwner(),
                    commentList -> adapter.setPostLatestComments(post.id, commentList));
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(this).get(PostsListFragmentViewModel.class);
    }

    void reloadData() {
        PostsModel.instance().refreshLatestPosts();
        CommentsModel.instance().refreshLatestComments();
    }
}
