package com.example.secret.model;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.secret.MyApplication;

@Database(entities = {User.class, Image.class}, version = 80)
abstract class AppLocalDbRepository extends RoomDatabase {
    public abstract UserDao  userDao();
    public abstract ImageDao imageDao();
}

public class AppLocalDb{
    static public AppLocalDbRepository getAppDb() {
        return Room.databaseBuilder(MyApplication.getMyContext(),
                        AppLocalDbRepository.class,
                        "dbFileName.db")
                .fallbackToDestructiveMigration()
                .build();
    }

    private AppLocalDb(){}
}

