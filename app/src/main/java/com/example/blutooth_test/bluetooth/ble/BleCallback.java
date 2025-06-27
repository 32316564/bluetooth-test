package com.example.blutooth_test.bluetooth.ble;


public interface BleCallback {
    void onDeviceFound(BleDevice device);
    void onScanStarted();
    void onScanFinished();
    void onConnected();
    void onDisconnected();
    void onServicesDiscovered();
    void onDataSent(boolean success);
    void onDataReceived(byte[] data);
    void onStringDataReceived(String data); // 新增方法，用于接收字符串格式数据
    void onError(String message);
    void onPaired(boolean b);

    void onConnecting();
}