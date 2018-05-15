package com.joselestnh.flashcards_client;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by Joseles on 05/04/2018.
 */

@Dao
public interface FlashcardDao {

    @Query("SELECT * FROM Flashcard")
    List<Flashcard> getAll();

    @Query("SELECT fid, collection, name, type, wordA, wordB, image, done FROM Flashcard WHERE collection = :collection")
    List<Flashcard> getAllByCollection(String collection);

    @Query("SELECT done FROM Flashcard WHERE collection = :collection")
    List<Integer> getProgressByCollection(String collection);

    @Query("SELECT 1 FROM Flashcard WHERE collection = :collection")
    int checkFlashcardsExistenceFor(String collection);

    @Update
    public void updateFlashcards(Flashcard... flashcards);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertFlashcard(Flashcard... flashcards);

    @Delete
    public void delete(Flashcard flashcard);

    @Delete
    public void deleteFlashcards(Flashcard... flashcards);
}
