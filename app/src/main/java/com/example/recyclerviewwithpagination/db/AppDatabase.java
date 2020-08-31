package com.example.recyclerviewwithpagination.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.recyclerviewwithpagination.model.DataModel;

@Database(entities = {DataModel.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
        private static AppDatabase mInstance;
        private static final String DATABASE_NAME = "Avatar";

        public abstract DataDao dataDao();

        public synchronized static AppDatabase getDatabaseInstance(Context context) {
            if (mInstance == null) {
                mInstance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE_NAME)
                        .allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .build();
            }
            return mInstance;
        }

        public static void destroyInstance() {
            mInstance = null;
        }
}
