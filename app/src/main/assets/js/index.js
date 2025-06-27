// 设备管理对象
    const bluetoothManager = {
        currentMode: null,
        connectedDevice: null,
        devices: new Map(), // 所有设备集合
        dataBuffer: [], // 数据缓冲区

        setMode(mode) {
            this.currentMode = mode;
            document.getElementById('current-mode').textContent =
                `当前模式: ${mode === 'classic' ? '经典蓝牙' : '低功耗蓝牙(BLE)'}`;
            Android.setBluetoothMode(mode);
            this.clearDevices();
            setTimeout(()=>{
                this.clearGPS();
            },500)
        },

        startScan() {
            if (!this.currentMode) {
                alert('请先选择蓝牙模式');
                return;
            }
            Android.startScan();
        },

        // 设备发现处理
        addDevice(address, name, type, isPaired) {
            // 如果设备已存在，更新信息；否则添加新设备
            const device = this.devices.get(address) || { address, name, type };
            device.isPaired = isPaired;
            device.name = name || device.name || '未知设备';
            this.devices.set(address, device);

            this.updateDeviceLists();
        },

        // 清空GPS值显示
        clearGPS(){
            const testElement = document.getElementById("test");
            const gpsQual = document.getElementById("gps-qual");
            const receivedEle = document.getElementById("received-data");

            testElement.innerHTML = '正在获取GPS...';
            gpsQual.innerHTML = '';
            receivedEle.innerHTML ='';
        },

        // 更新设备列表显示
        updateDeviceLists() {
            const pairedList = document.getElementById('paired-devices');
            const unpairedList = document.getElementById('unpaired-devices');

            // 清空列表
            pairedList.innerHTML = '';
            unpairedList.innerHTML = '';

            // 如果没有设备，显示提示
            if (this.devices.size === 0) {
                pairedList.innerHTML = '<div style="color:#999;text-align:center;">暂无设备</div>';
                unpairedList.innerHTML = '<div style="color:#999;text-align:center;">暂无设备</div>';
                return;
            }

            // 分类显示设备
            this.devices.forEach((device, address) => {
                const container = device.isPaired ? pairedList : unpairedList;
                const div = document.createElement('div');
                div.className = `device ${device.isPaired ? 'paired-device' : 'unpaired-device'}`;

                // 连接状态指示
                const isConnected = this.connectedDevice && this.connectedDevice.address === address;
                const statusText = isConnected ? '已连接' : '未连接';
                const statusClass = isConnected ? 'status-connected' : 'status-disconnected';

                // 按钮配置
                let buttonHtml = '';
                if (device.isPaired) {
                    buttonHtml = `
                        <button class="btn-connect" onclick="bluetoothManager.connectDevice('${address}','${device.name}')">
                            连接
                        </button>
                        <button class="btn-unpair" onclick="bluetoothManager.unpairDevice('${address}','${device.name}')">
                            取消配对
                        </button>
                    `;
                } else {
                    buttonHtml = `
                        <button class="btn-pair" onclick="bluetoothManager.pairDevice('${address}','${device.name}')">
                            配对
                        </button>
                    `;
                }

                div.innerHTML = `
                    <div class="device-info">
                        <span class="device-type ${device.type}-badge">
                            ${device.type === 'classic' ? '经典' : 'BLE'}
                        </span>
                        ${device.name} (${address})
                        <span class="status-indicator ${statusClass}">${statusText}</span>
                    </div>
                    <div>${buttonHtml}</div>
                `;
                container.appendChild(div);
            });
        },

        // 配对设备
        pairDevice(address, name) {
            console.log('开始配对:', address, name);
            Android.pairedDevice(address, name);
            alert(`正在配对 ${name || address}...`);
        },

        // 取消配对
        unpairDevice(address, name) {
            if (confirm(`确定要取消配对 ${name || address} 吗?`)) {
                Android.unpairDevice(address);
            }
        },

        // 连接设备
        connectDevice(address, name) {
            this.connectedDevice = { address, name };
            Android.connectDevice(address);
            alert(`正在连接 ${name}...`);
            this.updateDeviceLists(); // 更新连接状态显示
        },

        // 断开连接
        disconnect() {
            if (this.connectedDevice) {
                Android.disconnect();
                this.connectedDevice = null;
                this.updateDeviceLists(); // 更新连接状态显示
            } else {
                alert("当前没有已连接的设备");
            }
        },

        // 清空设备列表
        clearDevices() {
            this.devices.clear();
            this.updateDeviceLists();
        },

        // 发送数据
        sendData() {
            if (!this.connectedDevice) {
                alert('请先连接设备');
                return;
            }
            const data = document.getElementById('send-data').value;
            if (data) {
                Android.sendData(data);
                document.getElementById('send-data').value = '';
            }
        },

        // 单独的数据缓冲处理方法
        addDataToBuffer(parseGps) {
            const display = document.getElementById('received-data');
            const timestamp = new Date().toLocaleTimeString();

            // 添加新数据到缓冲区
            this.dataBuffer.push({timestamp, parseGps});

            // 保持缓冲区最多10条数据
            if (this.dataBuffer.length > 10) {
                this.dataBuffer.shift(); // 移除最旧的数据
            }

            // 重新渲染所有数据
            display.innerHTML = this.dataBuffer.map(item =>
                `<div><span style="color:#888">[${item.timestamp}]</span> <br>longitude: ${item.parseGps.longitude} <br> latitude: ${item.parseGps.latitude}</div>`
            ).join('');

            display.scrollTop = display.scrollHeight;
        },

        // 接收数据
        onDataReceived(data) {
            const testElement = document.getElementById("test");
            const gpsQual = document.getElementById("gps-qual");

               try {
                    // 直接解析数据为JSON对象
                    const parseGps = typeof data === 'string' ? JSON.parse(data) : data;

                    // 显示坐标
                    testElement.innerHTML = `${parseGps.GNGGA}<br>longitude：${parseGps.longitude}<br>latitude：${parseGps.latitude}<br>X:${parseGps.X}<br>Y:${parseGps.Y}`;

                    // 设置GPS质量状态
                    switch(parseGps.gpsQuality){
                        case 0:
                            gpsQual.innerText = "定位不可用或无效";
                            break;
                        case 1:
                            gpsQual.innerText = "单点定位";
                            break;
                        case 2:
                            gpsQual.innerText = "伪距差分或 SBAS 定位";
                            break;
                        case 4:
                            gpsQual.innerText = "RTK 固定解";
                            break;
                        case 5:
                            gpsQual.innerText = "RTK 浮点解";
                            break;
                        case 6:
                            gpsQual.innerText = "惯导定位";
                            break;
                        case 7:
                            gpsQual.innerText = "用户设定位置";
                            break;
                        default:
                            gpsQual.innerText = "未知";
                            break;
                    }

                    // 更新全局gps对象
                    // 更新全局gps对象（确保gps已定义）
                    if (typeof gps !== 'undefined') {
                        gps.longitude = parseGps.longitude;
                        gps.latitude = parseGps.latitude;
                    }

                    // 添加数据到缓冲区
                    this.addDataToBuffer(parseGps);
                } catch (error) {
                    console.log("解析 GPS 出错了:", error);
                    gps = null;
                }
        },
    };

    // 全局函数映射到管理器
    function setMode(mode) { bluetoothManager.setMode(mode); }
    function startScan() { bluetoothManager.startScan(); }
    function disconnect() { bluetoothManager.disconnect(); }
    function clearDevices() { bluetoothManager.clearDevices(); }
    function sendData() { bluetoothManager.sendData(); }

    // Java调用的回调函数
    // 扫描到设备
    function onClassicDeviceFound(address, name, isPaired) {
        bluetoothManager.addDevice(address, name, 'classic', isPaired);
    }

    function onBleDeviceFound(address, name, isPaired) {
        bluetoothManager.addDevice(address, name, 'ble', isPaired);
    }

    // 接收经典蓝牙数据
    function onClassicDataReceived(data) {
        bluetoothManager.onDataReceived(data);
    }
    // 接收低功耗蓝牙数据
    function onBleDataReceived(data) {
        bluetoothManager.onDataReceived(data);
    }
