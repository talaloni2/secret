package com.example.secret.model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface ImageDao {
    @Query("select * from Image where url = :url")
    Image getUserByUrl(String url);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Image image);
}
