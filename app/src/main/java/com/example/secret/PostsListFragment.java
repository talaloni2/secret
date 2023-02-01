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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.secret.databinding.FragmentPostsListBinding;
import com.example.secret.model.CommentsModel;
import com.example.secret.model.Post;
import com.example.secret.model.PostsModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class PostsListFragment extends Fragment {
    FragmentPostsListBinding binding;
    PostRecyclerAdapter adapter;
    PostsListFragmentViewModel viewModel;

    BottomNavigationView navigationView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPostsListBinding.inflate(inflater, container, false);
        navigationView = getActivity().findViewById(R.id.main_bottomNavigationView);
        navigationView.setVisibility(View.VISIBLE);
        View view = binding.getRoot();

        initPostsList(view);
        binding.btnLoadMore.setOnClickListener(v -> {
            PostsModel.instance().loadMorePosts().observe(getViewLifecycleOwner(), this::onPostsListChanged);
        });
        return view;
    }

    private void initPostsList(View view) {
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PostRecyclerAdapter(getLayoutInflater(), viewModel.getPosts().getValue());
        binding.recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(pos -> this.onPostClicked(view, pos));

        viewModel.getPosts().observe(getViewLifecycleOwner(), this::onPostsListChanged);

        PostsModel.instance().EventPostsListLoadingState.observe(getViewLifecycleOwner(), status ->
                binding.swipeRefresh.setRefreshing(status == PostsModel.LoadingState.LOADING));
        binding.swipeRefresh.setOnRefreshListener(PostsListViewUtils::reloadData);
    }

    private void onPostsListChanged(List<Post> postsList) {
        adapter.setPosts(postsList);
        for (Post post : postsList) {
            viewModel.getPostLatestComments(post.id).observe(getViewLifecycleOwner(),
                    commentList -> adapter.setPostLatestComments(post.id, commentList));
        }
    }

    private void onPostClicked(View view, int pos) {
        Log.d("TAG", "Row was clicked " + pos);
        Post post = adapter.getPosts().get(pos);
        PostsListFragmentDirections.ActionPostsListFragmentToSinglePostFragment action =
                PostsListFragmentDirections.actionPostsListFragmentToSinglePostFragment(
                        post.id
                );
        Navigation.findNavController(view).navigate(action);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(this).get(PostsListFragmentViewModel.class);
    }
}
