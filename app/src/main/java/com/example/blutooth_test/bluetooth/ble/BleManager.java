//package com.example.blutooth_test.blurtooth.ble;
//
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothGatt;
//import android.bluetooth.BluetoothGattCallback;
//import android.bluetooth.BluetoothGattCharacteristic;
//import android.bluetooth.BluetoothGattService;
//import android.bluetooth.BluetoothProfile;
//import android.bluetooth.le.BluetoothLeScanner;
//import android.bluetooth.le.ScanCallback;
//import android.bluetooth.le.ScanFilter;
//import android.bluetooth.le.ScanResult;
//import android.bluetooth.le.ScanSettings;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Build;
//import android.provider.Settings;
//
//import java.lang.reflect.Method;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//public class BleManager {
//    private final BluetoothLeScanner bleScanner;
//    private final BluetoothAdapter bluetoothAdapter;
//    private final BleCallback callback;
//    private BluetoothGatt bluetoothGatt;
//    private Context context;
//    private static final UUID UUID_STRING = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
//
//    public BleManager(Context context, BleCallback callback) {
//        this.context = context;
//        this.callback = callback;
//        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        this.bleScanner = bluetoothAdapter.getBluetoothLeScanner();
//    }
//
//    public void startScan() {
//        if (!bluetoothAdapter.isEnabled()) {
//            callback.onError("Bluetooth not enabled");
//            return;
//        }
//
//        ScanSettings settings = new ScanSettings.Builder()
//                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//                .build();
//
//        List<ScanFilter> filters = new ArrayList<>();
//        // 可以添加过滤条件
//        // filters.add(new ScanFilter.Builder().setServiceUuid(...).build());
//
//        bleScanner.startScan(filters, settings, scanCallback);
//    }
//
//    public void stopScan() {
//        bleScanner.stopScan(scanCallback);
//    }
//
//    public void connect(String deviceAddress) {
//        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
//        bluetoothGatt = device.connectGatt(context, false, gattCallback);
//    }
//
//    public void disconnect() {
//        if (bluetoothGatt != null) {
//            bluetoothGatt.disconnect();
//            bluetoothGatt.close();
//            bluetoothGatt = null;
//        }
//    }
//
//    public void sendData(byte[] data) {
//        if (bluetoothGatt != null) {
//            BluetoothGattService service = bluetoothGatt.getService(UUID_STRING);
//            if (service != null) {
//                BluetoothGattCharacteristic characteristic =
//                        service.getCharacteristic(UUID_STRING);
//                if (characteristic != null) {
//                    characteristic.setValue(data);
//                    bluetoothGatt.writeCharacteristic(characteristic);
//                }
//            }
//        }
//    }
//
//    // New pairing methods
//    public void pairDevice(BluetoothDevice device) {
//        if (device == null) {
//            callback.onError("Device is null");
//            return;
//        }
//
//        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
//            callback.onPaired(true);
//            return;
//        }
//
//        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                device.createBond();
//            } else {
//                // For older versions, use reflection
//                Method method = device.getClass().getMethod("createBond");
//                method.invoke(device);
//            }
//        } catch (Exception e) {
//            callback.onError("Pairing failed: " + e.getMessage());
//        }
//    }
//
//    public void unpairDevice(BluetoothDevice device) {
//        if (device == null) {
//            callback.onError("Device is null");
//            return;
//        }
//
//        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
//            callback.onPaired(false);
//            return;
//        }
//
//        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                Method method = device.getClass().getMethod("removeBond");
//                method.invoke(device);
//            } else {
//                // For older versions
//                Method method = device.getClass().getMethod("removeBond");
//                method.invoke(device);
//            }
//        } catch (Exception e) {
//            callback.onError("Unpairing failed: " + e.getMessage());
//        }
//    }
//
//    public void openBluetoothSettings() {
//        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(intent);
//    }
//
//    public void release() {
//        stopScan();
//        disconnect();
//    }
//
//    private final ScanCallback scanCallback = new ScanCallback() {
//        @Override
//        public void onScanResult(int callbackType, ScanResult result) {
//            super.onScanResult(callbackType, result);
//            callback.onDeviceFound(new BleDevice(result.getDevice()));
//        }
//
//        @Override
//        public void onScanFailed(int errorCode) {
//            super.onScanFailed(errorCode);
//            callback.onError("Scan failed with code: " + errorCode);
//        }
//    };
//
//    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
//        @Override
//        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                callback.onConnected();
//                gatt.discoverServices();
//            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                callback.onDisconnected();
//            }
//        }
//
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                callback.onServicesDiscovered();
//            }
//        }
//
//        @Override
//        public void onCharacteristicWrite(BluetoothGatt gatt,
//                                          BluetoothGattCharacteristic characteristic, int status) {
//            callback.onDataSent(status == BluetoothGatt.GATT_SUCCESS);
//        }
//
//        @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt,
//                                            BluetoothGattCharacteristic characteristic) {
//            callback.onDataReceived(characteristic.getValue());
//        }
//    };
//}

