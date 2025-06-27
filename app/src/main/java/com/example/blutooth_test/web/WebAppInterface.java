package com.example.blutooth_test.web;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.example.blutooth_test.MainActivity;
import com.example.blutooth_test.bluetooth.BluetoothType;
import com.example.blutooth_test.bluetooth.ble.BleCallback;
import com.example.blutooth_test.bluetooth.ble.BleDevice;
import com.example.blutooth_test.bluetooth.ble.BleManager;
import com.example.blutooth_test.bluetooth.classic.ClassicBluetoothManager;
import com.example.blutooth_test.bluetooth.classic.ClassicCallback;
import com.example.blutooth_test.bluetooth.classic.ClassicDevice;
import com.example.blutooth_test.utils.ActManager;
import com.example.blutooth_test.utils.GNGGAParser;
import com.example.blutooth_test.utils.GausKrugerProjection;
import com.example.blutooth_test.utils.PermissionUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class WebAppInterface {
    private static final String TAG = "WebAppInterface";
    private Activity activity;
    private ClassicBluetoothManager classicManager;
    private BleManager bleManager;
    private BluetoothType currentType = BluetoothType.NONE;
    private WebViewManager webViewManager;
    // 使用两个独立的Set分别存储设备地址
    private final Set<String> discoveredClassicDevices = new HashSet<>();
    private final Set<String> discoveredBleDevices = new HashSet<>();
    private BluetoothAdapter bluetoothAdapter;

    public WebAppInterface(Activity activity, WebViewManager webViewManager) {
        this.activity = activity;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.webViewManager = webViewManager;
        this.classicManager = new ClassicBluetoothManager(activity, new ClassicCallbackImpl());
        this.bleManager = new BleManager(activity, new BleCallbackImpl());
    }

    /**
     * 检查蓝牙是否可用
     */
    public boolean isBluetoothOn(){
        if(bleManager == null && classicManager == null) return false;
        switch (currentType) {
            case BLE:
                return bleManager.isBleBluetoothAvailable();
            case CLASSIC:
            default:
                return classicManager.isClassicBluetoothAvailable();
        }
    }

    /**
     * 连接成功后跳转到MainPage
     */
    @JavascriptInterface
    public void navigateToMain() {
        // 跳转到本地另一个HTML页面
        Intent intent = new Intent(activity, MainActivity.class);
        Bundle options = ActivityOptions.makeSceneTransitionAnimation(activity).toBundle();
        startActivity(activity, intent, options);
        //释放资源
        ActManager.getAppManager().finishActivity(activity);
    }

    //设置蓝牙模式
    @JavascriptInterface
    public void setBluetoothMode(String mode) {
        // 清理当前资源
        releaseCurrentResources();

        if ("classic".equals(mode)) {
            currentType = BluetoothType.CLASSIC;
        } else if ("ble".equals(mode)) {
            currentType = BluetoothType.BLE;
        }
    }

    //开始扫描
    @JavascriptInterface
    public void startScan() {
        //检查蓝牙是否启用
        boolean isBluetooth = isBluetoothOn();
        if (!isBluetooth) {
            Toast.makeText(activity, "蓝牙未启用或设备不支持蓝牙", Toast.LENGTH_SHORT).show();
        }

        //检查授权
        if (!PermissionUtils.checkBluetoothPermissions(activity)) {
            PermissionUtils.requestBluetoothPermissions((Activity) activity);
            return;
        }

        switch (currentType) {
            case CLASSIC:
                classicManager.cancelDiscovery();
                classicManager.startDiscovery();
                break;
            case BLE:
                bleManager.stopScan();
                bleManager.startScan(true); // 启用10秒扫描超时
                break;
            default:
                Log.e("WebAppInterface", "No bluetooth mode selected");
        }
    }

    //配对设备
    @JavascriptInterface
    public void pairedDevice(String deviceAddress, String deviceName) {
        Log.i(TAG, "开始配对设备: " + deviceName + " (" + deviceAddress + ")" + currentType);
        activity.runOnUiThread(() -> {
            try {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (adapter == null) {
                    Log.e(TAG, "设备不支持蓝牙");
                    return;
                }

                BluetoothDevice device = adapter.getRemoteDevice(deviceAddress);
                if (device == null) {
                    Log.e(TAG, "无法获取蓝牙设备: " + deviceAddress);
                    return;
                }

                Log.i(TAG, "开始配对设备: " + deviceName + " (" + deviceAddress + ")");

                switch (currentType) {
                    case CLASSIC:
                        classicManager.cancelDiscovery();
                        classicManager.pairDevice(device);
                        break;
                    case BLE:
                        bleManager.stopScan();
                        bleManager.pairDevice(device);
                        break;
                }
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "无效的蓝牙地址: " + deviceAddress, e);
            }
        });
    }

    //连接设备
    @JavascriptInterface
    public void connectDevice(String deviceAddress) {
        switch (currentType) {
            case CLASSIC:
                classicManager.cancelDiscovery();
                classicManager.connect(deviceAddress);
                break;
            case BLE:
                bleManager.stopScan();
                bleManager.connect(deviceAddress);
                break;
        }
    }


    /**
     * 断开连接
     */
    @JavascriptInterface
    public void disconnect() {
        switch (currentType) {
            case CLASSIC:
                classicManager.disconnect();
                break;
            case BLE:
                bleManager.disconnect();
                break;
        }
    }


    //发送数据
    @JavascriptInterface
    public void sendData(String data) {
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        switch (currentType) {
            case CLASSIC:
                classicManager.sendData(bytes);
                break;
            case BLE:
                bleManager.sendData(bytes);
                break;
        }
    }

    //释放资源
    public void releaseResources() {
        releaseCurrentResources();
        classicManager.release();
        bleManager.release();
        discoveredClassicDevices.clear();
        discoveredBleDevices.clear();
    }

    //释放当前资源
    private void releaseCurrentResources() {
        switch (currentType) {
            case CLASSIC:
                classicManager.cancelDiscovery();
                classicManager.disconnect();
                break;
            case BLE:
                bleManager.stopScan();
                bleManager.disconnect();
                break;
        }
    }

    //检查设备是否已经配对
    private boolean checkPairStatus(String deviceAddress) {
        try {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            // 获取已配对设备（每次扫描都刷新）
            Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
            Log.i(TAG, "startScan: yijing配对" + bondedDevices.toString());
            return device.getBondState() == BluetoothDevice.BOND_BONDED;
        } catch (Exception e) {
            Log.e(TAG, "Check paired state failed for: " + deviceAddress, e);
            return false;
        }
    }

    // 发送经典蓝牙设备信息到页面
    private void sendClassicDeviceToPage(String address, String name, boolean isPaired, String type) {
        String js = String.format("javascript:onClassicDeviceFound('%s', '%s', %b, '%s')",
                address,
                name != null ? name : "未知设备",
                isPaired,
                type);

        Log.i(TAG, "发送设备到页面: " + js);
        webViewManager.evaluateJavascript(js);
    }

    // 发送低功耗蓝牙设备信息到页面
    private void sendBleDeviceToPage(String address, String name, boolean isPaired, String type) {
        String js = String.format("javascript:onBleDeviceFound('%s', '%s', %b, '%s')",
                address,
                name != null ? name : "未知设备",
                isPaired,
                type);

        Log.i(TAG, "发送设备到页面: " + js);
        webViewManager.evaluateJavascript(js);
    }

    // 发送经典蓝牙接收的数据到页面
    private void sendClassicReceivedDataToPage(String data) {
        String js = String.format("javascript:onClassicDataReceived('%s')",
                data);

        Log.i(TAG, "发送设备到页面: " + js);
        webViewManager.evaluateJavascript(js);
    }

    // 发送经典蓝牙接收的数据到页面
    private void sendBleReceivedDataToPage(String data) {
        String js = String.format("javascript:onBleDataReceived('%s')",
                data);

        Log.i(TAG, "发送设备到页面: " + js);
        webViewManager.evaluateJavascript(js);
    }


    // 经典蓝牙回调实现类
    private class ClassicCallbackImpl implements ClassicCallback {
        @Override
        public void onDeviceFound(ClassicDevice device) {
            // 检查是否已发现过该经典设备
            if (discoveredClassicDevices.contains(device.getAddress())) {
                return;
            }

            discoveredClassicDevices.add(device.getAddress());

            boolean isPaired = checkPairStatus(device.getAddress());

            // 发送设备信息到页面
            sendClassicDeviceToPage(device.getAddress(), device.getName(), isPaired, "classic");
        }

        @Override
        public void onScanStarted() {
            Log.i(TAG, "onScanStarted: 开始扫描经典蓝牙");
            // 获取并发送所有已配对的经典蓝牙设备
            Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
            for (BluetoothDevice device : bondedDevices) {
                // 只发送经典蓝牙设备（可以根据需要添加更多过滤条件）
                sendClassicDeviceToPage(device.getAddress(), device.getName(), true, "classic");
            }
        }

        @Override
        public void onScanFinished() {
            Log.i(TAG, "onScanFinished:经典蓝牙扫描完成");
            discoveredClassicDevices.clear(); // 仅清空经典设备缓存
            sendMessageToPage("扫描结束");
        }

        @Override
        public void onConnecting() {
            Log.i(TAG, "onConnecting:经典蓝牙连接中...");

        }

        @Override
        public void onConnected() {
            Log.i(TAG, "onConnected:经典蓝牙连接成功");
            sendMessageToPage("连接成功");
        }

        @Override
        public void onDisconnected() {
            Log.i(TAG, "onDisconnected:经典蓝牙断开连接");

        }

        @Override
        public void onDataSent(boolean success) {
            Log.i(TAG, "onDataSent:经典蓝牙发送数据成功");

        }

        @Override
        public void onDataReceived(byte[] data) {
            Log.i(TAG, "onDataReceived:经典蓝牙接受数据成功==" + data.toString());

        }

        @Override
        public void onStringDataReceived(String data) {
            Log.i(TAG, "onStringDataReceived: 接收到strings" + data);
            try {
                // 解析数据
                GNGGAParser.GNGGAData parsedData = GNGGAParser.parse(data);

                // 2. 截取GNGGA字符串（从开头到第6个逗号）
                String shortenedGNGGA = truncateGNGGA(data, 6); // 保留前6段

                // 输出解析结果
                Log.i(TAG, "解析后的 GPS 数据: ");
                Log.i(TAG, "onDataReceived: " + parsedData);

                // 访问具体字段
                Log.i(TAG, "纬度: " + parsedData.getLatitude());
                Log.i(TAG, "经度: " + parsedData.getLongitude());
                Log.i(TAG, "海拔: " + parsedData.getAltitude() + " " + parsedData.getAltitudeUnit());


                // 调用转换方法
                GausKrugerProjection.Point xy_first = GausKrugerProjection.longLatToXY(parsedData.getLongitude(), parsedData.getLatitude());

                // 在获取xy_first后添加以下逻辑
                BigDecimal bigDecima_x = new BigDecimal(xy_first.X);
                BigDecimal bigDecimal_y = new BigDecimal(xy_first.Y);

                // 保留三位小数，四舍五入
                bigDecima_x = bigDecima_x.setScale(3, RoundingMode.HALF_UP);
                bigDecimal_y = bigDecimal_y.setScale(3, RoundingMode.HALF_UP);

                // 将解析后的数据转换为 JSON 格式
                JSONObject jsonData = new JSONObject();
                jsonData.put("latitude", parsedData.getLatitude());
                jsonData.put("longitude", parsedData.getLongitude());
                jsonData.put("gpsQuality", parsedData.getGpsQuality());
                jsonData.put("GNGGA", shortenedGNGGA); // 存储截取后的字符串
                jsonData.put("X", bigDecima_x.toString()); // 存储截取后的字符串
                jsonData.put("Y", bigDecimal_y.toString()); // 存储截取后的字符串

                // 将 JSON 数据转换为字符串
                String jsonString = jsonData.toString();

                // 将接收到的数据传递到 WebView
                sendClassicReceivedDataToPage(jsonString);

                Log.i(TAG, "onDataReceivedjsonString: "+jsonString);
            } catch (IllegalArgumentException e) {
                Log.i(TAG, "onDataReceived: 解析失败" + e.getMessage());
            } catch (JSONException e) {
                Log.i(TAG, "JSON 转换失败: " + e.getMessage());
            }
        }

        @Override
        public void onError(String message) {
            Log.i(TAG, "onError: 经典蓝牙错误："+ message);

        }

        @Override
        public void onPaired(boolean success) {
            Log.i(TAG, "onPaired: 经典蓝牙已经配对："+ success);
            //配对成功刷新
            if(success){
                classicManager.startDiscovery();
                sendMessageToPage("配对成功");
            }
        }

    }

    /**
     * 截取GNGGA字符串，保留前N段（以逗号分隔）
     */
    private String truncateGNGGA(String gngga, int maxSegments) {
        String[] parts = gngga.split(",", maxSegments + 1); // 多分割一段用于检查
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < Math.min(parts.length, maxSegments); i++) {
            if (i > 0) result.append(",");
            result.append(parts[i]);
        }
        return result.toString();
    }

    // 低功耗蓝牙回调实现类
    private class BleCallbackImpl implements BleCallback {
        @Override
        public void onDeviceFound(BleDevice device) {
            // 检查是否已发现过该BLE设备
            if (discoveredBleDevices.contains(device.getAddress())) {
                return;
            }

            discoveredBleDevices.add(device.getAddress());

            boolean isPaired = checkPairStatus(device.getAddress());

            // 发送设备信息到页面
            sendBleDeviceToPage(device.getAddress(), device.getName(), isPaired, "classic");
        }

        @Override
        public void onScanStarted() {
            Log.i(TAG, "onScanStarted: 开始扫描低功耗蓝牙");
            // 获取并发送所有已配对的经典蓝牙设备
            Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
            for (BluetoothDevice device : bondedDevices) {
                // 只发送经典蓝牙设备（可以根据需要添加更多过滤条件）
                sendClassicDeviceToPage(device.getAddress(), device.getName(), true, "classic");
            }
        }

        @Override
        public void onScanFinished() {
            Log.i(TAG, "onScanFinished:低功耗蓝牙扫描完成");
            discoveredBleDevices.clear(); // 仅清空BLE设备缓存
        }

        @Override
        public void onConnecting() {
            Log.i(TAG, "onConnecting: 开始连接低功耗蓝牙");
        }

        @Override
        public void onConnected() {
            Log.i(TAG, "onConnected: 低功耗蓝牙连接成功");

        }

        @Override
        public void onDisconnected() {
            Log.i(TAG, "onDisconnected: 低功耗蓝牙断开");

        }

        @Override
        public void onServicesDiscovered() {
            Log.i(TAG, "onServicesDiscovered: 低功耗蓝牙发现服务");

        }

        @Override
        public void onDataSent(boolean success) {
            Log.i(TAG, "onDataSent: 低功耗蓝牙发送数据成功");

        }

        @Override
        public void onDataReceived(byte[] data) {
            Log.i(TAG, "onDataReceived: 低功耗蓝牙接收到数据");

        }

        @Override
        public void onStringDataReceived(String data) {
            Log.i(TAG, "onStringDataReceived: 低功耗蓝牙接收到数据");
            try {
                // 解析数据
                GNGGAParser.GNGGAData parsedData = GNGGAParser.parse(data);

                // 2. 截取GNGGA字符串（从开头到第6个逗号）
                String shortenedGNGGA = truncateGNGGA(data, 6); // 保留前6段

                // 输出解析结果
                Log.i(TAG, "解析后的 GPS 数据: ");
                Log.i(TAG, "onDataReceived: " + parsedData);

                // 访问具体字段
                Log.i(TAG, "纬度: " + parsedData.getLatitude());
                Log.i(TAG, "经度: " + parsedData.getLongitude());
                Log.i(TAG, "海拔: " + parsedData.getAltitude() + " " + parsedData.getAltitudeUnit());


                // 调用转换方法
                GausKrugerProjection.Point xy_first = GausKrugerProjection.longLatToXY(parsedData.getLongitude(), parsedData.getLatitude());

                // 在获取xy_first后添加以下逻辑
                BigDecimal bigDecima_x = new BigDecimal(xy_first.X);
                BigDecimal bigDecimal_y = new BigDecimal(xy_first.Y);

                // 保留三位小数，四舍五入
                bigDecima_x = bigDecima_x.setScale(3, RoundingMode.HALF_UP);
                bigDecimal_y = bigDecimal_y.setScale(3, RoundingMode.HALF_UP);

                // 将解析后的数据转换为 JSON 格式
                JSONObject jsonData = new JSONObject();
                jsonData.put("latitude", parsedData.getLatitude());
                jsonData.put("longitude", parsedData.getLongitude());
                jsonData.put("gpsQuality", parsedData.getGpsQuality());
                jsonData.put("GNGGA", shortenedGNGGA); // 存储截取后的字符串
                jsonData.put("X", bigDecima_x.toString()); // 存储截取后的字符串
                jsonData.put("Y", bigDecimal_y.toString()); // 存储截取后的字符串

                // 将 JSON 数据转换为字符串
                String jsonString = jsonData.toString();

                // 将接收到的数据传递到 WebView
                sendBleReceivedDataToPage(jsonString);

                Log.i(TAG, "onDataReceivedjsonString: "+jsonString);
            } catch (IllegalArgumentException e) {
                Log.i(TAG, "onDataReceived: 解析失败" + e.getMessage());
            } catch (JSONException e) {
                Log.i(TAG, "JSON 转换失败: " + e.getMessage());
            }
        }

        @Override
        public void onError(String message) {
            Log.i(TAG, "onError: 低功耗蓝牙错误："+ message);

        }

        @Override
        public void onPaired(boolean b) {
            Log.i(TAG, "onPaired: 低功耗蓝牙已经配对："+ b);
        }

    }


    /**
     * 发送提示信息到页面
     * @param message
     */
    private void sendMessageToPage(String message) {
        String js = String.format("javascript:onMessageSend('%s')",
                message);

        Log.i(TAG, "发送设备到页面: " + js);
        webViewManager.evaluateJavascript(js);
    }
}