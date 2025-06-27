package com.example.blutooth_test.bluetooth.ble;


import android.bluetooth.BluetoothDevice;

public class BleDevice {
    private final String address;
    private final String name;
    private final int rssi;

    public BleDevice(BluetoothDevice device) {
        this(device.getAddress(), device.getName(), -1);
    }

    public BleDevice(BluetoothDevice device, int rssi) {
        this(device.getAddress(), device.getName(), rssi);
    }

    public BleDevice(String address, String name, int rssi) {
        this.address = address;
        this.name = name != null ? name : "Unknown";
        this.rssi = rssi;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public int getRssi() {
        return rssi;
    }
}