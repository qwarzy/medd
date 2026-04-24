package com.example.med.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "visits")
public class Visit {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public Date dateTime;
    public String doctorName;
    public String notes;

    public Visit(Date dateTime, String doctorName, String notes) {
        this.dateTime = dateTime;
        this.doctorName = doctorName;
        this.notes = notes;
    }
}