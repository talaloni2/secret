package com.example.secret;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secret.interfaces.Listener;
import com.example.secret.model.Comment;
import com.example.secret.model.CommentsModel;

import java.util.List;


class CommentViewHolder extends RecyclerView.ViewHolder {
    List<Comment> comments;
    TextView contentTv;

    public CommentViewHolder(@NonNull View itemView, CommentRecyclerAdapter.OnItemClickListener listener, List<Comment> comments) {
        super(itemView);
        this.comments = comments;
        contentTv = itemView.findViewById(R.id.comment_list_item_content_tv);
        itemView.setOnClickListener(view -> {
            int pos = getAdapterPosition();
            listener.onItemClick(pos);
        });
    }

    public void bind(Comment comment, int pos) {
        contentTv.setText(comment.content);
    }
}

public class CommentRecyclerAdapter extends RecyclerView.Adapter<CommentViewHolder> {
    OnItemClickListener listener;

    public static interface OnItemClickListener {
        void onItemClick(int pos);
    }

    LayoutInflater inflater;
    List<Comment> comments;

    public List<Comment> getComments() {
        return this.comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    public void addComment(Comment comment, Listener<Void> successListener, Listener<Void> failListener) {
        CommentsModel.instance().uploadComment(comment, successListener, failListener);
    }

    public CommentRecyclerAdapter(LayoutInflater inflater, List<Comment> comments) {
        this.inflater = inflater;
        this.comments = comments;
    }

    void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.comment_list_item, parent, false);
        return new CommentViewHolder(view, listener, comments);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment, position);
    }

    @Override
    public int getItemCount() {
        if (comments == null) return 0;
        return comments.size();
    }

}
