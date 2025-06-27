📱 应用简介
    一款支持经典蓝牙和低功耗蓝牙(BLE)双模连接的测试工具，提供完整的蓝牙设备管理、数据收发和GNSS数据解析功能。

    <div align="center">
        <img src="./assets/images/login.png" width="30%" alt="登录界面">
        <img src="./assets/images/main.png" width="30%" alt="主界面">
        <img src="./assets/images/connect.jpg" width="30%" alt="连接界面">
        <img src="./assets/images/received.png" width="30%" alt="收发数据界面">
    </div>

🛠️ 项目结构
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

✨ 核心功能
    双模蓝牙支持
    经典蓝牙模式：支持SPP协议设备连接
    低功耗蓝牙模式：支持BLE设备连接与通信
    一键切换，自动适配不同设备类型

    数据校验机制
    GNGGA数据：专业GNGGAParser校验
    Byte数据：CRC8循环冗余校验
    确保数据传输的完整性和准确性

    流畅交互体验
    Lottie动画加载效果
    实时数据接收展示
    简洁直观的操作界面

🚀 快速开始
    选择蓝牙模式
    点击顶部模式切换按钮选择经典/BLE模式

    扫描并连接设备
    // 启动扫描
    bluetoothManager.startScan();
    // 连接设备
    bluetoothManager.connectDevice(deviceAddress);

    数据收发测试
    自动接收设备数据并解析
    输入框发送文本数据到设备

🔍 技术亮点
    GNGGA数据解析:
    $GNGGA,175900.90,4004.74012345,N,11614.19234567,E,1,14,0.6,89.0123,M,-1.0987,M,09,9012*3C\r\n

    // 分片存储解析示例
    private StringBuilder nmeaBuffer = new StringBuilder();

    public void onDataReceived(String data) {
        nmeaBuffer.append(data);
        int startIdx = nmeaBuffer.indexOf("$");
        if (startIdx != -1) {
            String remaining = nmeaBuffer.substring(startIdx);
            // 解析完整NMEA语句
            processNMEAData(remaining);
            nmeaBuffer.setLength(0);
        }
    }
    动画效果实现
    <com.airbnb.lottie.LottieAnimationView
        android:id="@+id/scan_animation"
        android:layout_width="100dp"
        android:layout_height="100dp"
        app:lottie_fileName="bluetooth_scan.json"
        app:lottie_autoPlay="true"
        app:lottie_loop="true"/>

📸 更多截图
    <div align="center">
        <img src="./assets/images/location_pin.gif" width="45%" alt="扫描动画">
    </div>

📝 使用建议
    测试前确保设备蓝牙已开启
    经典蓝牙需要先配对后连接
    BLE设备需确认支持的服务和特征
    更多Lottie动画可从LottieFiles官网下载：
    https://lottiefiles.com/search?q=loading&category=animations