//详细的注释和优化后的代码结构
package com.example.blutooth_test.bluetooth.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 低功耗蓝牙(BLE)管理器
 * 功能：管理BLE设备扫描、连接、数据传输和服务发现
 * 特点：
 * 1. 线程安全的BLE操作
 * 2. 支持设备配对/取消配对
 * 3. 自动重连机制
 * 4. 完善的错误处理
 */
public class BleManager {
    private static final String TAG = "BleManager";

    // 默认的GATT服务和特征UUID (可根据实际设备修改)
    private static final UUID SERVICE_UUID = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb");

    // 扫描超时时间(毫秒)
    private static final long SCAN_TIMEOUT = 10000;

    private final BluetoothAdapter bluetoothAdapter;
    private final BluetoothLeScanner bleScanner;
    private final BleCallback callback;
    private final Context context;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private volatile BluetoothGatt bluetoothGatt;
    private volatile boolean isScanning = false;
    private volatile boolean isConnected = false;
    private volatile boolean shouldReconnect = false;
    private volatile BluetoothDevice targetDevice;

    //模拟接收数据
    private final Handler simulationHandler = new Handler(Looper.getMainLooper());
    private Runnable simulationRunnable;

    // 模拟NMEA数据
    private final List<String> simulatedNmeaSentences = Arrays.asList(
            "$GNGGA,025754.00,4004.74102107,N,11614.19532779,E,1,18,0.7,63.3224,M,-9.7848,M,00,0000*58\r\n",
            "$GNGGA,025756.00,4004.74102109,N,11614.19532781,E,1,18,0.7,63.3226,M,-9.7848,M,00,0000*60\r\n",
            "$GNGGA,025756.00,4120.74102109,N,11625.19532781,E,1,18,0.7,63.3226,M,-9.7848,M,00,0000*60\r\n",
            "$GNGGA,025756.00,4125.74102109,N,11620.19532781,E,1,18,0.7,63.3226,M,-9.7848,M,00,0000*60\r\n",
            "$GNGGA,025756.00,4128.74102109,N,11623.19532781,E,1,18,0.7,63.3226,M,-9.7848,M,00,0000*60\r\n"
    );
    private int currentSimulationIndex = 0;

    public BleManager(Context context, BleCallback callback) {
        this.context = context.getApplicationContext(); // 使用Application Context避免内存泄漏
        this.callback = callback;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bleScanner = bluetoothAdapter != null ? bluetoothAdapter.getBluetoothLeScanner() : null;

        if (bluetoothAdapter == null || bleScanner == null) {
            Log.e(TAG, "Bluetooth not supported on this device");
            callback.onError("Bluetooth not supported");
        }
    }

