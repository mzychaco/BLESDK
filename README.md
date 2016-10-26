# BLESDK

一个简单的基于Android 4.3的BLE蓝牙SDK实现

主要用于发现和接受BLE蓝牙设备的广播

## 引入

module's buid.gradle

```
dependencies {
    compile 'com.mzy.blelib:blelib:1.0.0'
}
```

## 使用

* 申请权限

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
```

* 代码调用
* SchoBLEManager为单例，需要时直接`getInstance()`
```java
@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SchoBLEManager schoBLEManager=SchoBLEManager.getInstance(this);
        schoBLEManager.bindConsumer(this);//绑定扫描结果的处理者
        schoBLEManager.startBLEService();//启动蓝牙服务
        schoBLEManager.startScanning();//开始扫描
}
```

* 通过实现`IBeaconConsumer`接口来处理扫描结果

```
public interface IBeaconConsumer {

    /**
     * 每当LBeaconManager检测到有iBeacon信号，会调用LBeaconConsumer的这个方法。
     * @param iBeacon － LBeaconParser解析蓝牙信号得到的LBeacon信号对象
     */

    void onIBeaconNew(IBeacon iBeacon);

    void onIBeaconUpdate(List<IBeacon> iBeaconList);

    void onIBeaconLeave(IBeacon iBeacon);

    void onError(IBeaconError error);

}
```

## 例子

* [NewBLESKDKActivity](https://github.com/mzychaco/BLESDK/blob/master/app/src/main/java/com/mzy/blesdk/NewBLESKDKActivity.java)



