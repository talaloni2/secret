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

    public Image(@NonNull String url, byte[] content) {
        this.url = url;
        this.content = content;
    }

    public Image() {
    }

    @NonNull
    public String getUrl() {
        return url;
    }

    public void setUrl(@NonNull String url) {
        this.url = url;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
