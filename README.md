📱 应用简介  
一款支持经典蓝牙和低功耗蓝牙(BLE)双模连接的测试工具，提供完整的蓝牙设备管理、数据收发和GNSS数据解析功能。

---

🛠️ 项目结构  
```
com.example.bluetooth-test/
├── bluetooth/              # 蓝牙核心模块
│   ├── classic/            # 经典蓝牙实现
│   │   ├── ClassicBluetoothManager.java
│   │   ├── ClassicCallback.java
│   │   └── ClassicDevice.java
│   ├── ble/                # BLE实现
│   │   ├── BleManager.java
│   │   ├── BleCallback.java
│   │   └── BleDevice.java
│   └── BluetoothType.java
├── web/                    # Web交互模块
│   ├── WebAppInterface.java
│   └── WebViewManager.java
├── utils/                  # 工具类
│   ├── PermissionUtils.java
│   └── BluetoothUtils.java
└── MainActivity.java       # 主界面
```

---

✨ 核心功能  
**双模蓝牙支持**  
- 经典蓝牙模式：支持SPP协议设备连接  
- 低功耗蓝牙模式：支持BLE设备连接与通信  
- 一键切换，自动适配不同设备类型  

**数据校验机制**  
- GNGGA数据：专业GNGGAParser校验  
- Byte数据：CRC8循环冗余校验  

**流畅交互体验**  
- Lottie动画加载效果  
- 实时数据接收展示  

---

🚀 快速开始  
1. **选择蓝牙模式**  
   点击顶部模式切换按钮选择经典/BLE模式  

2. **扫描并连接设备**  
   ```java
   // 启动扫描
   bluetoothManager.startScan();
   // 连接设备
   bluetoothManager.connectDevice(deviceAddress);
   ```

---

🔍 技术亮点  
**GNGGA数据解析**  
```
$GNGGA,175900.90,4004.74012345,N,11614.19234567,E,1,14,0.6,89.0123,M,-1.0987,M,09,9012*3C\r\n
```

**代码实现**  
```java
private StringBuilder nmeaBuffer = new StringBuilder();

public void onDataReceived(String data) {
    nmeaBuffer.append(data);
    int startIdx = nmeaBuffer.indexOf("$");
    if (startIdx != -1) {
        String remaining = nmeaBuffer.substring(startIdx);
        processNMEAData(remaining);  // 解析NMEA语句
        nmeaBuffer.setLength(0);
    }
}
```

---

📸 更多截图  
| 登录界面 | 连接界面 | 主界面 | 收发数据界面 | loading |
|----------|--------|
| ![登录截图](./assets/images/login.png) 

| ![连接截图](./assets/images/connect.jpg) 

| ![主界面截图](./assets/images/main.png) 

| ![收发数据截图](./assets/images/received.png) 

| ![loading截图](./assets/images/location.gif) 

---

📝 使用建议  
- 测试前确保设备蓝牙已开启  
- BLE设备需确认支持的服务和特征  
- [Lottie动画下载](https://lottiefiles.com/search?q=loading&category=animations)  