    /**
     * 检查蓝牙是否可用
     */
    public boolean isBleBluetoothAvailable() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    /**
     * 开始扫描BLE设备
     * @param withTimeout 是否启用扫描超时
     */
    public void startScan(boolean withTimeout) {
        if (bleScanner == null) {
            callback.onError("Bluetooth not available");
            return;
        }

        executorService.execute(() -> {
            if (isScanning) {
                stopScan();
            }

            // 准备扫描设置
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();

            // 可以添加过滤条件
            List<ScanFilter> filters = new ArrayList<>();
            // filters.add(new ScanFilter.Builder().setServiceUuid(SERVICE_UUID).build());

            // 开始扫描
            bleScanner.startScan(filters, settings, scanCallback);
            isScanning = true;
            mainHandler.post(() -> callback.onScanStarted());

            // 设置扫描超时
            if (withTimeout) {
                mainHandler.postDelayed(this::stopScan, SCAN_TIMEOUT);
            }
        });
    }

    /**
     * 停止扫描BLE设备
     */
    public void stopScan() {
        executorService.execute(() -> {
            if (isScanning && bleScanner != null) {
                bleScanner.stopScan(scanCallback);
                isScanning = false;
                mainHandler.post(() -> callback.onScanFinished());
            }
        });
    }

