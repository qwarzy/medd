package com.example.med.database;

import androidx.room.*;
import com.example.med.models.Visit;
import java.util.Date;
import java.util.List;

@Dao
public interface VisitDao {
    @Insert
    void insert(Visit visit);

    @Delete
    void delete(Visit visit);

    @Query("SELECT * FROM visits ORDER BY dateTime ASC")
    List<Visit> getAllVisits();

    @Query("SELECT * FROM visits WHERE dateTime >= :currentDate ORDER BY dateTime ASC")
    List<Visit> getUpcomingVisits(Date currentDate);
}