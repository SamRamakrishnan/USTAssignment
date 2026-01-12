package com.testproject.ustassignment;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "devices")
public class DeviceEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String ip;
    public String status;  // "Online" or "Offline"

    public DeviceEntity(String name, String ip, String status) {
        this.name = name;
        this.ip = ip;
        this.status = status;
    }
}