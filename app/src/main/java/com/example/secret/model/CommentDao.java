package com.example.secret.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CommentDao {
    @Query("select * from Comment")
    LiveData<List<Comment>> getAll();

    @Query("select * from Comment where postId = :postId order by lastUpdated desc")
    LiveData<List<Comment>> getCommentsByPostId(String postId);

    @Query("select * from Comment where postId = :postId order by lastUpdated desc limit :limit")
    LiveData<List<Comment>> getCommentsByPostIdLimited(String postId, int limit);

    @Query("select * from Comment where id = :commentId")
    Comment getCommentById(String commentId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Comment... comments);

    @Delete
    void delete(Comment comment);

    @Query("delete from Comment where postId = :postId")
    void deletePostComments(String postId);
}
