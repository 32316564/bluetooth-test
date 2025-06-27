package com.example.blutooth_test.bluetooth.classic;

import static com.example.blutooth_test.utils.CRC8Calculator.calculateCRC8;
import static com.example.blutooth_test.bluetooth.BluetoothConstant.RECEIVED_DATA_DIGIT;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.example.blutooth_test.bluetooth.DataType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 经典蓝牙管理器
 * 功能：管理蓝牙设备发现、连接、数据传输和配对等操作
 *
 * 主要功能：
 * 1. 蓝牙设备扫描与发现
 * 2. 设备配对与取消配对
 * 3. 建立和维护蓝牙连接
 * 4. 数据发送和接收（支持NMEA和自定义协议）
 * 5. 连接状态管理
 *
 * 使用说明：
 * 1. 初始化时需要传入Context和ClassicCallback回调接口
 * 2. 所有操作都是异步的，结果通过回调接口返回
 * 3. 使用完毕后必须调用release()释放资源
 */
public class ClassicBluetoothManager {
    private static final String TAG = "ClassicBluetoothManager";
    private static final UUID UUID_STRING = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int MAX_BUFFER_SIZE = 1024;
    private static final int NMEA_BUFFER_RETAIN_SIZE = 20;

    // 蓝牙核心组件
    private final BluetoothAdapter bluetoothAdapter;
    private final Context context;
    private final ClassicCallback callback;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // 连接相关状态
    private volatile BluetoothSocket socket;
    private volatile ConnectThread connectThread;
    private volatile ConnectedThread connectedThread;
    private volatile boolean isRunning;

    /**
     * 构造函数
     * @param context 上下文对象
     * @param callback 回调接口
     */
    public ClassicBluetoothManager(Context context, ClassicCallback callback) {
        this.context = context;
        this.callback = callback;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.isRunning = true;

        registerReceivers();
    }

    // ======================== 公共方法 ======================== //

