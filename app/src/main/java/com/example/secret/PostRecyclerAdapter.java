package com.example.secret;

import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secret.model.Comment;
import com.example.secret.model.ImageModel;
import com.example.secret.model.Post;

import java.util.Collections;
import java.util.List;
import java.util.Map;


class PostViewHolder extends RecyclerView.ViewHolder {
    List<Post> posts;
    Map<String, List<Comment>> postsLatestComments;
    ImageView backgroundImage;
    TextView contentTv;
    TextView comment1Tv;
    TextView comment2Tv;

    public PostViewHolder(@NonNull View itemView, PostRecyclerAdapter.OnItemClickListener listener, List<Post> posts, Map<String, List<Comment>> postsLatestComments) {
        super(itemView);
        this.posts = posts;
        this.postsLatestComments = postsLatestComments;
        backgroundImage = itemView.findViewById(R.id.list_item_background_img);
        contentTv = itemView.findViewById(R.id.list_item_content_tv);
        comment1Tv = itemView.findViewById(R.id.list_item_comment1);
        comment2Tv = itemView.findViewById(R.id.list_item_comment2);
        itemView.setOnClickListener(view -> {
            int pos = getAdapterPosition();
            listener.onItemClick(pos);
        });
    }

    public void bind(Post post, int pos) {
        List<Comment> latestComments = this.postsLatestComments.getOrDefault(post.id, Collections.emptyList());
        contentTv.setText(post.content);
        comment1Tv.setText(latestComments.size() > 0 ? latestComments.get(0).content : "");
        comment2Tv.setText(latestComments.size() > 1 ? latestComments.get(1).content : "");
        if (post.getBackgroundUrl() != null && post.getBackgroundUrl().length() > 5) {
            ImageModel.instance().getImage(post.getBackgroundUrl(), bitmap -> {
                backgroundImage.setImageDrawable(new BitmapDrawable(itemView.getResources(), bitmap));
            }, failReason -> {
                Log.w("PostRecyclerAdapter", String.format("Not viewing post %s due to %s", post.id, failReason));
            });
        } else {
            backgroundImage.setImageResource(R.drawable.sharing_secret_image);
        }
    }
}

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostViewHolder> {
    OnItemClickListener listener;

    public static interface OnItemClickListener {
        void onItemClick(int pos);
    }

    LayoutInflater inflater;
    List<Post> posts;
    Map<String, List<Comment>> postsLatestComments;

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

    public PostRecyclerAdapter(LayoutInflater inflater, List<Post> posts, Map<String, List<Comment>> postsLatestComments) {
        this.inflater = inflater;
        this.posts = posts;
        this.postsLatestComments = postsLatestComments;
    }

    void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new PostViewHolder(view, listener, posts, postsLatestComments);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post, position);
    }

    @Override
    public int getItemCount() {
        if (posts == null) return 0;
        return posts.size();
    }

}
