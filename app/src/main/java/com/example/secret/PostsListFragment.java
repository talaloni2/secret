package com.example.secret;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.secret.databinding.FragmentPostsListBinding;
import com.example.secret.model.CommentsModel;
import com.example.secret.model.PostsModel;

public class PostsListFragment extends Fragment {
    FragmentPostsListBinding binding;
    PostRecyclerAdapter adapter;
    PostsListFragmentViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPostsListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PostRecyclerAdapter(getLayoutInflater(), viewModel.getPosts().getValue(), viewModel.getPostsLatestComments());
        binding.recyclerView.setAdapter(adapter);

//        adapter.setOnItemClickListener(pos -> {
//            Log.d("TAG", "Row was clicked " + pos);
//            Post st = viewModel.getData().getValue().get(pos);
////                NavDirections.ActionPostsListFragmentToBlueFragment action = PostsListFragmentDirections.actionPostsListFragmentToBlueFragment(st.name);
////                Navigation.findNavController(view).navigate(action);
//        });

        binding.progressBar.setVisibility(View.GONE);

        viewModel.getPosts().observe(getViewLifecycleOwner(), list -> adapter.setPosts(list));

        PostsModel.instance().EventPostsListLoadingState.observe(getViewLifecycleOwner(), status -> {
            binding.swipeRefresh.setRefreshing(status == PostsModel.LoadingState.LOADING);
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            reloadData();
        });
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(this).get(PostsListFragmentViewModel.class);
    }

    void reloadData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        PostsModel.instance().refreshAllPosts();
        CommentsModel.instance().refreshAllComments();
        binding.progressBar.setVisibility(View.INVISIBLE);
    }
}