    /**
     * 检查蓝牙是否可用
     * @return true如果蓝牙可用且已启用
     */
    public boolean isClassicBluetoothAvailable() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    /**
     * 开始扫描蓝牙设备
     */
    public void startDiscovery() {
        if (bluetoothAdapter == null) {
            callback.onError("蓝牙不可用");
            return;
        }

        executorService.execute(() -> {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }

            if (bluetoothAdapter.startDiscovery()) {
                callback.onScanStarted();
            } else {
                callback.onError("扫描启动失败");
            }
        });
    }

    /**
     * 取消扫描
     */
    public void cancelDiscovery() {
        executorService.execute(() -> {
            if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
        });
    }

    /**
     * 配对设备
     * @param device 要配对的蓝牙设备
     */
    public void pairDevice(BluetoothDevice device) {
        executorService.execute(() -> {
            if (device == null) {
                callback.onError("设备为空");
                return;
            }

            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                callback.onPaired(true);
                return;
            }

            try {
                boolean result = createBond(device);
                if (!result) {
                    callback.onError("启动配对失败");
                }
            } catch (Exception e) {
                callback.onError("配对失败: " + e.getMessage());
            }
        });
    }

    /**
     * 取消设备配对
     * @param device 要取消配对的蓝牙设备
     */
    public void unpairDevice(BluetoothDevice device) {
        executorService.execute(() -> {
            if (device == null) {
                callback.onError("设备为空");
                return;
            }

            if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                callback.onPaired(false);
                return;
            }

            try {
                removeBond(device);
            } catch (Exception e) {
                callback.onError("取消配对失败: " + e.getMessage());
            }
        });
    }

    /**
     * 连接指定地址的蓝牙设备
     * @param deviceAddress 蓝牙设备MAC地址
     */
    public void connect(String deviceAddress) {
        executorService.execute(() -> {
            cancelPendingConnection();

            try {
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
                connectThread = new ConnectThread(device);
                connectThread.start();
            } catch (IllegalArgumentException e) {
                callback.onError("无效的设备地址");
            }
        });
    }

    /**
     * 断开当前连接
     */
    public void disconnect() {
        executorService.execute(() -> {
            cancelPendingConnection();
            closeCurrentConnection();
            callback.onDisconnected();
        });
    }

    /**
     * 发送数据
     * @param data 要发送的字节数组
     */
    public void sendData(byte[] data) {
        executorService.execute(() -> {
            if (connectedThread != null) {
                connectedThread.write(data);
            } else {
                callback.onError("未连接，无法发送数据");
                callback.onDataSent(false);
            }
        });
    }

    /**
     * 发送字符串数据
     * @param message 要发送的字符串(UTF-8编码)
     */
    public void sendData(String message) {
        sendData(message.getBytes(StandardCharsets.UTF_8));
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
     * 释放资源
     */
    public void release() {
        isRunning = false;
        disconnect();
        cancelDiscovery();

        try {
            context.unregisterReceiver(receiver);
        } catch (Exception e) {
            Log.w(TAG, "接收器未注册", e);
        }

        executorService.shutdown();
    }

    // ======================== 内部方法 ======================== //

    /**
     * 取消待处理的连接
     */
    private void cancelPendingConnection() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
    }

    /**
     * 关闭当前连接
     */
    private void closeCurrentConnection() {
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "关闭socket失败", e);
            }
            socket = null;
        }
    }

    /**
     * 注册广播接收器
     */
    private void registerReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(receiver, filter);
    }

    /**
     * 创建配对(兼容不同API版本)
     */
    private boolean createBond(BluetoothDevice device) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return device.createBond();
        } else {
            Method method = device.getClass().getMethod("createBond");
            return (Boolean) method.invoke(device);
        }
    }

    /**
     * 取消配对(使用反射)
     */
    private void removeBond(BluetoothDevice device) throws Exception {
        Method method = device.getClass().getMethod("removeBond");
        method.invoke(device);
    }

    // ======================== 内部类 ======================== //

    /**
     * 蓝牙广播接收器
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isRunning) return;

            String action = intent.getAction();
            if (action == null) return;

            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    handleDeviceFound(intent);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    callback.onScanFinished();
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    handleBondStateChanged(intent);
                    break;
            }
        }

        private void handleDeviceFound(Intent intent) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device != null) {
                callback.onDeviceFound(new ClassicDevice(device));
            }
        }

        private void handleBondStateChanged(Intent intent) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
            int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

            if (device != null) {
                if (state == BluetoothDevice.BOND_BONDED) {
                    callback.onPaired(true);
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDING) {
                    callback.onError("配对失败");
                }
            }
        }
    };

    /**
     * 连接线程 - 负责建立蓝牙连接
     */
    private class ConnectThread extends Thread {
        private final BluetoothDevice device;
        private BluetoothSocket socket;

        ConnectThread(BluetoothDevice device) {
            this.device = device;
        }

        @Override
        public void run() {
            bluetoothAdapter.cancelDiscovery();
            callback.onConnecting();

            try {
                socket = device.createRfcommSocketToServiceRecord(UUID_STRING);
                socket.connect();

                ClassicBluetoothManager.this.socket = socket;
                connectedThread = new ConnectedThread(socket);
                connectedThread.start();

                callback.onConnected();
            } catch (IOException e) {
                callback.onError("连接失败: " + e.getMessage());
                closeSocket();
                callback.onDisconnected();
            }
        }

        void cancel() {
            closeSocket();
        }

        private void closeSocket() {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "关闭连接socket失败", e);
            }
        }
    }

    /**
     * 已连接线程 - 负责数据通信
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        private final StringBuilder nmeaBuffer = new StringBuilder();
        private final ByteArrayOutputStream packetBuffer = new ByteArrayOutputStream();
        private DataType currentDataFormat = DataType.UTF8;

        ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "获取流失败", e);
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        //@Override
        //public void run() {
        //    byte[] buffer = new byte[1024]; // 数据缓冲区
        //    int bytes; // 读取的字节数
        //
        //    // 持续监听输入流
        //    while (isRunning) {
        //        try {
        //            // 阻塞读取数据
        //            bytes = mmInStream.read(buffer);
        //            if (bytes > 0) {
        //                //// 复制有效数据并回调
        //                //byte[] receivedData = new byte[bytes];
        //                //System.arraycopy(buffer, 0, receivedData, 0, bytes);
        //                //callback.onDataReceived(receivedData);
        //                // 处理接收到的数据
        //                Log.i(TAG, "receiveData: 接收到数据：" + bytes);
        //                processReceivedData(buffer, bytes);
        //            }
        //        } catch (IOException e) {
        //            if (isRunning) {
        //                // 只有运行状态下才报告错误
        //                callback.onError("读取数据失败: " + e.getMessage());
        //                callback.onDisconnected();
        //            }
        //            break;
        //        }
        //    }
        //}


        /**
         *  模拟数据配置
         */
        @Override
        public void run() {
            List<String> simulatedNmeaSentences = Arrays.asList(
                    "$GNGGA,025754.00,4004.74102107,N,11614.19532779,E,1,18,0.7,63.3224,M,-9.7848,M,00,0000*58\r\n",
                    "$GNRMC,025754.00,A,4004.74102107,N,11614.19532779,E,0.0,0.0,010124,,,A*78\r\n",
                    "$GNGGA,025756.00,4004.74102109,N,11614.19532781,E,1,18,0.7,63.3226,M,-9.7848,M,00,0000*60\r\n",
                    "$GNGGA,025756.00,4120.74102109,N,11625.19532781,E,1,18,0.7,63.3226,M,-9.7848,M,00,0000*60\r\n"
            );

            int currentIndex = 0;
            byte[] buffer = new byte[1024];

            while (isRunning) {
                try {
                    // 优先处理真实数据
                    if (inputStream != null && inputStream.available() > 0) {
                        int bytes = inputStream.read(buffer);
                        if (bytes > 0) {
                            processReceivedData(buffer, bytes);
                            continue;
                        }
                    }

                    // 模拟数据发送
                    Thread.sleep(100);
                    String currentData = simulatedNmeaSentences.get(currentIndex);
                    byte[] simulatedData = currentData.getBytes(StandardCharsets.UTF_8);
                    System.arraycopy(simulatedData, 0, buffer, 0, simulatedData.length);
                    processReceivedData(buffer, simulatedData.length);

                    currentIndex = (currentIndex + 1) % simulatedNmeaSentences.size();
                } catch (IOException e) {
                    handleConnectionError("读取数据失败: " + e.getMessage());
                    break;
                } catch (InterruptedException e) {
                    Log.e(TAG, "模拟数据线程中断");
                    break;
                }
            }
        }

        private void handleConnectionError(String message) {
            if (isRunning) {
                callback.onError(message);
                callback.onDisconnected();
            }
        }

        /**
         * 处理接收到的数据
         */
        private synchronized void processReceivedData(byte[] buffer, int length) {
            if (tryProcessNMEAData(buffer, length)) {
                return;
            }

            if (currentDataFormat == DataType.HEX && isCustomProtocolPacket(buffer)) {
                packetBuffer.write(buffer, 0, length);
                processCustomProtocol();
            } else {
                Log.d(TAG, "忽略非NMEA数据（当前模式: " + currentDataFormat + "）");
            }

            if (packetBuffer.size() > MAX_BUFFER_SIZE) {
                packetBuffer.reset();
            }
        }

        /**
         * 尝试处理NMEA数据
         */
        private boolean tryProcessNMEAData(byte[] buffer, int length) {
            if (nmeaBuffer.length() + length > MAX_BUFFER_SIZE) {
                Log.w(TAG, "NMEA缓冲区溢出风险，清空缓冲区");
                nmeaBuffer.setLength(0);
            }

            try {
                String newData = new String(buffer, 0, length, StandardCharsets.UTF_8);
                nmeaBuffer.append(newData);
                boolean hasProcessed = false;

                while (true) {
                    int startPos = nmeaBuffer.indexOf("$");
                    if (startPos < 0) {
                        retainBufferTail();
                        break;
                    }

                    int endPos = findNmeaLineEnd(startPos);
                    if (endPos < 0) break;

                    String fullSentence = nmeaBuffer.substring(startPos, endPos + 1).trim();
                    Log.i(TAG, "NMEA数据: " + fullSentence);
                    callback.onStringDataReceived(fullSentence);
                    hasProcessed = true;

                    nmeaBuffer.delete(0, endPos + 1);
                }
                return hasProcessed;
            } catch (Exception e) {
                Log.e(TAG, "NMEA处理异常", e);
                nmeaBuffer.setLength(0);
                return false;
            }
        }

        private void retainBufferTail() {
            if (nmeaBuffer.length() > 100) {
                int keepSize = Math.min(NMEA_BUFFER_RETAIN_SIZE, nmeaBuffer.length());
                String tmp = nmeaBuffer.substring(nmeaBuffer.length() - keepSize);
                nmeaBuffer.setLength(0);
                nmeaBuffer.append(tmp);
                Log.d(TAG, "清理无效前缀，保留缓冲区尾部");
            }
        }

        /**
         * 处理自定义协议数据
         */
        private void processCustomProtocol() {
            byte[] allData = packetBuffer.toByteArray();
            int processedPos = 0;

            while (processedPos < allData.length) {
                int headerPos = findHeader(allData, processedPos, (byte) 0xAA);
                if (headerPos == -1) break;

                if (headerPos + 2 >= allData.length) break;

                int packetLength = allData[headerPos + 1] & 0xFF;
                int fullPacketLength = packetLength + 2;

                if (headerPos + fullPacketLength > allData.length) break;

                byte[] packet = Arrays.copyOfRange(allData, headerPos, headerPos + fullPacketLength);

                if (verifyPacket(packet)) {
                    dispatchPacket(packet);
                    processedPos = headerPos + fullPacketLength;
                } else {
                    processedPos = headerPos + 1;
                    Log.w(TAG, "CRC校验失败，跳过该包");
                }
            }

            retainUnprocessedData(allData, processedPos);
        }

        private void retainUnprocessedData(byte[] allData, int processedPos) {
            if (processedPos > 0) {
                byte[] remaining = Arrays.copyOfRange(allData, processedPos, allData.length);
                packetBuffer.reset();
                if (remaining.length > 0) {
                    packetBuffer.write(remaining, 0, remaining.length);
                }
            }
        }

        /**
         * 分发数据包
         */
        private void dispatchPacket(byte[] packet) {
            switch (currentDataFormat) {
                case HEX:
                    RECEIVED_DATA_DIGIT = packet.length;
                    callback.onDataReceived(packet);
                    break;
                case UTF8:
                default:
                    try {
                        String data = new String(packet, "UTF-8").trim();
                        callback.onStringDataReceived(data);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "字符串转换失败", e);
                    }
                    break;
            }
        }

        /**
         * 发送数据
         */
        void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
                callback.onDataSent(true);
            } catch (IOException e) {
                callback.onError("发送数据失败: " + e.getMessage());
                callback.onDataSent(false);
            }
        }

        void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "关闭连接线程socket失败", e);
            }
        }

        // ======================== 工具方法 ======================== //

        /**
         * 检查是否是自定义的数据包 0xAA开头
         * @param buffer
         * @return
         */
        private boolean isCustomProtocolPacket(byte[] buffer) {
            return buffer != null && buffer.length >= 2 && buffer[0] == (byte) 0xAA;
        }

        /**
         * 检查校验位
         * @param startPos
         * @return
         */
        private int findNmeaLineEnd(int startPos) {
            int endPos = nmeaBuffer.indexOf("\n", startPos);
            if (endPos > 0 && endPos > startPos && nmeaBuffer.charAt(endPos - 1) == '\r') {
                endPos--;
            }
            return endPos;
        }

        /**
         * 查找数据头
         * @param data
         * @param start
         * @param header
         * @return
         */
        private int findHeader(byte[] data, int start, byte header) {
            for (int i = start; i < data.length; i++) {
                if (data[i] == header) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * 数据包CRC校验
         * @param packet
         * @return
         */
        private boolean verifyPacket(byte[] packet) {
            if (packet.length < 2) return false;
            byte[] data = Arrays.copyOfRange(packet, 0, packet.length - 1);
            byte crc = packet[packet.length - 1];
            return (calculateCRC8(data) == (crc & 0xFF));
        }
    }
}