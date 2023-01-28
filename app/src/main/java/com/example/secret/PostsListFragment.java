package com.example.secret;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.secret.databinding.FragmentPostsListBinding;
import com.example.secret.model.Comment;
import com.example.secret.model.CommentsModel;
import com.example.secret.model.Post;
import com.example.secret.model.PostsModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Map<String, List<Comment>> postsLatestCommentsData = new HashMap<>();
        for (Map.Entry<String, LiveData<List<Comment>>> entry : viewModel.getPostsLatestComments().entrySet()) {
            postsLatestCommentsData.put(entry.getKey(), entry.getValue().getValue());
        }
        adapter = new PostRecyclerAdapter(getLayoutInflater(), viewModel.getPosts().getValue(), postsLatestCommentsData);
        binding.recyclerView.setAdapter(adapter);

//        adapter.setOnItemClickListener(pos -> {
//            Log.d("TAG", "Row was clicked " + pos);
//            Post st = viewModel.getData().getValue().get(pos);
////                NavDirections.ActionPostsListFragmentToBlueFragment action = PostsListFragmentDirections.actionPostsListFragmentToBlueFragment(st.name);
////                Navigation.findNavController(view).navigate(action);
//        });

        binding.btnLoadMore.setOnClickListener(v -> {
            binding.progressBar.setVisibility(View.VISIBLE);
            PostsModel.instance().loadMorePosts().observe(getViewLifecycleOwner(), postsList -> {
                adapter.setPosts(postsList);
                CommentsModel.instance().refreshLatestComments();
                for (Post post : postsList) {
                    viewModel.getPostLatestComments(post.id).observe(getViewLifecycleOwner(),
                            commentList -> adapter.setPostLatestComments(post.id, commentList));
                }
                binding.progressBar.setVisibility(View.INVISIBLE);
            });
        });

        binding.progressBar.setVisibility(View.GONE);

        viewModel.getPosts().observe(getViewLifecycleOwner(), postsList -> {
            adapter.setPosts(postsList);
            for (Post post : postsList) {
                viewModel.getPostLatestComments(post.id).observe(getViewLifecycleOwner(),
                        commentList -> adapter.setPostLatestComments(post.id, commentList));
            }
        });

        PostsModel.instance().EventPostsListLoadingState.observe(getViewLifecycleOwner(), status ->
                binding.swipeRefresh.setRefreshing(status == PostsModel.LoadingState.LOADING));

        CommentsModel.instance().eventCommentsListLoadingState.observe(getViewLifecycleOwner(), status ->
                binding.swipeRefresh.setRefreshing(status == CommentsModel.LoadingState.LOADING));

        binding.swipeRefresh.setOnRefreshListener(this::reloadData);
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
