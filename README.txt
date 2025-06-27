业余测试app
项目结构：
com.example.bluetooth-test/
├── bluetooth/
│   ├── classic/
│   │   ├── ClassicBluetoothManager.java
│   │   ├── ClassicCallback.java
│   │   └── ClassicDevice.java
│   ├── ble/
│   │   ├── BleManager.java
│   │   ├── BleCallback.java
│   │   └── BleDevice.java
│   └── BluetoothType.java
├── web/
│   ├── WebAppInterface.java
│   └── WebViewManager.java
├── utils/
│   ├── PermissionUtils.java
│   └── BluetoothUtils.java
└── MainActivity.java

页面简洁，仅供参考
app: 双模蓝牙
1.经典蓝牙、低功耗蓝牙Ble的扫描、配对、连接、接收数据、发送数据功能
2.接收GNGGA数据使用GNGGAParser校验、Byte数据采用CRC8校验
3.加载动画使用：Lottie
更多动画可到官网下载：https://lottiefiles.com
4.内部模拟了接收GNGGA数据，并使用分片存储方式解析
5.连接即可收到数据
