package com.example.chatifygmail.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SenderDao {
    @Query("SELECT * FROM Sender ORDER BY unread DESC")
    LiveData<List<Sender>> loadAllSenders();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSender(Sender sender);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateSender(Sender sender);

    @Delete
    void deleteSender(Sender sender);

    @Query("DELETE FROM Sender WHERE emailAddress = :emailAddress")
    void deleteSenderbyEmail(String emailAddress);

    @Query("SELECT * FROM Sender WHERE emailAddress = :emailAddress")
    LiveData<Sender> loadSenderByEmailAddress(String emailAddress);

    @Query("SELECT * FROM Sender ORDER BY unread DESC")
    List<Sender> loadAllSendersSync();
}
