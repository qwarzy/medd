package com.example.med.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;
import com.example.med.models.BrushingRecord;
import com.example.med.models.Visit;

@Database(entities = {BrushingRecord.class, Visit.class}, version = 2)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract BrushingDao brushingDao();
    public abstract VisitDao visitDao();

    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "dentistry_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}