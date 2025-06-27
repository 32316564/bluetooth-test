package com.example.blutooth_test.utils;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;

import com.example.blutooth_test.bluetooth.BluetoothType;


public class BluetoothUtils {
    private static final String TAG = "BluetoothUtils";

    public static boolean isBluetoothEnabled() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter != null && adapter.isEnabled();
    }

    public static void enableBluetooth(Activity activity, int requestCode) {
        if (!isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, requestCode);
        }
    }

    public static String getDeviceTypeName(BluetoothDevice device) {
        try {
            switch (device.getType()) {
                case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                    return "Classic";
                case BluetoothDevice.DEVICE_TYPE_LE:
                    return "BLE";
                case BluetoothDevice.DEVICE_TYPE_DUAL:
                    return "Dual";
                default:
                    return "Unknown";
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting device type", e);
            return "Unknown";
        }
    }

    public static BluetoothType determineBluetoothType(BluetoothDevice device) {
        try {
            int type = device.getType();
            if (type == BluetoothDevice.DEVICE_TYPE_LE || type == BluetoothDevice.DEVICE_TYPE_DUAL) {
                return BluetoothType.BLE;
            }
            return BluetoothType.CLASSIC;
        } catch (Exception e) {
            Log.e(TAG, "Error determining bluetooth type", e);
            return BluetoothType.NONE;
        }
    }

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}