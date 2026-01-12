package com.testproject.ustassignment;



import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {DeviceEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DeviceDao deviceDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "device_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}