package com.example.secret.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PostDao {
    @Query("select * from Post order by lastUpdated desc")
    LiveData<List<Post>> getAll();

    @Query("select * from Post order by lastUpdated desc limit :limit")
    LiveData<List<Post>> getAllLimited(int limit);

    @Query("select * from Post where id = :postId")
    Post getPostById(String postId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Post... posts);

    @Delete
    void delete(Post post);

    @Query("delete from Post where id = :postId")
    void deletePost(String postId);
}
