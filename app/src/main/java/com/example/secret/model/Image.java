package com.example.secret.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Image {
    @PrimaryKey
    @NonNull
    String url;

    @ColumnInfo(name = "content", typeAffinity = ColumnInfo.BLOB)
    byte[] content;
}
