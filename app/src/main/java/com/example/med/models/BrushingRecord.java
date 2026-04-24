package com.example.med.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "brushing_records")
public class BrushingRecord {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public Date date;
    public boolean isMorning;

    public BrushingRecord(Date date, boolean isMorning) {
        this.date = date;
        this.isMorning = isMorning;
    }
}