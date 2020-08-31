package com.example.recyclerviewwithpagination.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.recyclerviewwithpagination.model.DataModel;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface DataDao
{


    @Insert
    void insert(DataModel data);

    @Delete
    void delete(DataModel data);

}
