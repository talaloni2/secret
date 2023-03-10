package com.example.secret;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secret.model.Comment;
import com.example.secret.model.Post;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class PostViewHolder extends RecyclerView.ViewHolder {
    List<Post> posts;
    Map<String, List<Comment>> postsLatestComments;
    ImageView backgroundImage;
    TextView contentTv;
    TextView comment1Tv;
    TextView comment2Tv;
    private View.OnClickListener onLoadMoreClicked;
    LinearLayout comment1Layout;
    LinearLayout comment2Layout;

    Button loadMoreBtn;
    ConstraintLayout postContentCompound;

    public PostViewHolder(@NonNull View itemView, PostRecyclerAdapter.OnItemClickListener listener, List<Post> posts, Map<String, List<Comment>> postsLatestComments, View.OnClickListener onLoadMoreClicked) {
        super(itemView);
        this.posts = posts;
        this.postsLatestComments = postsLatestComments;
        backgroundImage = itemView.findViewById(R.id.list_item_background_img);
        contentTv = itemView.findViewById(R.id.list_item_content_tv);
        comment1Layout = itemView.findViewById(R.id.list_item_comment_layout_1);
        comment2Layout = itemView.findViewById(R.id.list_item_comment_layout_2);
        comment1Tv = itemView.findViewById(R.id.list_item_comment1);
        comment2Tv = itemView.findViewById(R.id.list_item_comment2);
        loadMoreBtn = itemView.findViewById(R.id.post_list_item_load_more);
        postContentCompound = itemView.findViewById(R.id.post_content_compound);
        this.onLoadMoreClicked = onLoadMoreClicked;
        loadMoreBtn.setOnClickListener(this.onLoadMoreClicked);
        itemView.setOnClickListener(view -> {
            int pos = getBindingAdapterPosition();
            listener.onItemClick(pos);
        });
    }

    public void bindLoadMore(){
        loadMoreBtn.setVisibility(View.VISIBLE);
        postContentCompound.setVisibility(View.GONE);
    }
    public void bind(Post post, int pos) {
        loadMoreBtn.setVisibility(View.GONE);
        postContentCompound.setVisibility(View.VISIBLE);
        List<Comment> latestComments = this.postsLatestComments.getOrDefault(post.id, Collections.emptyList());
        contentTv.setText(post.content);
        setLatestComments(latestComments);
        if (post.getBackgroundUrl() != null && post.getBackgroundUrl().length() > 5) {
            Picasso.get().load(post.getBackgroundUrl()).placeholder(R.drawable.sharing_secret_image).into(backgroundImage);
        } else {
            backgroundImage.setImageResource(R.drawable.sharing_secret_image);
        }
    }

    private void setLatestComments(List<Comment> latestComments) {
        if (latestComments == null) return;

        setCommentViewOnSize(latestComments, 0, comment1Tv, comment1Layout);
        setCommentViewOnSize(latestComments, 1, comment2Tv, comment2Layout);
    }

    private void setCommentViewOnSize(List<Comment> latestComments, int idx, TextView comment1Tv, LinearLayout comment1Layout) {
        if (latestComments.size() > idx) {
            comment1Tv.setText(latestComments.get(idx).getContent());
            comment1Layout.setVisibility(View.VISIBLE);
            return;
        }
        comment1Layout.setVisibility(View.GONE);
    }
}

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostViewHolder> {
    OnItemClickListener listener;

    public static interface OnItemClickListener {
        void onItemClick(int pos);
    }

    LayoutInflater inflater;
    List<Post> posts;
    private View.OnClickListener onLoadMoreClicked;
    Map<String, List<Comment>> postsLatestComments;

    public List<Post> getPosts() {
        return this.posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    public void setPostLatestComments(String postId, List<Comment> latestComments) {
        this.postsLatestComments.put(postId, latestComments);
        notifyDataSetChanged();
    }

    public void setPostsLatestComments(Map<String, List<Comment>> postsLatestComments) {
        this.postsLatestComments = postsLatestComments;
        notifyDataSetChanged();
    }

    public PostRecyclerAdapter(LayoutInflater inflater, List<Post> posts, View.OnClickListener onLoadMoreClicked) {
        this.inflater = inflater;
        this.posts = posts;
        this.onLoadMoreClicked = onLoadMoreClicked;
        this.postsLatestComments = new HashMap<>();
    }

    void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new PostViewHolder(view, listener, posts, postsLatestComments, onLoadMoreClicked);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        if (posts == null || position == posts.size()){
            holder.bindLoadMore();
            return;
        }
        Post post = posts.get(position);
        holder.bind(post, position);
    }

    @Override
    public int getItemCount() {
        if (posts == null) return 1;
        return posts.size() + 1;
    }

}
