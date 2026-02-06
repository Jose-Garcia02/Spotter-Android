package com.josegarcia.appgym.data.entities;

public class SessionVolumeEntry {
    public long date;
    public float totalVolume;

    public SessionVolumeEntry(long date, float totalVolume) {
        this.date = date;
        this.totalVolume = totalVolume;
    }
}
