package com.example.med.database;

import androidx.room.*;
import com.example.med.models.BrushingRecord;
import java.util.List;

@Dao
public interface BrushingDao {
    @Insert
    void insert(BrushingRecord record);

    @Query("SELECT * FROM brushing_records ORDER BY date DESC")
    List<BrushingRecord> getAllRecords();
}