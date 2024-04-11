package com.glv.note_project.DataBase;

import static androidx.room.OnConflictStrategy.REPLACE;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.glv.note_project.Model.Notes;

import java.util.List;

@Dao
public interface MainDAO {

    @Insert
    void insert(Notes notes);

    @Insert
    void inserAll(List<Notes> notes);


    @Query("SELECT * FROM notes ORDER BY id DESC")
    List<Notes> getAll();

    @Query("UPDATE notes SET title = :title, notes = :notes, data = :data WHERE ID = :id")
    void update(int id, String title, String notes, String data);

    @Delete
    void delete(Notes notes);


    @Delete
    void delete_all(List<Notes> notes);

    @Query("UPDATE notes SET pinned = :pin WHERE ID = :id")
    void pin(int id, boolean pin);

}