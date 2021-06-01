package com.example.BLEAPP2;

/*
 * This is a class for the battery data captured from your BLE connection.
 *
 * */
public class BatteryData {
    private int batteryLevel;

    public BatteryData(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    } //konstruktor

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }
}
