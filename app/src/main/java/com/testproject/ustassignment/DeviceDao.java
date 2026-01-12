package com.testproject.ustassignment;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DeviceEntity device);

    @Query("SELECT * FROM devices")
    List<DeviceEntity> getAllDevices();

    @Update
    void update(DeviceEntity device);

    @Query("UPDATE devices SET status = :status WHERE ip = :ip")
    void updateStatus(String ip, String status);
}