package com.example.secret;

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

        binding.userPostsListRecyclerView.setHasFixedSize(true);
        binding.userPostsListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Map<String, List<Comment>> postsLatestCommentsData = new HashMap<>();
        for (Map.Entry<String, LiveData<List<Comment>>> entry : viewModel.getPostsLatestComments().entrySet()) {
            postsLatestCommentsData.put(entry.getKey(), entry.getValue().getValue());
        }
        adapter = new PostRecyclerAdapter(getLayoutInflater(), viewModel.getUserPosts(currentUser.id).getValue(), postsLatestCommentsData);
        binding.userPostsListRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(pos -> {
            Log.d("TAG", "Row was clicked " + pos);
            Post post = adapter.getPosts().get(pos);
            UserPostsListFragmentDirections.ActionUserPostsListFragmentToSinglePostFragment action =
                    UserPostsListFragmentDirections.actionUserPostsListFragmentToSinglePostFragment(
                            post.id
                    );
            Navigation.findNavController(view).navigate(action);
        });

        binding.userPostsListBtnLoadMore.setOnClickListener(v -> {
            PostsModel.instance().loadMoreUserPosts(currentUser.id).observe(getViewLifecycleOwner(), postsList -> {
                adapter.setPosts(postsList);
                CommentsModel.instance().refreshLatestComments();
                for (Post post : postsList) {
                    viewModel.getPostLatestComments(post.id).observe(getViewLifecycleOwner(),
                            commentList -> adapter.setPostLatestComments(post.id, commentList));
                }
            });
        });

        viewModel.getUserPosts(currentUser.id).observe(getViewLifecycleOwner(), postsList -> {
            adapter.setPosts(postsList);
            for (Post post : postsList) {
                viewModel.getPostLatestComments(post.id).observe(getViewLifecycleOwner(),
                        commentList -> adapter.setPostLatestComments(post.id, commentList));
            }
        });

        PostsModel.instance().EventPostsListLoadingState.observe(getViewLifecycleOwner(), status ->
                binding.userPostsListSwipeRefresh.setRefreshing(status == PostsModel.LoadingState.LOADING));

        CommentsModel.instance().eventCommentsListLoadingState.observe(getViewLifecycleOwner(), status ->
                binding.userPostsListSwipeRefresh.setRefreshing(status == CommentsModel.LoadingState.LOADING));

        binding.userPostsListSwipeRefresh.setOnRefreshListener(this::reloadData);
        return view;
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
