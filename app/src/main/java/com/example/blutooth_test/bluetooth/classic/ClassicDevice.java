package com.example.blutooth_test.bluetooth.classic;


import android.bluetooth.BluetoothDevice;

public class ClassicDevice {
    private final String address;
    private final String name;
    private final int bondState;

    public ClassicDevice(BluetoothDevice device) {
        this.address = device.getAddress();
        this.name = device.getName() != null ? device.getName() : "Unknown";
        this.bondState = device.getBondState();
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public boolean isPaired() {
        return bondState == BluetoothDevice.BOND_BONDED;
    }
}