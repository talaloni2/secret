package com.example.secret;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secret.model.Comment;
import com.example.secret.model.Post;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;


class PostViewHolder extends RecyclerView.ViewHolder {
    List<Post> posts;
    Map<String, List<Comment>> postsLatestComments;
    ImageView backgroundImage;
    TextView contentTv;

    public PostViewHolder(@NonNull View itemView, PostRecyclerAdapter.OnItemClickListener listener, List<Post> posts, Map<String, List<Comment>> postsLatestComments) {
        super(itemView);
        this.posts = posts;
        this.postsLatestComments = postsLatestComments;
        backgroundImage = itemView.findViewById(R.id.list_item_background_img);
        contentTv = itemView.findViewById(R.id.list_item_content_tv);
        itemView.setOnClickListener(view -> {
            int pos = getAdapterPosition();
            listener.onItemClick(pos);
        });
    }

    public void bind(Post post, int pos) {
        contentTv.setText(post.content);
        if (post.getBackgroundUrl() != null && post.getBackgroundUrl().length() > 5) {
            Picasso.get().load(post.getBackgroundUrl()).placeholder(R.drawable.avatar).into(backgroundImage);
        } else {
            backgroundImage.setImageResource(R.drawable.avatar);
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
