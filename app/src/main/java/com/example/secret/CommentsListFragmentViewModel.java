package com.example.secret;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.secret.model.Comment;
import com.example.secret.model.CommentsModel;

import java.util.List;

public class CommentsListFragmentViewModel extends ViewModel {
    LiveData<List<Comment>> comments = null;

    LiveData<List<Comment>> getPostComments(String postId) {
        if (this.comments == null) {
            this.comments = CommentsModel.instance().getCommentsByPostId(postId);
        }
        return this.comments;
    }
}