    /**
     * 连接指定地址的BLE设备（不带自动重连功能）
     * @param deviceAddress 设备MAC地址
     */
    public void connect(String deviceAddress) {
        executorService.execute(() -> {
            if (bluetoothAdapter == null) {
                mainHandler.post(() -> callback.onError("Bluetooth not available"));
                return;
            }

            try {
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);

                // 如果已连接，先断开
                if (bluetoothGatt != null) {
                    disconnect();
                }

                // 不使用自动重连(autoConnect设为false)
                bluetoothGatt = device.connectGatt(context, false, gattCallback);
                startSimulation(500);
                mainHandler.post(() -> callback.onConnecting());
            } catch (IllegalArgumentException e) {
                mainHandler.post(() -> callback.onError("Invalid device address"));
            }
        });
    }

    /**
     * 断开当前连接
     */
    public void disconnect() {
        executorService.execute(() -> {
            shouldReconnect = false;

            if (bluetoothGatt != null) {
                bluetoothGatt.disconnect();
                // bluetoothGatt.close() 将在onConnectionStateChange中调用
            }
        });
    }

    /**
     * 发送数据到连接的设备
     * @param data 要发送的数据
     */
    public void sendData(byte[] data) {
        executorService.execute(() -> {
            if (bluetoothGatt == null || !isConnected) {
                mainHandler.post(() -> {
                    callback.onError("Not connected to any device");
                    callback.onDataSent(false);
                });
                return;
            }

            BluetoothGattService service = bluetoothGatt.getService(SERVICE_UUID);
            if (service == null) {
                mainHandler.post(() -> {
                    callback.onError("Service not found");
                    callback.onDataSent(false);
                });
                return;
            }

            BluetoothGattCharacteristic characteristic =
                    service.getCharacteristic(CHARACTERISTIC_UUID);
            if (characteristic == null) {
                mainHandler.post(() -> {
                    callback.onError("Characteristic not found");
                    callback.onDataSent(false);
                });
                return;
            }

            // 设置数据并写入
            characteristic.setValue(data);
            boolean success = bluetoothGatt.writeCharacteristic(characteristic);

            if (!success) {
                mainHandler.post(() -> {
                    callback.onError("Failed to write characteristic");
                    callback.onDataSent(false);
                });
            }
        });
    }

    /**
     * 配对设备
     * @param device 要配对的设备
     */
    public void pairDevice(BluetoothDevice device) {
        executorService.execute(() -> {
            if (device == null) {
                mainHandler.post(() -> callback.onError("Device is null"));
                return;
            }

            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                mainHandler.post(() -> callback.onPaired(true));
                return;
            }

            try {
                boolean result;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    result = device.createBond();
                } else {
                    // 旧版本使用反射
                    Method method = device.getClass().getMethod("createBond");
                    result = (Boolean) method.invoke(device);
                }

                if (!result) {
                    mainHandler.post(() -> callback.onError("Failed to initiate pairing"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Pairing failed: " + e.getMessage()));
            }
        });
    }

    /**
     * 取消配对
     * @param device 要取消配对的设备
     */
    public void unpairDevice(BluetoothDevice device) {
        executorService.execute(() -> {
            if (device == null) {
                mainHandler.post(() -> callback.onError("Device is null"));
                return;
            }

            if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                mainHandler.post(() -> callback.onPaired(false));
                return;
            }

            try {
                Method method = device.getClass().getMethod("removeBond");
                method.invoke(device);
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Unpairing failed: " + e.getMessage()));
            }
        });
    }

    /**
     * 打开蓝牙设置界面
     */
    public void openBluetoothSettings() {
        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 开始模拟数据传输
     * @param intervalMillis 模拟数据发送间隔(毫秒)
     */
    public void startSimulation(int intervalMillis) {
        currentSimulationIndex = 0;

        simulationRunnable = new Runnable() {
            @Override
            public void run() {
                if (simulatedNmeaSentences.isEmpty()) {
                    return;
                }

                // 获取当前模拟数据
                String nmeaData = simulatedNmeaSentences.get(currentSimulationIndex);

                // 通过回调发送数据
                mainHandler.post(() -> {
                    callback.onStringDataReceived(nmeaData.trim());
                    Log.d(TAG, "发送模拟数据[" + currentSimulationIndex + "]: " + nmeaData.trim());
                });

                // 更新索引
                currentSimulationIndex = (currentSimulationIndex + 1) % simulatedNmeaSentences.size();

                // 安排下一次发送
                simulationHandler.postDelayed(this, intervalMillis);
            }
        };

        simulationHandler.postDelayed(simulationRunnable, intervalMillis);
    }

    /**
     * 停止模拟数据传输
     */
    public void stopSimulation() {
        if (simulationRunnable != null) {
            simulationHandler.removeCallbacks(simulationRunnable);
            simulationRunnable = null;
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        executorService.execute(() -> {
            stopScan();
            disconnect();
            stopSimulation(); // 新增
            executorService.shutdown();
        });
    }

    // 扫描回调
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            mainHandler.post(() -> callback.onDeviceFound(new BleDevice(result.getDevice())));
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
                mainHandler.post(() -> callback.onDeviceFound(new BleDevice(result.getDevice())));
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            isScanning = false;
            mainHandler.post(() -> {
                callback.onError("Scan failed with code: " + errorCode);
                callback.onScanFinished();
            });
        }
    };

    // GATT回调
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                isConnected = true;
                mainHandler.post(() -> callback.onConnected());
                // 发现服务
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                isConnected = false;
                mainHandler.post(() -> callback.onDisconnected());

                // 关闭GATT客户端
                gatt.close();
                bluetoothGatt = null;

                // 如果需要自动重连且目标设备存在
                if (shouldReconnect && targetDevice != null) {
                    mainHandler.postDelayed(() -> {
                        if (shouldReconnect) {
                            bluetoothGatt = targetDevice.connectGatt(context, false, gattCallback);
                        }
                    }, 2000); // 2秒后重连
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mainHandler.post(() -> callback.onServicesDiscovered());

                // 可以在这里启用特征通知
                enableCharacteristicNotification(gatt);
            } else {
                mainHandler.post(() -> callback.onError("Service discovery failed: " + status));
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
            boolean success = status == BluetoothGatt.GATT_SUCCESS;
            mainHandler.post(() -> callback.onDataSent(success));

            if (!success) {
                mainHandler.post(() -> callback.onError("Write failed: " + status));
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            byte[] data = characteristic.getValue();
            mainHandler.post(() -> callback.onDataReceived(data));
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "MTU changed to: " + mtu);
            }
        }
    };

    /**
     * 启用特征通知
     * @param gatt BluetoothGatt实例
     */
    private void enableCharacteristicNotification(BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(SERVICE_UUID);
        if (service == null) return;

        BluetoothGattCharacteristic characteristic =
                service.getCharacteristic(CHARACTERISTIC_UUID);
        if (characteristic == null) return;

        // 设置通知
        gatt.setCharacteristicNotification(characteristic, true);

        // 对于某些设备，还需要设置描述符
        /*
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }
        */
    }
}