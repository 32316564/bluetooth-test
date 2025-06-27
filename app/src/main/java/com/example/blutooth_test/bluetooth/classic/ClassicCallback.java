package com.example.blutooth_test.bluetooth.classic;


public interface ClassicCallback {
    void onDeviceFound(ClassicDevice device);
    void onScanStarted();
    void onScanFinished();
    void onConnecting();
    void onConnected();
    void onDisconnected();
    void onDataSent(boolean success);
    void onDataReceived(byte[] data);
    void onStringDataReceived(String data);
    void onError(String message);
    void onPaired(boolean b);
}